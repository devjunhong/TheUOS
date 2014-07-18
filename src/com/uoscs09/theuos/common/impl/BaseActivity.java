package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

/** onCreate���� �׸� ������ �ϴ� ��Ƽ��Ƽ */
@SuppressLint("Registered")
public class BaseActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppUtil.applyTheme(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		AppUtil.unbindDrawables(getWindow().getDecorView());
		super.onDestroy();
		System.gc();
	}
}
