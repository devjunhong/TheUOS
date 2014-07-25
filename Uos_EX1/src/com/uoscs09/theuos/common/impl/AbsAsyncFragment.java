package com.uoscs09.theuos.common.impl;

import java.io.IOException;
import java.util.concurrent.Callable;

import pkg.asyncexcute.AsyncCallback;
import pkg.asyncexcute.AsyncExecutor;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.uoscs09.theuos.common.util.AppUtil;

/**
 * {@code Fragment}�� {@code AsyncExcutor} �������̽��� ������ Ŭ����<br>
 * �� Ŭ������ ��� �޴� Ŭ������ {@code Callable} �������̽��� �ݵ�� �����ؾ��Ѵ�.<br>
 * ������ {@code Callable} �� ��׶��� �۾��� ����Ǵ� �ݹ��̴�.
 */
public abstract class AbsAsyncFragment<T> extends Fragment implements
		AsyncCallback<T>, Callable<T> {
	private AsyncExecutor<T> executor;

	@Override
	public void exceptionOccured(Exception e) {
		if (e instanceof IOException) {
			AppUtil.showInternetConnectionErrorToast(getActivity(),
					isMenuVisible());
		} else {
			AppUtil.showErrorToast(getActivity(), e, isMenuVisible());
		}
	}

	@Override
	public void cancelled() {
		AppUtil.showCanceledToast(getActivity(), isMenuVisible());
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
		executor = new AsyncExecutor<T>();
		executor.setCallable(this).setCallback(this)
				.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	/**
	 * ���� ����ǰ� �ִ� ��׶��� �۾��� ����Ѵ�.
	 * 
	 * @return {@code true} - �۾��� ���������� ����Ͽ��� ��<br>
	 *         {@code false} - �۾��� �������� �ʾҰų� ({@code null}),<br>
	 *         �۾��� ����� �� ���� ��<br>
	 *         (�밳 �̷����� �۾��� �̹� ���������� ����� ����̴�.)
	 */
	final protected boolean cancelExecutor() {
		if (executor != null) {
			return executor.cancel(true);
		} else {
			return false;
		}
	}

	/** �۾��� ó���ϴ� AsyncExcutor ��ü�� ��´�. */
	final protected AsyncExecutor<T> getExecutor() {
		return executor;
	}
}
