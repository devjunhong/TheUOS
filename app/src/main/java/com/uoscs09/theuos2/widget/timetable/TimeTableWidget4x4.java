package com.uoscs09.theuos2.widget.timetable;

import android.content.Context;
import android.widget.RemoteViews;

import com.uoscs09.theuos2.R;

public class TimeTableWidget4x4 extends TimeTableWidget {

	@Override
	protected synchronized RemoteViews getRemoteViews(Context context) {
		return new RemoteViews(context.getPackageName(),
					R.layout.widget_timetable_4x4);
	}

	@Override
	protected Class<? extends WidgetTimeTableListService2> getListServiceClass() {
		return Widget4x4ListService.class;
	}

	@Override
	protected Class<? extends TimeTableWidget> getWidgetClass() {
		return this.getClass();
	}
}