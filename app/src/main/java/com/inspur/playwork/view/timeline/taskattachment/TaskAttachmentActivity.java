package com.inspur.playwork.view.timeline.taskattachment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.view.common.BaseActivity;

/**
 * Created by Fan on 15-9-18.
 */
public class TaskAttachmentActivity extends BaseActivity {

    public static final String TASK_BEAN = "taskBean";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_attachment);
        Fragment fragment = TaskAttachmentFragment.getInstance((TaskBean) getIntent().getParcelableExtra(TASK_BEAN));
        getFragmentManager().beginTransaction().add(R.id.task_attachment_container, fragment).commit();
    }
}
