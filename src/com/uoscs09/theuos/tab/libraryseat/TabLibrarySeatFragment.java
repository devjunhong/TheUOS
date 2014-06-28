package com.uoscs09.theuos.tab.libraryseat;

import java.util.ArrayList;
import java.util.Calendar;

import pkg.asyncexcute.AsyncExecutor;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabLibrarySeatFragment extends
		AbsDrawableProgressFragment<ArrayList<SeatItem>> {
	/** �¼� ��Ȳ ����Ʈ ���� adapter */
	private ArrayAdapter<SeatItem> mSeatAdapter;
	/** ���� �� �¼� ���� ����Ʈ ���� adapter */
	private ArrayAdapter<String> mInfoAdapter;
	/** �¼� ���� ����Ʈ */
	private ArrayList<SeatItem> mSeatList;
	/** ���� �� �¼� ���� ����Ʈ */
	private ArrayList<String> mDissmissInfoList;
	/** �¼� ���� ����Ʈ �� */
	private ListView mSeatListView;
	/** ���� �� �¼� ���� ����Ʈ ��, infoDialog���� ��������. */
	private ListView mInfoListView;
	/** ��� �׼ǹٿ� �����Ǵ� layout, timeTextView�� ���ԵǾ� ����ȭ �ð��� ��Ÿ����. */
	private View mActionViewLayout;
	/** ��� �׼ǹٿ� �����Ǿ� ����ȭ �ð��� ��Ÿ���� TextView */
	private TextView mTimeTextView;
	/**
	 * ��� �׼ǹٿ� �����Ǵ� timeTextView�� ������ Text.<br>
	 * 
	 * {@code onSaveonSaveInstanceState()} ���� "COMMIT_TIME"��� �̸����� ����ȴ�.
	 */
	private String mCommitTime = StringUtil.NULL;
	/**
	 * ������ �¼� ���� ��ư ({@code R.id.action_info})�� �����ϸ� ��Ÿ���� AlertDialog<br>
	 * ������ �¼� ������ �����ش�.
	 */
	private AlertDialog mInfoDialog;
	/** �߾� ������ �¼� ���� Ȯ�� ������ */
	private final static String URL = "http://203.249.102.34:8080/seat/domian5.asp";
	/** bundle���� ����ȭ �ð� ���� String�� ����Ų��. */
	private final static String COMMIT_TIME = "COMMIT_TIME";
	/** bundle���� �¼� ���� List�� ����Ų��. */
	private final static String BUNDLE_LIST = "SeatList";
	/** bundle���� ������ �¼� ���� List�� ����Ų��. */
	private final static String INFO_LIST = "InfoList";
	/** {@code SubSeatWebActivity}�� ������ SeatItem�� ����Ų��. */
	protected final static String ITEM = "item";
	/**
	 * ListView�� header/footer ������ os������ ���� �޶����� ������ �����ϴ� ���� ����<br>
	 * <b>KITKAT
	 */
	private final static int VER_KITKAT = 19;
	/**
	 * ListView�� header/footer ������ os������ ���� �޶����� ������ �����ϴ� ���� ����<br>
	 * <b>JELLYBEAN
	 */
	private final static int VER_JELLYBEAN = 18;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			mCommitTime = savedInstanceState.getString(COMMIT_TIME);
			mSeatList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
			mDissmissInfoList = savedInstanceState
					.getStringArrayList(INFO_LIST);
		} else {
			this.mSeatList = new ArrayList<SeatItem>();
			mDissmissInfoList = new ArrayList<String>();
		}
		Activity activity = getActivity();
		mActionViewLayout = View.inflate(activity,
				R.layout.action_tab_lib_seat_view, null);
		mTimeTextView = (TextView) mActionViewLayout
				.findViewById(R.id.tab_library_seat_action_text_last_commit_time);
		mInfoAdapter = new SeatDissmissInfoListAdapter(activity,
				R.layout.list_layout_two_text_view, mDissmissInfoList);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_libraryseat, container,
				false);
		Activity activity = getActivity();
		switch (AppUtil.theme) {
		case Black:
			mSeatAdapter = new SeatListAdapter(activity,
					R.layout.list_layout_seat_dark, mSeatList);
			break;
		case BlackAndWhite:
		case White:
		default:
			mSeatAdapter = new SeatListAdapter(activity,
					R.layout.list_layout_seat, mSeatList);
			break;
		}

		viewInit(rootView);
		if (mSeatList.size() == 0) {
			AsyncExecutor<ArrayList<SeatItem>> executor = getExecutor();
			if (executor == null
					|| !executor.getStatus().equals(AsyncTask.Status.FINISHED)) {
				excute();
			}
		} else {
			mSeatListView.removeFooterView(getLoadingView());
		}
		mSeatListView.setOnItemClickListener(itemClickListenerOfLanguageList);

		mInfoListView = new ListView(activity);
		TextView emptyView = new TextView(activity);
		emptyView.setText(R.string.tab_library_seat_dissmiss_info_not_loading);
		mInfoListView.setEmptyView(emptyView);
		mInfoListView.setAdapter(mInfoAdapter);

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(COMMIT_TIME, mCommitTime);
		outState.putParcelableArrayList(BUNDLE_LIST, mSeatList);
		outState.putStringArrayList(INFO_LIST, mDissmissInfoList);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
			mTimeTextView.setTextColor(Color.WHITE);
			inflater.inflate(R.menu.tab_library_seat_dark, menu);
			break;
		case Black:
			inflater.inflate(R.menu.tab_library_seat_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_library_seat, menu);
			break;
		}

		mTimeTextView.setText(mCommitTime);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setCustomView(mActionViewLayout);
		actionBar.setDisplayShowCustomEnabled(true);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			AsyncExecutor<ArrayList<SeatItem>> executor = getExecutor();
			if (executor != null && VERSION.SDK_INT < VER_KITKAT
					&& !executor.getStatus().equals(AsyncTask.Status.RUNNING)) {
				mSeatListView.addFooterView(getLoadingView());
			}
			excute();
			return true;
		case R.id.action_info:
			AsyncExecutor<ArrayList<SeatItem>> excutor = getExecutor();
			if (excutor != null
					&& excutor.getStatus().equals(AsyncTask.Status.RUNNING)) {
				AppUtil.showToast(getActivity(),
						R.string.tab_library_seat_dissmiss_info_not_loading,
						true);
				return true;
			}
			if (mInfoDialog == null) {
				mInfoDialog = new AlertDialog.Builder(getActivity())
						.setTitle(getText(R.string.action_dissmiss_info))
						.setView(mInfoListView).create();
			}
			mInfoDialog.show();
			return true;
		default:
			return false;
		}
	}

	private void viewInit(View rootView) {
		mSeatListView = (ListView) rootView
				.findViewById(R.id.tab_library_list_seat);
		if (VERSION.SDK_INT < VER_KITKAT) {
			mSeatListView.addFooterView(getLoadingView());
		}
		mSeatListView.setAdapter(mSeatAdapter);
	}

	@Override
	protected void excute() {
		if (VERSION.SDK_INT > VER_JELLYBEAN) {
			AsyncExecutor<ArrayList<SeatItem>> executor = getExecutor();
			if (executor == null) {
				mSeatListView.addFooterView(getLoadingView());
			} else if (executor != null
					&& executor.getStatus() != Status.RUNNING) {
				mSeatListView.addFooterView(getLoadingView());
			}
		}
		mSeatList.clear();
		mSeatAdapter.clear();
		super.excute();
	}

	@Override
	public void onPostExcute() {
		super.onPostExcute();
		mSeatListView.removeFooterView(getLoadingView());
	}

	@Override
	public void onResult(ArrayList<SeatItem> result) {
		updateTimeView();
		mSeatAdapter.clear();
		mSeatAdapter.addAll(result);
		mSeatAdapter.notifyDataSetChanged();
		mInfoAdapter.notifyDataSetChanged();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<SeatItem> call() throws Exception {
		String body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR);
		ArrayList<SeatItem> callSeatList = (ArrayList<SeatItem>) ParseFactory
				.create(ParseFactory.What.Seat, body, 0).parse();

		// '������ �¼� ����' ������ ����Ʈ�� �߰�
		SeatItem dissmisInfo = callSeatList.remove(callSeatList.size() - 1);
		mDissmissInfoList.clear();
		mInfoAdapter.clear();
		String[] array = dissmisInfo.occupySeat.split(StringUtil.NEW_LINE);
		for (int i = 0; i < array.length - 1; i += 2) {
			mDissmissInfoList.add(array[i] + "+" + array[i + 1]);
		}

		// �̿���� 50%�� �Ѵ� ���͵���� �������� ����
		if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_SEAT,
				false)) {
			getFilteredList(callSeatList);
		}
		return callSeatList;
	}

	private void getFilteredList(ArrayList<SeatItem> originalList) {
		SeatItem item;
		// ���͵�� �ε���
		final int[] filterArr = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 23,
				24, 25, 26, 27, 28 };
		final int size = filterArr.length;
		for (int i = size - 1; i > -1; i--) {
			item = originalList.get(filterArr[i]);
			if (Double.valueOf(item.utilizationRate) >= 50) {
				originalList.remove(item);
			}
		}
	}

	private OnItemClickListener itemClickListenerOfLanguageList = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			if (!isMenuVisible())
				return;
			SeatItem item = (SeatItem) arg0.getItemAtPosition(position);
			if (item == null)
				return;
			Activity activity = getActivity();
			Intent intent = new Intent(activity, SubSeatWebActivity.class);
			intent.putExtra(ITEM, item);
			startActivity(intent);
			AppUtil.overridePendingTransition(activity, 1);
		}
	};

	private void updateTimeView() {
		// Fragment�� Attatch ���� ���� ���
		if (getActivity() == null)
			return;
		Calendar c = Calendar.getInstance();
		int ampm = c.get(Calendar.AM_PM);
		int h = c.get(Calendar.HOUR) == 0 ? 12 : c.get(Calendar.HOUR);
		int m = c.get(Calendar.MINUTE);
		int s = c.get(Calendar.SECOND);

		StringBuilder sb = new StringBuilder();
		sb.append(getText(R.string.tab_library_seat_last_update));

		if (ampm == Calendar.AM) {
			sb.append(StringUtil.STR_AM);
		} else {
			sb.append(StringUtil.STR_PM);
		}
		sb.append(h);
		sb.append(':');
		if (m < 10) {
			sb.append(0);
		}
		sb.append(m);
		sb.append(':');
		if (s < 10) {
			sb.append(0);
		}
		sb.append(s);
		mCommitTime = sb.toString();
		if (mTimeTextView != null) {
			mTimeTextView.setText(mCommitTime);
		}
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_refresh);
	}

}
