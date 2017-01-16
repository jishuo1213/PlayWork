package com.inspur.playwork.model.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by fan on 16-8-2.
 */
public class SocketEvent implements Parcelable, Comparable<SocketEvent> {
    private static final String TAG = "SocketEvent";

    public String fbId;
    public int eventCode;
    public String eventInfo;
    public boolean isServerDelete;
    public boolean isClientProcess;
    public long reciveTime;

    protected SocketEvent(Parcel in) {
        fbId = in.readString();
        eventCode = in.readInt();
        eventInfo = in.readString();
        isServerDelete = in.readByte() != 0;
        isClientProcess = in.readByte() != 0;
        reciveTime = in.readLong();
    }

    public SocketEvent(String fbId, int eventCode, String eventInfo) {
        this.fbId = fbId;
        this.eventCode = eventCode;
        this.eventInfo = eventInfo;
        isServerDelete = false;
        isClientProcess = false;
        reciveTime = 0;
    }

    public static final Creator<SocketEvent> CREATOR = new Creator<SocketEvent>() {
        @Override
        public SocketEvent createFromParcel(Parcel in) {
            return new SocketEvent(in);
        }

        @Override
        public SocketEvent[] newArray(int size) {
            return new SocketEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fbId);
        dest.writeInt(eventCode);
        dest.writeString(eventInfo);
        dest.writeByte((byte) (isServerDelete ? 1 : 0));
        dest.writeByte((byte) (isClientProcess ? 1 : 0));
        dest.writeLong(reciveTime);
    }

    @Override
    public int compareTo(@NonNull SocketEvent another) {
        if (another.reciveTime > this.reciveTime) {
            return -1;
        } else if (another.reciveTime == this.reciveTime) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SocketEvent)
            return ((SocketEvent) o).fbId.equals(this.fbId);
        else
            return o instanceof String && o.equals(this.fbId);
    }

    @Override
    public String toString() {
        return "SocketEvent{" +
                "fbId='" + fbId + '\'' +
                ", eventCode=" + eventCode +
                ", eventInfo='" + eventInfo + '\'' +
                ", isServerDelete=" + isServerDelete +
                ", isClientProcess=" + isClientProcess +
                '}';
    }
}
