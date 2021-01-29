package com.project.hmsscan.custom;

import android.os.Parcel;
import android.os.Parcelable;

public final class ScanRect implements Parcelable {

    private int left;
    private int top;
    private int right;
    private int bottom;

    public static final Creator<ScanRect> CREATOR = new Creator<ScanRect>() {
        @Override
        public ScanRect createFromParcel(Parcel in) {
            return new ScanRect(in);
        }

        @Override
        public ScanRect[] newArray(int size) {
            return new ScanRect[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.left);
        parcel.writeInt(this.top);
        parcel.writeInt(this.right);
        parcel.writeInt(this.bottom);
    }

    public int describeContents() {
        return 0;
    }

    public final int getLeft() {
        return this.left;
    }

    public final int getTop() {
        return this.top;
    }

    public final int getRight() {
        return this.right;
    }

    public final int getBottom() {
        return this.bottom;
    }

    public ScanRect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public ScanRect(Parcel parcel) {
        this(parcel.readInt(), parcel.readInt(), parcel.readInt(), parcel.readInt());
    }
}