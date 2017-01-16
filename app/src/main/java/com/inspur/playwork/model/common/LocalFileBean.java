package com.inspur.playwork.model.common;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.view.common.pulltorefresh.BaseRefreshView;

import java.io.File;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by fan on 16-3-28.
 */
public class LocalFileBean implements Parcelable, Comparable<LocalFileBean> {

    private static final String TAG = "LocalFileBeanFan";

    private static final String TEXT_TYPE = "text";
    private static final String IMAGE_TYPE = "image";
    private static final String AUDIO_TYPE = "audio";
    private static final String VIDEO_TYPE = "video";
    private static final String APPLICATION_TYPE = "application";

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    public static final int ROOT_DIR = 0;
    public static final int NORMAL_DIR = 1;
    public static final int NORMAL_FILE = 2;

    public static final int IMAGE = 0;
    public static final int AUDIO = 1;
    public static final int VIDEO = 2;
    public static final int TXT = 3;
    public static final int PDF = 4;
    public static final int APK = 5;
    public static final int ZIP = 6;
    public static final int OTHERS = 7;

    public String currentPath;//此文件的当前路径,这是一个全路径
    public String name;//文件名,显示用的名字
    public boolean isDir;
    public boolean isRoot;
    public String parentPath;
    public long createTime;
    public long size;
    public int fileType = -1;

    public LocalFileBean(boolean isRoot, String currentPath) {
        this.isRoot = isRoot;
        this.currentPath = currentPath;
        File file = new File(currentPath);
        if (isRoot) {
            parentPath = null;
            name = currentPath;
        } else {
            parentPath = file.getParent();
        }
        name = file.getName();
        isDir = file.isDirectory();
        Log.i(TAG, "LocalFileBean: " + currentPath + isDir);
        if (!isDir) {
            size = file.length();
            createTime = file.lastModified();
            String mimeType = FileUtil.getMimeType(Uri.fromFile(file).toString());
            if (mimeType == null) {
                fileType = OTHERS;
                return;
            }
            String[] sqliteMimeType = mimeType.split("/");
            Log.i(TAG, "LocalFileBean: " + sqliteMimeType[0] + "====" + sqliteMimeType[1]);
            switch (sqliteMimeType[0]) {
                case TEXT_TYPE:
                    fileType = TXT;
                    break;
                case IMAGE_TYPE:
                    fileType = IMAGE;
                    break;
                case AUDIO_TYPE:
                    fileType = AUDIO;
                    break;
                case VIDEO_TYPE:
                    fileType = VIDEO;
                    break;
                case APPLICATION_TYPE:
                    String fileType = FileUtil.getFileType(mimeType);
                    switch (fileType) {
                        case "apk":
                            this.fileType = APK;
                            break;
                        case "pdf":
                            this.fileType = PDF;
                            break;
                        case "zip":
                            this.fileType = ZIP;
                            break;
                        default:
                            this.fileType = OTHERS;
                            break;
                    }
                    break;
            }
        }
    }

    private LocalFileBean(Parcel in) {
        currentPath = in.readString();
        isDir = in.readByte() != 0;
        isRoot = in.readByte() != 0;
        parentPath = in.readString();
        size = in.readLong();
        createTime = in.readLong();
        name = in.readString();
        fileType = in.readInt();
    }

    public static final Creator<LocalFileBean> CREATOR = new Creator<LocalFileBean>() {
        @Override
        public LocalFileBean createFromParcel(Parcel in) {
            return new LocalFileBean(in);
        }

        @Override
        public LocalFileBean[] newArray(int size) {
            return new LocalFileBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currentPath);
        dest.writeByte((byte) (isDir ? 1 : 0));
        dest.writeByte((byte) (isRoot ? 1 : 0));
        dest.writeString(parentPath);
        dest.writeLong(size);
        dest.writeLong(createTime);
        dest.writeString(name);
        dest.writeInt(fileType);
    }

    public ArrayList<LocalFileBean> getChildFiles() {
        File file = new File(currentPath);
        if (!isDir)
            return null;
        Log.i(TAG, "getChildFiles: " + currentPath);
        String[] childFiles = file.list(fileFilter);
        ArrayList<LocalFileBean> localFiles = new ArrayList<>();
        if (childFiles.length > 0) {
            for (String path : childFiles) {
                if (!currentPath.endsWith(File.separator)) {
                    localFiles.add(new LocalFileBean(false, currentPath + File.separator + path));
                } else {
                    localFiles.add(new LocalFileBean(false, currentPath + path));
                }
            }
        }
        Collections.sort(localFiles);
        return localFiles;
    }

    public String getFileSizeStr() {
        float result;
        if (size <= KB) {
            return size + "B";
        } else if (size < MB) {
            result = size / KB;
            return result + "KB";
        } else if (size < GB) {
            result = size / MB;
            return result + "MB";
        } else {
            result = size / GB;
            return result + "GB";
        }
    }

    private static FilenameFilter fileFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return !filename.startsWith(".");
        }
    };

    @Override
    public int compareTo(@NonNull LocalFileBean another) {
        if (isDir == another.isDir) {
            return name.compareToIgnoreCase(another.name);
        } else {
            return isDir ? -1 : 1;
        }
    }

    @Override
    public String toString() {
        return "LocalFileBean{" +
                "currentPath='" + currentPath + '\'' +
                ", name='" + name + '\'' +
                ", isDir=" + isDir +
                ", isRoot=" + isRoot +
                ", parentPath='" + parentPath + '\'' +
                ", createTime=" + createTime +
                ", size=" + size +
                ", fileType=" + fileType +
                '}';
    }
}
