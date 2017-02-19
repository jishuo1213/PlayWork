package com.inspur.playwork.view.application.weekplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.model.weekplan.SimpleTaskBean;
import com.inspur.playwork.utils.DateUtils;

/**
 * Created by fan on 17-2-15.
 */
public class AddWeekPlanDialogFragment extends DialogFragment {
    private static final String TAG = "AddPlanFragmentFan";

    private static final String CLICK_TASK_BEAN = "click_bean";

    public interface AddTaskEventListener {
        SimpleTaskBean getNextTask();

        SimpleTaskBean getPreviousTask();

        void refreshTaskContent();
    }

    private SimpleTaskBean currentTaskBean;
    private AddTaskEventListener listener;
    private TextView dateView;
    private EditText taskContentView;
    private TextView taskContentTextView;

    public static AddWeekPlanDialogFragment getInstance(SimpleTaskBean taskBean) {
        AddWeekPlanDialogFragment fragment = new AddWeekPlanDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CLICK_TASK_BEAN, taskBean);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AddTaskEventListener) {
            listener = (AddTaskEventListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        currentTaskBean = getArguments().getParcelable(CLICK_TASK_BEAN);
        Dialog dialog = new Dialog(getActivity(), R.style.normal_dialog);
        View addTaskView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_week_plan_add_task, null);
        dialog.setContentView(addTaskView);
        initAddTaskView(addTaskView, currentTaskBean);
        return dialog;
    }

    int editType;

    private void initAddTaskView(View addTaskView, SimpleTaskBean taskBean) {
        Log.i(TAG, "initAddTaskView: " + taskBean.toString());
        dateView = (TextView) addTaskView.findViewById(R.id.tv_week_date);
        taskContentView = (EditText) addTaskView.findViewById(R.id.edit_task_content);
        taskContentTextView = (TextView) addTaskView.findViewById(R.id.text_task_content);
        editType = taskContentView.getInputType();
        final ImageButton previousBtn = (ImageButton) addTaskView.findViewById(R.id.htime_app_back);
        final ImageButton nextBtn = (ImageButton) addTaskView.findViewById(R.id.htime_app_forward);
        setUpViews(taskBean);
        previousBtn.setOnClickListener(addTaskNextListener);
        nextBtn.setOnClickListener(addTaskNextListener);
//        showKeyboard(editText);
        taskContentView.post(new Runnable() {
            @Override
            public void run() {
                showKeyboard(taskContentView);
            }
        });
        taskContentView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            addTaskNextListener.onClick(nextBtn);
                            return true;
                        case KeyEvent.KEYCODE_DEL:
                            Log.i(TAG, "onKey: back");
                            if (TextUtils.isEmpty(taskContentView.getText())) {
                                addTaskNextListener.onClick(previousBtn);
                                return true;
                            } else {
                                return false;
                            }
                    }
                }
                return false;
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void setUpViews(SimpleTaskBean taskBean) {
        dateView.setText(DateUtils.getCalendarDateWeek(taskBean.startTime) + (taskBean.unClearTime == TaskBean.NOON ? " 上午:" :
                (taskBean.unClearTime == TaskBean.AFTERNOON ? " 下午:" : " 晚上:")));
        Log.i(TAG, "setUpViews: " + taskBean.isCurrentUserTask());
        if (!TextUtils.isEmpty(taskBean.taskContent)) {
            if (taskBean.isCurrentUserTask()) {
                taskContentView.setText(taskBean.taskContent);
                taskContentView.setSelection(taskBean.taskContent.length());
                taskContentTextView.setVisibility(View.GONE);
                taskContentView.setVisibility(View.VISIBLE);
            } else {
                taskContentTextView.setText(taskBean.taskContent);
                taskContentView.setVisibility(View.GONE);
                taskContentTextView.setVisibility(View.VISIBLE);
            }
        } else {
            if (taskBean.isCurrentUserTask()) {
                taskContentView.setText("");
                taskContentTextView.setVisibility(View.GONE);
                taskContentView.setVisibility(View.VISIBLE);
            } else {
                taskContentTextView.setText("");
                taskContentView.setVisibility(View.GONE);
                taskContentTextView.setVisibility(View.VISIBLE);
            }
        }
    }


    private void showKeyboard(View v) {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    private void hideInputMethod() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive())
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private View.OnClickListener addTaskNextListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SimpleTaskBean taskBean = null;
            refreshTaskBean();
            switch (v.getId()) {
                case R.id.htime_app_back:
                    taskBean = listener.getPreviousTask();
                    break;
                case R.id.htime_app_forward:
                    taskBean = listener.getNextTask();
                    break;
            }
            currentTaskBean = taskBean;
            setUpViews(currentTaskBean);
        }
    };

    private void refreshTaskBean() {
        if (currentTaskBean.isCurrentUserTask()) {
            String newTask = taskContentView.getText().toString();
            if (!newTask.equals(currentTaskBean.taskContent)) {
                currentTaskBean.taskContent = newTask;
                if (currentTaskBean.isEmptyTask()) {
                    currentTaskBean.taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
                }
                Log.i(TAG, "onClick: refresh");
                listener.refreshTaskContent();
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        refreshTaskBean();
        super.onDismiss(dialog);
    }
}
