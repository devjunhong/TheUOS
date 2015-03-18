package com.uoscs09.theuos.tab.emptyroom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsDrawableProgressFragment;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;
import com.uoscs09.theuos.common.util.OApiUtil.Term;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseEmptyRoom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

/**
 * 빈 강의실을 조회하는 fragment
 */
public class TabSearchEmptyRoomFragment extends
        AbsDrawableProgressFragment<ArrayList<ClassRoomItem>> implements
        DialogInterface.OnClickListener, View.OnClickListener {
    @ReleaseWhenDestroy
    private ArrayAdapter<ClassRoomItem> adapter;
    @AsyncData
    private ArrayList<ClassRoomItem> mClassRoomList;
    @ReleaseWhenDestroy
    protected AlertDialog dialog;
    private Hashtable<String, String> params = new Hashtable<>();
    @ReleaseWhenDestroy
    private Spinner buildingSpinner;
    @ReleaseWhenDestroy
    private Spinner timeSpinner;
    @ReleaseWhenDestroy
    private Spinner termSpinner;
    @ReleaseWhenDestroy
    private TextView[] textViews;
    private String mTermString;
    private int sortFocus;
    private boolean isReverse = false;
    private static final String BUILDING = "building";
    private static final String URL = "http://wise.uos.ac.kr/uosdoc/api.ApiUcsFromToEmptyRoom.oapi";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initTable();
        Context context = getActivity();
        View dialogLayout = View.inflate(context,
                R.layout.dialog_search_empty_room, null);
        buildingSpinner = (Spinner) dialogLayout
                .findViewById(R.id.etc_empty_spinner_building);
        timeSpinner = (Spinner) dialogLayout
                .findViewById(R.id.etc_empty_spinner_time);
        termSpinner = (Spinner) dialogLayout
                .findViewById(R.id.etc_empty_spinner_term);

        initSearchDialog(dialogLayout);
        if (savedInstanceState != null) {
            mClassRoomList = savedInstanceState
                    .getParcelableArrayList(BUILDING);
            mTermString = savedInstanceState.getString("time");
        } else {
            mClassRoomList = new ArrayList<>();
        }
        super.onCreate(savedInstanceState);
    }

    private void initSearchDialog(View innerView) {
        dialog = new MaterialDialog.Builder(getActivity())
                .customView(innerView, true)
                .positiveText(R.string.confirm)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        excute();
                    }
                })
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.tab_search_empty_room, container,
                false);
        adapter = new SearchEmptyRoomAdapter(getActivity(),
                R.layout.list_layout_empty_room, mClassRoomList);
        textViews = new TextView[4];
        int[] ids = {R.id.tab_search_empty_room_text_building_name,
                R.id.tab_search_empty_room_text_room_no,
                R.id.tab_search_empty_room_text_room_subj,
                R.id.tab_search_empty_room_text_room_person};
        int i = 0;
        for (int id : ids) {
            textViews[i] = (TextView) root.findViewById(id);
            textViews[i++].setOnClickListener(this);
        }

        ListView listView = (ListView) root.findViewById(R.id.etc_search_list);

        listView.setAdapter(adapter);
        View empty = root.findViewById(R.id.tab_search_subject_empty_view);
        empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
        listView.setEmptyView(empty);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BUILDING, mClassRoomList);
        outState.putString("time", mTermString);
        super.onSaveInstanceState(outState);

    }

    private void putParams() {
        Calendar c = Calendar.getInstance();
        int time = timeSpinner.getSelectedItemPosition() + 1;
        String wdayTime = String.valueOf(c.get(Calendar.DAY_OF_WEEK))
                + (time < 10 ? "0" : StringUtil.NULL) + String.valueOf(time);
        String building = ((String) buildingSpinner.getSelectedItem())
                .split(StringUtil.SPACE)[0];

        params.put(BUILDING, building);
        params.put("wdayTime", wdayTime);
        params.put(OApiUtil.TERM,
                OApiUtil.getTermCode(Term.values()[termSpinner
                        .getSelectedItemPosition()]));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_search_empty_room, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                dialog.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onTransactResult(ArrayList<ClassRoomItem> result) {
        mClassRoomList.clear();
        mClassRoomList.addAll(result);
        adapter.notifyDataSetChanged();
        AppUtil.showToast(getActivity(), String.valueOf(result.size())
                + getString(R.string.search_found), true);

        mTermString = timeSpinner.getSelectedItem().toString()
                .split(StringUtil.NEW_LINE)[1]
                + StringUtil.NEW_LINE + termSpinner.getSelectedItem();
        setSubtitleWhenVisible(mTermString);
    }

    private void initTable() {
        String date = new SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
                .format(new Date());
        params.put(OApiUtil.API_KEY, OApiUtil.UOS_API_KEY);
        params.put(OApiUtil.YEAR, OApiUtil.getYear());
        params.put(BUILDING, StringUtil.NULL);
        params.put("dateFrom", date);
        params.put("dateTo", date);
        params.put("classRoomDiv", StringUtil.NULL);
        params.put("wdayTime", StringUtil.NULL);
        params.put("aplyPosbYn", "Y");
    }

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<ClassRoomItem> call() throws Exception {
        putParams();

        if (params.get(BUILDING).equals("00")) {
            final String[] buildings = {"01", "02", "03", "04", "05", "06",
                    "08", "09", "10", "11", "13", "14", "15", "16", "17", "18",
                    "19", "20", "23", "24", "25", "33"};
            ArrayList<ClassRoomItem> list = new ArrayList<>();
            String body;
            for (String bd : buildings) {
                params.put(BUILDING, bd);
                body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR, params, StringUtil.ENCODE_EUC_KR);

                list.addAll(new ParseEmptyRoom(body).parse());
            }
            return list;
        } else {
            String body = HttpRequest.getBody(URL, StringUtil.ENCODE_EUC_KR, params, StringUtil.ENCODE_EUC_KR);
            return new ParseEmptyRoom(body).parse();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        cancelExecutor();
    }

    @Override
    public void onClick(View v) {
        if (mClassRoomList.isEmpty()) {
            return;
        }
        int field;
        int id = v.getId();
        for (TextView tv : textViews) {
            tv.setCompoundDrawables(null, null, null, null);
        }
        isReverse = id == sortFocus && !isReverse;
        sortFocus = id;
        switch (id) {
            case R.id.tab_search_empty_room_text_building_name:
                field = 0;
                break;
            case R.id.tab_search_empty_room_text_room_no:
                field = 1;
                break;
            case R.id.tab_search_empty_room_text_room_subj:
                field = 2;
                break;
            case R.id.tab_search_empty_room_text_room_person:
                field = 3;
                break;
            default:
                return;
        }
        Drawable d = getResources().getDrawable(AppUtil.getStyledValue(getActivity(), isReverse ? R.attr.ic_navigation_collapse : R.attr.ic_navigation_expand));
        textViews[field].setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

        adapter.sort(ClassRoomItem.getComparator(field, isReverse));
    }

    @Override
    protected MenuItem getLoadingMenuItem(Menu menu) {
        return menu.findItem(R.id.action_search);
    }

    @Override
    protected CharSequence getSubtitle() {
        return mTermString;
    }

}