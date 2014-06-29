package com.uoscs09.theuos.common.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;
import android.os.AsyncTask;

public class AsyncLoader<Data> {
	private static final ThreadFactory sThreadFactory = new ThreadFactory() {
		private final AtomicInteger mCount = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "AsyncLoader #" + mCount.getAndIncrement());
		}
	};
	private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(
			10);
	public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
			4, 128, 1, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

	/**
	 * �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 * @param l
	 *            �۾� ������ ȣ��� callback
	 */
	public void excute(Callable<Data> task, OnTaskFinishedListener l) {
		getTasker(task, l).executeOnExecutor(THREAD_POOL_EXECUTOR);
	}

	/**
	 * �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 * @param callback
	 *            �۾� ������ ȣ��� callback
	 */
	public void excute(Callable<Data> task, AsyncCallback<Data> callback) {
		new AsyncExecutor<Data>().setCallable(task).setCallback(callback)
				.executeOnExecutor(THREAD_POOL_EXECUTOR);
	}

	/**
	 * ����� ��ȯ���� �ʴ� �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param r
	 *            �񵿱� �۾��� �ǽõ� {@link Runnable}
	 */
	public static void excute(Runnable r) {
		THREAD_POOL_EXECUTOR.execute(r);
	}

	private AsyncTask<Void, Void, Data> getTasker(Callable<Data> task,
			final OnTaskFinishedListener l) {
		return new AsyncExecutor<Data>().setCallable(task).setCallback(
				new AsyncCallback.Base<Data>() {
					public void onResult(Data result) {
						if (l != null)
							l.onTaskFinished(false, result);
					}

					@Override
					public void exceptionOccured(Exception e) {
						if (l != null)
							l.onTaskFinished(true, e);
					}
				});
	}

	/** �� ���� �۾� �� ȣ��� listener */
	public interface OnTaskFinishedListener {
		/**
		 * �� ���� �۾� �� ȣ��Ǵ� �޼ҵ�
		 * 
		 * @param isExceptionOccoured
		 *            Exception �߻� ����
		 * @param data
		 *            <li>Exception�� �߻��� ��� : {@link Exception}��ü</li> <li>
		 *            Exception�� �߻����� ���� ��� : {@link Callable} ���� ��ȯ�� ���</li>
		 */
		public void onTaskFinished(boolean isExceptionOccoured, Object data);
	}

}
