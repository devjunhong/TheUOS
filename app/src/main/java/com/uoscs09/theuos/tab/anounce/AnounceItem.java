package com.uoscs09.theuos.tab.anounce;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos.common.util.StringUtil;

public class AnounceItem implements Parcelable {
	public String type;
	public String title;
	public String date;
	public String onClickString;

	public AnounceItem() {
		type = title = date = onClickString = StringUtil.NULL;
	}

	public AnounceItem(String type, String title, String date,
			String onClickString) {
		this.type = type;
		this.date = date;
		this.title = title;
		this.onClickString = onClickString;
	}

	private AnounceItem(Parcel source) {
		type = source.readString();
		title = source.readString();
		date = source.readString();
		onClickString = source.readString();
	}

	/**
	 * type, title, date, onClinkString 순으로 이루어진 StringArray를 반환한다.
	 * 
	 * @since 2.31
	 */
	public String[] toStringArray() {
		return new String[] { type, title, date, onClickString };
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(title);
		dest.writeString(date);
		dest.writeString(onClickString);
	}

	public static final Parcelable.Creator<AnounceItem> CREATOR = new Parcelable.Creator<AnounceItem>() {

		@Override
		public AnounceItem[] newArray(int size) {
			return new AnounceItem[size];
		}

		@Override
		public AnounceItem createFromParcel(Parcel source) {
			return new AnounceItem(source);
		}
	};
}
