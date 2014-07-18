package com.uoscs09.theuos.common;

import java.util.concurrent.Callable;

import com.javacan.asyncexcute.AsyncCallback;
import com.javacan.asyncexcute.AsyncExecutor;

import android.os.AsyncTask;

public class AsyncLoader<Data> {
	/**
	 * �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param task
	 *            �񵿱� �۾��� �ǽõ� {@link Callable}
	 * @param l
	 *            �۾� ������ ȣ��� callback
	 */
	public void excute(Callable<Data> task, OnTaskFinishedListener l) {
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
	public void excute(Callable<Data> task, AsyncCallback<Data> callback) {
		new AsyncExecutor<Data>().setCallable(task).setCallback(callback)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * ����� ��ȯ���� �ʴ� �񵿱� �۾��� �����Ѵ�.
	 * 
	 * @param r
	 *            �񵿱� �۾��� �ǽõ� {@link Runnable}
	 */
	public static void excute(Runnable r) {
		AsyncTask.THREAD_POOL_EXECUTOR.execute(r);
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
