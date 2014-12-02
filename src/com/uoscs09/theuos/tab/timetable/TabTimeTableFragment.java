package com.uoscs09.theuos.tab.timetable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.method.TextKeyListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.AsyncLoader;
import com.uoscs09.theuos.common.AsyncLoader.OnTaskFinishedListener;
import com.uoscs09.theuos.common.ListViewBitmapWriteTask;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.GraphicUtil;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.TimeTableHttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabTimeTableFragment extends
		AbsDrawableProgressFragment<ArrayList<TimeTableItem>> implements
		DialogInterface.OnClickListener, View.OnClickListener {
	@ReleaseWhenDestroy
	private AlertDialog loginDialog;
	@AsyncData
	private List<TimeTableItem> mTimetableList;
	@ReleaseWhenDestroy
	protected ArrayAdapter<TimeTableItem> adapter;
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
	protected Term term;
	@ReleaseWhenDestroy
	protected ListView listView;
	@ReleaseWhenDestroy
	private TimeTableInfoCallback cb;
	private boolean mIsOnLoad;
	private Map<String, Integer> colorTable;
	private String mTermText;
	private String mTimeTableYear;

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context context = getActivity();
		if (savedInstanceState != null) {
			mTimetableList = savedInstanceState
					.getParcelableArrayList(IOUtil.FILE_TIMETABLE);
			colorTable = (Map<String, Integer>) savedInstanceState
					.getSerializable("color");
		} else {
			mTimetableList = new ArrayList<TimeTableItem>();
			colorTable = new Hashtable<String, Integer>();
		}
		cb = new TimeTableInfoCallback(context);

		PrefUtil pref = PrefUtil.getInstance(context);
		int termValue = pref.get("timetable_term", -1);
		if (termValue != -1) {
			term = Term.values()[termValue];
			cb.term = term;
		} else {
			term = OApiUtil.getTerm();
			cb.term = term;
		}
		mTimeTableYear = pref.get("timetable_year",
				OApiUtil.getSemesterYear(term));
		cb.year = mTimeTableYear;

		adapter = new TimetableAdapter(context, R.layout.list_layout_timetable,
				mTimetableList, colorTable, cb);
		View wiseDialogLayout = View.inflate(context,
				R.layout.dialog_timetable_wise_login, null);
		mWiseYearSpinner = (Spinner) wiseDialogLayout
				.findViewById(R.id.dialog_wise_spinner_year);
		mWiseYearSpinner.setAdapter(new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_dropdown_item, OApiUtil
						.getYears()));
		mWiseYearSpinner.setSelection(2);

		mWiseIdView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_id_input);
		mWisePasswdView = (EditText) wiseDialogLayout
				.findViewById(R.id.dialog_wise_passwd_input);
		mWiseTermSpinner = (Spinner) wiseDialogLayout
				.findViewById(R.id.dialog_wise_spinner_term);
		loginDialog = new AlertDialog.Builder(context)
				.setTitle(R.string.tab_timetable_wise_login_title)
				.setView(wiseDialogLayout)
				.setPositiveButton(R.string.confirm, this)
				.setNegativeButton(R.string.cancel, this).create();
		AppUtil.setDialogMaterial(loginDialog, getActivity());

		if (termValue != -1)
			setTermTextViewText(term, context);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE: {
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
				AppUtil.showToast(context,
						R.string.tab_timetable_wise_login_warning_null, true);
				clearText();
				return;
			} else {
				excute();
			}
			break;
		}
		case DialogInterface.BUTTON_NEGATIVE: {
			clearPassWd();
			break;
		}
		default:
			break;
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(IOUtil.FILE_TIMETABLE,
				(ArrayList<? extends Parcelable>) mTimetableList);
		outState.putSerializable("color", (Serializable) colorTable);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.tab_timetable, container, false);
		View emptyView = rootView.findViewById(R.id.tab_timetable_empty);
		emptyView.setOnClickListener(this);
		listView = (ListView) rootView.findViewById(R.id.time_table_listView1);
		listView.setEmptyView(emptyView);
		listView.setAdapter(adapter);

		return rootView;
	}

	@Override
	public void onResume() {
		if (adapter.isEmpty()) {
			mIsOnLoad = true;
			excute();
		}
		super.onResume();
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
			if (isRunning()) {
				AppUtil.showToast(getActivity(), R.string.progress_ongoing,
						true);
			} else
				loginDialog.show();
			return true;
		case R.id.action_delete:
			if (deleteDialog == null) {
				deleteDialog = getDeleteDialog();
				AppUtil.setDialogMaterial(deleteDialog, getActivity());
			}
			deleteDialog.show();
			return true;
		case R.id.action_save:
			saveTimetable();
			return true;
		default:
			return false;
		}
	}

	private void saveTimetable() {
		if (adapter.isEmpty()) {
			AppUtil.showToast(getActivity(), "시간표 정보가 없습니다.", true);
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(PrefUtil.getPictureSavedPath(getActivity()))
				.append("timetable_").append(mTimeTableYear).append('_')
				.append(term).append('_')
				.append(String.valueOf(System.currentTimeMillis()))
				.append(".png");
		String dir = sb.toString();
		ListViewBitmapWriteTask task = new ListViewBitmapWriteTask(
				getActivity(), dir, listView) {
			@Override
			public Bitmap getBitmap() {
				Bitmap capture = null, titleBitmap = null, bitmap = null;
				View title = null;
				try {
					bitmap = super.getBitmap();
					title = rootView.findViewById(R.id.tab_timetable_title);
					title.setDrawingCacheEnabled(true);
					title.buildDrawingCache(true);
					titleBitmap = title.getDrawingCache(true);
					if (titleBitmap == null)
						titleBitmap = GraphicUtil.createBitmapFromView(title);
					capture = GraphicUtil
							.getWholeListViewItemsToBitmap(listView);
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
		};
		task.excute();
	}

	@Override
	public void onTransactResult(ArrayList<TimeTableItem> result) {
		Context context = getActivity();
		if (result.isEmpty()) {
			if (!mIsOnLoad) {
				AppUtil.showToast(context,
						R.string.tab_timetable_wise_login_warning_fail,
						isMenuVisible());
			}
			mIsOnLoad = false;
			return;
		}
		mIsOnLoad = false;
		mTimetableList.clear();
		mTimetableList.addAll(result);
		if (adapter != null)
			adapter.notifyDataSetChanged();

		setTermTextViewText(term, context);
	}

	private void setTermTextViewText(Term term, Context context) {
		mTermText = mTimeTableYear
				+ " / "
				+ context.getResources().getStringArray(R.array.terms)[term
						.ordinal()];
		setSubtitleWhenVisible(mTermText);
	}

	@Override
	protected void onTransactPostExcute() {
		super.onTransactPostExcute();
		clearPassWd();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<TimeTableItem> call() throws Exception {
		ArrayList<TimeTableItem> result;
		Context context = getActivity();
		// Fragment가 처음 Attach되었을 때, 파일에서 시간표을 읽어온다.
		if (mIsOnLoad) {
			result = (ArrayList<TimeTableItem>) readTimetable(context);
		} else { // 사용자가 WISE에 시간표 정보를 요청하였을 때
			term = Term.values()[mWiseTermSpinner.getSelectedItemPosition()];
			mTimeTableYear = mWiseYearSpinner.getSelectedItem().toString();
			String body = TimeTableHttpRequest.getHttpBodyPost(
					mWiseIdView.getText(), mWisePasswdView.getText(), term,
					mTimeTableYear);
			cb.term = term;
			cb.year = mTimeTableYear;
			result = (ArrayList<TimeTableItem>) ParseFactory.create(
					ParseFactory.What.TimeTable, body, 0).parse();

			PrefUtil pref = PrefUtil.getInstance(context);
			pref.put("timetable_term", term.ordinal());
			pref.put("timetable_year", mTimeTableYear);
			TimeTableInfoCallback.clearAllAlarm(context);
			saveColorTable(context, makeColorTable(result));
		}

		// 시간표를 정상적으로 불러왔다면, 시간표를 저장하고,
		// 시간표의 과목과 과목의 색을 Mapping한다.
		if (!result.isEmpty()) {
			IOUtil.saveToFile(context, IOUtil.FILE_TIMETABLE,
					Activity.MODE_PRIVATE, result);
			colorTable.putAll(getColorTable(result, context));
		}
		return result;
	}

	@Override
	public void exceptionOccured(Exception e) {
		if (e instanceof ClientProtocolException
				|| e instanceof NullPointerException) {
			AppUtil.showToast(getActivity(),
					R.string.tab_timetable_wise_login_warning_fail,
					isMenuVisible());
		} else {
			super.exceptionOccured(e);
		}
	}

	/**
	 * 시간표 정보를 파일로부터 읽어온다.
	 * 
	 * @return 시간표 정보 list, 파일이 없다면 빈 list
	 */
	public static List<TimeTableItem> readTimetable(Context context) {
		List<TimeTableItem> list = IOUtil.readFromFileSuppressed(context,
				IOUtil.FILE_TIMETABLE);
		if (list == null) {
			list = new ArrayList<TimeTableItem>();
		}
		return list;
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
	 * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 작성한다.
	 * 
	 * @param timetable
	 *            시간표
	 * @return 과목이름이 Key이고, Value가 컬러를 가리키는 Integer인 Map<br>
	 *         * 컬러는 단순한 정수이며, AppUtil을 통해 Color integer를 얻어와야 한다.
	 */
	public static Hashtable<String, Integer> makeColorTable(
			List<TimeTableItem> timetable) {
		Hashtable<String, Integer> table = new Hashtable<String, Integer>();
		final int size = timetable.size();
		String name;
		TimeTableItem item;
		String[] array;
		int j = 0, h = 0;
		for (int i = 0; i < size; i++) {
			item = timetable.get(i);
			array = new String[] { item.mon, item.tue, item.wed, item.thr,
					item.fri, item.sat };
			for (h = 0; h < array.length; h++) {
				name = OApiUtil.getSubjectName(array[h]);
				if (!name.equals(StringUtil.NULL) && !name.equals(array[h])
						&& !table.containsKey(name)) {
					table.put(name, j++);
				}
			}
		}
		return table;
	}

	/**
	 * 주어진 시간표정보를 통해 시간표 각 과목과 컬러를 mapping하는 Map을 파일에서 읽어오거나 작성한다.
	 * 
	 * @param timetable
	 *            시간표
	 * @param context
	 * @return 시간표의 각 과목과 컬러를 mapping하는 Map
	 */
	public static Hashtable<String, Integer> getColorTable(
			List<TimeTableItem> timetable, Context context) {
		Hashtable<String, Integer> table = readColorTableFromFile(context);

		if (table == null || table.size() == 0) {
			table = makeColorTable(timetable);
			saveColorTable(context, table);
		}
		return table;
	}

	/**
	 * 주어진 시간표 컬러 Map을 파일로 저장한다.
	 * 
	 * @param colorTable
	 *            color map
	 * @param context
	 * */
	public static void saveColorTable(Context context,
			Hashtable<String, Integer> colorTable) {
		IOUtil.saveToFileAsync(context, IOUtil.FILE_COLOR_TABLE,
				Activity.MODE_PRIVATE, colorTable, null);
	}

	/**
	 * color map을 파일로 부터 읽어온다.
	 */
	public static Hashtable<String, Integer> readColorTableFromFile(
			Context context) {
		return IOUtil.readFromFileSuppressed(context, IOUtil.FILE_COLOR_TABLE);
	}

	private AlertDialog getDeleteDialog() {
		return new AlertDialog.Builder(getActivity())
				.setMessage(R.string.confirm_delete)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								AsyncLoader.excute(new Callable<Boolean>() {

									@Override
									public Boolean call() throws Exception {
										boolean b = getActivity().deleteFile(
												IOUtil.FILE_TIMETABLE);
										if (b) {
											getActivity().deleteFile(
													IOUtil.FILE_COLOR_TABLE);
										}
										return b;
									}
								}, new OnTaskFinishedListener() {
									@Override
									public void onTaskFinished(
											boolean isExceptionOccoured,
											Object data) {
										boolean result = (Boolean) data;
										if (!isExceptionOccoured && result) {
											adapter.clear();
											adapter.notifyDataSetChanged();
											mTimetableList.clear();
											Context context = getActivity();
											AppUtil.showToast(context,
													R.string.excute_delete,
													isVisible());
											TimeTableInfoCallback
													.clearAllAlarm(context);
											PrefUtil.getInstance(context).put(
													"timetable_term", -1);
											mTermText = StringUtil.NULL;
										} else {
											AppUtil.showToast(getActivity(),
													R.string.file_not_found,
													isMenuVisible());
										}
									}

								});
							}
						}).setNegativeButton(R.string.cancel, null).create();
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_wise);
	}

	@Override
	protected CharSequence getSubtitle() {
		return mTermText;
	}
}
