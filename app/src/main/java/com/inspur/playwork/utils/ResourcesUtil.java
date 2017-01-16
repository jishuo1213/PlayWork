package com.inspur.playwork.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Created by Fan on 15-9-16.
 */
public class ResourcesUtil {

    //  private static ResourcesUtil ourInstance = new ResourcesUtil();

    private Resources resources;
    private float scale;

    public static ResourcesUtil getInstance() {
        return SingleRefreshManager.getInstance().getResourcesUtil();
    }

    public ResourcesUtil() {
    }

    public void init(Context context) {
        resources = context.getResources();
        scale = context.getResources().getDisplayMetrics().density;
    }

    public int getColor(int id) {
        return resources.getColor(id);
    }

    public Drawable getDrawable(int id) {
        return resources.getDrawable(id, null);
    }

    public int[] getIntArray(int id) {
        return resources.getIntArray(id);
    }

    public int dpToPx(int dp) {
        return (int) (dp * scale + 0.5f);
    }

    public int dpToPx(float dp) {
        return (int) (dp * scale + 0.5f);
    }

    public void clean() {
        resources = null;
    }
}
