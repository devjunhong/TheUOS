package com.uoscs09.theuos.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uoscs09.theuos.common.util.AppUtil;
/** ���� intent�� �޾� �ʿ��� ���񽺸� �����ϴ� Ŭ����*/
public class BReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			AppUtil.startOrStopServiceAnounce(context);
		}
	}
}
