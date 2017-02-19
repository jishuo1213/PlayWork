package com.inspur.playwork.view.timeline.addtask;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.UItoolKit;

import java.util.Calendar;

/**
 * Created by Fan on 15-11-23.
 */
public class AddCrossDayRootView extends LinearLayout {

    private static final String TAG = "AddCrossDayRootViewFan";

    private TextView taskStartTime;

    private TextView taskEndTime;

    private EditText inputTaskContent;

    private TaskBean taskBean;

    private long startTime;
    private long endTime;

    private Calendar currnetDay;

    private boolean isEditTime;

    private boolean isEditTask;

    private int taskType;

    private boolean hasEnter;

    private boolean isChoseStartTime = true;

    private AddCrossDayEventListener listener;

    private AddEditTaskResultListener editTaskResultListener;

    private int pos;

    private boolean isConfrim;

    /*缓存数据使用的*/
    private long startTimeCache, endTimeCache;

    private String subjectCache;

    public interface AddCrossDayEventListener {

        void onTaskStartTimeClick();

        void onTaskEndTimeClick();

        void onCancelAdd();

        void onInputContentClick();
    }

    public AddCrossDayRootView(Context context) {
        this(context, null);
    }

    public AddCrossDayRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddCrossDayRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(TaskBean taskBean, long selectedTime, boolean isEditTime, int pos) {

        Calendar selectedTime1 = Calendar.getInstance();
        currnetDay = Calendar.getInstance();
        DateUtils.trimCalendarDate(currnetDay);
        selectedTime1.setTimeInMillis(selectedTime);
        DateUtils.trimCalendarDate(selectedTime1);

        this.isEditTime = isEditTime;
        this.pos = pos;
        this.taskBean = taskBean;

        if (this.taskBean.isEmptyTask()) {
            taskType = TaskBean.CROSS_DAY_TASK_TIME_UNCLEAR;
            startTime = selectedTime1.getTimeInMillis();
            endTime = -1;
            isEditTask = false;
        } else {
            taskType = taskBean.taskType;
            startTimeCache = startTime = taskBean.startTime;
            endTimeCache = endTime = taskBean.endTime;
            subjectCache = taskBean.taskContent;
            isEditTask = true;
        }
    }

    public void setListener(AddCrossDayEventListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        taskStartTime = (TextView) findViewById(R.id.tv_task_start_time);
        inputTaskContent = (EditText) findViewById(R.id.edit_input_task_content);
        taskEndTime = (TextView) findViewById(R.id.tv_task_end_time);
        ImageButton confrimImgBtn = (ImageButton) findViewById(R.id.btn_confrim);
//        setOnClickListener(crossDayClickListener);
        confrimImgBtn.setOnClickListener(crossDayClickListener);
        taskStartTime.setOnClickListener(crossDayClickListener);
        inputTaskContent.setOnFocusChangeListener(contentFocusChangeListener);
        taskEndTime.setOnClickListener(crossDayClickListener);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        hasEnter = false;
        if (!isEditTime) {
            inputTaskContent.setFocusable(true);
            inputTaskContent.setFocusableInTouchMode(true);
            inputTaskContent.requestFocus();
            inputTaskContent.post(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(inputTaskContent);
                }
            });
        }

        inputTaskContent.setText("");

        if (isEditTask) {
            inputTaskContent.setText(taskBean.taskContent);
            inputTaskContent.setSelection(taskBean.taskContent.length());
        }

        ((KeyEventEditText) inputTaskContent).setListener(keyUpListener);

        inputTaskContent.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        if (!hasEnter) {
                            String text = inputTaskContent.getText().toString().trim();
                            if (TextUtils.isEmpty(text)) {
                                UItoolKit.showToastShort(getContext(), "任务内容不能为空");
                                return true;
                            }
                            clickConfirm();
                            hasEnter = true;
                        }
                        return true;
                }
                return false;
            }
        });
        setTimeText();
    }

    private OnFocusChangeListener contentFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                taskStartTime.setSelected(false);
                taskEndTime.setSelected(false);
                listener.onInputContentClick();
            }
        }
    };


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setEditTaskResultListener(AddEditTaskResultListener editTaskResultListener) {
        this.editTaskResultListener = editTaskResultListener;
    }

    private OnClickListener crossDayClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_task_start_time:
                    hideInputMethod();
                    isChoseStartTime = true;
                    inputTaskContent.clearFocus();
                    taskEndTime.setSelected(false);
                    v.setSelected(true);
                    listener.onTaskStartTimeClick();
                    break;
                case R.id.tv_task_end_time:
                    hideInputMethod();
                    inputTaskContent.clearFocus();
                    isChoseStartTime = false;
                    taskStartTime.setSelected(false);
                    v.setSelected(true);
                    listener.onTaskEndTimeClick();
                    break;
                case R.id.btn_confrim:
                    if (inputTaskContent.getText().toString().trim().length() > 0)
                        clickConfirm();
                    else
                        UItoolKit.showToastShort(getContext(), "内容不能为空");
                    break;
            }
        }
    };

    public void dismiss() {
        hideInputMethod();
        ViewGroup parent = (ViewGroup) getParent();
        isConfrim = false;
        if (parent != null) {
            parent.removeView(AddCrossDayRootView.this);
            setTag(false);
            listener.onCancelAdd();
        }
    }


    public void setTime(long time) {
        if (isChoseStartTime)
            startTime = time;
        else
            endTime = time;
        setTimeText();
    }

    private void setTimeText() {
        if (endTime == 0)
            endTime = -1;
        if (endTime == -1) {
            taskStartTime.setText(" " + DateUtils.getLongTimeDateText(startTime));
            taskEndTime.setText("     ?    ");
        } else {
            taskStartTime.setText(" " + DateUtils.getLongTimeDateText(startTime));
            taskEndTime.setText(DateUtils.getLongTimeDateText(endTime) + "");
        }
    }

    private void addEditTaskSuccess(String taskId) {
        isConfrim = false;
        if (!isEditTask) {
            UItoolKit.showToastShort(getContext(), "添加任务成功");
            taskBean.taskId = taskId;
            if (editTaskResultListener != null) {
                editTaskResultListener.onAddTaskResult(true, taskBean, pos);
            }
        } else {
            UItoolKit.showToastShort(getContext(), "编辑任务成功");
            if (editTaskResultListener != null)
                editTaskResultListener.onEditTaskResult(taskBean, pos);
        }
        dismiss();
    }

    public void addEditTaskResult(boolean result, String taskId) {
        if (result) {
            addEditTaskSuccess(taskId);
        } else {
            UItoolKit.showToastShort(getContext(), "添加任务失败");
            hasEnter = false;
            isConfrim = false;
        }
    }

    private void clickConfirm() {
        if (isConfrim)
            return;

        isConfrim = true;
        if (isEditTask && endTime == endTimeCache && startTimeCache == startTime && subjectCache.equals(inputTaskContent.getText().toString())) {
            dismiss();
            return;
        }

        if (endTime != -1 && endTime < currnetDay.getTimeInMillis()) {
            UItoolKit.showToastShort(getContext(), "结束时间不能在今天之前");
            endTime = -1;
            taskType = TaskBean.CROSS_DAY_TASK_TIME_UNCLEAR;
            setTimeText();
            return;
        }

        taskBean.taskContent = inputTaskContent.getText().toString();
        taskBean.startTime = startTime;
        taskBean.endTime = endTime;
        if (endTime > startTime) {
            taskType = TaskBean.CROSS_DAY_TASK;
        }

        taskBean.taskType = taskType;
        taskBean.unClearTime = -1;
        taskBean.taskCreator = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        taskBean.setTimeString();
        TimeLineStoresNew.getInstance().addCrossDayTask(taskBean);
//        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK, taskBean);
    }

    public void cancelClick() {
        if (inputTaskContent.getText().toString().trim().length() > 0) {
            clickConfirm();
        } else {
            dismiss();
        }
    }


    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, 0);
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputTaskContent.getWindowToken(), 0);
    }

    private KeyEventEditText.PreImeKeyListener keyUpListener = new KeyEventEditText.PreImeKeyListener() {
        @Override
        public void onKeyUp() {
            dismiss();
        }
    };
}
