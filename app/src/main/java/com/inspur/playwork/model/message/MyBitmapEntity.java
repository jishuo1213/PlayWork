package com.inspur.playwork.model.message;

/**
 * 九宫格图片坐标类
 *
 * @author 笑面V客(bugcode@foxmail.com)
 */
public class MyBitmapEntity {
    public float x;
    public float y;
    public float width;
    public float height;
    public static int devide = 1;
    public int index = -1;

    @Override
    public String toString() {
        return "MyBitmap [x=" + x + ", y=" + y + ", width=" + width
                + ", height=" + height + ", devide=" + devide + ", index="
                + index + "]";
    }
}
