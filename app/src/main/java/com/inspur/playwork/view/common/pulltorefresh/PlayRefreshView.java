package com.inspur.playwork.view.common.pulltorefresh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.inspur.playwork.utils.DeviceUtil;

/**
 * Created by Fan on 15-9-18.
 */
public class PlayRefreshView extends BaseRefreshView{

    private static final float SCALE_START_PERCENT = 0.5f;
    private static final int ANIMATION_DURATION = 1000;

    private final static float SKY_RATIO = 0.65f;
    private static final float SKY_INITIAL_SCALE = 1.05f;

    private final static float TOWN_RATIO = 0.22f;
    private static final float TOWN_INITIAL_SCALE = 1.20f;
    private static final float TOWN_FINAL_SCALE = 1.30f;

    private static final float SUN_FINAL_SCALE = 0.75f;
    private static final float SUN_INITIAL_ROTATE_GROWTH = 1.2f;
    private static final float SUN_FINAL_ROTATE_GROWTH = 1.5f;


    private PullToRefreshView parent;
    private Matrix mMatrix;

    private Animation animation;

    private int screenWidth;

    private int skyHeight;
    private float skyTopOffSet;
    private float skyMoveOffSet;



    public PlayRefreshView(Context context, PullToRefreshView layout) {
        super(context, layout);
        this.parent = layout;
        mMatrix = new Matrix();

        setUpAnimation();

        parent.post(new Runnable() {
            @Override
            public void run() {
                initDimens(parent.getWidth());
            }
        });
    }

    private void initDimens(int width) {
        if(width < 0 || width == screenWidth) return;

        screenWidth = width;

        skyHeight = (int) (SKY_RATIO * screenWidth);
        skyTopOffSet = (skyHeight * 0.38f);
        skyMoveOffSet = DeviceUtil.dpTopx(getContext(), 15);


    }


    @Override
    public void draw(Canvas canvas) {

    }



    @Override
    public void setPercent(float percent, boolean invalidate) {

    }

    @Override
    public void offsetTopAndBottom(int offset) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    private void setUpAnimation(){
        animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

            }
        };

        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatCount(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1000);
    }
}
