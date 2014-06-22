package com.uoscs09.theuos.tab.timetable;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.OApiUtil;

/** �ð�ǥ �˸� �̺�Ʈ�� �޾� �˸��� ���� BroadcastReceiver */
public class TimeTableNotiReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(TimeTableInfoCallback.ACTION_SET_ALARM)) {
			long[] vibrate = new long[] { 200, 200, 500, 300 };
			int code = intent
					.getIntExtra(TimeTableInfoCallback.INTENT_CODE, 11);
			String name = intent.getStringExtra(OApiUtil.SUBJECT_NAME);
			String when = AppUtil.readFromFile(context, String.valueOf(code));
			if ("�˸� ����".equals(when)) {
				return;
			}
			long time = System.currentTimeMillis();
			String content = "���� (" + name + ") ���� " + when + " �Դϴ�.";
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notify = new Notification.Builder(context)
					.setDefaults(Notification.DEFAULT_ALL)
					.setAutoCancel(true)
					.setContentTitle(
							context.getText(R.string.tab_timetable_noti))
					.setContentText(content).setTicker(content)
					.setLights(0xff00ff00, 1000, 3000)
					.setSmallIcon(R.drawable.ic_launcher).setVibrate(vibrate)
					.setWhen(time).getNotification();
			nm.notify(code * 100, notify);
		}
	}
}
