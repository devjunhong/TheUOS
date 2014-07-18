package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/** onCreate���� �׸� ������ �ϴ� fragment��Ƽ��Ƽ */
@SuppressLint("Registered")
public class BaseFragmentActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle arg0) {
		AppUtil.applyTheme(this);
		super.onCreate(arg0);
	}

	@Override
	protected void onDestroy() {
		AppUtil.unbindDrawables(getWindow().getDecorView());
		super.onDestroy();
		System.gc();
	}
}
