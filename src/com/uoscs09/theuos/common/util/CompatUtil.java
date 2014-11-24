package com.uoscs09.theuos.common.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.webkit.WebSettings;

public class CompatUtil {

	private static final boolean checkSdkVersionHoneyComb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	@SuppressLint("NewApi")
	public static final void dialogIcon(AlertDialog dialog, int attr) {
		if (checkSdkVersionHoneyComb()) {
			dialog.setIconAttribute(attr);
		} else {
			dialog.setIcon(AppUtil.getStyledValue(dialog.getContext(), attr));
		}
	}

	@SuppressLint("NewApi")
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final void AsyncTaskExcute(AsyncTask task) {
		if (checkSdkVersionHoneyComb()) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} else {
			task.execute();
		}
	}

	@SuppressLint("NewApi")
	public static final void setDisplayZoomControls(WebSettings settings,
			boolean enable) {
		if (checkSdkVersionHoneyComb()) {
			settings.setDisplayZoomControls(enable);
		}
	}
}
