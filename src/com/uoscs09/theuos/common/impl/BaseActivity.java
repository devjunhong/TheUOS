package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.app.Activity;
import android.os.Bundle;
/**onCreate���� �׸� ������ �ϴ� ��Ƽ��Ƽ*/
public class BaseActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppUtil.applyTheme(this);
		super.onCreate(savedInstanceState);
	}
}
