package com.uoscs09.theuos.setting;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.uoscs09.theuos.common.impl.BaseActivity;
/** ���� activity, �ֿ� ������ SettingsFragment�� �����Ǿ� �ִ�.*/
public class SettingActivity extends BaseActivity {  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new SettingsFragment()).commit();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default:
			return false;
		}
	}
}
