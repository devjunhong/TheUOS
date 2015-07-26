package com.uoscs09.theuos2.tab.phonelist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.uoscs09.theuos2.R;
import com.uoscs09.theuos2.annotation.AsyncData;
import com.uoscs09.theuos2.annotation.ReleaseWhenDestroy;
import com.uoscs09.theuos2.async.AsyncFragmentJob;
import com.uoscs09.theuos2.base.AbsAsyncFragment;
import com.uoscs09.theuos2.http.HttpRequest;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.StringUtil;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TabPhoneFragment extends AbsAsyncFragment<ArrayList<PhoneItem>> {
    @ReleaseWhenDestroy
    private ArrayAdapter<PhoneItem> phoneAdapter;
    @ReleaseWhenDestroy
    private MaterialDialog mProgressDialog;
    @ReleaseWhenDestroy
    private AlertDialog dialog;
    private static final String PHONE_LIST = "phone_list";
    @AsyncData
    private List<PhoneItem> mPhoneList;

    private final ParsePhone mParser = new ParsePhone();

    private boolean mIsInit;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(PHONE_LIST, (ArrayList<? extends Parcelable>) mPhoneList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mPhoneList = savedInstanceState.getParcelableArrayList(PHONE_LIST);
        } else {
            mPhoneList = new ArrayList<>();
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_phone, container, false);

        initProgress();
        initDialog();

        // 리스트 어댑터 생성
        phoneAdapter = new PhoneListAdapter(getActivity(), mPhoneList, mOnClickListener);
        // 리스트 뷰
        ListView phoneListView = (ListView) rootView.findViewById(R.id.tab_phone_list_phone);

        // 어댑터 설정
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
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tab_phone, menu);
        final MenuItem searchMenu = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

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
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view,
                                          boolean queryTextFocused) {
                    if (!queryTextFocused) {
                        searchMenu.collapseActionView();
                        searchView.setQuery("", false);
                    }
                }
            });
            searchView.setSubmitButtonEnabled(true);
            searchView.setQueryHint(getText(R.string.tab_phone_search_hint));
        } else {
            AppUtil.showToast(getActivity(), "compatibility error");
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPhoneList.isEmpty()) {
            mIsInit = true;
            execute();
        }
    }

    private AsyncTask<Void, Void, ArrayList<PhoneItem>> mAsyncTask;

    private void execute() {
        mAsyncTask = super.execute(new Job());
    }

    private class Job extends AsyncFragmentJob.Base<ArrayList<PhoneItem>> {
        @Override
        public ArrayList<PhoneItem> call() throws Exception {

            ArrayList<PhoneItem> phoneNumberList = new ArrayList<>();
            if (mIsInit) {
                PhoneNumberDB telDB = PhoneNumberDB.getInstance(getActivity());
                phoneNumberList = (ArrayList<PhoneItem>) telDB.readAll(StringUtil.NULL);
            } else {
                ArrayList<String> urlList = getUrlList();

                final int progressNumber = 100 / urlList.size();
                int howTo;
                int size = urlList.size();
                for (int i = 0; i < size; i++) {
                    if (isJobCancelled()) {
                        return null;
                    }
                    try {
                        if (i < 7) {
                            howTo = ParsePhone.SUBJECT;
                        } else if (i < 8) {
                            howTo = ParsePhone.CULTURE;
                        } else if (i < 12) {
                            howTo = ParsePhone.BOTTOM;
                        } else {
                            howTo = ParsePhone.BODY;
                        }

                        mParser.setHowTo(howTo);
                        phoneNumberList.addAll(
                                HttpRequest.Builder.newStringRequestBuilder(urlList.get(i))
                                        .build()
                                        .wrap(mParser)
                                        .get()
                        );

                    } catch (UnknownHostException e) {
                        throw e;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mProgressDialog.setProgress(progressNumber * i - 1);
                }
                // DB 에 데이터 등록
                PhoneNumberDB telDB = PhoneNumberDB.getInstance(getActivity());
                for (PhoneItem item : phoneNumberList) {
                    telDB.insertOrUpdate(item);
                }
                phoneNumberList = (ArrayList<PhoneItem>) telDB.readAll(StringUtil.NULL);
                mProgressDialog.setProgress(100);
            }
            return phoneNumberList;
        }

        @Override
        public void onResult(ArrayList<PhoneItem> result) {
            phoneAdapter.clear();
            phoneAdapter.addAll(result);
            phoneAdapter.notifyDataSetChanged();
            if (mIsInit) {
                mIsInit = false;
            } else {
                AppUtil.showToast(getActivity(), R.string.finish_update, isMenuVisible());
            }
        }
    }

    private boolean isJobCancelled() {
        return mAsyncTask == null || mAsyncTask.isCancelled();
    }

    private void cancelJob() {
        if (mAsyncTask != null)
            mAsyncTask.cancel(true);
    }


    private void initProgress() {
        if (mProgressDialog == null)
            mProgressDialog = AppUtil.getProgressDialog(getActivity(), true, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelJob();
                }
            });
    }

    private void initDialog() {
        dialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.tab_phone_caution_update)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        execute();
                    }
                })
                .create();
    }

    @Override
    protected void onPostExecute() {
        // super.onTransactPostExecute();
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    private ArrayList<String> getUrlList() {
        // 여기는 과 사무실 파싱
        String publicAffairs = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/claw/claw5.jsp?x=1&y=1&w=2";
        String engineering = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cengineering/cengineering5.jsp?x=1&y=3&w=2";
        String humanities = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/chumanities/chumanities5.jsp?x=1&y=4&w=2";
        String naturalSciences = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cnscience/cnscience5.jsp?x=1&y=5&w=2";
        String urban = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/cuscience/cuscience5.jsp?x=1&y=6&w=2";
        String artAndPhysical = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/carts/carts5.jsp?x=1&y=7&w=2";
        String openMajor = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/openmajor/openmajor4.jsp?x=1&y=8&w=2";
        String general = "http://www.uos.ac.kr/kor_2010/html/academic/colleges/liberal/liberal4.jsp?x=1&y=9&w=2";

        // 여기는 아래 부분만 파싱
        String house = "http://www.uos.ac.kr/kor_2010/html/clife/campus/house/house.jsp?x=1&y=1&w=6";
        String health = "http://www.uos.ac.kr/kor_2010/html/clife/campus/health/health.jsp?x=1&y=2&w=6";
        String stdSubject = "http://www.uos.ac.kr/kor_2010/html/clife/campus/insurance/insurance.jsp?x=1&y=4&w=6";
        String sports = "http://www.uos.ac.kr/kor_2010/html/clife/campus/scomplex/scomplex.jsp?x=1&y=6&w=6";
        // 여기부터는 내부 내용을 파싱
        String store = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc.jsp?x=1&y=7&w=60";
        String store2 = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc2.jsp?x=1&y=7&w=6";
        String vending = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc3.jsp?x=1&y=7&w=6";
        String bookstore = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc5.jsp?x=1&y=7&w=6";
        String copy = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc6.jsp?x=1&y=7&w=6";
        String eyestore = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc7.jsp?x=1&y=7&w=6";
        String souvenir = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc8.jsp?x=1&y=7&w=6";
        String post = "http://www.uos.ac.kr/kor_2010/html/clife/campus/etc/etc9.jsp?x=1&y=7&w=6";

        ArrayList<String> urlList = new ArrayList<>();
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

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            PhoneItem item = (PhoneItem) v.getTag();
            final Context context = getActivity();
            final String phoneNum = parseTelNumber(item.sitePhoneNumber);
            new AlertDialog.Builder(context)
                    .setTitle(item.siteName + context
                            .getString(R.string.tab_phone_confirm_call))
                    .setMessage(phoneNum).setCancelable(true)
                    .setPositiveButton(R.string.confirm, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNum));
                            context.startActivity(callIntent);
                        }
                    }).setNegativeButton(R.string.cancel, null).create().show();

        }

        private String parseTelNumber(String telNumber) {
            if (telNumber.startsWith("도서관")) {
                return telNumber.split(StringUtil.SPACE)[2];
            } else {
                return telNumber.split("~")[0].split(",")[0];
            }
        }
    };

    @NonNull
    @Override
    protected String getFragmentNameForTracker() {
        return "TabPhoneFragment";
    }
}
