package com.uoscs09.theuos.common;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.LinearLayout;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.BaseActivity;
import com.uoscs09.theuos.common.impl.annotaion.ReleaseWhenDestroy;
import com.uoscs09.theuos.common.util.AppUtil;

/** WebView가 포함된 액티비티, 액티비티 종료시(onDestroy) webView를 destory함 */
public abstract class WebViewActivity extends BaseActivity {
	protected NonLeakingWebView mWebView;
	protected WebSettings settings;
	@ReleaseWhenDestroy
	protected Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		mToolbar = (Toolbar) View.inflate(this, R.layout.view_toolbar, null);
		mWebView = new NonLeakingWebView(this);

		setSupportActionBar(mToolbar);
		layout.addView(mToolbar);
		layout.addView(mWebView);
		setContentView(layout);

		settings = mWebView.getSettings();
		getSupportActionBar().setDisplayOptions(
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
	}

	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			settings = null;
			mWebView.clearCache(true);
			mWebView.loadUrl("about:blank");
			AppUtil.unbindDrawables(mWebView);
			mWebView.destroy();
			mWebView = null;
			System.gc();
		}
		super.onDestroy();
	}
}
