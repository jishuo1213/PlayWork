package com.inspur.playwork.weiyou.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


public class ContactInputView extends ViewGroup {

    private static final String TAG = "FlowLayoutFan";


    public ContactInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(
            LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获得它的父容器为它设置的测量模式和大小
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);


        // 如果是warp_content情况下，记录宽和高
        //int width = 0;
        int height = 0;
        /**
         * 记录每一行的宽度，width不断取最大宽度
         */
        int lineWidth = 0;
        /**
         * 每一行的高度，累加至height
         */
        int lineHeight = 0;

        int cCount = getChildCount();

        // 遍历每个子元素
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            // 测量每一个child的宽和高
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            // 得到child的lp
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            // 当前子空间实际占据的宽度
            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            // 当前子空间实际占据的高度
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;
            /**
             * 如果加入当前child，则超出最大宽度，则的到目前最大宽度给width，类加height 然后开启新行
             */
            if (lineWidth + childWidth > sizeWidth) {
                //width = Math.max(lineWidth, childWidth);// 取最大的
                lineWidth = childWidth; // 重新开启新行，开始记录
                // 叠加当前高度，
                height += lineHeight;
                // 开启记录下一行的高度
                lineHeight = childHeight;
            } else {            // 否则累加值lineWidth,lineHeight取最大高度
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            // 如果是最后一个，则将当前记录的最大宽度和当前lineWidth做比较
            if (i == cCount - 1) {
                //width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension(sizeWidth, (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : height);
    }

    /**
     * 存储所有的View，按行记录
     */
    /**
     * 记录每一行的最大高度
     */

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int lineHeight = 0;
        // 存储每一行所有的childView
        int cCount = getChildCount();

        int currentLeft = 0, currentTop = 0;


        int width = getWidth();
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (currentLeft + childWidth + lp.rightMargin + lp.leftMargin > width) {
                currentLeft = l;
                currentTop = currentTop + lineHeight;
                int cl = currentLeft + lp.leftMargin;
                int ct = currentTop + lp.topMargin;
                int cr = cl + childWidth;
                int cb = ct + childHeight;
                child.layout(cl, ct, cr, cb);
                currentLeft = cr + lp.rightMargin;
                lineHeight = Math.max(lineHeight, childHeight + lp.bottomMargin + lp.topMargin);
            } else {
                int cl = currentLeft + lp.leftMargin;
                int ct = currentTop + lp.topMargin;
                int cr = cl + childWidth;
                int cb = ct + childHeight;
                child.layout(cl, ct, cr, cb);
                currentLeft = cr + lp.rightMargin;
                lineHeight = Math.max(lineHeight, childHeight + lp.bottomMargin + lp.topMargin);
            }
        }
    }

    @Override
    public void addView(View child) {
        Log.i(TAG, "addView");
        super.addView(child, getChildCount() - 1);
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
    }

    public View getLastView() {
        return getChildAt(getChildCount() - 2);
    }
}
