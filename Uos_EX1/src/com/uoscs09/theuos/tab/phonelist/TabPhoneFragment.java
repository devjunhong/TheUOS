package com.uoscs09.theuos.tab.phonelist;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabPhoneFragment extends AbsAsyncFragment<ArrayList<PhoneItem>>
		implements View.OnClickListener {
	protected ArrayAdapter<PhoneItem> phoneAdapter;
	protected ProgressDialog progress;
	private AlertDialog dialog;
	protected static final String PHONE_LIST = "phone_list";
	private List<PhoneItem> phoneNumList;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(PHONE_LIST,
				(ArrayList<? extends Parcelable>) phoneNumList);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			phoneNumList = savedInstanceState
					.getParcelableArrayList(PHONE_LIST);
		} else {
			phoneNumList = PhoneNumberDB.getInstance(getActivity()).readAll();
			if (phoneNumList == null) {
				phoneNumList = new ArrayList<PhoneItem>();
			}
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_phone, container, false);

		initProgress();
		dialog = settingAlertDialog();

		// ����Ʈ ����� ����
		switch (AppUtil.theme) {
		case Black:
			phoneAdapter = new PhoneListAdapter(getActivity(),
					R.layout.list_layout_phone_dark, phoneNumList, this);
			break;
		case BlackAndWhite:
		case White:
		default:
			phoneAdapter = new PhoneListAdapter(getActivity(),
					R.layout.list_layout_phone, phoneNumList, this);
			break;
		}

		// ����Ʈ ��
		ListView phoneListView = (ListView) rootView
				.findViewById(R.id.tab_phone_list_phone);

		// ����� ����
		phoneListView.setAdapter(phoneAdapter);

		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			dialog.show();
			return true;
		case R.id.action_web:
			Activity activity = getActivity();
			startActivity(new Intent(activity, PhoneListWebActivity.class));
			AppUtil.overridePendingTransition(activity, 0);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.tab_phone_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_phone, menu);
			break;
		}
		MenuItem searchMenu = menu.findItem(R.id.action_search);

		SearchView searchView = (SearchView) searchMenu.getActionView();
		searchView.setOnQueryTextListener(new OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				phoneAdapter.getFilter().filter(newText);
				return true;
			}
		});
		searchView.setSubmitButtonEnabled(true);
		searchView.setQueryHint(getText(R.string.tab_phone_search_hint));
		searchMenu.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<PhoneItem> call() throws Exception {
		ArrayList<String> urlList = getUrlList();

		final int progressNumber = 100 / urlList.size();

		ArrayList<PhoneItem> phoneNumberList = new ArrayList<PhoneItem>();
		int howTo;
		String body = null;
		int size = urlList.size();
		for (int i = 0; i < size; i++) {
			if (getExecutor().isCancelled()) {
				return null;
			}
			try {
				body = HttpRequest.getBody(urlList.get(i));
				if (i < 7) {
					howTo = ParseFactory.Value.SUBJECT;
				} else if (i < 8) {
					howTo = ParseFactory.Value.CULTURE;
				} else if (i < 12) {
					howTo = ParseFactory.Value.BOTTOM;
				} else {
					howTo = ParseFactory.Value.BODY;
				}

				phoneNumberList.addAll((ArrayList<PhoneItem>) ParseFactory
						.create(ParseFactory.PHONE, body, howTo).parse());

			} catch (UnknownHostException e) {
				throw e;
			} catch (Exception e) {
				e.printStackTrace();
			}

			progress.setProgress(progressNumber * i - 1);
		}
		// DB �� ������ ���
		PhoneNumberDB telDB = PhoneNumberDB.getInstance(getActivity());
		for (PhoneItem item : phoneNumberList) {
			telDB.insertOrUpdate(item);
		}
		progress.setProgress(100);
		return telDB.readAll();
	}

	private void initProgress() {
		if (progress == null)
			progress = AppUtil.getProgressDialog(getActivity(), true,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancelExecutor();
						}
					});
	}

	private AlertDialog settingAlertDialog() {
		return new Builder(getActivity()).setTitle(R.string.caution)
				.setMessage(R.string.tab_phone_caution_update)
				.setNegativeButton(R.string.cancel, null)
				.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						progress.show();
						excute();
					}
				}).create();
	}

	@Override
	public void onPostExcute() {
		progress.dismiss();
	}

	@Override
	public void onResult(ArrayList<PhoneItem> result) {
		phoneAdapter.clear();
		phoneAdapter.addAll(result);
		phoneAdapter.notifyDataSetChanged();
		AppUtil.showToast(getActivity(), R.string.finish_update,
				isMenuVisible());
	}

	private ArrayList<String> getUrlList() {
		// ����� �� �繫�� �Ľ�
		String publicAffairs = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/claw/claw5.jsp?x=1&y=1&w=2";
		String engineering = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cengineering/cengineering5.jsp?x=1&y=3&w=2";
		String humanities = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/chumanities/chumanities5.jsp?x=1&y=4&w=2";
		String naturalSciences = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cnscience/cnscience5.jsp?x=1&y=5&w=2";
		String urban = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cuscience/cuscience5.jsp?x=1&y=6&w=2";
		String artAndPhysical = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/carts/carts5.jsp?x=1&y=7&w=2";
		String openMajor = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/openmajor/openmajor4.jsp?x=1&y=8&w=2";
		String general = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/liberal/liberal4.jsp?x=1&y=9&w=2";

		// ����� �Ʒ� �κи� �Ľ�
		String house = "http://www.uos.ac.kr/kor_2010/html/clife/campus/house/house.jsp?x=1&y=1&w=6";
		String health = "http://www.uos.ac.kr/kor_2010/html/clife/campus/health/health.jsp?x=1&y=2&w=6";
		String stdSubject = "http://www.uos.ac.kr/kor_2010/html/clife/campus/insurance/insurance.jsp?x=1&y=4&w=6";
		String sports = "http://www.uos.ac.kr/kor_2010/html/clife/campus/scomplex/scomplex.jsp?x=1&y=6&w=6";
		// ������ʹ� ���� ������ �Ľ�
		String store = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc.jsp?x=1&y=7&w=60";
		String store2 = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc2.jsp?x=1&y=7&w=6";
		String vending = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc3.jsp?x=1&y=7&w=6";
		String bookstore = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc5.jsp?x=1&y=7&w=6";
		String copy = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc6.jsp?x=1&y=7&w=6";
		String eyestore = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc7.jsp?x=1&y=7&w=6";
		String souvenir = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc8.jsp?x=1&y=7&w=6";
		String post = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc9.jsp?x=1&y=7&w=6";

		ArrayList<String> urlList = new ArrayList<String>();
		urlList.add(publicAffairs);
		urlList.add(engineering);
		urlList.add(humanities);
		urlList.add(naturalSciences);
		urlList.add(urban);
		urlList.add(artAndPhysical);
		urlList.add(openMajor);
		urlList.add(general);

		urlList.add(house);
		urlList.add(health);
		urlList.add(stdSubject);
		urlList.add(sports);

		urlList.add(store);
		urlList.add(store2);
		urlList.add(vending);
		urlList.add(bookstore);
		urlList.add(copy);
		urlList.add(eyestore);
		urlList.add(souvenir);
		urlList.add(post);
		return urlList;
	}

	@Override
	public void onClick(View v) {
		PhoneItem item = (PhoneItem) v.getTag();
		final Context context = getActivity();
		final String phoneNum = parseTelNumber(item.sitePhoneNumber);
		new AlertDialog.Builder(context)
				.setTitle(
						item.siteName
								+ context.getString(R.string.tab_phone_confirm_call))
				.setMessage(phoneNum).setCancelable(true)
				.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri
								.parse("tel:" + phoneNum));
						context.startActivity(callIntent);
						AppUtil.overridePendingTransition((Activity) context, 1);
					}
				}).setNegativeButton(R.string.cancel, null).create().show();
	}

	private String parseTelNumber(String telNumber) {
		if (telNumber.startsWith("������")) {
			return telNumber.split(StringUtil.SPACE)[2];
		} else {
			return telNumber.split("~")[0].split(",")[0];
		}
	}
}
