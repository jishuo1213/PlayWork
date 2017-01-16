package com.inspur.playwork.view.timeline.addtask;

import java.util.Calendar;

/**
 * Created by Fan on 15-9-17.
 */
public interface SelectedTimeChangeListener {
    void onSelectDayChange(Calendar selectedDay);
    void onUnClearTimeSelect(int unClearTime);
}
