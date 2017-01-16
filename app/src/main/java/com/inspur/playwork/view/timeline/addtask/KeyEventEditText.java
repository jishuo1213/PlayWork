package com.inspur.playwork.view.timeline.addtask;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Fan on 15-12-22.
 */
public class KeyEventEditText extends EditText {

    public interface PreImeKeyListener {
        void onKeyUp();
    }

    private PreImeKeyListener listener;

    public KeyEventEditText(Context context) {
        super(context);
    }

    public KeyEventEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyEventEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(PreImeKeyListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                listener.onKeyUp();
                return true;
            } else {
                return super.onKeyPreIme(keyCode, event);
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }
}
