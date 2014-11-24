package com.uoscs09.theuos.common;

import java.util.List;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.uoscs09.theuos.common.impl.AbsArrayAdapter;
import com.uoscs09.theuos.common.util.AppUtil;
import com.uoscs09.theuos.common.util.AppUtil.AppTheme;

/** TextView가 하나있고, icon이 붙은 layout의 Adapter를 제공하는 클래스. */
public class SimpleTextViewAdapter extends AbsArrayAdapter<Integer> {
	protected int textViewId;
	protected int textViewTextColor = 0;
	protected AppTheme iconTheme;
	protected DrawblePosition position;
	protected Rect drawableBound;
	protected boolean isDrawableForMenu = false;

	public enum DrawblePosition {
		TOP, BOTTOM, LEFT, RIGHT
	}

	public SimpleTextViewAdapter(Context context, int layout, List<Integer> list) {
		super(context, layout, list);
		this.textViewId = android.R.id.text1;
		this.position = DrawblePosition.LEFT;
		this.iconTheme = AppUtil.theme;
	}

	private SimpleTextViewAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View setView(int position, View convertView, ViewHolder holder) {
		Holder h = (Holder) holder;
		int item = getItem(position);
		TextView tv = h.tv;
		tv.setText(item);
		Drawable d;
		if (iconTheme != null) {
			d = getContext().getResources().getDrawable(
					AppUtil.getPageIcon(item, iconTheme));
		} else {
			if (isDrawableForMenu) {
				d = getContext().getResources().getDrawable(
						AppUtil.getPageIconForMenu(getContext(), item));
			} else {
				d = getContext().getResources().getDrawable(
						AppUtil.getPageIcon(getContext(), item));
			}
		}
		switch (this.position) {
		case TOP:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, d, null, null);
			} else
				tv.setCompoundDrawablesWithIntrinsicBounds(null, d, null, null);
			break;
		case BOTTOM:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, null, null, d);
			} else
				tv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, d);
			break;
		case RIGHT:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(null, null, d, null);
			} else
				h.tv.setCompoundDrawablesWithIntrinsicBounds(null, null, d,
						null);
			break;
		default:
			if (drawableBound != null) {
				d.setBounds(drawableBound);
				tv.setCompoundDrawables(d, null, null, null);
			} else
				h.tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null,
						null);
			break;
		}
		h.tv.setTextColor(textViewTextColor == 0 ? getContext().getResources()
				.getColor(
						AppUtil.getStyledValue(getContext(),
								android.R.attr.colorForeground))
				: textViewTextColor);
		return convertView;
	}

	@Override
	public ViewHolder getViewHolder(View convertView) {
		return new Holder(convertView);
	}

	private class Holder implements ViewHolder {
		public TextView tv;

		public Holder(View v) {
			tv = (TextView) v.findViewById(textViewId);
		}
	}

	/** SimpleTextviewAdater의 Builder class */
	public static class Builder {
		private SimpleTextViewAdapter product;

		public Builder(Context context, int layout, List<Integer> list) {
			this.product = new SimpleTextViewAdapter(context, layout, list);
		}

		/** layout에 포함된 TextView의 id를 설정한다 */
		public Builder setTextViewId(int id) {
			product.textViewId = id;
			return this;
		}

		/** TextView의 icon의 위치를 설정한다. */
		public Builder setDrawablePosition(DrawblePosition position) {
			product.position = position;
			return this;
		}

		/** TextView의 icon의 크기를 설정한다. */
		public Builder setDrawableBounds(Rect bounds) {
			product.drawableBound = bounds;
			return this;
		}

		/**
		 * TextView의 icon의 테마를 설정한다.<br>
		 * <i>추후에 이 메소드를 삭제할 예정</i>
		 */
		@Deprecated
		public Builder setDrawableTheme(AppTheme theme) {
			product.iconTheme = theme;
			return this;
		}

		public Builder setTextViewTextColor(int color) {
			product.textViewTextColor = color;
			return this;
		}

		public Builder setDrawableForMenu(boolean forMenu) {
			product.isDrawableForMenu = forMenu;
			return this;
		}

		public ArrayAdapter<Integer> create() {
			return product;
		}
	}
}
