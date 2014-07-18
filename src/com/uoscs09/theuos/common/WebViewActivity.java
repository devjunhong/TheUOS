package com.uoscs09.theuos.common;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebSettings;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.BaseActivity;
import com.uoscs09.theuos.common.util.AppUtil;

/** WebView�� ���Ե� ��Ƽ��Ƽ, ��Ƽ��Ƽ �����(onDestroy) webView�� destory�� */
public abstract class WebViewActivity extends BaseActivity {
	protected NonLeakingWebView mWebView;
	protected WebSettings settings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWebView = new NonLeakingWebView(this);
		setContentView(mWebView);
		settings = mWebView.getSettings();
		getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
						| ActionBar.DISPLAY_SHOW_TITLE);
	}

	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.zoom_enter, R.anim.zoom_exit);
		super.onPause();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return false;
		}
	}
}
