package com.uoscs09.theuos.widget.timetable;


import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.uoscs09.theuos.R;
import com.uoscs09.theuos.base.AbsListRemoteViewsFactory;
import com.uoscs09.theuos.tab.timetable.Subject;
import com.uoscs09.theuos.tab.timetable.TabTimeTableFragment2;
import com.uoscs09.theuos.tab.timetable.TimeTable;
import com.uoscs09.theuos.util.AppUtil;
import com.uoscs09.theuos.util.PrefUtil;
import com.uoscs09.theuos.util.StringUtil;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;

public abstract class WidgetTimeTableListService2 extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return getListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    protected abstract ListRemoteViewsFactory getListRemoteViewsFactory(Context context, Intent intent);

    protected static abstract class ListRemoteViewsFactory extends AbsListRemoteViewsFactory<Subject[]> {
        private TimeTable mTimeTable;
        private final int mAppWidgetId;
        private Hashtable<String, Integer> colorTable;
        private final int[] viewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_frame,
                R.id.widget_time_table_list_tue_frame,
                R.id.widget_time_table_list_wed_frame,
                R.id.widget_time_table_list_thr_frame,
                R.id.widget_time_table_list_fri_frame};
        private final int[] textViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon,
                R.id.widget_time_table_list_tue,
                R.id.widget_time_table_list_wed,
                R.id.widget_time_table_list_thr,
                R.id.widget_time_table_list_fri};
        private final int[] subViewIds = {
                R.id.widget_time_table_list_peroid,
                R.id.widget_time_table_list_mon_sub,
                R.id.widget_time_table_list_tue_sub,
                R.id.widget_time_table_list_wed_sub,
                R.id.widget_time_table_list_thr_sub,
                R.id.widget_time_table_list_fri_sub};

        private String[] periodTimeArray, buildingNames;

        public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
            super(applicationContext);
            this.mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            periodTimeArray = applicationContext.getResources().getStringArray(R.array.tab_timetable_timelist_only_time);
            buildingNames = applicationContext.getResources().getStringArray(R.array.building_names_univ);
            getData();
        }

        @Override
        public int getCount() {
            return PrefUtil.getInstance(getContext()).get(PrefUtil.KEY_TIMETABLE_LIMIT, false) ? mTimeTable.maxTime : super.getCount();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        protected abstract boolean isBigSize();

        @Override
        public RemoteViews getViewAt(int position) {
            Subject[] item = getItem(position);
            RemoteViews views = new RemoteViews(getContext().getPackageName(), isBigSize() ? R.layout.list_layout_widget_timetable_5x4 : R.layout.list_layout_widget_timetable_4x4);

            views.setTextViewText(viewIds[0], periodTimeArray[position]);

            if (colorTable == null) {
                getData();
            }

            Integer idx;

            int id, subId, j;
            for (int i = 1; i < viewIds.length; i++) {
                j = i - 1;
                id = textViewIds[i];
                subId = subViewIds[i];
                views.setTextColor(id, Color.WHITE);
                views.setTextColor(subId, Color.WHITE);

                Subject subject = item[j];
                // 현재 표시하려는 과목과 리스트뷰의 한 단계 위의 과목의 이름이 같으면
                // 내용을 표시하지 않음
                if (subject.isEqualToUpperPeriod) {
                    views.setTextViewText(id, StringUtil.NULL);
                    views.setTextViewText(subId, StringUtil.NULL);

                } else {

                    if (Locale.getDefault().equals(Locale.KOREA)) {
                        views.setTextViewText(id, subject.subjectName);

                    } else {
                        views.setTextViewText(id, subject.subjectNameEng);
                    }

                    int buildingCode = subject.buildingCode - 1;
                    if (buildingCode > -1 && buildingNames.length > buildingCode)
                        views.setTextViewText(subId, buildingNames[buildingCode] + StringUtil.NEW_LINE + subject.building);
                    else
                        views.setTextViewText(subId, StringUtil.NULL);
                }

                idx = colorTable.get(item[j].subjectName);
                views.setInt(viewIds[i], "setBackgroundColor", idx != null ? AppUtil.getTimeTableColor(getContext(), idx) : 0);

            }

            // 오늘 날짜의 과목의 글자색을 검은색으로 줌
            int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            if (day > 0 && day < textViewIds.length) {
                views.setTextColor(textViewIds[day], Color.BLACK);
                views.setTextColor(subViewIds[day], Color.BLACK);
            }

            // 위젯 날짜가 시스템 날짜와 다르면 위젯을 업데이트 하라고 broadcast함
            PrefUtil pref = PrefUtil.getInstance(getContext());
            if (pref.get(TimeTableWidget.WIDGET_TIMETABLE_DAY, 0) != day) {
                pref.put(TimeTableWidget.WIDGET_TIMETABLE_DAY, day);

                getContext().sendBroadcast(new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId));
            }
            return views;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            getData();
        }

        @Override
        public void onDestroy() {
        }

        private void getData() {
            mTimeTable = TabTimeTableFragment2.readTimetable(getContext());
            clear();
            if (mTimeTable != null) {
                addAll(0, mTimeTable.subjects);
                colorTable = TabTimeTableFragment2.getColorTable(mTimeTable, getContext());
            }
        }

    }
}
