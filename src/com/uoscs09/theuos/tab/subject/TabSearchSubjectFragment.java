package com.uoscs09.theuos.tab.subject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabSearchSubjectFragment extends
		AbsDrawableProgressFragment<ArrayList<SubjectItem>> implements
		OnItemClickListener, OnItemSelectedListener, View.OnClickListener {
	@ReleaseWhenDestroy
	private SubjectAdapter adapter;
	@AsyncData
	protected ArrayList<SubjectItem> mSubjectList;
	protected Hashtable<String, String> params;
	@ReleaseWhenDestroy
	protected AlertDialog ad;
	@ReleaseWhenDestroy
	protected EditText et;
	@ReleaseWhenDestroy
	private Spinner sp1, sp2, sp3, sp4, termSpinner;
	private int[] selections = new int[4];
	@ReleaseWhenDestroy
	private TextView[] textViews;
	@ReleaseWhenDestroy
	private TextView actionTextview;
	private int sortFocusViewId;
	private boolean isInverse = false;
	protected static int width;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		params = new Hashtable<String, String>();
		setLoadingViewEnable(false);
		Context context = getActivity();
		View dialogView = View.inflate(context, R.layout.dialog_search_subject,
				null);
		getAlertDialog(dialogView);

		et = (EditText) dialogView.findViewById(R.id.etc_search_subj_editText1);
		sp1 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner1);
		sp2 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner2);
		sp3 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner3);
		sp4 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner4);
		termSpinner = (Spinner) dialogView
				.findViewById(R.id.etc_search_subj_spinner_term);

		sp1.setOnItemSelectedListener(this);
		sp2.setOnItemSelectedListener(this);
		sp3.setOnItemSelectedListener(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		Context context = getActivity();

		actionTextview = (TextView) View.inflate(context,
				R.layout.action_textview, null);
		rootView = inflater.inflate(R.layout.tab_search_subj, container, false);
		View empty = rootView.findViewById(R.id.tab_search_subject_empty_view);
		empty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.show();
			}
		});
		if (savedInstanceState != null) {
			mSubjectList = savedInstanceState
					.getParcelableArrayList("mSubjectList");
			actionTextview.setText(savedInstanceState.getString("action"));
		} else {
			mSubjectList = new ArrayList<SubjectItem>();
		}
		ListView listView = (ListView) rootView
				.findViewById(R.id.tab_search_subject_list_view);
		adapter = new SubjectAdapter(context, R.layout.list_layout_subject,
				mSubjectList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setEmptyView(empty);

		int[] ids = { R.id.tab_search_subject_sub_dept1,
				R.id.tab_search_subject_sub_div1, R.id.tab_search_subject_no1,
				R.id.tab_search_subject_class_div1,
				R.id.tab_search_subject_sub_nm1, R.id.tab_search_subject_yr1,
				R.id.tab_search_subject_credit1,
				R.id.tab_search_subject_prof_nm1,
				R.id.tab_search_subject_class_nm1,
				R.id.tab_search_subject_tlsn_cnt1,
				R.id.tab_search_subject_tlsn_limit1 };
		textViews = new TextView[ids.length];
		int i = 0;
		for (int id : ids) {
			textViews[i] = (TextView) rootView.findViewById(id);
			textViews[i++].setOnClickListener(this);
		}
		width = getResources().getDisplayMetrics().widthPixels / 12;
		setTextViewSize(width);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (mSubjectList.isEmpty()) {
			return;
		}
		int field = 0;
		int id = v.getId();
		int bias = 0;
		for (TextView tv : textViews) {
			tv.setCompoundDrawables(null, null, null, null);
		}
		if (id == sortFocusViewId) {
			isInverse = !isInverse;
		} else {
			isInverse = false;
		}
		sortFocusViewId = id;
		switch (id) {
		case R.id.tab_search_subject_sub_dept1:
			field = 0;
			break;
		case R.id.tab_search_subject_sub_div1:
			field = 1;
			break;
		case R.id.tab_search_subject_no1:
			field = 3;
			bias = -1;
			break;
		case R.id.tab_search_subject_class_div1:
			field = 4;
			bias = -1;
			break;
		case R.id.tab_search_subject_sub_nm1:
			field = 5;
			bias = -1;
			break;
		case R.id.tab_search_subject_yr1:
			field = 6;
			bias = -1;
			break;
		case R.id.tab_search_subject_credit1:
			field = 7;
			bias = -1;
			break;
		case R.id.tab_search_subject_prof_nm1:
			field = 8;
			bias = -1;
			break;
		case R.id.tab_search_subject_class_nm1:
			field = 10;
			bias = -2;
			break;
		case R.id.tab_search_subject_tlsn_cnt1:
			field = 11;
			bias = -2;
			break;
		case R.id.tab_search_subject_tlsn_limit1:
			field = 12;
			bias = -2;
			break;
		default:
			return;
		}
		int drawableId;
		switch (AppUtil.theme) {
		case Black:
			drawableId = isInverse ? R.drawable.ic_action_navigation_collapse_dark
					: R.drawable.ic_action_navigation_expand_dark;
			break;
		case BlackAndWhite:
		case White:
		default:
			drawableId = isInverse ? R.drawable.ic_action_navigation_collapse
					: R.drawable.ic_action_navigation_expand;
			break;
		}
		Drawable d = getResources().getDrawable(drawableId);
		textViews[field + bias].setCompoundDrawablesWithIntrinsicBounds(d,
				null, null, null);

		adapter.sort(SubjectItem.getComparator(field, isInverse));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelableArrayList("mSubjectList", mSubjectList);
		outState.putString("action", actionTextview.getText().toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				newConfig.screenWidthDp, getResources().getDisplayMetrics()) / 12;
		setTextViewSize(width);
		adapter.notifyDataSetChanged();
		super.onConfigurationChanged(newConfig);
	}

	private void setTextViewSize(int px) {
		int[] ints = { 2, 2, 2, 1, 4, 1, 1, 2, 5, 1, 1 };
		int i = 0;
		for (TextView tv : textViews) {
			tv.setWidth(px * ints[i++]);
		}
		// ((LinearLayout)
		// getView().findViewById(R.id.tab_search_subject_line_layout))
		// .setMinimumWidth(px * 23);
	}

	@Override
	public void onItemClick(AdapterView<?> ad, View v, int pos, long id) {
		SubjectInfoDialFrag.showDialog(getFragmentManager(),
				(SubjectItem) ad.getItemAtPosition(pos), getActivity(),
				termSpinner.getSelectedItemPosition());
	}

	private void getAlertDialog(View v) {
		final Context context = getActivity();
		ad = new AlertDialog.Builder(context)
				.setView(v)
				.setTitle(R.string.title_tab_search_subject)
				.setMessage(R.string.tab_book_subject_opt)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								InputMethodManager ipm = (InputMethodManager) context
										.getSystemService(Activity.INPUT_METHOD_SERVICE);
								ipm.hideSoftInputFromWindow(
										et.getWindowToken(), 0);
								excute();
							}
						}).create();
	}

	@Override
	public void onTransactResult(ArrayList<SubjectItem> result) {
		adapter.clear();
		adapter.addAll(result);
		adapter.notifyDataSetChanged();
		AppUtil.showToast(getActivity(), String.valueOf(result.size())
				+ getString(R.string.search_found), true);

		actionTextview.setText(termSpinner.getSelectedItem().toString());
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<SubjectItem> call() throws Exception {
		String query;
		params.clear();
		params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
		params.put(OApiUtil.YEAR, OApiUtil.getYear());
		params.put(OApiUtil.TERM,
				OApiUtil.getTermCode(Term.values()[termSpinner
						.getSelectedItemPosition()]));
		switch (sp1.getSelectedItemPosition()) {
		default:
		case 0:// ����
			query = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrCultTimeInq.oapi";
			params.put("subjectDiv",
					getCultSubjectDiv(sp2.getSelectedItemPosition()));
			break;
		case 1:// ����
			query = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi";
			switch (selections[1]) {
			case R.array.search_subj_major_2_0_0:
				params.putAll(getMajorDeptDiv(sp3.getSelectedItemPosition(),
						sp4.getSelectedItemPosition()));
				break;
			default:
				params.putAll(getMajorDeptDiv2(selections[1],
						sp4.getSelectedItemPosition()));
				break;
			}
			break;
		}
		params.put("subjectNm", URLEncoder.encode(et.getText().toString(),
				StringUtil.ENCODE_EUC_KR));
		String body = HttpRequest.getBody(query, StringUtil.ENCODE_EUC_KR,
				params);
		return (ArrayList<SubjectItem>) ParseFactory.create(
				ParseFactory.What.Subject, body, 0).parse();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.tab_search_subject, menu);
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(actionTextview);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_search:
			ad.show();
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		switch (arg0.getId()) {
		case R.id.etc_search_subj_spinner1: {
			int array;
			switch (pos) {
			case 0:
				array = R.array.search_cult_1;
				break;
			case 1:
				array = R.array.search_subj_major_1_0;
				break;
			default:
				return;
			}
			selections[0] = array;
			ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
			sp2.setAdapter(aa);
			break;
		}
		case R.id.etc_search_subj_spinner2: {
			int array, array2 = 0;
			if (sp1.getSelectedItemPosition() == 0) {
				sp4.setVisibility(View.INVISIBLE);
				array = R.array.search_cult_2;
			} else {
				sp4.setVisibility(View.VISIBLE);
				switch (pos) {
				case 0:
					array = R.array.search_subj_major_2_0_0;
					break;
				case 1:
					array = R.array.search_subj_major_2_0_1;
					array2 = R.array.search_subj_major_3_0_1_0;
					break;
				case 2:
					array = R.array.search_subj_major_2_0_2;
					array2 = R.array.search_subj_major_3_0_2_0;
					break;
				case 3:
					array = R.array.search_subj_major_2_0_3;
					array2 = R.array.search_subj_major_3_0_3_0;
					break;
				case 4:
					array = R.array.search_subj_major_2_0_4;
					array2 = R.array.search_subj_major_3_0_4_0;
					break;
				case 5:
					array = R.array.search_subj_major_2_0_5;
					array2 = R.array.search_subj_major_3_0_5_0;
					break;
				case 6:
					array = R.array.search_subj_major_2_0_6;
					array2 = R.array.search_subj_major_3_0_6_0;
					break;
				case 7:
					array = R.array.search_subj_major_2_0_7;
					array2 = R.array.search_subj_major_3_0_7_0;
					break;
				case 8:
					array = R.array.search_subj_major_2_0_8;
					array2 = R.array.search_subj_major_3_0_8_0;
					break;
				default:
					return;
				}
				if (pos > 0) {
					ArrayAdapter<CharSequence> aaa = createArrayAdapter(array2);
					sp4.setAdapter(aaa);
					selections[2] = array2;
				}
			}
			selections[1] = array;
			ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
			sp3.setAdapter(aa);
			break;
		}
		case R.id.etc_search_subj_spinner3: {
			int array;
			if (sp1.getSelectedItemPosition() == 0) {
				return;
			} else {
				switch (sp2.getSelectedItemPosition()) {
				case 0:// ����
					switch (pos) {
					case 0:
						array = R.array.search_subj_major_3_0_0_0;
						break;
					case 1:
						array = R.array.search_subj_major_3_0_0_1;
						break;
					case 2:
						array = R.array.search_subj_major_3_0_0_2;
						break;
					case 3:
						array = R.array.search_subj_major_3_0_0_3;
						break;
					case 4:
						array = R.array.search_subj_major_3_0_0_4;
						break;
					case 5:
						array = R.array.search_subj_major_3_0_0_5;
						break;
					case 6:
						array = R.array.search_subj_major_3_0_0_6;
						break;
					case 7:
						array = R.array.search_subj_major_3_0_0_7;
						break;
					default:
						return;
					}
					break;
				default:
					return;
				}
			}
			selections[3] = array;
			ArrayAdapter<CharSequence> aa = createArrayAdapter(array);
			sp4.setAdapter(aa);
			break;
		}
		default:
			break;
		}
	}

	private ArrayAdapter<CharSequence> createArrayAdapter(int arrayResource) {
		ArrayAdapter<CharSequence> aa = ArrayAdapter
				.createFromResource(
						getActivity(),
						arrayResource,
						AppUtil.theme == AppTheme.Black ? R.layout.spinner_simple_item_dark
								: R.layout.spinner_simple_item);
		aa.setDropDownViewResource(AppUtil.theme == AppTheme.Black ? R.layout.spinner_simple_dropdown_item_dark
				: R.layout.spinner_simple_dropdown_item);
		return aa;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private String getCultSubjectDiv(int subjectDiv) {
		switch (subjectDiv) {
		default:
		case 0:
			return "A01";
		case 1:
			return "A02";
		case 2:
			return "A06";
		case 3:
			return "A07";
		}
	}

	private Hashtable<String, String> getMajorDeptDiv(int deptDiv, int subDept) {
		Hashtable<String, String> table = new Hashtable<String, String>(3);
		switch (deptDiv) {
		case 0:// �������
			table.put("deptDiv", "210");
			table.put("dept", "A201120212");
			switch (subDept) {
			case 0:// ����
				table.put("subDept", "A201140214");
				break;
			case 1:// ��������
				table.put("subDept", "A201150215");
				break;
			case 2:// ����
				table.put("subDept", "A201160216");
				break;
			case 3:// ��ȸ����
				table.put("subDept", "A201170217");
				break;
			case 4:// ����
				table.put("subDept", "A201180218");
				break;
			case 5:// ����
				table.put("subDept", "A202200320");
				break;
			}
			break;
		case 1:// �濵����
			table.put("deptDiv", "210");
			table.put("dept", "A201130213");
			table.put("subDept", "A201190219");
			break;
		case 2:// ��������
			table.put("deptDiv", "220");
			table.put("dept", "A200110111");
			switch (subDept) {
			case 0:
				table.put("subDept", "A200160116");// -����������ǻ�Ͱ��к�
				break;
			case 1:
				table.put("subDept", "A200130113");// -ȭ�а��а�
				break;
			case 2:
				table.put("subDept", "A200170117");// -����������а�
				break;
			case 3:
				table.put("subDept", "A200180118");// -�ż�����а�
				break;
			case 4:
				table.put("subDept", "A200190119");// -�����а�
				break;
			case 5:
				table.put("subDept", "A200200120");// -��ǻ�Ͱ��к�
				break;
			}
			break;
		case 3:// �ι�����
			table.put("deptDiv", "210");
			table.put("dept", "A200220122");
			switch (subDept) {
			case 0:
				table.put("subDept", "A200230123");// -������а�
				break;
			case 1:
				table.put("subDept", "A200240124");// -������а�
				break;
			case 2:
				table.put("subDept", "A200250125");// -�����а�
				break;
			case 3:
				table.put("subDept", "A200260126");// -ö�а�
				break;
			case 4:
				table.put("subDept", "A201020202");// -�߱��ȭ�а�
				break;
			}
			break;
		case 4:// �ڿ����д���
			table.put("deptDiv", "210");
			table.put("dept", "A200280128");
			switch (subDept) {
			case 0:
				table.put("subDept", "A200310131");// -���а�
				break;
			case 1:
				table.put("subDept", "A200300130");// -����а�
				break;
			case 2:
				table.put("subDept", "A200320132");// -�����а�
				break;
			case 3:
				table.put("subDept", "A200330133");// -������а�
				break;
			case 4:
				table.put("subDept", "A200290129");// -ȯ������а�
				break;
			}
			break;
		case 5:// ���ð��д���
			table.put("deptDiv", "210");
			table.put("dept", "A200370137");
			switch (subDept) {
			case 0:
				table.put("subDept", "A200380138");// -���������а�
				break;
			case 1:
				table.put("subDept", "A200400140");// -���û�ȸ�а�
				break;
			case 2:
				table.put("subDept", "A200890189");// -����������
				break;
			case 3:
				table.put("subDept", "A200900190");// -�����������
				break;
			case 4:
				table.put("subDept", "A200490149");// -���ð��а�
				break;
			case 5:
				table.put("subDept", "A200500150");// -������а�
				break;
			case 6:
				table.put("subDept", "A200510151");// -�����а�
				break;
			case 7:
				table.put("subDept", "A200450145");// -ȯ����к�
				break;
			case 8:
				table.put("subDept", "A201000200");// -�����������а�
				break;
			case 9:
				table.put("subDept", "A201010201");// -�ҹ�����а�
				break;
			}
			break;
		case 6:// ����ü������
			table.put("deptDiv", "210");
			table.put("dept", "A200590159");
			switch (subDept) {
			case 0:
				table.put("subDept", "A200810181");// -��������������
				break;
			case 1:
				table.put("subDept", "A200820182");// -�ð�����������
				break;
			case 2:
				table.put("subDept", "A200610161");// -ȯ�������а�
				break;
			case 3:
				table.put("subDept", "A200620162");// -�����а�
				break;
			case 4:
				table.put("subDept", "A200540154");// -���������а�
				break;
			}
			break;
		case 7:// ����������
			table.put("deptDiv", "210");
			table.put("dept", "A201100210");
			table.put("subDept", "A201110211");
			break;
		}
		return table;
	}

	private Hashtable<String, String> getMajorDeptDiv2(int deptDiv, int subDept) {
		Hashtable<String, String> table = new Hashtable<String, String>(3);
		switch (deptDiv) {
		case R.array.search_subj_major_2_0_1:// ���п�
			table.put("deptDiv", "310");
			table.put("dept", "A300010101");
			switch (subDept) {
			case 0:// ���������а�
				table.put("subDept", "A300030103");
				break;
			case 1:// �����а�
				table.put("subDept", "A300040104");
				break;
			case 2:// -��ȸ�����а�
				table.put("subDept", "A300320132");
				break;
			case 3:// -���û�ȸ�а�
				table.put("subDept", "A300360136");
				break;
			case 4:// -���а�
				table.put("subDept", "A300050105");
				break;
			case 5:// -���������а�
				table.put("subDept", "A300370137");
				break;
			case 6:// -�濵�а�
				table.put("subDept", "A300060106");
				break;
			case 7:// -�����а�
				table.put("subDept", "A300070107");
				break;
			case 8:// -������а�
				table.put("subDept", "A300270127");
				break;
			case 9:// -������а�
				table.put("subDept", "A300260126");
				break;
			case 10:// -�����а�
				table.put("subDept", "A300280128");
				break;
			case 11:// -ö�а�
				table.put("subDept", "A300310131");
				break;
			case 12:// -�����а�
				table.put("subDept", "A300200120");
				break;
			case 13:// -ȯ������а�
				table.put("subDept", "A300210121");
				break;
			case 14:// -��ǻ�Ͱ��а�
				table.put("subDept", "A302160316");
				break;
			case 15:// �����а�
				table.put("subDept", "A300240124");
				break;
			case 16:// -������а�
				table.put("subDept", "A300250125");
				break;
			case 17:// -�����а�
				table.put("subDept", "A300110111");
				break;
			case 18:// --������а�
				table.put("subDept", "A300120112");
				break;
			case 19:// --�����а�
				table.put("subDept", "A300410141");
				break;
			case 20:// A300130113-ȯ����а�
				table.put("subDept", "A300130113");
				break;
			case 21:// -ȭ�а��а�
				table.put("subDept", "A300140114");
				break;
			case 22:// -���ð��а�
				table.put("subDept", "A300160116");
				break;
			case 23:// -������а�
				table.put("subDept", "A300170117");
				break;
			case 24:// - -�ż�����а�
				table.put("subDept", "A300390139");
				break;
			case 25:// -����������а�
				table.put("subDept", "A300340134");
				break;
			case 26:// -�����������а�
				table.put("subDept", "A301860286");
				break;
			case 27:// ȯ�������а�
				table.put("subDept", "A300420142");
				break;
			case 28:// -����������ǻ�Ͱ��а�
				table.put("subDept", "A302040304");
				break;
			case 29:// -�����а�
				table.put("subDept", "A301940294");
				break;
			case 30:// -���������а�
				table.put("subDept", "A302310331");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_2:// �����������п�
			table.put("deptDiv", "310");
			table.put("dept", "A300430143");
			table.put("subDept", "A302100310");
			break;
		case R.array.search_subj_major_2_0_3:// ������
			table.put("deptDiv", "310");
			table.put("dept", "A300500150");
			table.put("subDept", "A300520152");
			break;
		case R.array.search_subj_major_2_0_4:// ����
			table.put("deptDiv", "310");
			table.put("dept", "A302020302");
			table.put("subDept", "A302030303");
			break;
		case R.array.search_subj_major_2_0_5:// ����
			table.put("deptDiv", "310");
			table.put("dept", "A300570157");
			switch (subDept) {
			case 0:// ���������а�
				table.put("subDept", "A300590159");
				break;
			case 1:// ������а�
				table.put("subDept", "A300720172");
				break;
			case 2:// -��ȸ�����а�
				table.put("subDept", "A300750175");
				break;
			case 3:// -��������а�
				table.put("subDept", "A300640164");
				break;
			case 4:// -������а�
				table.put("subDept", "A300690169");
				break;
			case 5:// -�����а�
				table.put("subDept", "A300700170");
				break;
			case 6:// -ȯ����а�
				table.put("subDept", "A300710171");
				break;
			case 7:// -�ε����а�
				table.put("subDept", "A301840284");
				break;
			case 8:// -������ȭ�а�
				table.put("subDept", "A301900290");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_6:// �濵
			table.put("deptDiv", "310");
			table.put("dept", "A300760176");
			table.put("subDept", "A300780178");
			break;
		case R.array.search_subj_major_2_0_7:// ���б��
			table.put("deptDiv", "301");
			table.put("dept", "A302190319");
			switch (subDept) {
			case 0:// ȭ�а��а�
				table.put("subDept", "A302210321");
				break;
			case 1:// �ż�����а�
				table.put("subDept", "A302230323");
				break;
			case 2:// �����а�
				table.put("subDept", "A302240324");
				break;
			case 3:// ȯ������а�
				table.put("subDept", "A302250325");
				break;
			case 4:// �����а�
				table.put("subDept", "A302260326");
				break;
			case 5:// ����������а�
				table.put("subDept", "A302220322");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_8:// ����
			table.put("deptDiv", "321");
			table.put("dept", "A300960196");
			switch (subDept) {
			case 0:// ���������
				table.put("subDept", "A300980198");
				break;
			case 1:// ���������
				table.put("subDept", "A300990199");
				break;
			case 2:// ���б�������
				table.put("subDept", "A301000200");
				break;
			case 3:// ���米������
				table.put("subDept", "A301010201");
				break;
			case 4:// �����н�/�������
				table.put("subDept", "A302300330");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_9:// ����
			switch (subDept) {
			case 0:// �۷ι��Ǽ��濵�а�
				table.put("deptDiv", "310");
				table.put("dept", "A303100410");
				table.put("subDept", "A303110411");
				break;
			case 1:// ÷�ܳ�����ð����а�
				table.put("deptDiv", "310");
				table.put("dept", "A303100410");
				table.put("subDept", "A303120412");
				break;
			case 2:// �������ð������α׷�
				table.put("deptDiv", "321");
				table.put("dept", "A302280328");
				table.put("subDept", "A302290329");
				break;
			default:
				return null;
			}
			break;
		default:
			return null;
		}
		return table;
	}

	@Override
	protected MenuItem getLoadingMenuItem(Menu menu) {
		return menu.findItem(R.id.action_search);
	}
}
