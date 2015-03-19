package com.uoscs09.theuos.tab.timetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.widgets.ColorSelector;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.annotaion.AsyncData;
import com.uoscs09.theuos.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.base.AbsProgressFragment;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.AsyncLoader.OnTaskFinishedListener;
import com.uoscs09.theuos.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos.http.TimeTableHttpRequest;
import com.uoscs09.theuos.http.parse.ParseTimeTable2;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.GraphicUtil;
import com.uoscs09.theuos.util.IOUtil;
import com.uoscs09.theuos.util.OApiUtil;
import com.uoscs09.theuos.util.OApiUtil.Semester;
import com.uoscs09.theuos.util.PrefUtil;
import com.uoscs09.theuos.util.StringUtil;

import org.apache.http.client.ClientProtocolException;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;

public class TabTimeTableFragment2 extends AbsProgressFragment<TimeTable> implements View.OnClickListener, TimeTableAdapter2.OnItemClickListener, ColorSelector.OnColorSelectedListener {
    @ReleaseWhenDestroy
    private AlertDialog loginDialog;
    @ReleaseWhenDestroy
    protected View rootView;
    @ReleaseWhenDestroy
    protected EditText mWiseIdView, mWisePasswdView;
    @ReleaseWhenDestroy
    private Spinner mWiseTermSpinner;
    @ReleaseWhenDestroy
    private Spinner mWiseYearSpinner;
    @ReleaseWhenDestroy
    private AlertDialog deleteDialog;
    @ReleaseWhenDestroy
    protected ListView listView;
    private ViewGroup mToolBarParent;
    @ReleaseWhenDestroy
    private LinearLayout mTabParent;

    private final ParseTimeTable2 mParser2 = new ParseTimeTable2();
    @AsyncData
    private TimeTable mTimeTable;
    private TimeTableAdapter2 mTimeTableAdapter2;

    String mTermText;
    private boolean mIsOnLoad;
    private Hashtable<String, Integer> colorTable;
    @ReleaseWhenDestroy
    private View emptyView;


    private SubjectDetailDialogFragment mSubjectDetailDialog = new SubjectDetailDialogFragment();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Context context = getActivity();
        if (savedInstanceState != null) {
            colorTable = (Hashtable<String, Integer>) savedInstanceState.getSerializable("color");
            mTimeTable = savedInstanceState.getParcelable(IOUtil.FILE_TIMETABLE);

        } else {
            colorTable = new Hashtable<>();
            mTimeTable = new TimeTable();

        }

        mSubjectDetailDialog.setColorTable(colorTable);
        mSubjectDetailDialog.setColorSelectedListener(this);

        initDialog();

        if (mTimeTable.semesterCode != null)
            setTermTextViewText(context);
        super.onCreate(savedInstanceState);

        mToolBarParent = (ViewGroup) getActivity().findViewById(R.id.toolbar_parent);
        mTabParent = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, mToolBarParent, false);

        mTimeTableAdapter2 = new TimeTableAdapter2(context, mTimeTable, colorTable);
        mTimeTableAdapter2.setOnItemClickListener(this);
    }

    private void initDialog() {
        Context context = getActivity();
        View wiseDialogLayout = View.inflate(context, R.layout.dialog_timetable_wise_login, null);

        mWiseYearSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_year);
        mWiseYearSpinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, OApiUtil.getYears()));
        mWiseYearSpinner.setSelection(2);

        mWiseIdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_id_input);
        mWisePasswdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_passwd_input);
        mWiseTermSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_term);
        loginDialog = new MaterialDialog.Builder(context)
                .title(R.string.tab_timetable_wise_login_title)
                .customView(wiseDialogLayout, true)
                .positiveText(R.string.confirm)
                .positiveColorAttr(R.attr.colorPrimaryDark)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        clearPassWd();
                    }

                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        String id = mWiseIdView.getText().toString();
                        Context context = getActivity();

                        if (id.equals("123456789") && mWisePasswdView.length() < 1) {
                            if (AppUtil.test) {
                                AppUtil.test = false;
                            } else {
                                AppUtil.test = true;
                                AppUtil.showToast(context, "test", isVisible());
                            }

                            PrefUtil.getInstance(context).put("test", AppUtil.test);
                            clearText();
                            return;
                        }

                        if (mWisePasswdView.length() < 1 || StringUtil.NULL.equals(id)) {
                            AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_null, true);
                            clearText();
                        } else {
                            execute();
                        }
                    }
                })
                .build();
    }


    private void clearText() {
        clearId();
        clearPassWd();
    }

    private void clearId() {
        if (mWiseIdView != null && mWiseIdView.length() > 0) {
            TextKeyListener.clear(mWiseIdView.getText());
        }
    }

    private void clearPassWd() {
        if (mWisePasswdView != null && mWisePasswdView.length() > 0) {
            TextKeyListener.clear(mWisePasswdView.getText());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(IOUtil.FILE_TIMETABLE, mTimeTable);
        outState.putSerializable("color", colorTable);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_timetable, container, false);

        emptyView = rootView.findViewById(R.id.tab_timetable_empty);
        emptyView.setOnClickListener(this);

        listView = (ListView) rootView.findViewById(R.id.time_table_listView1);
        listView.setEmptyView(emptyView);
        listView.setAdapter(mTimeTableAdapter2);

        FloatingActionButton actionButton = (FloatingActionButton) rootView.findViewById(R.id.tab_timetable_action_btn);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                } else
                    loginDialog.show();
            }
        });

        registerProgressView(rootView.findViewById(R.id.progress_layout));
        return rootView;
    }

    @Override
    public void onResume() {
        if (mTimeTableAdapter2.isEmpty()) {
            mIsOnLoad = true;
            execute();
        }
        super.onResume();
    }

    @Override
    public void onItemClick(TimeTableAdapter2.ViewHolder vh, View v, Subject subject) {
        if (subject.isEqualsTo(Subject.EMPTY))
            return;

        mSubjectDetailDialog.setSubject(subject);
        mSubjectDetailDialog.setTimeTable(mTimeTable);

        if (!mSubjectDetailDialog.isAdded())
            mSubjectDetailDialog.show(getFragmentManager(), "subject");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_timetable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.action_wise:
                if (isRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);
                } else
                    loginDialog.show();
                return true;
*/
            case R.id.action_delete:
                if (deleteDialog == null) {
                    initDeleteDialog();
                }

                deleteDialog.show();
                return true;

            case R.id.action_save:
                saveTimetableImage();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        addOrRemoveTabMenu(isVisibleToUser);
    }

    private void addOrRemoveTabMenu(boolean visible) {
        if (mToolBarParent == null || mTabParent == null)
            return;
        if (visible) {
            if (mTabParent.getParent() == null)
                mToolBarParent.addView(mTabParent);
        } else if (mToolBarParent.indexOfChild(mTabParent) > 0) {
            mToolBarParent.removeView(mTabParent);
        }
    }

    private void saveTimetableImage() {
        if (mTimeTableAdapter2.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_id, true);
            return;
        }

        String dir = PrefUtil.getPictureSavedPath(getActivity()) + "timetable_" + mTimeTable.year + '_' + mTimeTable.semesterCode + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

        TimeTableImageSaveTask task = new TimeTableImageSaveTask(dir, listView, mTabParent);
        task.execute();
    }

    @Override
    public void onTransactResult(TimeTable result) {
        Context context = getActivity();
        if (result == null || result.isEmpty()) {
            if (!mIsOnLoad) {
                AppUtil.showToast(context, R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
            } else {
                emptyView.setVisibility(View.VISIBLE);
            }


            mIsOnLoad = false;
            return;
        }
        mIsOnLoad = false;

        mTimeTable.copyFrom(result);
        mTimeTableAdapter2.notifyDataSetChanged();

        setTermTextViewText(context);
    }

    private void setTermTextViewText(Context context) {
        mTermText = mTimeTable.year
                + " / "
                + context.getResources().getStringArray(R.array.terms)[mTimeTable.semesterCode.ordinal()];
        setSubtitleWhenVisible(mTermText);
    }

    @Override
    protected void execute() {
        emptyView.setVisibility(View.INVISIBLE);

        super.execute();
    }

    @Override
    protected void onTransactPostExecute() {
        super.onTransactPostExecute();
        clearPassWd();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TimeTable call() throws Exception {
        TimeTable result;

        Context context = getActivity();

        // Fragment가 처음 Attach되었을 때, 파일에서 시간표을 읽어온다.
        if (mIsOnLoad) {
            result = readTimetable(context);
            if (result != null) {
                colorTable.clear();
                colorTable.putAll(readColorTableFromFile(context));
            }

        } else {
            // 사용자가 WISE에 시간표 정보를 요청하였을 때
            Semester semester = Semester.values()[mWiseTermSpinner.getSelectedItemPosition()];
            String mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();

            HttpURLConnection connection = TimeTableHttpRequest.getHttpConnectionPost(mWiseIdView.getText(), mWisePasswdView.getText(), semester, mTimeTableYear);

            try {
                result = mParser2.parse(connection.getInputStream());
            } finally {
                connection.disconnect();
            }

            // 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
            // 시간표의 과목과 과목의 색을 Mapping한다.
            if (result != null && !result.isEmpty()) {

                Hashtable<String, Integer> colorTable = makeColorTable(result);
                saveColorTable(context, colorTable);
                this.colorTable.clear();
                this.colorTable.putAll(colorTable);

                IOUtil.saveToFile(context, IOUtil.FILE_TIMETABLE, Activity.MODE_PRIVATE, result);

            }

        }

        return result;
    }

    @Override
    public void exceptionOccured(Exception e) {
        if (e instanceof ClientProtocolException || e instanceof NullPointerException) {
            e.printStackTrace();
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
        } else {
            super.exceptionOccured(e);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_empty:
                loginDialog.show();
                break;
            default:
                break;
        }
    }


    /**
     * 시간표 정보를 파일로부터 읽어온다.
     *
     * @return 시간표 정보 파일이 없다면 null
     */
    @Nullable
    public static TimeTable readTimetable(Context context) {
        return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_TIMETABLE);
    }

    /**
     * 주어진 시간표 컬러 Map을 파일로 저장한다.
     *
     * @param colorTable color map
     */
    public static void saveColorTable(Context context, Hashtable<String, Integer> colorTable) {
        IOUtil.saveToFileAsync(context, IOUtil.FILE_COLOR_TABLE, Activity.MODE_PRIVATE, colorTable, null);
    }

    /**
     * color map을 파일로 부터 읽어온다.
     */
    public static Hashtable<String, Integer> readColorTableFromFile(Context context) {
        return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_COLOR_TABLE);
    }

    /**
     * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 파일에서 읽어오거나 작성한다.
     *
     * @param timeTable 시간표
     * @return 시간표의 각 과목과 컬러를 mapping하는 Map
     */
    public static Hashtable<String, Integer> getColorTable(TimeTable timeTable, Context context) {
        Hashtable<String, Integer> table = readColorTableFromFile(context);

        if (table == null || table.size() == 0) {
            table = makeColorTable(timeTable);
            saveColorTable(context, table);
        }
        return table;
    }


    /**
     * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 작성한다.
     *
     * @param timetable 시간표
     * @return 과목이름이 Key이고, Value가 컬러를 가리키는 Integer인 Map<br>
     * * 컬러는 단순한 정수이며, AppUtil을 통해 Color integer를 얻어와야 한다.
     */
    public static Hashtable<String, Integer> makeColorTable(TimeTable timetable) {
        Hashtable<String, Integer> table = new Hashtable<>();

        ArrayList<Subject[]> subjects = timetable.subjects;

        String subjectName;
        int i = 0;
        for (Subject[] subjectArray : subjects) {
            for (Subject subject : subjectArray) {

                if (subject.equals(Subject.EMPTY))
                    continue;

                subjectName = subject.subjectName;
                if (!subjectName.equals(StringUtil.NULL) && !table.containsKey(subjectName)) {
                    table.put(subjectName, i++);
                }
            }
        }

        return table;
    }

    private void initDeleteDialog() {
        deleteDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.confirm_delete)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);

                        AsyncLoader.excute(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                boolean b = getActivity().deleteFile(IOUtil.FILE_TIMETABLE);
                                if (b) {
                                    getActivity().deleteFile(IOUtil.FILE_COLOR_TABLE);
                                    AppUtil.clearTimeTableColor(getActivity());

                                }

                                return b;
                            }
                        }, new OnTaskFinishedListener() {
                            @Override
                            public void onTaskFinished(boolean isExceptionOccurred, Object data) {
                                boolean result = (Boolean) data;
                                if (!isExceptionOccurred && result) {
                                    mTimeTableAdapter2.clear();
                                    mTimeTableAdapter2.notifyDataSetChanged();

                                    Context context = getActivity();
                                    AppUtil.showToast(context, R.string.execute_delete, isVisible());

                                    //TimeTableInfoCallback.clearAllAlarm(context);

                                    mTermText = StringUtil.NULL;

                                } else {
                                    AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                                }
                            }
                        });
                    }
                })
                .negativeText(R.string.cancel)
                .build();
    }

    @Override
    protected CharSequence getSubtitle() {
        return mTermText;
    }

    @Override
    public void onColorSelected(int i) {
        mTimeTableAdapter2.notifyDataSetChanged();
    }

    private static class TimeTableImageSaveTask extends ListViewBitmapWriteTask {
        private WeakReference<View> titleViewRef;

        public TimeTableImageSaveTask(String fileName, ListView listView, View titleView) {
            super(fileName, listView);
            titleViewRef = new WeakReference<>(titleView);
        }

        @Override
        public Bitmap getBitmap() {
            try {
                Looper.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap capture = null, titleBitmap = null, bitmap;
            View title = null;
            try {
                capture = super.getBitmap();
                title = titleViewRef.get();

                title.setDrawingCacheEnabled(true);
                title.buildDrawingCache(true);
                titleBitmap = title.getDrawingCache(true);
                if (titleBitmap == null)
                    titleBitmap = GraphicUtil.createBitmapFromView(title);
                bitmap = GraphicUtil.merge(titleBitmap, capture);

                return bitmap;
            } finally {
                if (capture != null)
                    capture.recycle();
                if (titleBitmap != null)
                    titleBitmap.recycle();
                if (title != null) {
                    title.destroyDrawingCache();
                    title.setDrawingCacheEnabled(false);
                }
            }
        }
    }
}
