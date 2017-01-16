package com.inspur.playwork.view.timeline.addtask;

import com.inspur.playwork.model.timeline.TaskBean;

/**
 * Created by Fan on 15-10-19.
 */
public interface AddEditTaskResultListener {
    void onAddTaskResult(boolean isSuccess, TaskBean taskBean, int pos);

    void onEditTaskResult(TaskBean taskBean, int pos);

    void onDismiss();
}
