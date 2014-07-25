package com.uoscs09.theuos.tab.anounce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

/**
 * �������� �˸��̰� ����Ǵ� {@code Service}<br>
 * �������� �˸��̴� ���ͳ� ������ �ʿ��ϹǷ� ������ {@code Thread}�� ���� ����ȴ�.<br>
 * ���� ���� ����� ������ ������� �ʴ� ����̴�. <br>
 * �̸� ���� ������� �ڵ������� Ư�� �ð����� ó���� �ϴ� �������� �����Ǿ���. <br>
 * <br>
 * ���� ����� ���Ǹ� ���� ������ �����Ͽ� �˷��ִ� ������� �����Ϸ���, <br>
 * <b>GCM</b>���� �̿��Ͽ� <b>JSON</b> �����͸� �����ϴ� ��ĵ��� ����ϸ� �� ���̴�.
 * */
public class ServiceForAnounce extends Service {
	protected static final String TAG = "Service_Anounce";
	protected static final long WAIT_TIME = 2 * 60 * 60 * 1000;
	protected static final long WAIT_MIN = 5000;
	protected static final long WAIT_MAX = 86330000;
	protected boolean isThreadFinish = false;
	protected static final int NOTIFICATION_NUMBER = 9090;
	private Thread worker;
	protected PrefUtil pref;
	protected boolean isServiceEnabled;
	/** ������ URL */
	protected static final String[] URL_LIST = {
			"http://www.uos.ac.kr/korNotice/list.do?list_id=FA1&pageIndex=1",
			"http://www.uos.ac.kr/korNotice/list.do?list_id=FA2&pageIndex=1",
			"http://scholarship.uos.ac.kr/scholarship.do?process=list&brdbbsseq=1&x=1&y=1&w=3&pageNo=1" };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (pref == null)
			pref = PrefUtil.getInstance(getApplicationContext());
		isServiceEnabled = pref.get(PrefUtil.KEY_CHECK_ANOUNCE_SERVICE, true);
		if (!isServiceEnabled) {
			return super.onStartCommand(intent, 0, startId);
		}
		if (worker == null || !worker.isAlive()) {
			worker = new Worker();
			worker.start();
		} else {
			synchronized (worker) {
				worker.notify();
			}
		}
		return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId);
	}

	@Override
	public void onDestroy() {
		if (worker != null) {
			isThreadFinish = true;
			worker.interrupt();
			worker = null;
		}
		super.onDestroy();
	}

	private class Worker extends Thread {
		@Override
		public void run() {
			while (!isThreadFinish) {
				boolean isServiceEnabled = pref.get(
						PrefUtil.KEY_CHECK_ANOUNCE_SERVICE, true);
				if (!isServiceEnabled) {
					break;
				}
				// ���� �ð��� �˸� ���� �ð��� ���Ͽ�
				// �������� ��� �ð��� ����
				try {
					while (true) {
						// ���� ���� ���� �ʾҴٸ� ���
						int hour = pref.get(StringUtil.STR_HOUR, -1);
						int min = pref.get(StringUtil.STR_MIN, -1);
						if (hour == -1 || min == -1) {
							synchronized (this) {
								this.wait(WAIT_TIME);
							}
						} else {
							long waitTimeByMill = getWaitTime(hour, min);
							// ����ؼ� ���� ��� �ð��� WAIT_TIME ���� ������
							// �� ��ŭ�� ����ϰ� �˸� ���� �ܰ�� �����Ѵ�.
							synchronized (this) {
								this.wait(waitTimeByMill + 1);
							}
							if (WAIT_TIME > waitTimeByMill) {
								hour = pref.get(StringUtil.STR_HOUR, -1);
								min = pref.get(StringUtil.STR_MIN, -1);
								waitTimeByMill = getWaitTime(hour, min);
								// notify Ȯ�� �� ���� �ð� ����
								if (waitTimeByMill < WAIT_MIN
										|| waitTimeByMill > WAIT_MAX) {
									break;
								}
							}
							// continue;
						} // end of else
					} // end of while

					String[] keywords = pref
							.get(PrefUtil.KEY_KEYWORD_ANOUNCE, StringUtil.NULL)
							.trim().split(StringUtil.NEW_LINE);
					if (keywords != null) {
						setNotification(keywords);
						synchronized (this) {
							this.wait(WAIT_TIME);
						}
					}// end of if
				} catch (InterruptedException e) {
					break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}// end of while
			Log.i(TAG, "Thread Finished");
			return;
		}

		/**
		 * �־��� Ű���带 ������, ���������� �˻��Ͽ� <br>
		 * ���ǿ� �´� ���������� {@code NotificationManager}�� ����Ѵ�.
		 * 
		 * @param keywords
		 *            ����ȭ�鿡�� ������ �˻� Ű���� ����Ʈ
		 */
		@SuppressWarnings("unchecked")
		private void setNotification(final String[] keywords)
				throws IOException {
			int notiNum = NOTIFICATION_NUMBER;
			NotificationManager mNotiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String date = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
					.format(new Date());
			AnounceItem item;
			String web = StringUtil.NULL;
			String body;
			List<AnounceItem> list;
			Context context = getApplicationContext();
			for (String keyword : keywords) {
				keyword = keyword.trim();
				if (keyword.equals(StringUtil.NULL))
					continue;
				for (int j = 0; j < URL_LIST.length; j++) {
					// TODO ù������ Ž���� �ƴ� ���������� �˻��ϴ� ������� ����
					body = HttpRequest.getBody(URL_LIST[j]);
					list = (List<AnounceItem>) ParseFactory.create(
							ParseFactory.ANOUNCE,
							body,
							j == 2 ? ParseFactory.Value.BODY
									: ParseFactory.Value.BASIC).parse();

					// ������ Ű���尡 ���� ���Ե� ���������� �˸�������
					int size = list.size();
					String type = "����";
					for (int i = 0; i < size; i++) {
						item = list.get(i);
						if (item.date.contains(date)
								&& item.title.contains(keyword)
								&& !item.type.contains(type)) {
							switch (j) {
							case 0:
								web = "http://www.uos.ac.kr/korNotice/view.do?list_id=FA1&sort=1&seq="
										+ item.onClickString;
								break;
							case 1:
								web = "http://www.uos.ac.kr/korNotice/view.do?list_id=FA2&sort=1&seq="
										+ item.onClickString;
								break;
							case 2:
								web = "http://scholarship.uos.ac.kr/scholarship.do?process=view&brdBbsseq=1&x=1&y=1&w=3&"
										+ item.onClickString;
								break;
							default:
								break;
							}
							Intent intent = AppUtil.setWebPageIntent(web);
							PendingIntent pi = PendingIntent.getActivity(
									context, 0, intent, 0);
							mNotiManager.notify(notiNum++,
									notiBuilder(item, pi, context));
						}
					}
				}
			}
		}

		private long getWaitTime(int hour, int min) {
			Calendar cal = Calendar.getInstance();
			int hour_now = cal.get(Calendar.HOUR_OF_DAY);
			int min_now = cal.get(Calendar.MINUTE);

			int waitHour = hour - hour_now;
			int waitMin = min - min_now;
			if (waitMin < 0) {
				waitMin += 60;
				waitHour -= 1;
			}
			if (waitHour < 0) {
				waitHour += 24;
			}
			long waitTimeByMill = (waitHour * 60 + waitMin) * 60 * 1000;
			return waitTimeByMill;
		}
	};

	protected Notification notiBuilder(AnounceItem item, PendingIntent pi,
			Context context) {
		long[] PATTERN = { 200, 300, 200 };
		return new Notification.Builder(context)
				.setContentTitle(Html.fromHtml(item.type))
				.setContentText(Html.fromHtml(item.title))
				.setContentIntent(StringUtil.NULL.equals(item.date) ? null : pi)
				.setSmallIcon(R.drawable.ic_launcher)
				.setTicker(getText(R.string.setting_anounce_noti))
				.setAutoCancel(true).setVibrate(PATTERN).getNotification();
	}
}
