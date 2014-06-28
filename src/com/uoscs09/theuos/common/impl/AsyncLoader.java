package com.uoscs09.theuos.common.impl;

import java.util.concurrent.Callable;

import android.os.AsyncTask;
import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;

public class AsyncLoader<T> {

	/**
	 * �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 * @param l
	 *            �۾� ������ ȣ��� callback
	 */
	public void excute(Callable<T> task, OnTaskFinishedListener l) {
		getTasker(task, l).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 * @param callback
	 *            �۾� ������ ȣ��� callback
	 */
	public void excute(Callable<T> task, AsyncCallback<T> callback) {
		new AsyncExecutor<T>().setCallable(task).setCallback(callback)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * ����� ��ȯ���� �ʴ� �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 */
	public void excute(Callable<T> task) {
		new AsyncExecutor<T>().setCallable(task).executeOnExecutor(
				AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private AsyncTask<Void, Void, T> getTasker(Callable<T> task,
			final OnTaskFinishedListener l) {
		return new AsyncExecutor<T>().setCallable(task).setCallback(
				new AsyncCallback.Base<T>() {
					public void onResult(T result) {
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
