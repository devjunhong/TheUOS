package com.uoscs09.theuos.tab.anounce;

import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.common.impl.AbsArrayAdapter;

public class AnounceAdapter extends AbsArrayAdapter<AnounceItem> {
	private AnounceAdapter(Context context) {
		super(context, 0);
	}

	public AnounceAdapter(Context context, int layout, List<AnounceItem> list) {
		super(context, layout, list);
	}

	/** <내용> 형식의 공지사항을 제대로 표시하기 위해 설정한 패턴 */
	private static final Pattern HTML_PATTERN = Pattern
			.compile(".*<[[a-z][A-Z][0-9]]+>.*");

	@Override
	public View setView(int position, View v, ViewHolder holder) {
		String[] array = getItem(position).toStringArray();
		Holder h = (Holder) holder;
		int i = 0;
		for (TextView tv : h.textArray) {
			Spanned span = null;
			String content = array[i++];

			// HTML로 표현되어야 할 문자열을 HTML로 표현한다.
			// <>이 포함된 문자열이지만 HTML이 아닌것은 Patten으로 거른다.
			if (HTML_PATTERN.matcher(content).find()) {
				span = Html.fromHtml(content);
				tv.setText(span != null ? span : content);
			} else {
				tv.setText(content);
			}
		}
		return v;
	}

	@Override
	public ViewHolder getViewHolder(View v) {
		return new Holder(v);
	}

	protected static class Holder implements ViewHolder {
		public TextView[] textArray;

		public Holder(View v) {
			textArray = new TextView[3];
			textArray[0] = (TextView) v
					.findViewById(R.id.tab_anounce_list_text_type);
			textArray[1] = (TextView) v
					.findViewById(R.id.tab_anounce_list_text_title);
			textArray[2] = (TextView) v
					.findViewById(R.id.tab_anounce_list_text_date);
		}
	}
}
