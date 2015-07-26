package com.uoscs09.theuos2.tab.libraryseat;

import android.os.Parcel;
import android.os.Parcelable;

import com.uoscs09.theuos2.util.StringUtil;

import java.io.Serializable;

public class SeatItem implements Parcelable, Serializable {
    private static final long serialVersionUID = 2517523347755809098L;
    public String roomName;
    public String occupySeat;
    public String vacancySeat;
    public String utilizationRate;
    public int index;

    public SeatItem() {
        roomName = occupySeat = vacancySeat = utilizationRate = StringUtil.NULL;
    }

    /*
    public SeatItem(String name, String occupySeat, String vacancySeat, String utilizationRate, int index) {
        this.roomName = name.trim();
        this.occupySeat = occupySeat.trim();
        this.vacancySeat = vacancySeat.trim();
        this.utilizationRate = utilizationRate.trim();
        this.index = index;
    }
    */

    protected SeatItem(Parcel source) {
        roomName = source.readString();
        occupySeat = source.readString();
        vacancySeat = source.readString();
        utilizationRate = source.readString();
        index = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.roomName);
        dest.writeString(this.occupySeat);
        dest.writeString(this.vacancySeat);
        dest.writeString(this.utilizationRate);
        dest.writeInt(this.index);
    }

    public static final Parcelable.Creator<SeatItem> CREATOR = new Parcelable.Creator<SeatItem>() {

        @Override
        public SeatItem createFromParcel(Parcel source) {
            return new SeatItem(source);
        }

        @Override
        public SeatItem[] newArray(int size) {
            return new SeatItem[size];
        }
    };
}
