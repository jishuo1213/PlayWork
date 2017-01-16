package com.inspur.playwork.view.message.chat.emoji;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.EmojiHandler;

import java.util.ArrayList;

/**
 * Created by fan on 16-9-26.
 */
public class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {
    private static final String TAG = "EmojiAdapter";

    private ArrayList<String> emojiList;

    private int start, end;

    private RecyclerView recyclerView;

    private EmojiSelectListener listener;

    public interface EmojiSelectListener {
        void onEmojiClick(String emoji);

        void onBackspaceClick();
    }

    EmojiAdapter(RecyclerView view, int start, int end) {
        this.start = start;
        this.end = end;
        this.recyclerView = view;
        emojiList = EmojiHandler.getInstance().getEmojiList();
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.emoji_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(EmojiHandler.getInstance().getEmojiPanSpannableString(
                holder.textView.getContext(), getItem(position), (int) holder.textView.getTextSize()));
        holder.textView.setOnClickListener(emojiClickListener);
    }

    private String getItem(int position) {
        if (position <= (end - start)) {
            return emojiList.get(start + position);
        }
        return "[/:backspace]";
    }

    @Override
    public int getItemCount() {
        return end - start + 2;
    }

    public void setListener(EmojiSelectListener listener) {
        this.listener = listener;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView;
        }
    }

    private View.OnClickListener emojiClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int pos = recyclerView.getChildAdapterPosition(v);
            Log.i(TAG, "onClick: " + (listener == null));
            if (pos == end - start + 1) {
                if (listener != null)
                    listener.onBackspaceClick();
            } else {
                if (listener != null) {
                    listener.onEmojiClick(getItem(pos));
                }
            }
        }
    };
}
