package com.inspur.playwork.view.timeline;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;

/**
 * Created by Fan on 15-9-7.
 */
public class TaskRecyclerItemTouchCallBack extends ItemTouchHelper.Callback {


    private static final String TAG = "TaskRecyclerCallBackFan";

    public interface ItemTouchHelperAdapter {
        void onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target);

        void onDropItem(RecyclerView.ViewHolder viewHolder);
    }

    private ItemTouchHelperAdapter itemTouchHelperAdapter;

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipFlags = 0;
        return makeMovementFlags(dragFlags, swipFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (itemTouchHelperAdapter != null) {
            itemTouchHelperAdapter.onItemMove(viewHolder, target);
            return true;
        }
        return false;
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    public void setItemTouchHelperAdapter(ItemTouchHelperAdapter itemTouchHelperAdapter) {
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        itemTouchHelperAdapter.onDropItem(viewHolder);
    }

    public void clearListener(){
        itemTouchHelperAdapter = null;
    }
}
