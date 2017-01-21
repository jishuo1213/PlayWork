package com.inspur.playwork.utils;


import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.EditText;

import com.inspur.playwork.R;
import com.inspur.playwork.view.message.chat.emoji.EmojiFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fan on 16-9-23.
 */
public class EmojiHandler {
    private static final String TAG = "EmojiHandler";

    private static EmojiHandler ourInstance = new EmojiHandler();

    public static EmojiHandler getInstance() {
        return ourInstance;
    }

    private ArrayMap<String, String> emojiMap;

    private ArrayList<String> emojiNameList;

//    private ArrayMap<String, ImageSpan> imageSpanMap;

    private Pattern patten;

    private EmojiHandler() {
        emojiMap = new ArrayMap<>();
        emojiNameList = new ArrayList<>();
//        imageSpanMap = new ArrayMap<>();
        patten = Pattern.compile("\\[[/][:][^]]+\\]", Pattern.MULTILINE);
    }

    public void initEmjiMap(Context context) {
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(context.getResources().openRawResource(R.raw.encode_mapping));
        JSONObject jsonObject = null;
        char[] buffer = new char[1024 * 4];
        try {
            int count;
            while ((count = reader.read(buffer)) > 0) {
                builder.append(buffer, 0, count);
            }
            reader.close();
            jsonObject = new JSONObject(builder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if (jsonObject != null) {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Log.i(TAG, "initEmjiMap: " + key);
                JSONObject emoji = jsonObject.optJSONObject(key);
                Log.i(TAG, "initEmjiMap: " + emoji.optString("name"));
                emojiMap.put("[" + key + "]", emoji.optString("name"));
                emojiNameList.add("[" + key + "]");
            }
        }

        Collections.sort(emojiNameList);
        emojiMap.put("[/:backspace]", "backspace");
    }

    public ArrayList<String> getEmojiList() {
        return emojiNameList;
    }

    public SpannableString getEmojiSpannableString(Context context, CharSequence str, int size) {
        SpannableString builder = new SpannableString(str);
        Matcher matcher = patten.matcher(builder);
        while (matcher.find()) {
            String key = matcher.group();
            if (!emojiMap.containsKey(key)) {
                continue;
            }
            ImageSpan imageSpan;
            int end = matcher.start() + key.length();
            imageSpan = getImageSpan(context, size, key);
            builder.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public SpannableString getEmojiPanSpannableString(Context context, CharSequence str, int size) {
        SpannableString builder = new SpannableString(str);
        Matcher matcher = patten.matcher(builder);
        while (matcher.find()) {
            Drawable emoji;
            String key = matcher.group();
            if (!emojiMap.containsKey(key)) {
                continue;
            }
            emoji = getEmojiDrawable(context.getApplicationContext(), key, size);
            int end = matcher.start() + key.length();
            ImageSpan imageSpan = new ImageSpan(emoji);
            builder.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }


    private ImageSpan getImageSpan(Context context, int size, String key) {
        Drawable emoji;
        ImageSpan imageSpan;
//        if (!imageSpanMap.containsKey(key)) {
        emoji = getEmojiDrawable(context.getApplicationContext(), key, size);
        imageSpan = new ImageSpan(emoji);
//            imageSpanMap.put(key, imageSpan);
//        } else {
//            imageSpan = imageSpanMap.get(key);
//        }
        return imageSpan;
    }

    private Drawable getEmojiDrawable(Context context, String key, int size) {
        Drawable emoji;
        int resId = context.getResources().getIdentifier(emojiMap.get(key), "drawable",
                context.getPackageName());
        if (Build.VERSION.SDK_INT < 22)
            emoji = context.getResources().getDrawable(resId);
        else
            emoji = context.getResources().getDrawable(resId, context.getTheme());
        emoji.setBounds(0, 0, size, size);
        return emoji;
    }

    public void appendEmoji(EditText editText, String emojiStr, int size) {
        editText.append(emojiStr);
//        Drawable emoji = getEmojiDrawable(editText.getContext().getApplicationContext(), emojiStr, size);
        Spannable text = editText.getText();
        ImageSpan imageSpan = getImageSpan(editText.getContext().getApplicationContext(), size, emojiStr);
        text.setSpan(imageSpan, text.length() - emojiStr.length(), text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        editText.setText(text);
    }

    public boolean hasEmoji(String str) {
        Matcher matcher = patten.matcher(str);
        return matcher.find();
    }


    public ArrayList<Fragment> getEmojiFragmentList() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        int start = 0;
        int precount = 27;
        int count = emojiNameList.size();
        for (; start < count; ) {
            int end = start + precount - 1;
            if (end > count - 1) {
                end = count - 1;
            }
            EmojiFragment fragment = (EmojiFragment) EmojiFragment.getInstance(start, end);
            fragments.add(fragment);
            start += precount;
        }
        return fragments;
    }

    public String replaceEmoji(String emojiContent) {
        return emojiContent.replaceAll("\\[[/][:][^]]+\\]", "[表情]");
    }

    public void clear() {
        emojiMap.clear();
        emojiNameList.clear();
//        imageSpanMap.clear();
    }

    public void clearSpans() {
//        imageSpanMap.clear();
    }
}
