package com.uoscs09.theuos.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.tab.timetable.TimeTableInfoCallback;

/** 여러 intent를 받아 필요한 서비스를 실행하는 클래스 */
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
