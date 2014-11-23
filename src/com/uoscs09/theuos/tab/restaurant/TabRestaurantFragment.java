package com.uoscs09.theuos.tab.restaurant;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.IOUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabRestaurantFragment extends
		AbsDrawableProgressFragment<ArrayList<RestItem>> {
	@ReleaseWhenDestroy
	private ScrollView mScrollView;
	@ReleaseWhenDestroy
	private TextView mSemesterTimeView, mVacationTimeView,
			mContentBreakfastView, mContentLunchView, mContentSupperView;
	private int mCurrentMenuButtonID;
	@AsyncData
	private ArrayList<RestItem> mRestList;
	private String mCurrentRestName;

	private static final String BUTTON = "button";
	private static final String REST = "rest_list";
	private static final String[] timeSemester = {
			"학기중\n조식 : 08:00~10:00\n중식 : 11:00~14:00\n15:00~17:00",
			"학기중\n중식 : 11:30~14:00\n석식 : 15:00~19:00\n토요일 : 휴무",
			"학기중\n중식 : 11:30~13:30\n석식 : 17:00~18:30\n토요일 : 휴무",
			StringUtil.NULL, StringUtil.NULL };
	private static final String[] timeVacation = {
			"방학중\n조식 : 09:00~10:00\n	08:30~10:00\n(계절학기 기간)\n중식 : 11:00~14:00\n15:00~17:00\n석식 : 17:00~18:30\n토요일 : 휴무",
			"방학중\n중식 : 11:30~14:00\n석식 : 16:00~18:30\n토요일 : 휴무",
			"방학중 : 휴관\n\n\n", StringUtil.NULL, StringUtil.NULL };

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(BUTTON, mCurrentMenuButtonID);
		outState.putParcelableArrayList(REST, mRestList);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCurrentMenuButtonID = savedInstanceState.getInt(BUTTON);
			mRestList = savedInstanceState.getParcelableArrayList(REST);
		} else {
			mCurrentMenuButtonID = R.id.tab_rest_button_students_hall;
			mRestList = new ArrayList<RestItem>();
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_restaurant, container,
				false);
		for (int id : new int[] { R.id.tab_rest_button_students_hall,
				R.id.tab_rest_button_anekan, R.id.tab_rest_button_natural,
				R.id.tab_rest_button_main_8th, R.id.tab_rest_button_living }) {
			View menuView = rootView.findViewById(id);
			menuView.setOnClickListener(mButtonClickListener);
			rootView.setTag(id, menuView);
		}
		mScrollView = (ScrollView) rootView.findViewById(R.id.tab_rest_scroll);
		mSemesterTimeView = (TextView) rootView
				.findViewById(R.id.tab_rest_text_semester);
		mVacationTimeView = (TextView) rootView
				.findViewById(R.id.tab_rest_text_vacation);
		mContentBreakfastView = (TextView) rootView
				.findViewById(R.id.tab_rest_text_breakfast);
		mContentLunchView = (TextView) rootView
				.findViewById(R.id.tab_rest_text_lunch);
		mContentSupperView = (TextView) rootView
				.findViewById(R.id.tab_rest_text_supper);

		return rootView;
	}

	@Override
	public void onResume() {
		if (mRestList.isEmpty())
			excute();
		else
			getView().findViewById(mCurrentMenuButtonID).performClick();
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_restaurant, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			excute();
			return true;
		default:
			return false;
		}
	}

	private final OnClickListener mButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			View prev = getView().findViewWithTag("selected");
			if (prev != null) {
				prev.setSelected(false);
				prev.setTag(null);
			}
			v.setSelected(true);
			v.setTag("selected");
			mScrollView.scrollTo(0, 0);
			mCurrentMenuButtonID = v.getId();
			performClick(mCurrentMenuButtonID);
		}
	};

	private void performClick(int buttonId) {
		switch (buttonId) {
		case R.id.tab_rest_button_students_hall:
			setSemesterAndVacationText(0);
			setBody("학생회관 1층");
			break;
		case R.id.tab_rest_button_anekan:
			setSemesterAndVacationText(1);
			setBody("양식당 (아느칸)");
			break;
		case R.id.tab_rest_button_natural:
			setSemesterAndVacationText(2);
			setBody("자연과학관");
			break;
		case R.id.tab_rest_button_main_8th:
			setSemesterAndVacationText(3);
			setBody("본관 8층");
			break;
		case R.id.tab_rest_button_living:
			setSemesterAndVacationText(4);
			setBody("생활관");
			break;
		default:
			return;
		}
	}

	private void setSemesterAndVacationText(int i) {
		if (mSemesterTimeView != null)
			mSemesterTimeView.setText(timeSemester[i]);
		if (mVacationTimeView != null)
			mVacationTimeView.setText(timeVacation[i]);
	}

	public void setBody(final String name) {
		mCurrentRestName = name;
		setSubtitleWhenVisible(name);
		RestItem item = null;
		if (mRestList != null) {
			int size = mRestList.size();
			for (int i = 0; i < size; i++) {
				item = mRestList.get(i);
				if (name.contains(item.title)) {
					mContentBreakfastView.setText(item.breakfast);
					mContentLunchView.setText(item.lunch);
					mContentSupperView.setText(item.supper);
					break;
				}
				item = null;
			}
		}
		if (item == null) {
			mContentBreakfastView.setText(R.string.tab_rest_no_info);
			mContentLunchView.setText(R.string.tab_rest_no_info);
			mContentSupperView.setText(R.string.tab_rest_no_info);
		}
	}

	@Override
	public ArrayList<RestItem> call() throws Exception {
		Context context = getActivity();
		if (OApiUtil.getDateTime()
				- PrefUtil.getInstance(context).get(
						PrefUtil.KEY_REST_DATE_TIME, 0) < 3) {
			try {
				ArrayList<RestItem> list = IOUtil.readFromFile(context,
						IOUtil.FILE_REST);
				if (list != null)
					return list;
			} catch (Exception e) {
			}
		}
		// web에서 읽어온지 오래되었거나, 파일이 존재하지 않은경우
		// wer에서 읽어옴
		return getRestListFromWeb(context);
	}

	/** web에서 식단표을 읽어온다. */
	@SuppressWarnings("unchecked")
	public static ArrayList<RestItem> getRestListFromWeb(Context context)
			throws IOException {
		ArrayList<RestItem> list;
		String body = HttpRequest
				.getBody("http://m.uos.ac.kr/mkor/food/list.do");
		list = (ArrayList<RestItem>) ParseFactory.create(
				ParseFactory.What.Rest, body, ParseFactory.Value.BASIC).parse();
		IOUtil.saveToFile(context, IOUtil.FILE_REST, Activity.MODE_PRIVATE,
				list);
		PrefUtil.getInstance(context).put(PrefUtil.KEY_REST_DATE_TIME,
				OApiUtil.getDate());
		return list;
	}

	@Override
	public void exceptionOccured(Exception e) {
		super.exceptionOccured(e);
	}

	@Override
	public void onTransactResult(ArrayList<RestItem> result) {
		mRestList.clear();
		mRestList.addAll(result);
		getView().findViewById(mCurrentMenuButtonID).performClick();
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_refresh);
	}

	@Override
	protected CharSequence getSubtitle() {
		return mCurrentRestName;
	}
}
