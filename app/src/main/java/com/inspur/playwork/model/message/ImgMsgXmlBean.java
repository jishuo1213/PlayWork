package com.inspur.playwork.model.message;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fan on 15-10-9.
 */
public class ImgMsgXmlBean implements Parcelable {
    public String src;
    public String id;

    public ImgMsgXmlBean() {
    }

    protected ImgMsgXmlBean(Parcel in) {
        src = in.readString();
        id = in.readString();
    }

    public static final Creator<ImgMsgXmlBean> CREATOR = new Creator<ImgMsgXmlBean>() {
        @Override
        public ImgMsgXmlBean createFromParcel(Parcel in) {
            return new ImgMsgXmlBean(in);
        }

        @Override
        public ImgMsgXmlBean[] newArray(int size) {
            return new ImgMsgXmlBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(src);
        dest.writeString(id);
    }
}
