package com.uoscs09.theuos.tab.schedule;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class ScheduleItemWrapper implements Parcelable {
	public ArrayList<ScheduleItem> scheduleList;
	public ArrayList<BoardItem> boardList;

	public ScheduleItemWrapper() {
		scheduleList = new ArrayList<ScheduleItem>();
		boardList = new ArrayList<BoardItem>();
	}

	public ScheduleItemWrapper(ArrayList<ScheduleItem> scheduleList,
			ArrayList<BoardItem> boardList) {
		this.scheduleList = scheduleList;
		this.boardList = boardList;
	}

	protected ScheduleItemWrapper(Parcel in) {
		this();
		in.readList(boardList, BoardItem.class.getClassLoader());
		in.readList(scheduleList, ScheduleItem.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(boardList);
		dest.writeList(scheduleList);
	}

	public static final Creator<ScheduleItemWrapper> CREATOR = new Creator<ScheduleItemWrapper>() {

		@Override
		public ScheduleItemWrapper createFromParcel(Parcel source) {
			return new ScheduleItemWrapper(source);
		}

		@Override
		public ScheduleItemWrapper[] newArray(int size) {
			return new ScheduleItemWrapper[size];
		}

	};
}
