package com.uoscs09.theuos2.tab.timetable;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.util.SimpleArrayMap;
import android.support.v7.app.AlertDialog;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.gc.materialdesign.widgets.ColorSelector;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.async.AsyncUtil;
import com.uoscs09.theuos2.async.Processor;
import com.uoscs09.theuos2.async.Request;
import com.uoscs09.theuos2.base.AbsProgressFragment;
import com.uoscs09.theuos2.common.SerializableArrayMap;
import com.uoscs09.theuos2.customview.NestedListView;
import com.uoscs09.theuos2.http.TimeTableHttpRequest;
import com.uoscs09.theuos2.parse.XmlParserWrapper;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.IOUtil;
import com.uoscs09.theuos2.util.ImageUtil;
import com.uoscs09.theuos2.util.OApiUtil;
import com.uoscs09.theuos2.util.OApiUtil.Semester;
import com.uoscs09.theuos2.util.PrefUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

public class TabTimeTableFragment2 extends AbsProgressFragment<TimeTable>
        implements View.OnClickListener, Request.ResultListener<TimeTable>, Request.ErrorListener, Processor<TimeTable, TimeTable> {
    private AlertDialog mLoginDialog;
    protected View rootView;
    protected EditText mWiseIdView, mWisePasswdView;
    private Spinner mWiseTermSpinner, mWiseYearSpinner;
    private AlertDialog mDeleteDialog;
    protected NestedListView mTimetableListView;
    private View emptyView;
    private Dialog mProgressDialog;

    @AsyncData
    private TimeTable mTimeTable;
    private TimeTableAdapter2 mTimeTableAdapter2;

    private final SerializableArrayMap<String, Integer> colorTable = new SerializableArrayMap<>();


    private static final ParseTimeTable2 TIME_TABLE_PARSER = new ParseTimeTable2();


    private final SubjectDetailDialogFragment mSubjectDetailDialog = new SubjectDetailDialogFragment();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Map<String, Integer> colorMap = (Map<String, Integer>) savedInstanceState.getSerializable("color");
            if (colorMap != null)
                colorTable.putAll(colorMap);
            mTimeTable = savedInstanceState.getParcelable(IOUtil.FILE_TIMETABLE);

        } else {
            mTimeTable = new TimeTable();

        }

        mSubjectDetailDialog.setColorTable(colorTable);
        mSubjectDetailDialog.setColorSelectedListener(new ColorSelector.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int i) {
                mTimeTableAdapter2.notifyDataSetChanged();
            }
        });

        initDialog();

        super.onCreate(savedInstanceState);

        if (mTimeTable.semesterCode != null)
            setTermTextViewText(mTimeTable);


        ViewGroup mTabParent = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_tab_timetable_toolbar_menu, getToolbarParent(), false);

        mTimeTableAdapter2 = new TimeTableAdapter2(getActivity(), mTimeTable, colorTable);
        mTimeTableAdapter2.setOnItemClickListener(new TimeTableAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(TimeTableAdapter2.TimeTableViewHolder vh, View v, Subject subject) {
                if (subject.isEqualsTo(Subject.EMPTY))
                    return;

                mSubjectDetailDialog.setSubject(subject);
                mSubjectDetailDialog.setTimeTable(mTimeTable);

                if (!mSubjectDetailDialog.isAdded()) {
                    mSubjectDetailDialog.show(getFragmentManager(), "subject");
                    sendClickEvent("detail subject");
                }

            }
        });

        registerTabParentView(mTabParent);

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
        emptyView.findViewById(R.id.tab_timetable_empty_text).setOnClickListener(this);

        mTimetableListView = (NestedListView) rootView.findViewById(R.id.time_table_listView1);
        mTimetableListView.setEmptyView(emptyView);
        mTimetableListView.setAdapter(mTimeTableAdapter2);

        registerProgressView(rootView.findViewById(R.id.progress_layout));

        if (savedInstanceState == null)
            readTimetableFromFileOnFragmentCreated();

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_timetable, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_wise:
                if (isTaskRunning()) {
                    AppUtil.showToast(getActivity(), R.string.progress_ongoing, true);

                } else {
                    sendClickEvent("wise login");
                    mLoginDialog.show();
                }
                return true;

            case R.id.action_delete:
                if (mDeleteDialog == null) {
                    initDeleteDialog();
                }

                sendClickEvent("delete timetable");
                mDeleteDialog.show();
                return true;

            case R.id.action_save:

                sendClickEvent("save timetable image");
                saveTimetableImage();
                return true;

            default:
                return false;
        }
    }

    private void dismissProgressDialog() {
        mProgressDialog.dismiss();
        mProgressDialog.setOnCancelListener(null);
    }

    private void saveTimetableImage() {
        if (mTimeTableAdapter2.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_not_exist, true);
            return;
        }

        if (mProgressDialog == null)
            mProgressDialog = AppUtil.getProgressDialog(getActivity(), false, getText(R.string.progress_ongoing), null);

        mTimeTableAdapter2.changeLayout(true);

        String dir = PrefUtil.getPicturePath(getActivity()) + "timetable_" + mTimeTable.year + '_' + mTimeTable.semesterCode + '_' + String.valueOf(System.currentTimeMillis()) + ".png";

        final AsyncTask<Void, ?, String> task = new ImageUtil.ListViewBitmapRequest.Builder(mTimetableListView, mTimeTableAdapter2)
                .setHeaderView(getTabParentView())
                .build()
                .wrap(new ImageUtil.ImageWriteProcessor(dir))
                .getAsync(
                        new Request.ResultListener<String>() {
                            @Override
                            public void onResult(final String result) {
                                dismissProgressDialog();
                                mTimeTableAdapter2.changeLayout(false);

                                Snackbar.make(getView(), getText(R.string.tab_timetable_saved), Snackbar.LENGTH_LONG)
                                        .setAction(R.string.action_open, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                                                AppUtil.startActivityWithScaleUp(getActivity(), intent, v);
                                            }
                                        })
                                        .show();
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                dismissProgressDialog();
                                mTimeTableAdapter2.changeLayout(false);

                                AppUtil.showErrorToast(getActivity(), e, true);
                            }
                        }
                );
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsyncUtil.cancelTask(task);
            }
        });
        mProgressDialog.show();

    }

    private void setTermTextViewText(@NonNull TimeTable timeTable) {
        setSubtitleWhenVisible(timeTable.getYearAndSemester());
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        emptyView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute() {
        super.onPostExecute();
        clearPassWd();
    }

    void execute() {
        Semester semester = Semester.values()[mWiseTermSpinner.getSelectedItemPosition()];
        String mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();

        execute(true,
                TimeTableHttpRequest.newRequest(mWiseIdView.getText(), mWisePasswdView.getText(), semester, mTimeTableYear)
                        .wrap(new XmlParserWrapper<>(TIME_TABLE_PARSER))
                        .wrap(IOUtil.<TimeTable>newFileWriteProcessor(getActivity(), IOUtil.FILE_TIMETABLE))
                        .wrap(this), this, this, true);
    }

    @Override
    public TimeTable process(TimeTable timeTable) throws Exception {
        // 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
        // 시간표의 과목과 과목의 색을 Mapping 한다.
        if (timeTable != null && !timeTable.isEmpty()) {

            Context context = getActivity();
            SerializableArrayMap<String, Integer> newColorTable = TimetableUtil.makeColorTable(timeTable);
            TimetableUtil.saveColorTable(context, newColorTable);

            colorTable.clear();
            colorTable.putAll((SimpleArrayMap<String, Integer>) newColorTable);

            timeTable.getClassTimeInformationTable();
            //TimetableUtil.writeTimetable(context, result);

        }
        return timeTable;
    }

    @Override
    public void onResult(TimeTable result) {
        if (result == null || result.isEmpty()) {
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());

            if (mTimeTableAdapter2.isEmpty())
                emptyView.setVisibility(View.VISIBLE);

            return;
        }

        mTimeTable.copyFrom(result);
        mTimeTableAdapter2.notifyDataSetChanged();

        setTermTextViewText(mTimeTable);
    }

    @Override
    public void onError(Exception e) {
        if (mTimeTableAdapter2.isEmpty())
            emptyView.setVisibility(View.VISIBLE);

        if (e instanceof IOException || e instanceof NullPointerException) {
            e.printStackTrace();
            AppUtil.showToast(getActivity(), R.string.tab_timetable_wise_login_warning_fail, isMenuVisible());
        } else {
            simpleErrorRespond(e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_timetable_empty_text:
                sendEmptyViewClickEvent();
                mLoginDialog.show();
                break;

            default:
                break;
        }
    }

    private void readTimetableFromFileOnFragmentCreated() {
        AsyncUtil.newRequest(
                new Callable<TimeTable>() {
                    @Override
                    public TimeTable call() throws Exception {
                        TimeTable timeTable = TimetableUtil.readTimetable(getActivity());
                        if (timeTable != null) {
                            colorTable.clear();
                            SimpleArrayMap<String, Integer> map = TimetableUtil.readColorTableFromFile(getActivity());
                            if (map != null)
                                colorTable.putAll(map);
                            timeTable.getClassTimeInformationTable();
                        }

                        return timeTable;
                    }
                })
                .getAsync(
                        new Request.ResultListener<TimeTable>() {
                            @Override
                            public void onResult(TimeTable result) {
                                if (result == null || result.isEmpty()) {
                                    emptyView.setVisibility(View.VISIBLE);

                                } else {
                                    mTimeTable.copyFrom(result);
                                    mTimeTableAdapter2.notifyDataSetChanged();

                                    setTermTextViewText(mTimeTable);
                                }
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                Log.e("TimeTable", "cannot read timetable from file.", e);
                            }
                        }
                );
    }

    private void initDialog() {
        Context context = getActivity();
        View wiseDialogLayout = View.inflate(context, R.layout.dialog_timetable_wise_login, null);

        mWiseYearSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_year);
        mWiseYearSpinner.setAdapter(new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, OApiUtil.getYears()));
        mWiseYearSpinner.setSelection(2);

        mWiseIdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_id_input);
        mWisePasswdView = (EditText) wiseDialogLayout.findViewById(R.id.dialog_wise_passwd_input);
        mWiseTermSpinner = (Spinner) wiseDialogLayout.findViewById(R.id.dialog_wise_spinner_term);

        DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        loginToWise();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        clearPassWd();
                        break;

                    default:
                        break;
                }
            }
        };

        mLoginDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.tab_timetable_wise_login_title)
                .setView(wiseDialogLayout)
                .setPositiveButton(R.string.confirm, l)
                .setNegativeButton(R.string.cancel, l)
                .create();
    }

    private void loginToWise() {
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


    private void initDeleteDialog() {
        mDeleteDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteTimetable();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    void deleteTimetable() {
        mDeleteDialog.show();
        AsyncUtil.newRequest(
                new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return TimetableUtil.deleteTimetable(getActivity());
                    }
                })
                .getAsync(
                        new Request.ResultListener<Boolean>() {
                            @Override
                            public void onResult(Boolean result) {
                                mDeleteDialog.dismiss();
                                if (result) {
                                    mTimeTableAdapter2.clear();

                                    mTimeTable.copyFrom(new TimeTable());
                                    mTimeTableAdapter2.notifyDataSetChanged();

                                    AppUtil.showToast(getActivity(), R.string.execute_delete, isVisible());
                                    setSubtitleWhenVisible(null);
                                } else {
                                    AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                                }
                            }
                        },
                        new Request.ErrorListener() {
                            @Override
                            public void onError(Exception e) {
                                mDeleteDialog.dismiss();
                                AppUtil.showToast(getActivity(), R.string.file_not_found, isMenuVisible());
                            }
                        }
                );

    }


    @Override
    protected CharSequence getSubtitle() {
        if (mTimeTable != null)
            return mTimeTable.getYearAndSemester();
        else
            return null;
    }

    @NonNull
    @Override
    public String getScreenNameForTracker() {
        return "TabTimeTableFragment2";
    }

}
