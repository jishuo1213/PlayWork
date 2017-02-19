package com.inspur.playwork.view.timeline.addtask;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.stores.timeline.TimeLineStoresNew;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.UItoolKit;

import java.util.Calendar;

/**
 * Created by Fan on 15-12-8.
 */
public class AddTodayRootView extends RelativeLayout {

    private static final String TAG = "AddTodayRootViewFan";

    private TaskBean taskBean;

    private EditText taskTime, inputTaskCount;
    // private ImageButton confirmButton;

    private View unClearTimeRootView, timeLineRootView;
    private TextView noon, afterNoon, night;

    private long startTime, endTime;

    private Calendar selectedTime;

    private int taskType, unClearTime, pos,
            startHour = -1, endHour = -1, startMinute = -1, endMinute = -1,
            screenHeight, softInputMethodHeight;

    private boolean isTimeFromClick, isEditTime, isEditTask, isFirstInputTaskTime, hasEnter = false, isConfrim = false;
    private AddEditTaskResultListener listener;

    private Handler handler;

    /*缓存数据使用的*/
    private long startTimeCache, endTimeCache;

    private String subjectCache;

    public AddTodayRootView(Context context) {
        this(context, null);
    }

    public AddTodayRootView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddTodayRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData(context);
    }

    private void initData(Context context) {
        handler = new Handler();
        screenHeight = DeviceUtil.getDeviceScreenHeight(context);
        softInputMethodHeight = PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        unClearTimeRootView = null;
        if (isEditTask) {
            if (taskType == TaskBean.TODAY_TASK)
                taskTime.setText(taskBean.startTimeString + " - " + taskBean.endTimeString);
            else
                taskTime.setText(taskBean.startTimeString);
            inputTaskCount.setText(taskBean.taskContent);
            inputTaskCount.setSelection(taskBean.taskContent.length());
        } else {
            taskTime.setText(taskBean.startTimeString);
            inputTaskCount.setText("");
        }
        isFirstInputTaskTime = true;


        ViewTreeObserver viewTreeObserver = timeLineRootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);

        if (!isEditTime) {
            inputTaskCount.setFocusable(true);
            inputTaskCount.setFocusableInTouchMode(true);
            inputTaskCount.requestFocus();
            inputTaskCount.post(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(inputTaskCount);
                }
            });
        } else {
            taskTime.setFocusable(true);
            taskTime.setFocusableInTouchMode(true);
            taskTime.requestFocus();
            taskTime.post(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(taskTime);
                    taskTime.setText("");
                    // showUnClearTimes();
                }
            });
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (Build.VERSION.SDK_INT > 16)
            timeLineRootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        else
            //noinspection deprecation
            timeLineRootView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        removeView(unClearTimeRootView);
        noon = null;
        afterNoon = null;
        night = null;
    }


    public void init(TaskBean taskBean, long selectedTime, boolean isEditTime, int pos) {
        this.taskBean = taskBean;
        this.selectedTime = Calendar.getInstance();
        this.selectedTime.setTimeInMillis(selectedTime);
        startHour = endHour = startMinute = endMinute = -1;
        this.pos = pos;
        this.isEditTime = isEditTime;
        if (taskBean.isEmptyTask()) { //新增任务
            long currentTime = this.selectedTime.getTimeInMillis();
            taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
            switch (taskBean.unClearTime) {
                case TaskBean.EMPTY_NOON:
                    setUnClearTime(0, 11);
                    break;
                case TaskBean.EMPTY_AFTERNOON:
                    setUnClearTime(12, 17);
                    break;
                case TaskBean.EMPTY_NIGHT:
                    setUnClearTime(18, 23);
                    break;
                case TaskBean.EMPTY_TODAY_TASK:
                    switch (DateUtils.getTimeNoonOrAfterNoon(Calendar.getInstance())) {
                        case 1:
                            setUnClearTime(0, 11);
                            break;
                        case 2:
                            setUnClearTime(12, 17);
                            break;
                        case 3:
                            setUnClearTime(18, 23);
                            break;
                    }
                    break;
            }
            this.selectedTime.setTimeInMillis(currentTime);
            isEditTask = false;
            unClearTime = taskBean.unClearTime;
        } else { //编辑任务
            taskType = taskBean.taskType;
            startTimeCache = startTime = taskBean.startTime;
            endTimeCache = endTime = taskBean.endTime;
            subjectCache = taskBean.taskContent;
            isEditTask = true;
            unClearTime = taskBean.unClearTime;
        }
    }

/*    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateUIAction action) {
        int type = action.getActionType();
        if (type == TimeLineActions.TIME_LINE_ADD_EDIT_TASK_SUCCESS) {
            String taskId = (String) action.getActionData().get(0);
            addEditTaskSuccess(taskId);
        } else if (type == TimeLineActions.TIME_LINE_ADD_EDIT_TASK_FAIL) {
            UItoolKit.showToastShort(getContext(), "添加任务失败");
            hasEnter = false;
            isConfrim = false;
        }
    }*/


    public void addEditTaskResult(boolean result, String taskId) {
        if (result) {
            addEditTaskSuccess(taskId);
        } else {
            UItoolKit.showToastShort(getContext(), "添加任务失败");
            hasEnter = false;
            isConfrim = false;
        }
    }


    private void addEditTaskSuccess(String taskId) {
        if (!isEditTask) {
            UItoolKit.showToastShort(getContext(), "添加任务成功");
            taskBean.taskId = taskId;
            hasEnter = false;
            if (listener != null) {
                isConfrim = false;
                listener.onAddTaskResult(true, taskBean, pos);
            }
        } else {
            UItoolKit.showToastShort(getContext(), "编辑任务成功");
            hasEnter = false;
            if (listener != null) {
                isConfrim = false;
                listener.onEditTaskResult(taskBean, pos);
            }
        }
//            hideInputMethod();
        dismiss();
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputTaskCount.getWindowToken(), 0);
    }


    public void dismiss() {
        hideInputMethod();
        hideUnClearTimes();
        isConfrim = false;
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(AddTodayRootView.this);
            setTag(false);
        }
        listener.onDismiss();
    }


    public void setListener(AddEditTaskResultListener listener) {
        this.listener = listener;
    }

    private void initView(View v) {
        taskTime = (EditText) v.findViewById(R.id.tv_task_start_time);
        inputTaskCount = (EditText) v.findViewById(R.id.edit_input_task_content);
        //confirmButton = (ImageButton) v.findViewById(R.id.img_add_task_confirm);
        ImageButton confrimImgBtn = (ImageButton) v.findViewById(R.id.btn_confrim);
        v.setOnClickListener(addTaskClickListener);
        confrimImgBtn.setOnClickListener(addTaskClickListener);

        taskTime.setOnFocusChangeListener(taskTimeFocusChange);

        taskTime.addTextChangedListener(taskTimeWatcher);

        inputTaskCount.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            if (!hasEnter) {
                                if (inputTaskCount.getText().toString().trim().length() > 0)
                                    confirmClick();
                                else
                                    UItoolKit.showToastShort(getContext(), "内容不能为空");
                                hasEnter = true;
                            }
                            return true;
                        default:
                            return false;
                    }
                }
                return false;
            }
        });

        ((KeyEventEditText) inputTaskCount).setListener(keyUpListener);

        ((KeyEventEditText) taskTime).setListener(keyUpListener);

        taskTime.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            taskTime.clearFocus();
                            inputTaskCount.setFocusable(true);
                            inputTaskCount.setFocusableInTouchMode(true);
                            inputTaskCount.requestFocus();
                            showKeyboard(inputTaskCount);
                            return true;
                        case KeyEvent.KEYCODE_DEL:
                            startHour = endHour = startMinute = endMinute = -1;
                            taskTime.setText("");
                            return true;
                        default:
                            return false;
                    }
                }
                return false;
            }
        });

        taskTime.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', ':', '~'
                };
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }
        });
    }

    public void setTimeLineRootView(View timeLineRootView) {
        this.timeLineRootView = timeLineRootView;
    }


    private void confirmClick() {
        Log.i(TAG, "confirmClick: " + isConfrim);
        if (isConfrim)
            return;
        isConfrim = true;

        calculateTaskTime();

        if (isEditTask && endTime == endTimeCache && startTimeCache == startTime && subjectCache.equals(inputTaskCount.getText().toString())) {
            dismiss();
            return;
        }
        taskBean.taskContent = inputTaskCount.getText().toString();
        taskBean.startTime = startTime;
        taskBean.endTime = endTime;
        taskBean.taskType = taskType;
        Log.i(TAG, "confirmClick: " + taskBean.taskType);
        taskBean.unClearTime = DateUtils.getTimeNoonOrAfterNoon(startTime);
        taskBean.taskCreator = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        taskBean.setTimeString();
        TimeLineStoresNew.getInstance().addTodayTask(taskBean);
//        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK, taskBean);
    }

    public void cancelClick() {
        if (inputTaskCount.getText().toString().trim().length() > 0) {
            confirmClick();
        } else {
            dismiss();
        }
    }


    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getContext().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showUnClearTimes(int height) {
        initUnClearTimeViews(height);

        unClearTimeRootView.setVisibility(View.VISIBLE);
        setUnClearTimeSelected();
    }

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            timeLineRootView.getWindowVisibleDisplayFrame(r);
            if ((screenHeight - r.bottom) > 0 && screenHeight - r.bottom != softInputMethodHeight && taskTime.isFocused()) {
                handler.removeCallbacks(showUnClearTimeRunnable);
                int numInputHeight = screenHeight - r.bottom;
                if (unClearTimeRootView != null)
                    removeView(unClearTimeRootView);
                showUnClearTimes(numInputHeight);
                //noinspection deprecation
                timeLineRootView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
            } /*else {
                hideUnClearTimes();
            }*/
        }
    };

    private void initUnClearTimeViews(int height) {
        unClearTimeRootView = LayoutInflater.from(getContext()).inflate(R.layout.layout_un_clear_time, this, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(getContext(), 40));
        layoutParams.topMargin = getHeight() + DeviceUtil.dpTopx(getContext(), 49) - height - DeviceUtil.dpTopx(getContext(), 40);
/*        if (isEditTime)
        else
            layoutParams.topMargin = getHeight() + DeviceUtil.dpTopx(getContext(), 49) - height;*/
        unClearTimeRootView.setLayoutParams(layoutParams);
        noon = (TextView) unClearTimeRootView.findViewById(R.id.tv_noon);
        afterNoon = (TextView) unClearTimeRootView.findViewById(R.id.tv_after_noon);
        night = (TextView) unClearTimeRootView.findViewById(R.id.tv_night);
        noon.setOnClickListener(addTaskClickListener);
        afterNoon.setOnClickListener(addTaskClickListener);
        night.setOnClickListener(addTaskClickListener);
        addView(unClearTimeRootView);
    }


    private void calculateTaskTime() {
        if (isTimeLegal()) {
            selectedTime.set(Calendar.HOUR_OF_DAY, startHour);
            selectedTime.set(Calendar.MINUTE, startMinute);
            startTime = selectedTime.getTimeInMillis();
            selectedTime.set(Calendar.HOUR_OF_DAY, endHour);
            selectedTime.set(Calendar.MINUTE, endMinute);
            endTime = selectedTime.getTimeInMillis();
            taskType = TaskBean.TODAY_TASK;
        }
    }

    private boolean isTimeLegal() {
        return startHour != -1 && startMinute != -1 && endHour != -1 && endMinute != -1;
    }

    private void hideUnClearTimes() {
        if (unClearTimeRootView != null) {
            unClearTimeRootView.setVisibility(View.GONE);
        }
    }

    private void setUnClearTimeSelected() {
        switch (unClearTime) {
            case TaskBean.NOON:
                noon.setSelected(true);
                noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.AFTERNOON:
                afterNoon.setSelected(true);
                afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.NIGHT:
                night.setSelected(true);
                night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.EMPTY_NOON:
                noon.setSelected(true);
                noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.EMPTY_AFTERNOON:
                afterNoon.setSelected(true);
                afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.EMPTY_NIGHT:
                night.setSelected(true);
                night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                break;
            case TaskBean.EMPTY_TODAY_TASK:
                switch (DateUtils.getTimeNoonOrAfterNoon(Calendar.getInstance().getTimeInMillis())) {
                    case TaskBean.NOON:
                        noon.setSelected(true);
                        noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                        break;
                    case TaskBean.AFTERNOON:
                        afterNoon.setSelected(true);
                        afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                        break;
                    case TaskBean.NIGHT:
                        night.setSelected(true);
                        night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                        break;
                }
                break;
        }
    }

    private void setUnClearTime(int startHour, int endHour) {
        selectedTime.set(Calendar.HOUR_OF_DAY, startHour);
        selectedTime.set(Calendar.MINUTE, 0);
        startTime = selectedTime.getTimeInMillis();
        selectedTime.set(Calendar.HOUR_OF_DAY, endHour);
        selectedTime.set(Calendar.MINUTE, 59);
        endTime = selectedTime.getTimeInMillis();
    }

    private View.OnFocusChangeListener taskTimeFocusChange = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (unClearTimeRootView == null) {
                    handler.postDelayed(showUnClearTimeRunnable, 300);/*showUnClearTimes(softInputMethodHeight);*/
                } else {
                    unClearTimeRootView.setVisibility(View.VISIBLE);
                }
                if (isFirstInputTaskTime && !isEditTime) {
                    taskTime.setText("");
                    isFirstInputTaskTime = false;
                }
            } else {
                hideUnClearTimes();
                if (!isFirstInputTaskTime && !isEditTask)
                    if (TextUtils.isEmpty(taskTime.getText())) {
                        taskTime.setText(taskBean.startTimeString);
                        isFirstInputTaskTime = true;
                    }
            }
        }
    };

    private Runnable showUnClearTimeRunnable = new Runnable() {
        @Override
        public void run() {
            showUnClearTimes(softInputMethodHeight);
        }
    };

    private void resetUnClearViews() {

        if (noon.isSelected()) {
            noon.setSelected(false);
            noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
        }

        if (afterNoon.isSelected()) {
            afterNoon.setSelected(false);
            afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
        }

        if (night.isSelected()) {
            night.setSelected(false);
            night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
        }
    }

    private KeyEventEditText.PreImeKeyListener keyUpListener = new KeyEventEditText.PreImeKeyListener() {
        @Override
        public void onKeyUp() {
            dismiss();
        }
    };

/*    private TextWatcher taskContentWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length() > 0)
                confrimImgBtn.setV
        }
    };*/


    private TextWatcher taskTimeWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(s)) {
              /*  if (confirmButton.getVisibility() == View.INVISIBLE && isEditTask)
                    confirmButton.setVisibility(View.VISIBLE);*/
                if (noon == null || afterNoon == null || night == null) {
                    return;
                }


                if ((noon.isSelected() || afterNoon.isSelected() || night.isSelected())) {
                    if (isTimeFromClick) {
                        isTimeFromClick = false;
                        return;
                    }
                    isTimeFromClick = false;
                    resetUnClearViews();
                    s.delete(0, s.length() - 1);
                    return;
                }

                switch (taskTime.getSelectionStart()) {
                    case 1: {
                        int hour = s.charAt(0);
                        if (hour > 50) {
                            s.insert(0, "0");
                            startHour = hour - 48;
                        }
                        break;
                    }
                    case 2: {
                        int hour = (s.charAt(0) - 48) * 10 + (s.charAt(1) - 48);
                        startHour = hour;
                        if (hour > 23) {
                            startHour = 23;
                            s.replace(0, 2, 23 + "");
                        } else {
                            s.insert(2, ":");
                        }
                        break;
                    }
                    case 4: {
                        int minuteFirst = s.charAt(3);
                        if (minuteFirst > 53) {
                            s.insert(3, "0");
                            startMinute = minuteFirst - 48;
                        }
                        break;
                    }
                    case 5: {
                        startMinute = (s.charAt(3) - 48) * 10 + (s.charAt(4) - 48);
                        s.insert(5, " ~ ");
                        break;
                    }
                    case 9: {
                        int hour = s.charAt(8);
                        if (hour > 50) {
                            s.insert(8, "0");
                            endHour = hour - 48;
                        } else {
                            int startHour = s.charAt(0);
                            if (hour < startHour) {
                                s.delete(8, 9);
                            }
                        }
                        break;
                    }
                    case 10: {
                        int hour = (s.charAt(8) - 48) * 10 + (s.charAt(9) - 48);
                        endHour = hour;

                        if (endHour < startHour) {
                            endHour = startHour;
                            s.replace(8, 10, "");
                        } else if (hour > 23) {
                            endHour = 23;
                            s.replace(8, 10, 23 + "");
                        } else {
                            s.insert(10, ":");
                        }
                        break;
                    }
                    case 12: {
                        int minuteFirst = s.charAt(11);
                        if (minuteFirst > 53) {
                            s.insert(11, "0");
                            endMinute = minuteFirst - 48;
                        }
                        break;
                    }
                    case 13: {
                        endMinute = (s.charAt(11) - 48) * 10 + (s.charAt(12) - 48);
                        if (endHour == startHour && endMinute <= startMinute) {
                            endMinute = -1;
                            s.delete(11, 13);
                        }
                        break;
                    }
                }
            }
        }
    };


    private OnClickListener addTaskClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_noon:
                    isTimeFromClick = true;
                    taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
                    setUnClearTime(0, 11);
                    v.setSelected(true);
                    noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                    afterNoon.setSelected(false);
                    afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    night.setSelected(false);
                    night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    taskTime.setText("上午");
                    taskTime.setSelection(taskTime.length());
                    unClearTime = TaskBean.NOON;
                    break;
                case R.id.tv_after_noon:
                    isTimeFromClick = true;
                    taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
                    setUnClearTime(12, 17);
                    v.setSelected(true);
                    afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                    noon.setSelected(false);
                    noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    night.setSelected(false);
                    night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    taskTime.setText("下午");
                    taskTime.setSelection(taskTime.length());
                    unClearTime = TaskBean.AFTERNOON;
                    break;
                case R.id.tv_night:
                    isTimeFromClick = true;
                    taskType = TaskBean.TODAY_TASK_TIME_UNCLEAR;
                    setUnClearTime(18, 23);
                    v.setSelected(true);
                    night.setTextColor(ResourcesUtil.getInstance().getColor(R.color.white));
                    noon.setSelected(false);
                    noon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    afterNoon.setSelected(false);
                    afterNoon.setTextColor(ResourcesUtil.getInstance().getColor(R.color.key_text));
                    taskTime.setText("晚上");
                    taskTime.setSelection(taskTime.length());
                    unClearTime = TaskBean.NIGHT;
                    break;
                case R.id.add_today_task_root:
//                    dismiss();
                    cancelClick();
                    break;
                case R.id.btn_confrim:
                    if (inputTaskCount.getText().toString().trim().length() > 0)
                        confirmClick();
                    else
                        UItoolKit.showToastShort(getContext(), "内容不能为空");
                    break;
            }
        }
    };
}
