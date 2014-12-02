package com.uoscs09.theuos.tab.anounce;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.util.Log;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.PrefUtil;
import com.uoscs09.theuos.common.util.StringUtil;
import com.uoscs09.theuos.http.HttpRequest;
import com.uoscs09.theuos.http.parse.ParseFactory;

/**
 * 공지사항 알리미가 실행되는 {@code Service}<br>
 * 공지사항 알리미는 인터넷 연결이 필요하므로 내부의 {@code Thread}를 통해 실행된다.<br>
 * 현재 구현 방식은 서버를 사용하지 않는 방식이다. <br>
 * 이를 위해 사용자의 핸드폰에서 특정 시간마다 처리를 하는 방향으로 설정되었다. <br>
 * <br>
 * 추후 사용자 편의를 위해 서버를 구동하여 알려주는 방식으로 구현하려면, <br>
 * <b>GCM</b>등을 이용하여 <b>JSON</b> 데이터를 전달하는 방식등을 사용하면 될 것이다.
 * */
public class ServiceForAnounce extends Service {
	protected static final String TAG = "Service_Anounce";
	protected static final long WAIT_TIME = 2 * 60 * 60 * 1000;
	protected static final long WAIT_MIN = 5000;
	protected static final long WAIT_MAX = 86330000;
	protected boolean isThreadFinish = false;
	protected static final int NOTIFICATION_NUMBER = 9090;
	private Thread mWorker;
	protected PrefUtil mPrefUtil;
	protected boolean isServiceEnabled;
	/** 접속할 URL */
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
		if (mPrefUtil == null)
			mPrefUtil = PrefUtil.getInstance(getApplicationContext());
		isServiceEnabled = mPrefUtil.get(PrefUtil.KEY_CHECK_ANOUNCE_SERVICE, true);
		if (!isServiceEnabled) {
			return super.onStartCommand(intent, 0, startId);
		}
		if (mWorker == null || !mWorker.isAlive()) {
			mWorker = new Worker();
			mWorker.start();
		} else {
			synchronized (mWorker) {
				mWorker.notify();
			}
		}
		return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId);
	}

	@Override
	public void onDestroy() {
		if (mWorker != null) {
			isThreadFinish = true;
			mWorker.interrupt();
			mWorker = null;
		}
		super.onDestroy();
	}

	private class Worker extends Thread {
		@Override
		public void run() {
			while (!isThreadFinish) {
				boolean isServiceEnabled = mPrefUtil.get(
						PrefUtil.KEY_CHECK_ANOUNCE_SERVICE, true);
				if (!isServiceEnabled) {
					break;
				}
				// 현재 시각과 알림 설정 시간을 비교하여
				// 스레드의 대기 시간을 설정
				try {
					while (true) {
						// 값이 설정 되지 않았다면 대기
						int hour = mPrefUtil.get(StringUtil.STR_HOUR, -1);
						int min = mPrefUtil.get(StringUtil.STR_MIN, -1);
						if (hour == -1 || min == -1) {
							synchronized (this) {
								this.wait(WAIT_TIME);
							}
						} else {
							long waitTimeByMill = getWaitTime(hour, min);
							// 계산해서 구한 대기 시간이 WAIT_TIME 보다 작으면
							// 그 만큼만 대기하고 알림 설정 단계로 진행한다.
							synchronized (this) {
								this.wait(waitTimeByMill + 1);
							}
							if (WAIT_TIME > waitTimeByMill) {
								hour = mPrefUtil.get(StringUtil.STR_HOUR, -1);
								min = mPrefUtil.get(StringUtil.STR_MIN, -1);
								waitTimeByMill = getWaitTime(hour, min);
								// notify 확인 및 지연 시간 보정
								if (waitTimeByMill < WAIT_MIN
										|| waitTimeByMill > WAIT_MAX) {
									break;
								}
							}
							// continue;
						} // end of else
					} // end of while

					String[] keywords = mPrefUtil
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
		 * 주어진 키워드를 가지고, 공지사항을 검색하여 <br>
		 * 조건에 맞는 공지사항을 {@code NotificationManager}에 등록한다.
		 * 
		 * @param keywords
		 *            설정화면에서 설정된 검색 키워드 리스트
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
					// TODO 첫페이지 탐색이 아닌 공지사항을 검색하는 방법으로 수정
					body = HttpRequest.getBody(URL_LIST[j]);
					list = (List<AnounceItem>) ParseFactory.create(
							ParseFactory.What.Anounce,
							body,
							j == 2 ? ParseFactory.Value.BODY
									: ParseFactory.Value.BASIC).parse();

					// 설정한 키워드가 제목에 포함된 공지사항을 알림설정함
					int size = list.size();
					String type = "공지";
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

	@SuppressWarnings("deprecation")
	@SuppressLint ("Correctness")
	protected Notification notiBuilder(AnounceItem item, PendingIntent pi,
			Context context) {
		long[] PATTERN = { 200, 300, 200 };
		NotificationCompat.Builder b = new NotificationCompat.Builder(context)
				.setContentTitle(Html.fromHtml(item.type))
				.setContentText(Html.fromHtml(item.title))
				.setContentIntent(StringUtil.NULL.equals(item.date) ? null : pi)
				.setSmallIcon(R.drawable.ic_launcher)
				.setTicker(getText(R.string.setting_anounce_noti))
				.setAutoCancel(true).setVibrate(PATTERN);
		if (VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			return b.getNotification();
		} else {
			return b.build();
		}
	}
}
