package com.uoscs09.theuos.tab.subject;

import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsAsyncFragment;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

public class TabSearchSubjectFragment extends
		AbsAsyncFragment<ArrayList<SubjectItem>> implements
		OnItemClickListener, OnItemSelectedListener, View.OnClickListener {
	private SubjectAdapter adapter;
	protected ArrayList<SubjectItem> list;
	protected static final String MAJOR = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi?apiKey="
			+ OApiUtil.UOS_API_KEY;
	protected static final String CULT = "http://wise.uos.ac.kr/uosdoc/api.ApiUcrCultTimeInq.oapi?apiKey="
			+ OApiUtil.UOS_API_KEY;
	private String qry;
	protected AlertDialog ad;
	protected EditText et;
	private Spinner sp1, sp2, sp3, sp4;
	protected ProgressDialog prog;
	private int[] selections = new int[4];
	private TextView[] textViews;
	private int sortFocusViewId;
	private boolean isInverse = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		Context context = getActivity();
		prog = AppUtil.getProgressDialog(context, false, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelExecutor();
			}
		});
		View dialogView = View.inflate(context, R.layout.dialog_search_subject,
				null);
		getAlertDialog(dialogView);

		et = (EditText) dialogView.findViewById(R.id.etc_search_subj_editText1);
		sp1 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner1);
		sp2 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner2);
		sp3 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner3);
		sp4 = (Spinner) dialogView.findViewById(R.id.etc_search_subj_spinner4);

		sp1.setOnItemSelectedListener(this);
		sp2.setOnItemSelectedListener(this);
		sp3.setOnItemSelectedListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView;
		switch (AppUtil.theme) {
		case Black:
			rootView = inflater.inflate(R.layout.tab_search_subj_dark,
					container, false);
			break;
		case BlackAndWhite:
		case White:
		default:
			rootView = inflater.inflate(R.layout.tab_search_subj, container,
					false);
			break;
		}

		int width = getResources().getDisplayMetrics().widthPixels / 12;
		setTextViewSize(width, rootView);

		View empty = rootView.findViewById(R.id.tab_search_subject_empty_view);
		empty.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ad.show();
			}
		});
		if (savedInstanceState != null) {
			list = savedInstanceState.getParcelableArrayList("list");
		} else {
			list = new ArrayList<SubjectItem>();
		}
		ListView listView = (ListView) rootView
				.findViewById(R.id.tab_search_subject_list_view);
		adapter = new SubjectAdapter(
				getActivity(),
				AppUtil.theme == AppTheme.Black ? R.layout.list_layout_subject_dark
						: R.layout.list_layout_subject, list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setEmptyView(empty);
		qry = "&year=" + OApiUtil.getYear() + "&term=" + OApiUtil.getTerm();

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

		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (list.isEmpty()) {
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
		outState.putParcelableArrayList("list", list);
		super.onSaveInstanceState(outState);
	}

	private void setTextViewSize(int px, View v) {
		((TextView) v.findViewById(R.id.tab_search_subject_sub_dept1))
				.setWidth(px * 2);
		((TextView) v.findViewById(R.id.tab_search_subject_sub_div1))
				.setWidth(px * 2);
		((TextView) v.findViewById(R.id.tab_search_subject_no1))
				.setWidth(px * 2);
		((TextView) v.findViewById(R.id.tab_search_subject_class_div1))
				.setWidth(px);
		((TextView) v.findViewById(R.id.tab_search_subject_sub_nm1))
				.setWidth(px * 4);
		((TextView) v.findViewById(R.id.tab_search_subject_yr1)).setWidth(px);
		((TextView) v.findViewById(R.id.tab_search_subject_credit1))
				.setWidth(px);
		((TextView) v.findViewById(R.id.tab_search_subject_prof_nm1))
				.setWidth(px * 2);
		((TextView) v.findViewById(R.id.tab_search_subject_class_nm1))
				.setWidth(px * 5);
		((TextView) v.findViewById(R.id.tab_search_subject_tlsn_cnt1))
				.setWidth(px);
		((TextView) v.findViewById(R.id.tab_search_subject_tlsn_limit1))
				.setWidth(px);
		((LinearLayout) v.findViewById(R.id.tab_search_subject_line_layout))
				.setMinimumWidth(px * 23);
	}

	@Override
	public void onItemClick(AdapterView<?> ad, View v, int pos, long id) {
		showDialFrag(getFragmentManager(),
				(SubjectItem) ad.getItemAtPosition(pos));
		// new SubjectInfoDialFrag().setItem(
		// (SubjectItem) ad.getItemAtPosition(pos)).show(
		// getFragmentManager(), "info");
	}

	private void showDialFrag(FragmentManager fm, SubjectItem item) {
		Bundle b = new Bundle();
		b.putParcelable("item", item);
		DialogFragment f = (DialogFragment) fm.getFragment(b, "info");
		if (f == null)
			f = (DialogFragment) Fragment.instantiate(getActivity(),
					SubjectInfoDialFrag.class.getName(), b);
		f.show(fm, "info");
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
								prog.show();
								ipm.hideSoftInputFromWindow(
										et.getWindowToken(), 0);
								excute();
							}
						}).create();
	}

	@Override
	public void onPostExcute() {
		prog.dismiss();
	}

	@Override
	public void onResult(ArrayList<SubjectItem> result) {
		adapter.clear();
		adapter.addAll(result);
		adapter.notifyDataSetChanged();
		AppUtil.showToast(getActivity(), String.valueOf(result.size())
				+ getString(R.string.search_found), true);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<SubjectItem> call() throws Exception {
		StringBuilder sb = new StringBuilder();
		switch (sp1.getSelectedItemPosition()) {
		case 0:// ����
			sb.append(CULT).append(qry);
			getCultSubjectDiv(sp2.getSelectedItemPosition(), sb);
			break;
		case 1:// ����
			sb.append(MAJOR).append(qry);
			switch (selections[1]) {
			case R.array.search_subj_major_2_0_0:
				getMajorDeptDiv(sp3.getSelectedItemPosition(),
						sp4.getSelectedItemPosition(), sb);
				break;
			default:
				getMajorDeptDiv2(selections[1], sp4.getSelectedItemPosition(),
						sb);
				break;
			}
			break;
		}
		sb.append("&subjectNm=");
		sb.append(URLEncoder.encode(et.getText().toString(),
				StringUtil.ENCODE_EUC_KR));
		String query = sb.toString();
		String body = HttpRequest.getBody(query, StringUtil.ENCODE_EUC_KR);
		return (ArrayList<SubjectItem>) ParseFactory.create(
				ParseFactory.ETC_SUBJECT, body, 0).parse();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		switch (AppUtil.theme) {
		case BlackAndWhite:
		case Black:
			inflater.inflate(R.menu.tab_search_subject_dark, menu);
			break;
		case White:
		default:
			inflater.inflate(R.menu.tab_search_subject, menu);
			break;
		}
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

	private StringBuilder getCultSubjectDiv(int subjectDiv, StringBuilder sb) {
		switch (subjectDiv) {
		case 0:
			sb.append("&subjectDiv=A01");
			break;
		case 1:
			sb.append("&subjectDiv=A02");
			break;
		case 2:
			sb.append("&ubjectDiv=A06");
			break;
		case 3:
			sb.append("&subjectDiv=A07");
			break;
		}
		return sb;
	}

	private StringBuilder getMajorDeptDiv(int deptDiv, int subDept,
			StringBuilder sb) {
		switch (deptDiv) {
		case 0:// �������
			sb.append("&deptDiv=210&dept=A201120212&subDept=");
			switch (subDept) {
			case 0:// ����
				sb.append("A201140214");
				break;
			case 1:// ��������
				sb.append("A201150215");
				break;
			case 2:// ����
				sb.append("A201160216");
				break;
			case 3:// ��ȸ����
				sb.append("A201170217");
				break;
			case 4:// ����
				sb.append("A201180218");
				break;
			case 5:// ����
				sb.append("A202200320");
				break;
			}
			break;
		case 1:// �濵����
			sb.append("&deptDiv=210&dept=A201130213&subDept=A201190219");
			break;
		case 2:// ��������
			sb.append("&deptDiv=220&dept=A200110111&subDept=");
			switch (subDept) {
			case 0:
				sb.append("A200160116");// -����������ǻ�Ͱ��к�
				break;
			case 1:
				sb.append("A200130113");// -ȭ�а��а�
				break;
			case 2:
				sb.append("A200170117");// -����������а�
				break;
			case 3:
				sb.append("A200180118");// -�ż�����а�
				break;
			case 4:
				sb.append("A200190119");// -�����а�
				break;
			case 5:
				sb.append("A200200120");// -��ǻ�Ͱ��к�
				break;
			}
			break;
		case 3:// �ι�����
			sb.append("&deptDiv=210&dept=A200220122&subDept=");
			switch (subDept) {
			case 0:
				sb.append("A200230123");// -������а�
				break;
			case 1:
				sb.append("A200240124");// -������а�
				break;
			case 2:
				sb.append("A200250125");// -�����а�
				break;
			case 3:
				sb.append("A200260126");// -ö�а�
				break;
			case 4:
				sb.append("A201020202");// -�߱��ȭ�а�
				break;
			}
			break;
		case 4:// �ڿ����д���
			sb.append("&deptDiv=210&dept=A200280128&subDept=");
			switch (subDept) {
			case 0:
				sb.append("A200310131");// -���а�
				break;
			case 1:
				sb.append("A200300130");// -����а�
				break;
			case 2:
				sb.append("A200320132");// -�����а�
				break;
			case 3:
				sb.append("A200330133");// -������а�
				break;
			case 4:
				sb.append("A200290129");// -ȯ������а�
				break;
			}
			break;
		case 5:// ���ð��д���
			sb.append("&deptDiv=210&dept=A200370137&subDept=");
			switch (subDept) {
			case 0:
				sb.append("A200380138");// -���������а�
				break;
			case 1:
				sb.append("A200400140");// -���û�ȸ�а�
				break;
			case 2:
				sb.append("A200890189");// -����������
				break;
			case 3:
				sb.append("A200900190");// -�����������
				break;
			case 4:
				sb.append("A200490149");// -���ð��а�
				break;
			case 5:
				sb.append("A200500150");// -������а�
				break;
			case 6:
				sb.append("A200510151");// -�����а�
				break;
			case 7:
				sb.append("A200450145");// -ȯ����к�
				break;
			case 8:
				sb.append("A201000200");// -�����������а�
				break;
			case 9:
				sb.append("A201010201");// -�ҹ�����а�
				break;
			}
			break;
		case 6:// ����ü������
			sb.append("&deptDiv=210&dept=A200590159&subDept=");
			switch (subDept) {
			case 0:
				sb.append("A200810181");// -��������������
				break;
			case 1:
				sb.append("A200820182");// -�ð�����������
				break;
			case 2:
				sb.append("A200610161");// -ȯ�������а�
				break;
			case 3:
				sb.append("A200620162");// -�����а�
				break;
			case 4:
				sb.append("A200540154");// -���������а�
				break;
			}
			break;
		case 7:// ����������
			sb.append("&deptDiv=210&dept=A201100210&subDept=A201110211");
			break;
		}
		return sb;
	}

	private StringBuilder getMajorDeptDiv2(int deptDiv, int subDept,
			StringBuilder sb) {
		switch (deptDiv) {
		case R.array.search_subj_major_2_0_1:// ���п�
			sb.append("&deptDiv=310&dept=A300010101&subDept=");
			switch (subDept) {
			case 0:// ���������а�
				sb.append("A300030103");
				break;
			case 1:// �����а�
				sb.append("A300040104");
				break;
			case 2:// -��ȸ�����а�
				sb.append("A300320132");
				break;
			case 3:// -���û�ȸ�а�
				sb.append("A300360136");
				break;
			case 4:// -���а�
				sb.append("A300050105");
				break;
			case 5:// -���������а�
				sb.append("A300370137");
				break;
			case 6:// -�濵�а�
				sb.append("A300060106");
				break;
			case 7:// -�����а�
				sb.append("A300070107");
				break;
			case 8:// -������а�
				sb.append("A300270127");
				break;
			case 9:// -������а�
				sb.append("A300260126");
				break;
			case 10:// -�����а�
				sb.append("A300280128");
				break;
			case 11:// -ö�а�
				sb.append("A300310131");
				break;
			case 12:// -�����а�
				sb.append("A300200120");
				break;
			case 13:// -ȯ������а�
				sb.append("A300210121");
				break;
			case 14:// -��ǻ�Ͱ��а�
				sb.append("A302160316");
				break;
			case 15:// �����а�
				sb.append("A300240124");
				break;
			case 16:// -������а�
				sb.append("A300250125");
				break;
			case 17:// -�����а�
				sb.append("A300110111");
				break;
			case 18:// --������а�
				sb.append("A300120112");
				break;
			case 19:// --�����а�
				sb.append("A300410141");
				break;
			case 20:// A300130113-ȯ����а�
				sb.append("A300130113");
				break;
			case 21:// -ȭ�а��а�
				sb.append("A300140114");
				break;
			case 22:// -���ð��а�
				sb.append("A300160116");
				break;
			case 23:// -������а�
				sb.append("A300170117");
				break;
			case 24:// - -�ż�����а�
				sb.append("A300390139");
				break;
			case 25:// -����������а�
				sb.append("A300340134");
				break;
			case 26:// -�����������а�
				sb.append("A301860286");
				break;
			case 27:// ȯ�������а�
				sb.append("A300420142");
				break;
			case 28:// -����������ǻ�Ͱ��а�
				sb.append("A302040304");
				break;
			case 29:// -�����а�
				sb.append("A301940294");
				break;
			case 30:// -���������а�
				sb.append("A302310331");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_2:// �����������п�
			sb.append("&deptDiv=310&dept=A300430143&subDept=A302100310");
			break;
		case R.array.search_subj_major_2_0_3:// ������
			sb.append("&deptDiv=310&dept=A300500150&subDept=A300520152");
			break;
		case R.array.search_subj_major_2_0_4:// ����
			sb.append("&deptDiv=310&dept=A302020302&subDept=A302030303");
			break;
		case R.array.search_subj_major_2_0_5:// ����
			sb.append("&deptDiv=310&dept=A300570157&subDept=");
			switch (subDept) {
			case 0:// ���������а�
				sb.append("A300590159");
				break;
			case 1:// ������а�
				sb.append("A300720172");
				break;
			case 2:// -��ȸ�����а�
				sb.append("A300750175");
				break;
			case 3:// -��������а�
				sb.append("A300640164");
				break;
			case 4:// -������а�
				sb.append("A300690169");
				break;
			case 5:// -�����а�
				sb.append("A300700170");
				break;
			case 6:// -ȯ����а�
				sb.append("A300710171");
				break;
			case 7:// -�ε����а�
				sb.append("A301840284");
				break;
			case 8:// -������ȭ�а�
				sb.append("A301900290");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_6:// �濵
			sb.append("&deptDiv=310&dept=A300760176&subDept=A300780178");
			break;
		case R.array.search_subj_major_2_0_7:// ���б��
			sb.append("&deptDiv=301&dept=A302190319&subDept=");
			switch (subDept) {
			case 0:// ȭ�а��а�
				sb.append("A302210321");
				break;
			case 1:// �ż�����а�
				sb.append("A302230323");
				break;
			case 2:// �����а�
				sb.append("A302240324");
				break;
			case 3:// ȯ������а�
				sb.append("A302250325");
				break;
			case 4:// �����а�
				sb.append("A302260326");
				break;
			case 5:// ����������а�
				sb.append("A302220322");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_8:// ����
			sb.append("&deptDiv=321&dept=A300960196&subDept=");
			switch (subDept) {
			case 0:// ���������
				sb.append("A300980198");
				break;
			case 1:// ���������
				sb.append("A300990199");
				break;
			case 2:// ���б�������
				sb.append("A301000200");
				break;
			case 3:// ���米������
				sb.append("A301010201");
				break;
			case 4:// �����н�/�������
				sb.append("A302300330");
				break;
			default:
				return null;
			}
			break;
		case R.array.search_subj_major_2_0_9:// ����

			switch (subDept) {
			case 0:// �۷ι��Ǽ��濵�а�
				sb.append("&deptDiv=310&dept=A303100410&subDept=A303110411");
				break;
			case 1:// ÷�ܳ�����ð����а�
				sb.append("&deptDiv=310&dept=A303100410&subDept=A303120412");
				break;
			case 2:// �������ð������α׷�
				sb.append("&deptDiv=321&dept=A302280328&subDept=A302290329");
				break;
			default:
				return null;
			}
			break;
		default:
			return null;
		}
		return sb;
	}
}
