package com.uoscs09.theuos.common.impl;

import com.uoscs09.theuos.common.util.AppUtil;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * 기본적인 편의 기능이 구현된 Fragment<br>
 * <br>
 * 지원되는 기능 : <br>
 * <li>{@link #getActionBar()} - 현 Activity의 ActionBar를 가져온다</> <li>
 * {@link #setSubtitleWhenVisible(CharSequence)} - Fragment가 UI에 보여질 때,
 * subTitle을 설정한다.</>
 */
public class BaseFragment extends Fragment {

	@Override
	public void onDetach() {
		AppUtil.releaseResource(this);
		super.onDetach();
		System.gc();
	}

	/**
	 * 현 Activity의 ActionBar를 가져온다
	 */
	protected final ActionBar getActionBar() {
		if (isAdded())
			return ((ActionBarActivity) getActivity()).getSupportActionBar();
		else
			return null;
	}

	/**
	 * Fragment가 UI에 보여질 때, subTitle을 설정한다
	 */
	protected void setSubtitleWhenVisible(CharSequence subTitle) {
		if (isMenuVisible()) {
			if (getActionBar() != null)
				getActionBar().setSubtitle(subTitle);
		}
	}

	@Override
	public void setMenuVisibility(boolean menuVisible) {
		super.setMenuVisibility(menuVisible);
		if (menuVisible) {
			if (getActionBar() != null)
				getActionBar().setSubtitle(getSubtitle());
		} else {
			if (getActionBar() != null)
				getActionBar().setSubtitle(null);
		}
	}

	/** ActionBar의 subTitle */
	protected CharSequence getSubtitle() {
		return null;
	}
}
