package com.inspur.playwork.view.timeline.addtask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.NumberKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.MainActivity;
import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.utils.DateUtils;
import com.inspur.playwork.utils.DeviceUtil;
import com.inspur.playwork.utils.PreferencesHelper;
import com.inspur.playwork.utils.ResourcesUtil;
import com.inspur.playwork.utils.UItoolKit;

import java.util.Calendar;

/**
 * Created by Fan on 15-10-16.
 */
public class AddTodayTaskDialogFragment extends DialogFragment implements TextWatcher, View.OnClickListener {


    private static final String TAG = "AddTaskDialogFan";

    private static final String TASK_BEAN = "taskBean";
    private static final String EMPTY_TASK_POS = "position";
    private static final String MARGIN_TOP = "margin_top";
    private static final String SELECTED_TIME = "selected_times";
    private static final String EDIT_TIME = "edit_time";

    private TaskBean taskBean;

    private EditText taskTime;
    private EditText inputTaskCount;
    // private ImageButton confirmButton;

    private View unClearTimeRootView;
    private TextView noon, afterNoon, night;
    private View rootView, addTaskRootView;

    private long startTime;

    private long endTime;

    private Calendar selectedTime;

    private int taskType;

    private int pos;

    private boolean isFirstInputTaskTime;

    private boolean isEditTask;

    private AddEditTaskResultListener listener;

    private int startHour = -1, endHour = -1, startMinute = -1, endMinute = -1;

    private boolean isEditTime;

    private boolean hasEnter = false;

    private int screenHeight;

    private boolean isTimeFromClick;

    private int oneHandrodDp, rootViewHeight;

    private int softInputMethodHeight;

    private Handler handler;

    private int unClearTime;

    public static DialogFragment getInstance(TaskBean mBean, int marginTop, int pos, Calendar selectedTime) {
        DialogFragment dialogFragment = new AddTodayTaskDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(TASK_BEAN, mBean);
        args.putInt(MARGIN_TOP, marginTop);
        args.putInt(EMPTY_TASK_POS, pos);
        args.putSerializable(SELECTED_TIME, selectedTime);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public static DialogFragment getInstance(TaskBean taskBean, int y, int pos, Calendar selectedDay, boolean isEditTime) {
        DialogFragment dialogFragment = new AddTodayTaskDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(TASK_BEAN, taskBean);
        args.putInt(MARGIN_TOP, y);
        args.putInt(EMPTY_TASK_POS, pos);
        args.putSerializable(SELECTED_TIME, selectedDay);
        args.putBoolean(EDIT_TIME, isEditTime);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        oneHandrodDp = DeviceUtil.dpTopx(getActivity(), 100);
        Dispatcher.getInstance().register(this);
        taskBean = getArguments().getParcelable(TASK_BEAN);
        pos = getArguments().getInt(EMPTY_TASK_POS);
        selectedTime = Calendar.getInstance();
        Calendar calendar = (Calendar) getArguments().getSerializable(SELECTED_TIME);
        assert calendar != null;
        selectedTime.setTimeInMillis(calendar.getTimeInMillis());
        selectedTime.set(Calendar.SECOND, 0);
        selectedTime.set(Calendar.MILLISECOND, 0);
        screenHeight = DeviceUtil.getDeviceScreenHeight(getActivity());
        if (taskBean.isEmptyTask()) { //新增任务
            long currentTime = selectedTime.getTimeInMillis();
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
            }
            selectedTime.setTimeInMillis(currentTime);
            isEditTask = false;
            unClearTime = taskBean.unClearTime;
        } else { //编辑任务
            taskType = taskBean.taskType;
            startTime = taskBean.startTime;
            endTime = taskBean.endTime;
            isEditTask = true;
            unClearTime = taskBean.unClearTime;
        }

        ((MainActivity) getActivity()).setIsAddOrEditTask(true);
       // ((MainActivity) getActivity()).setAddEditDialog(this);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), 1);
        @SuppressLint("InflateParams")
        View dialogRoot = LayoutInflater.from(getActivity()).inflate(R.layout.layout_add_today_task_dialog, null);
        dialog.setContentView(dialogRoot);
        initView(dialogRoot);
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = DeviceUtil.getDeviceScreenWidth(getActivity());
        int marginTop = getArguments().getInt(MARGIN_TOP);
        lp.gravity = Gravity.TOP;
        lp.x = 0;
        lp.y = marginTop;
        softInputMethodHeight = PreferencesHelper.getInstance().readIntPreference(PreferencesHelper.INPUT_HEIGHT);
        lp.height = rootView.getHeight() - marginTop;
        rootViewHeight = lp.height;
        isEditTime = getArguments().getBoolean(EDIT_TIME, false);
        dialogWindow.setAttributes(lp);
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
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (Build.VERSION.SDK_INT > 16)
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        else
            //noinspection deprecation
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        Dispatcher.getInstance().unRegister(this);
        hideInputMethod();
        ((MainActivity) getActivity()).setIsAddOrEditTask(false);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (Build.VERSION.SDK_INT > 16)
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        else
            //noinspection deprecation
            rootView.getViewTreeObserver().removeGlobalOnLayoutListener(globalLayoutListener);
        Dispatcher.getInstance().unRegister(this);
        hideInputMethod();
        listener.onAddTaskResult(false, taskBean, pos);

        ((MainActivity) getActivity()).setIsAddOrEditTask(false);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(UpdateUIAction action) {
        int type = action.getActionType();
        if (type == TimeLineActions.TIME_LINE_ADD_EDIT_TASK_SUCCESS) {
            if (!isEditTask) {
                UItoolKit.showToastShort(getActivity(), "添加任务成功");
                taskBean.taskId = (String) action.getActionData().get(0);
                if (listener != null) {
                    listener.onAddTaskResult(true, taskBean, pos);
                }
            } else {
                UItoolKit.showToastShort(getActivity(), "编辑任务成功");
                if (listener != null)
                    listener.onEditTaskResult(taskBean, pos);
            }
            hideInputMethod();
            dismiss();
        } else if (type == TimeLineActions.TIME_LINE_ADD_EDIT_TASK_FAIL) {
            UItoolKit.showToastShort(getActivity(), "添加任务失败");
            hasEnter = false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showUnClearTimes(int height) {
        if (unClearTimeRootView == null) {
            initUnClearTimeViews(height);
        }

        unClearTimeRootView.setVisibility(View.VISIBLE);
        setUnClearTimeSelected();
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

    private void initUnClearTimeViews(int height) {
        unClearTimeRootView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_un_clear_time, (ViewGroup) addTaskRootView, false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, DeviceUtil.dpTopx(getActivity(), 40));
        layoutParams.topMargin = rootViewHeight - height - DeviceUtil.dpTopx(getActivity(), 40);
        unClearTimeRootView.setLayoutParams(layoutParams);
        noon = (TextView) unClearTimeRootView.findViewById(R.id.tv_noon);
        afterNoon = (TextView) unClearTimeRootView.findViewById(R.id.tv_after_noon);
        night = (TextView) unClearTimeRootView.findViewById(R.id.tv_night);
        noon.setOnClickListener(this);
        afterNoon.setOnClickListener(this);
        night.setOnClickListener(this);
        ((ViewGroup) addTaskRootView).addView(unClearTimeRootView);
    }

    private void hideUnClearTimes() {
        if (unClearTimeRootView != null)
            unClearTimeRootView.setVisibility(View.GONE);
    }


    private void initView(View v) {
        addTaskRootView = v;
        taskTime = (EditText) v.findViewById(R.id.tv_task_start_time);
        inputTaskCount = (EditText) v.findViewById(R.id.edit_input_task_content);
        //confirmButton = (ImageButton) v.findViewById(R.id.img_add_task_confirm);
        v.setOnClickListener(this);
        ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener);
        if (isEditTask) {
            if (taskType == TaskBean.TODAY_TASK)
                taskTime.setText(taskBean.startTimeString + " - " + taskBean.endTimeString);
            else
                taskTime.setText(taskBean.startTimeString);
            inputTaskCount.setText(taskBean.taskContent);
        } else {
            taskTime.setText(taskBean.startTimeString);
        }
        inputTaskCount.addTextChangedListener(this);
        isFirstInputTaskTime = true;

        taskTime.setOnFocusChangeListener(taskTimeFocusChange);

        taskTime.addTextChangedListener(taskTimeWatcher);
        taskTime.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DEL:
                        startHour = endHour = startMinute = endMinute = -1;
                        taskTime.setText("");
                        return true;
                    default:
                        return false;
                }
            }
        });


        inputTaskCount.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        if (!hasEnter) {
                            confirmClick();
                            hasEnter = true;
                        }
                        return true;
                }
                return false;
            }
        });
        taskTime.setKeyListener(new NumberKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', ':', '-'
                };
            }

            @Override
            public int getInputType() {
                return InputType.TYPE_CLASS_NUMBER;
            }
        });
    }

    public void setListener(AddEditTaskResultListener listener) {
        this.listener = listener;
    }


    public void setRootView(View rootView) {
        this.rootView = rootView;
    }


    private Runnable showUnClearTimeRunnable = new Runnable() {
        @Override
        public void run() {
            showUnClearTimes(softInputMethodHeight);
        }
    };

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            if (screenHeight - r.bottom > oneHandrodDp && taskTime.isFocused()) {
                handler.removeCallbacks(showUnClearTimeRunnable);
                showUnClearTimes(screenHeight - r.bottom);
            } else {
                hideUnClearTimes();
            }
        }
    };

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
                        s.insert(5, " - ");
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
/*        if (TextUtils.isEmpty(s.toString()))
            confirmButton.setVisibility(View.INVISIBLE);
        else
            confirmButton.setVisibility(View.VISIBLE);*/
    }


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
                getDialog().cancel();
                break;
        }
    }

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

    private void confirmClick() {
        calculateTaskTime();
        taskBean.taskContent = inputTaskCount.getText().toString();
        taskBean.startTime = startTime;
        taskBean.endTime = endTime;
        taskBean.taskType = taskType;
        taskBean.unClearTime = DateUtils.getTimeNoonOrAfterNoon(startTime);
        taskBean.taskCreator = PreferencesHelper.getInstance().readStringPreference(PreferencesHelper.USER_NAME);
        taskBean.setTimeString();
        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_ADD_EDIT_TASK, taskBean);
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

    private void setUnClearTime(int startHour, int endHour) {
        selectedTime.set(Calendar.HOUR_OF_DAY, startHour);
        selectedTime.set(Calendar.MINUTE, 0);
        startTime = selectedTime.getTimeInMillis();
        selectedTime.set(Calendar.HOUR_OF_DAY, endHour);
        selectedTime.set(Calendar.MINUTE, 59);
        endTime = selectedTime.getTimeInMillis();
    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputTaskCount.getWindowToken(), 0);
    }
}
