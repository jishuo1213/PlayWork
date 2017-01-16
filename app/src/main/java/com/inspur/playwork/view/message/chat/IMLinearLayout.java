package com.inspur.playwork.view.message.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

/**
 * Created by Fan on 16-2-17.
 */
public class IMLinearLayout extends LinearLayout {

    private static final String TAG = "IMLinearLayout";

    private InputMethodListener listener;

    public interface InputMethodListener {
        void onSizeChange(int w, int h, int oldw, int oldh);
    }

    public IMLinearLayout(Context context) {
        super(context);
    }

    public IMLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IMLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(InputMethodListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged() called with: " + "w = [" + w + "], h = [" + h + "], oldw = [" + oldw + "], oldh = [" + oldh + "]");
        if (listener != null) {
            listener.onSizeChange(w, h, oldw, oldh);
        }
    }
}
