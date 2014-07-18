package com.uoscs09.theuos.common.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;
import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.annotaion.AsyncData;
import com.uoscs09.theuos.common.util.AppUtil;

/**
 * {@code Fragment}�� {@code AsyncExcutor} �������̽��� ������ Ŭ����<br>
 * �� Ŭ������ ��� �޴� Ŭ������ {@code Callable} �������̽��� �ݵ�� �����ؾ��Ѵ�.<br>
 * ������ {@code Callable} �� ��׶��� �۾��� ����Ǵ� �ݹ��̴�.
 */
public abstract class AbsAsyncFragment<T> extends BaseFragment implements
		AsyncCallback<T>, Callable<T> {
	private AsyncExecutor<T> executor;
	private boolean mRunning = false;
	private final static Map<String, Object> sAsyncDataStoreMap = new ConcurrentHashMap<String, Object>();
	private Context mContext;

	/**
	 * {@code super.onCreate()}�� ȣ���ϸ�, ������ �� ���� �۾� ó�� ����� ����<br>
	 * {@code @AsyncData} annotation�� ������ ��ü�� ���� ���� �� ���� �ִ�.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mContext = getActivity();
		Object data = getAsyncData(getClass().getName());
		if (data != null) {
			Field[] fs = getClass().getDeclaredFields();
			for (Field f : fs) {
				if (f.getAnnotation(AsyncData.class) != null) {
					f.setAccessible(true);
					try {
						f.set(this, data);
					} catch (IllegalAccessException e) {
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
					f.setAccessible(false);
					break; // ���� �ϳ��� ������ �����
				}
			}
		}
	}

	/** ���� ��׶��� �۾��� ���� �� ���� ���θ� ��ȯ�Ѵ�. */
	public final boolean isRunning() {
		return mRunning;
	}

	/**
	 * Main Thread���� �񵿱� �۾��� �����ϰ�, �����ϴ� �޼ҵ�<br>
	 * �񵿱� �۾��� ����Ǳ� ���� �ʿ��� �۾��� �� �޼ҵ带 ȣ���ϱ� ���� <br>
	 * ó���ϰų�, �� �޼ҵ带 ��ӹ޾� ������ �����Ѵ�.<br>
	 */
	protected void excute() {
		if (executor != null && !executor.isCancelled()) {
			executor.cancel(true);
		}
		mRunning = true;
		setExcuter(true);
		executor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		sAsyncDataStoreMap.remove(getClass().getName());
	}

	/**
	 * ��׶��� �۾��� �����Ѵ�.
	 * 
	 * @param force
	 *            �ٸ� �۾��� �������ΰͰ� ������� ������ �����ϴ��� ����
	 * @return ���� ����
	 */
	public final boolean setExcuter(boolean force) {
		if (!force && executor.getStatus().equals(AsyncTask.Status.RUNNING))
			return false;
		else {
			executor = new AsyncExecutor<T>().setCallable(this).setCallback(
					this);
			return true;
		}
	}

	/**
	 * ���� ����ǰ� �ִ� ��׶��� �۾��� ����Ѵ�.
	 * 
	 * @return {@code true} - �۾��� ���������� ����Ͽ��� ��<br>
	 *         {@code false} - �۾��� �������� �ʾҰų� ({@code null}),<br>
	 *         �۾��� ����� �� ���� ��<br>
	 *         (�밳 �̷����� �۾��� �̹� ���������� ����� ����̴�.)
	 */
	protected final boolean cancelExecutor() {
		if (executor != null) {
			boolean b = executor.cancel(true);
			if (b || executor.getStatus().equals(AsyncTask.Status.FINISHED))
				mRunning = false;
			return b;
		} else {
			mRunning = false;
			return false;
		}
	}

	/** �۾��� ó���ϴ� AsyncExcutor ��ü�� ��´�. */
	protected final AsyncExecutor<T> getExecutor() {
		return executor;
	}

	@Override
	public final void onPostExcute() {
		mRunning = false;
		if (isVisible()) {
			onTransactPostExcute();
		}
	}

	/** ���� Fragment�� �����ϴ� ���¿��� �񵿱� �۾��� ������ UI Thread�� ���� �� �� ȣ��ȴ�. */
	protected abstract void onTransactPostExcute();

	@Override
	public final void onResult(T result) {
		if (isVisible())
			onTransactResult(result);
		else {
			putAsyncData(getClass().getName(), result);
			notifyFinishWhenBackground(mContext, result);
			mContext = null;
		}
	}

	@Override
	public void exceptionOccured(Exception e) {
		if (isVisible()) {
			if (e instanceof IOException) {
				AppUtil.showInternetConnectionErrorToast(getActivity(),
						isMenuVisible());
			} else {
				AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
			}
		} else {
			notifyFinishWhenBackground(mContext, e);
			mContext = null;
		}
	}

	@Override
	public void cancelled() {
		AppUtil.showCanceledToast(getActivity(), isMenuVisible());
	}

	/**
	 * �񵿱� �۾��� ��������, Fragment�� �ı��Ǿ��� ��, ȣ��ȴ�. <br>
	 * <br>
	 * �⺻������ ������ �۾��� notification�� ���� ���̴�.
	 * 
	 * @param context
	 *            Fragment�� Activity
	 * @param result
	 *            �۾��� �������� �� - 'T' ��ü<br>
	 *            �۾��� �����Ͽ��� �� - Exception ��ü
	 */
	protected void notifyFinishWhenBackground(Context context, Object result) {
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification noti;
		CharSequence resultMesage;
		if (result instanceof Exception) {
			resultMesage = context.getText(R.string.progress_fail);
		} else {
			resultMesage = context.getText(R.string.finish_update);
		}
		int titleRes = AppUtil.getPageResByClass(getClass());
		CharSequence title;
		if (titleRes != -1) {
			title = context.getText(titleRes);
		} else {
			title = context.getText(R.string.progress_finish);
		}

		noti = new NotificationCompat.Builder(context).setAutoCancel(true)
				.setContentTitle(title).setContentText(resultMesage)
				.setSmallIcon(R.drawable.ic_launcher)
				.setTicker(context.getText(R.string.progress_finish)).build();
		nm.notify(AppUtil.titleResIdToOrder(titleRes), noti);
	}

	@Override
	public void onDetach() {
		if (!mRunning) {
			mContext = null;
		}
		super.onDetach();
	}

	/** ���� Fragment�� �����ϰ�, �񵿱� �۾��� ���������� ������ �� ȣ��ȴ�. */
	public abstract void onTransactResult(T result);

	/**
	 * �񵿱� �۾��� ���� ��, Fragment�� �̹� �ı��Ǿ��� �� ȣ��Ǿ� <br>
	 * �������� Map�� �����͸� �����Ѵ�.
	 * 
	 * @param key
	 *            ������ �������� key, ���ø����̼ǿ��� �������� �������̹Ƿ� ��ġ�� �ʰ� �����Ͽ��� �Ѵ�.
	 * @param obj
	 *            ������ ������
	 * @return ���� ���� ����, �ش� key�� �����ߴٸ� ������� �ʰ� false�� ��ȯ�Ѵ�.
	 */
	protected boolean putAsyncData(String key, T obj) {
		if (!sAsyncDataStoreMap.containsKey(key)) {
			sAsyncDataStoreMap.put(key, obj);
			return true;
		} else
			return false;
	}

	/**
	 * �񵿱� �۾����� ���� ����� data�� �����´�. ������ data�� Map���� �����ȴ�.
	 * 
	 * @param key
	 *            ����� data�� ������ key
	 * @return ����� data, ����� data�� ���ٸ� null�� ��ȯ�Ѵ�.
	 * */
	protected final static Object getAsyncData(String key) {
		return sAsyncDataStoreMap.remove(key);
	}
}
