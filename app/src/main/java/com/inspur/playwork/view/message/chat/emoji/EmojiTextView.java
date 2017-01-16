package com.inspur.playwork.view.message.chat.emoji;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.inspur.playwork.utils.EmojiHandler;

/**
 * Created by fan on 16-9-23.
 */
public class EmojiTextView extends TextView {
    private static final String TAG = "EmojiTextView";

    public EmojiTextView(Context context) {
        super(context,null);
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs,0);
    }

    public EmojiTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setText(CharSequence text, BufferType type) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        EmojiHandler.getInstance().getEmojiSpannableString(getContext().getApplicationContext(), text, (int) getTextSize());
        super.setText(builder, type);
    }
}
