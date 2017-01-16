package com.inspur.playwork.view.message.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * 行之间有分割线的EditText
 */
public class DividerEditText extends EditText {

    private final int padding = 20;
    private Rect mRect;
    private Paint mPaint;

    public DividerEditText(Context context) {
        super(context);
        this.init();
    }

    public DividerEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public DividerEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        mRect = new Rect();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(1);
        mPaint.setAntiAlias(true);
        PathEffect pathEffect = new DashPathEffect(new float[]{1, 2, 4, 8}, 1);
        mPaint.setPathEffect(pathEffect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int count = getLineCount();
        int lineHeight = 0;
        int i = 0;
        int countHeight;
        if (count == 1) {
            lineHeight = getLineBounds(0, mRect) + padding;
            countHeight = lineHeight * 5;
        } else if(count > 1 && count <5) {
            while (i < count) {
                lineHeight = getLineBounds(i, mRect) + padding;
                i++;
            }
            countHeight = lineHeight + ((getLineBounds(1, mRect) - getLineBounds(0, mRect)) * (5 - count));
        } else {
            while (i < count) {
                lineHeight = getLineBounds(i, mRect) + padding;
                i++;
            }
            countHeight = lineHeight;
        }
        setMeasuredDimension(width, countHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int count = getLineCount();
        int lineHeight = 0;
        int i = 0;
        while (i < count) {
            lineHeight = getLineBounds(i, mRect) + padding;
            canvas.drawLine(mRect.left, lineHeight, mRect.right, lineHeight, mPaint);
            i++;
        }
        int maxLines = 5;
        int avgHeight = lineHeight / count;
        int currentLineHeight = lineHeight;

        while(i < maxLines){
            currentLineHeight = currentLineHeight + avgHeight;
            canvas.drawLine(mRect.left, currentLineHeight, mRect.right, currentLineHeight, mPaint);
            i++;
        }
        super.onDraw(canvas);
    }
}
