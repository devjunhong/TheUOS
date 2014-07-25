package com.uoscs09.theuos.common;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/** URI â�� �ƴ� ���� URL�� load�ϴ� ���� */
public class CustomWebViewClient extends WebViewClient {

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		view.loadUrl(url);
		return true;
	}
}
