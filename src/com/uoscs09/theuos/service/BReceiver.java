package com.uoscs09.theuos.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.tab.timetable.TimeTableInfoCallback;

/** ���� intent�� �޾� �ʿ��� ���񽺸� �����ϴ� Ŭ���� */
public class BReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			AppUtil.startOrStopServiceAnounce(context);
		} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
			TimeTableInfoCallback.clearAllAlarm(context);
		}
	}
}
