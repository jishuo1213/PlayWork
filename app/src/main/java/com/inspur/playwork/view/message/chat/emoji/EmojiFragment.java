package com.inspur.playwork.view.message.chat.emoji;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.R;
import com.inspur.playwork.utils.PLog;

/**
 * Created by fan on 16-9-26.
 */
public class EmojiFragment extends Fragment {
    private static final String TAG = "EmojiFragment";

    private static final String START_POS = "start_pos";
    private static final String END_POS = "end_pos";

    private int start, end;

    private EmojiAdapter.EmojiSelectListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null)
            listener = (EmojiAdapter.EmojiSelectListener) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null) {
            listener = (EmojiAdapter.EmojiSelectListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public static Fragment getInstance(int start, int end) {
        PLog.i(TAG, "getInstance: start:" + start + "end:" + end);
        Fragment fragment = new EmojiFragment();
        Bundle args = new Bundle();
        args.putInt(START_POS, start);
        args.putInt(END_POS, end);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        start = arg.getInt(START_POS);
        end = arg.getInt(END_POS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.emoji_layout, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recy_emoji);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 7));
        EmojiAdapter adapter = new EmojiAdapter(recyclerView, start, end);
        adapter.setListener(listener);
        recyclerView.setAdapter(adapter);
    }
}
