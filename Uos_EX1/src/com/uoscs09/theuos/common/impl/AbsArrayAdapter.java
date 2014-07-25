package com.uoscs09.theuos.common.impl;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/** View Holder ������ ����ϴ� ArrayAdapter */
public abstract class AbsArrayAdapter<T> extends ArrayAdapter<T> {
	protected int layout;

	public AbsArrayAdapter(Context context, int layout, List<T> list) {
		super(context.getApplicationContext(), layout, list);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, T[] array) {
		super(context.getApplicationContext(), layout, array);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout) {
		super(context.getApplicationContext(), layout);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, int textViewID) {
		super(context.getApplicationContext(), layout, textViewID);
		this.layout = layout;
	}

	public AbsArrayAdapter(Context context, int layout, int textViewID,
			List<T> list) {
		super(context.getApplicationContext(), layout, textViewID, list);
		this.layout = layout;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View view = convertView;
		if (view == null) {
			view = View.inflate(getContext(), layout, null);
			holder = getViewHolder(view);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		return setView(position, view, holder);
	}

	/**
	 * getView �� ������ �ϴ� �޼ҵ��̴�.
	 * 
	 * @param position
	 *            ����Ʈ�� ��ġ
	 * @param convertView
	 *            ���� �Ϸ��� View
	 * @param holder
	 *            getView���� �����Ǵ� ViewHolder
	 * @return convertView - �ݵ�� ������ convertView�� ��ȯ�ؾ��Ѵ�.
	 */
	public abstract View setView(int position, View convertView,
			ViewHolder holder);

	/**
	 * @param view
	 *            ViewHolder ��ü�� ����µ� ���� View
	 * @return AbsArrayAdapter�� ��ӹ��� Ŭ������<br>
	 *         ������ ViewHolder
	 */
	public abstract ViewHolder getViewHolder(View convertView);

	public static interface ViewHolder {
	}
}
