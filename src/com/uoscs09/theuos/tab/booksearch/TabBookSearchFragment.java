package com.uoscs09.theuos.tab.booksearch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabBookSearchFragment extends
		AbsDrawableProgressFragment<ArrayList<BookItem>> implements
		OnQueryTextListener, AbsListView.OnScrollListener, View.OnClickListener {
	/** 리스트 뷰가 스크롤 되는지 여부 */
	private boolean isInvokeScroll = true;
	/** 비동기 작업 결과가 비었는지 여부 */
	private boolean isResultEmpty = true;
	/** 현재 mCurrentPage */
	protected int mCurrentPage = 1;
	/** 중앙 도서관에 질의할 매개변수들 */
	private String mEncodedQuery;
	private String mRawQuery;
	// private String mOptionString;
	@ReleaseWhenDestroy
	protected ArrayAdapter<BookItem> mBookListAdapter;
	@ReleaseWhenDestroy
	protected AnimationAdapter mAnimAdapter;
	@AsyncData
	private ArrayList<BookItem> mBookList;
	@ReleaseWhenDestroy
	private ListView mListView;
	/** ListView의 mEmptyView */
	@ReleaseWhenDestroy
	private View mEmptyView;
	/** option : catergory */
	@ReleaseWhenDestroy
	protected Spinner oi;
	/** option : sort */
	@ReleaseWhenDestroy
	protected Spinner os;
	/** 옵션을 선택하게 하는 Dialog */
	@ReleaseWhenDestroy
	protected AlertDialog optionDialog;
	/** 검색 메뉴, 검색할 단어가 입력되는 곳 */
	@ReleaseWhenDestroy
	protected MenuItem searchMenu;
	@ReleaseWhenDestroy
	protected ActionMode actionMode;

	private static final String BUNDLE_LIST = "BookList";
	private static final String BUNDLE_PAGE = "BookPage";
	private static final String QUERY = "Query";
	private static final String OI_SEL = "oi";
	private static final String OS_SEL = "os";
	private static final String URL = "http://mlibrary.uos.ac.kr/search/tot/result?sm=&st=KWRD&websysdiv=tot&si=TOTAL&pn=";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		int oiSelect = 0, osSelect = 0;
		setMenuRefresh(false);
		setDrawableForMenu(false);
		if (savedInstanceState != null) {
			mBookList = savedInstanceState.getParcelableArrayList(BUNDLE_LIST);
			oiSelect = savedInstanceState.getInt(OI_SEL);
			osSelect = savedInstanceState.getInt(OS_SEL);
			mEncodedQuery = savedInstanceState.getString(QUERY);
			mCurrentPage = savedInstanceState.getInt(BUNDLE_PAGE);
			mRawQuery = savedInstanceState.getString("mRawQuery",
					StringUtil.NULL);
		} else {
			mBookList = new ArrayList<BookItem>();
			mCurrentPage = 1;
		}
		Context context = getActivity();

		View dialogLayout = View.inflate(context,
				R.layout.dialog_tab_book_spinners, null);
		oi = (Spinner) dialogLayout
				.findViewById(R.id.tab_book_action_spinner_oi);
		os = (Spinner) dialogLayout
				.findViewById(R.id.tab_book_action_spinner_os);
		oi.setSelection(oiSelect);
		os.setSelection(osSelect);
		optionDialog = new AlertDialog.Builder(context)
				.setView(dialogLayout)
				.setTitle(R.string.tab_book_book_opt)
				.setMessage(R.string.tab_book_book_opt_sub)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setActionText();
								if (!mBookList.isEmpty()) {
									mBookList.clear();
									mCurrentPage = 1;
									excute();
								}
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								oi.setSelection(0);
								os.setSelection(0);
								setActionText();
							}
						}).setIconAttribute(R.attr.ic_action_help).create();
		AppUtil.setAlertDialogMaterial(optionDialog, getActivity());
		super.onCreate(savedInstanceState);
	}

	protected void setActionText() {
		// String text1 = oi.getSelectedItemPosition() == 0 ? StringUtil.NULL
		// : "분류 : " + oi.getSelectedItem();
		// String text2 = os.getSelectedItemPosition() == 0 ? StringUtil.NULL
		// : "정렬 : " + os.getSelectedItem();
		// mOptionString = !text1.equals(StringUtil.NULL) ? text1
		// + StringUtil.NEW_LINE + text2 : text2;
		// optionTextView.setText(text);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList(BUNDLE_LIST, mBookList);
		outState.putInt(OI_SEL, oi.getSelectedItemPosition());
		outState.putInt(OS_SEL, os.getSelectedItemPosition());
		outState.putInt(BUNDLE_PAGE, mCurrentPage);
		outState.putString(QUERY, mEncodedQuery);
		outState.putString("mRawQuery", mRawQuery);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.tab_book_search, container,
				false);

		mEmptyView = rootView.findViewById(R.id.tab_book_empty);
		mEmptyView.findViewById(R.id.tab_book_search_empty_info1)
				.setOnClickListener(this);
		mEmptyView.findViewById(R.id.tab_book_search_empty_info2)
				.setOnClickListener(this);
		if (mBookList.size() != 0) {
			mEmptyView.setVisibility(View.INVISIBLE);
			isResultEmpty = false;
		} else {
			mEmptyView.setVisibility(View.VISIBLE);
		}

		mBookListAdapter = new BookItemListAdapter(getActivity(),
				R.layout.list_layout_book, mBookList, mLongClickListener);
		mAnimAdapter = new AlphaInAnimationAdapter(mBookListAdapter);
		// 리스트 뷰
		mListView = (ListView) rootView.findViewById(R.id.tab_book_list_search);
		mListView.addFooterView(getLoadingView());
		// 스크롤 리스너 등록
		mListView.setOnScrollListener(this);
		mAnimAdapter.setAbsListView(mListView);
		mListView.setAdapter(mAnimAdapter);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tab_book_search_empty_info1:
			searchMenu.expandActionView();
			break;
		case R.id.tab_book_search_empty_info2:
			optionDialog.show();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_book_search, menu);
		searchMenu = menu.findItem(R.id.action_search);

		final SearchView searchView = (SearchView) MenuItemCompat
				.getActionView(searchMenu);
		if (searchView != null) {
			searchView.setOnQueryTextListener(this);
			searchView.setSubmitButtonEnabled(true);
			searchView.setQueryHint(getText(R.string.search_hint));
			searchView
					.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
						@Override
						public void onFocusChange(View view,
								boolean queryTextFocused) {
							if (!queryTextFocused) {
								searchMenu.collapseActionView();
								searchView.setQuery("", false);
							}
						}
					});
		} else {
			AppUtil.showToast(getActivity(), "compactibility error");
		}
		setActionText();
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	protected void excute() {
		mEmptyView.setVisibility(View.GONE);
		super.excute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_help:
			optionDialog.show();
			return true;
		case R.id.action_search:
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean onQueryTextSubmit(String q) {
		InputMethodManager ipm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		mRawQuery = q.trim();
		if (mRawQuery.equals(StringUtil.NULL)) {
			AppUtil.showToast(getActivity(), R.string.search_input_empty,
					isMenuVisible());
		} else {
			ipm.hideSoftInputFromWindow(searchMenu.getActionView()
					.getWindowToken(), 0);
			searchMenu.collapseActionView();
			String lastQuery;
			try {
				lastQuery = URLEncoder.encode(mRawQuery,
						StringUtil.ENCODE_UTF_8);
			} catch (UnsupportedEncodingException e) {
				AppUtil.showToast(getActivity(), "Text Encoding Error!",
						isMenuVisible());
				return true;
			}
			mEncodedQuery = lastQuery;
			mCurrentPage = 1;
			mBookList.clear();
			setSubtitleWhenVisible(mRawQuery);
			mAnimAdapter.reset();
			excute();
		}
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return true;
	}

	protected String getSpinnerItemString(int which, int pos) {
		switch (which) {
		case 0:
			switch (pos) {
			case 1:
				return "DISP01";
			case 2:
				return "DISP02";
			case 3:
				return "DISP03";
			case 4:
				return "DISP04";
			case 5:
				return "DISP06";
			}
		case 1:
			switch (pos) {
			case 1:
				return "ASC";
			case 2:
				return "DESC";
			}
		default:
			return StringUtil.NULL;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<BookItem> call() throws Exception {
		String OS = getSpinnerItemString(1, os.getSelectedItemPosition());
		String OI = getSpinnerItemString(0, oi.getSelectedItemPosition());
		boolean check = true;
		StringBuilder sb = new StringBuilder();
		sb.append(URL).append(mCurrentPage).append("&q=").append(mEncodedQuery);
		String lastQuery = null;
		String RM = "&websysdiv=tot";
		if (!OI.equals(StringUtil.NULL)) {
			sb.append("&oi=").append(OI);
			lastQuery = StringUtil.remove(sb.toString(), RM);
			check = false;
		}
		if (!OS.equals(StringUtil.NULL)) {
			sb.append("&os=").append(OS);
			lastQuery = sb.toString();
			if (check) {
				lastQuery = StringUtil.remove(lastQuery, RM);
				check = false;
			}
		}
		if (check) {
			lastQuery = sb.toString();
		}
		String body = HttpRequest.getBody(lastQuery);

		ArrayList<BookItem> bookList = (ArrayList<BookItem>) ParseFactory
				.create(ParseFactory.What.Book, body, ParseFactory.Value.BASIC)
				.parse();

		// 대여 가능 도서만 가져옴
		if (PrefUtil.getInstance(getActivity()).get(PrefUtil.KEY_CHECK_BORROW,
				false)
				&& bookList.size() > 0) {
			bookList = getFilteredList(bookList);
		}
		return bookList;
	}

	@Override
	public void onTransactResult(ArrayList<BookItem> result) {
		Context context = getActivity();
		if (context != null) {
			if (result.size() == 0) {
				AppUtil.showToast(context, R.string.search_result_empty,
						isMenuVisible());
				isResultEmpty = true;
			} else {
				AppUtil.showToast(context, String.valueOf(result.size())
						+ context.getString(R.string.search_found),
						isMenuVisible());
				mBookList.addAll(result);
				mBookListAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onTransactPostExcute() {
		super.onTransactPostExcute();
		isResultEmpty = false;
	}

	protected ArrayList<BookItem> getFilteredList(
			ArrayList<BookItem> originalList) {
		ArrayList<BookItem> newList = new ArrayList<BookItem>();
		BookItem item;
		final String cando = "가능";
		int size = originalList.size();
		for (int i = 0; i < size; i++) {
			item = originalList.get(i);
			if (item.bookState.contains(cando)) {
				newList.add(item);
			}
		}
		if (newList.size() == 0) {
			item = new BookItem();
			item.bookInfo = getString(R.string.tab_book_not_found);
			newList.add(item);
		}
		return newList;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 리스트의 마지막에 도달하였을 경우에
		// 이 이벤트가 처음 일어났고, 이전에 검색결과가 0이 아닌 경우에만
		// 새로운 검색을 시도한다.
		if (totalItemCount > 1
				&& (firstVisibleItem + visibleItemCount) == totalItemCount) {
			if (!isInvokeScroll && !isResultEmpty) {
				isInvokeScroll = true;
				mCurrentPage++;
				excute();
			}
		} else {
			isInvokeScroll = false;
		}
	}

	@ReleaseWhenDestroy
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.tab_book_contextual, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			String str = mode.getTitle().toString();
			switch (item.getItemId()) {
			case R.id.action_copy:
				copyItem(str);
				mode.finish();
				return true;
			case R.id.action_search:
				searchItem(str);
				mode.finish();
				return true;
			default:
				return false;
			}
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// View v = (View) mode.getTag();
			// if (v != null)
			// v.setSelected(false);
			actionMode = null;
		}
	};

	@Override
	public void onDestroyOptionsMenu() {
		if (actionMode != null)
			actionMode.finish();
	}

	protected void copyItem(String text) {
		ClipboardManager clipboard = (ClipboardManager) getActivity()
				.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("copy", text);
		clipboard.setPrimaryClip(clip);
	}

	protected void searchItem(String text) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_WEB_SEARCH);
		intent.putExtra(SearchManager.QUERY, text);
		startActivity(intent);
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_search);
	}

	/** listView 내부의 TextView에서 호출 됨 */
	private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			if (actionMode == null)
				actionMode = getActivity().startActionMode(mActionModeCallback);

			// View prevView = (View) actionMode.getTag();
			// if (prevView != null)
			// prevView.setSelected(false);
			// v.setSelected(true);
			actionMode.setTag(v);
			actionMode.setTitle(((TextView) v).getText());
			return true;
		}
	};

	@Override
	protected boolean putAsyncData(String key, ArrayList<BookItem> obj) {
		mBookList.addAll(obj);
		return super.putAsyncData(key, mBookList);
	}

	@Override
	protected CharSequence getSubtitle() {
		return mRawQuery;
	}
}
