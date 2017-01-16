package com.inspur.playwork.view.timeline;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;

/**
 * Created by Fan on 15-9-17.
 */
public class DeleteDialogFragment extends DialogFragment {


    private static final String TASK_BEAN = "taskBean";

    private TaskBean taskBean;


    public static DialogFragment getInstance(TaskBean taskBean) {
        Bundle args = new Bundle();
        args.putParcelable(TASK_BEAN, taskBean);
        DialogFragment dialogFragment = new DeleteDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        taskBean = getArguments().getParcelable(TASK_BEAN);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("退出任务");
        @SuppressLint("InflateParams")
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.layout_exit_or_delete_task, null);
        TextView deleteView = (TextView) v.findViewById(R.id.tv_delete_text);
        deleteView.setText("确定要退出" + taskBean.taskContent + "?");
        builder.setView(v);
        builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_QUIT_TASK, taskBean.taskId);
                TimeLineStoresNew.getInstance().quitTaskByTaskId(taskBean.taskId);
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

/*        if (taskBean.isCurrentUserTask()) {
            builder.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_DELETE_TASK, taskBean.taskId);
                }
            });
        }*/
        return builder.create();
    }
}
