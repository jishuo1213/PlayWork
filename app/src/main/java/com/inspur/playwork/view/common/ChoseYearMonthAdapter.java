package com.inspur.playwork.view.common;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.playwork.R;


/**
 * Created by Fan on 15-12-30.
 */
public class ChoseYearMonthAdapter extends RecyclerView.Adapter<ChoseYearMonthAdapter.ViewHolder> implements View.OnClickListener {

    public static final int YEAR_TYPE = 1;
    public static final int MONTH_TYPE = 2;

    private int count;
    private int firstNum;
    private int type;

    private ChoseTimeListener listener;

    private RecyclerView parent;

    @Override
    public void onClick(View v) {
        if (parent == null)
            parent = (RecyclerView) v.getParent();
        int pos = parent.getChildAdapterPosition(v);
        listener.onYearChose(firstNum + pos, type);
    }

    public interface ChoseTimeListener {
        void onYearChose(int year, int type);
    }

    public ChoseYearMonthAdapter(int count, int firstNum, int type) {
        this.count = count;
        this.firstNum = firstNum;
        this.type = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_single_textview, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (type == MONTH_TYPE)
            holder.textView.setText(firstNum + position + "月");
        else if (type == YEAR_TYPE)
            holder.textView.setText(firstNum + position + "年");
        holder.textView.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }

    public void setListener(ChoseTimeListener listener) {
        this.listener = listener;
    }
}
