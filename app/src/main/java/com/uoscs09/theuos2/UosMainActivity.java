package com.uoscs09.theuos2;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uoscs09.theuos2.base.BaseActivity;
import com.uoscs09.theuos2.common.BackPressCloseHandler;
import com.uoscs09.theuos2.setting.SettingActivity;
import com.uoscs09.theuos2.util.AppUtil;
import com.uoscs09.theuos2.util.PrefUtil;

import java.util.ArrayList;

/**
 * Main Activity, ViewPager가 존재한다.
 */
public class UosMainActivity extends BaseActivity implements DrawerLayout.DrawerListener {
    /**
     * ViewPager
     */
    private ViewPager mViewPager;
    /**
     * ViewPager Adapter
     */
    private FragmentStatePagerAdapter mPagerAdapter;
    /**
     * 뒤로 두번눌러 종료
     */
    private BackPressCloseHandler mBackCloseHandler;
    /**
     * 화면 순서를 나타내는 리스트
     */
    private ArrayList<Integer> mPageOrderList;

    protected DrawerLayout mDrawerLayout;
    /**
     * Left ListView
     */
    private RecyclerView mDrawerListView;
    /**
     * ActionBar Toggle
     */
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;

    private static final int START_SETTING = 999;
    public static final String SAVED_TAB_NUM = "saved_tab_num";
    private View mLeftDrawerLayout;

    private void initValues() {
        PrefUtil pref = PrefUtil.getInstance(this);
        AppUtil.initStaticValues(pref);
        mPageOrderList = AppUtil.loadEnabledPageOrder(this);
        if (pref.get(PrefUtil.KEY_HOME, true) || mPageOrderList.isEmpty()) {
            mPageOrderList.add(0, R.string.title_section0_home);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        // .detectAll().penaltyLog().penaltyDialog().build());
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
        // .penaltyLog().penaltyDeath().build());
        /* 호출 순서를 바꾸지 말 것 */
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        initValues();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_uosmain);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // getSupportActionBar().setHideOnContentScrollEnabled(true);
        // getSupportActionBar().setHideOffset(40);

        initPager();
        initDrawer();
        /* 호출 순서를 바꾸지 말 것 */

        int tabNumber = getIntent().getIntExtra(SAVED_TAB_NUM, 0);
        if (savedInstanceState != null) {
            navigateItem(mPageOrderList.indexOf(savedInstanceState.getInt(SAVED_TAB_NUM)), false);
        } else if (tabNumber != 0) {
            navigateItem(mPageOrderList.indexOf(tabNumber), false);
        } else {
            navigateItem(0, false);
        }

        AppUtil.startOrStopServiceAnounce(getApplicationContext());
        mBackCloseHandler = new BackPressCloseHandler();
        System.gc();

        //startActivity(new Intent(this, TestActivity.class));
    }

    @NonNull
    @Override
    protected String getScreenName() {
        return "UOSMainActivity";
    }

    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVED_TAB_NUM, getCurrentPageId());
        super.onSaveInstanceState(outState);
    }

    /**
     * ViewPager를 전환하는 메소드
     *
     * @param position    전환될 위치
     * @param isFromPager 메소드가 ViewPager로 부터 호출되었는지 여부 <br>
     *                    loop를 방지하는 역할을 한다.
     */
    protected void navigateItem(int position, boolean isFromPager) {
        if (position < 0) {
            navigateItem(1, isFromPager);
            return;
        }

        if (!isFromPager) {
            mViewPager.setCurrentItem(position, true);
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        }

        int res = mPageOrderList.get(position);
        if (res != -1) {
            getSupportActionBar().setTitle(res);
            // getSupportActionBar().setIcon(AppUtil.getPageIcon(res));
        }
    }

    private void initDrawer() {
        @SuppressWarnings("unchecked")
        ArrayList<Integer> list = (ArrayList<Integer>) mPageOrderList.clone();
        list.add(R.string.setting);
        list.add(R.string.action_exit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_uos_drawer_layout);

        initDrawerDetail();

        // AppUtil.getStyledValue(this,R.attr.menu_ic_navigation_drawer)
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(this);
    }

    private void initDrawerDetail() {
        mLeftDrawerLayout = mDrawerLayout.findViewById(R.id.left_drawer);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mDrawerListView = (RecyclerView) mLeftDrawerLayout.findViewById(R.id.drawer_listview);
        mDrawerListView.setLayoutManager(layoutManager);
        mDrawerListView.setAdapter(new DrawerAdapter());

        mLeftDrawerLayout.findViewById(R.id.drawer_setting_ripple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingActivity();
                mDrawerLayout.closeDrawer(mLeftDrawerLayout);
            }
        });

        /*
        mLeftDrawerLayout.findViewById(R.id.drawer_exit_ripple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
*/
    }

    private void initPager() {
        mPagerAdapter = new IndexPagerAdapter(getSupportFragmentManager(),  mPageOrderList, this);
        mViewPager = (ViewPager) findViewById(R.id.activity_pager_viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                navigateItem(position, true);
            }
        });
        //mViewPager.setOffscreenPageLimit(mPagerAdapter.getCount());
        switch (AppUtil.theme) {
            case BlackAndWhite:
                mViewPager.setPageTransformer(true, new PagerTransformer(2));
                break;
            case White:
                mViewPager.setPageTransformer(true, new PagerTransformer(1));
                break;
            case LightBlue:
                break;
            case Black:
            default:
                mViewPager.setPageTransformer(true, new PagerTransformer(0));
                break;
        }

    }

    /**
     * SettingActivity를 시작한다.
     */
    protected void startSettingActivity() {
        startActivityForResult(new Intent(this, SettingActivity.class), START_SETTING);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * 현재 페이지의 인덱스를 얻는다.
     */
    protected int getCurrentPageIndex() {
        return mViewPager.getCurrentItem();
    }

    /**
     * 현재 페이지의 id를 얻는다.
     */
    protected int getCurrentPageId() {
        return mPageOrderList.get(getCurrentPageIndex());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDrawerToggle.isDrawerIndicatorEnabled());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        getSupportActionBar().setDisplayHomeAsUpEnabled(mDrawerToggle.isDrawerIndicatorEnabled());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDrawerOpen = mDrawerLayout.isDrawerOpen(mLeftDrawerLayout);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!isDrawerOpen);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                openOrCloseDrawer();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // AppUtil.clearApplicationFile(getCacheDir());
        // AppUtil.clearApplicationFile(getExternalCacheDir());
        AppUtil.closeAllDatabase(getApplicationContext());
    }

    /**
     * 새로운 인텐트를 받을때 그것을 검사해서 <br>
     * FinishSelf = true 이면 어플리케이션 종료
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra("FinishSelf", false)) {
            // TODO 이곳에서 종료 전 처리를 한다.
            // AppUtil.clearApplicationFile(getCacheDir());
            // AppUtil.clearApplicationFile(getExternalCacheDir());
            AppUtil.closeAllDatabase(this);
            finish();
            overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
            // 지정 시간 후 모든 스레드 종료
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            }, 300);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        mBackCloseHandler = null;
        mDrawerListView = null;
        mDrawerToggle = null;
        mPageOrderList = null;
        mPagerAdapter = null;
        mViewPager = null;
        mLeftDrawerLayout = null;
        mDrawerLayout = null;
        super.onDetachedFromWindow();
    }

    @Override
    public void onBackPressed() {
        if (AppUtil.isScreenSizeSmall(this) && mDrawerLayout.isDrawerOpen(mLeftDrawerLayout)) {
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);

        }/* else if (PrefUtil.getInstance(getApplicationContext()).get(PrefUtil.KEY_HOME, true)) {
            if (getCurrentPageIndex() == 0) {
                doBack();
            }

            navigateItem(0, false);

        } */else {
            doBack();
        }
    }

    private void doBack() {
        if (mBackCloseHandler.onBackPressed()) {
            AppUtil.exit(this);
        } else {
            AppUtil.showToast(this, R.string.before_finish, true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // activity를 재시작함
        // XXX potential memory leak!!!!!
        if (resultCode == AppUtil.RELAUNCH_ACTIVITY) {
            finish();
            overridePendingTransition(R.anim.enter_fade, R.anim.exit_hold);
            startActivity(getIntent().putExtra(SAVED_TAB_NUM, getCurrentPageId()));
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (!PrefUtil.getInstance(this).get(PrefUtil.KEY_HOME, true)) {
                openOrCloseDrawer();

            } else if (getCurrentPageIndex() != 0) {
                openOrCloseDrawer();

            }

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDrawerClosed(View drawerView) {
        mDrawerToggle.onDrawerClosed(drawerView);
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        mDrawerToggle.onDrawerOpened(drawerView);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
    }

    @Override
    public void onDrawerStateChanged(int newState) {
        mDrawerToggle.onDrawerStateChanged(newState);
    }

    private void openOrCloseDrawer() {
        if (mDrawerLayout.isDrawerOpen(mLeftDrawerLayout))
            mDrawerLayout.closeDrawer(mLeftDrawerLayout);
        else
            mDrawerLayout.openDrawer(mLeftDrawerLayout);
    }

    public static class PagerTransformer implements ViewPager.PageTransformer {
        private final int i;

        public PagerTransformer(int i) {
            this.i = i;
        }

        @Override
        public void transformPage(View arg0, float arg1) {
            switch (i) {
                case 1:
                    transfromZoom(arg0, arg1);
                    break;
                case 2:
                    transformDepth(arg0, arg1);
                    break;
                case 3:
                    break;
                default:
                    arg0.setRotationY(arg1 * -30);
                    break;
            }
        }

        private void transfromZoom(View view, float position) {
            final float MIN_SCALE = 0.85f;
            final float MIN_ALPHA = 0.5f;
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as
                // well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE)
                        / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }

        private void transformDepth(View view, float position) {
            final float MIN_SCALE = 0.75f;
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
                        * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }


    private class DrawerAdapter extends RecyclerView.Adapter<ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_layout_drawer ,parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int titleStringId = mPageOrderList.get(position);

            holder.img.setImageResource(AppUtil.getPageIcon(holder.itemView.getContext() , titleStringId));
            holder.text.setText(titleStringId);
        }

        @Override
        public int getItemCount() {
            return mPageOrderList.size();
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView img;
        TextView text;
        public ViewHolder(View itemView) {
            super(itemView);
            View ripple = itemView.findViewById(R.id.drawer_list_ripple);
            ripple.setOnClickListener(this);

            img = (ImageView) ripple.findViewById(R.id.drawer_list_img);
            text = (TextView) ripple.findViewById(R.id.drawer_list_text);
        }

        @Override
        public void onClick(View v) {
            navigateItem(getLayoutPosition(), false);
        }
    }
}