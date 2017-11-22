package com.tyc129.nfcmap.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Code on 2017/10/21 0021.
 *
 * @author 谈永成
 * @version 1.0
 */
public class DisplayItem implements Parcelable {
    private String id;
    private String tag;
    private Bitmap header;
    private String content;

    public String getId() {
        return id;
    }

    public DisplayItem setId(@NonNull String id) {
        this.id = id;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public DisplayItem setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Bitmap getHeader() {
        return header;
    }

    public DisplayItem setHeader(Bitmap header) {
        this.header = header;
        return this;
    }

    public String getContent() {
        return content;
    }

    public DisplayItem setContent(String content) {
        this.content = content;
        return this;
    }

    public static final Parcelable.Creator<DisplayItem> CREATOR = new Creator<DisplayItem>() {
        @Override
        public DisplayItem createFromParcel(Parcel parcel) {
            DisplayItem item = new DisplayItem();
            item.setId(parcel.readString());
            item.setTag(parcel.readString());
            item.setHeader(Bitmap.CREATOR.createFromParcel(parcel));
            item.setContent(parcel.readString());
            return item;
        }

        @Override
        public DisplayItem[] newArray(int i) {
            return new DisplayItem[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getId());
        parcel.writeString(getTag());
        parcel.writeParcelable(getHeader(), PARCELABLE_WRITE_RETURN_VALUE);
        parcel.writeString(getContent());
    }
}
