package com.inspur.playwork.model.timeline;

import java.util.Comparator;

/**
 * Created by Fan on 15-10-27.
 */
public class TodayTaskBeanComparator implements Comparator<TaskBean> {
    @Override
    public int compare(TaskBean lhs, TaskBean rhs) {

        boolean leftHasSort;
        boolean rightHasSort;

        boolean lhsIsEmpty = lhs.isEmptyTask();
        if (rhs == null)
            return 1;
        boolean rhsIsEmpty = rhs.isEmptyTask();

        leftHasSort = lhs.sortNum != Integer.MAX_VALUE;
        rightHasSort = rhs.sortNum != Integer.MAX_VALUE;

        boolean lhsIsClear = lhs.isTodayUnClearTask();
        boolean rhsIsClear = rhs.isTodayUnClearTask();

        if (lhsIsEmpty) {
            if (rhsIsEmpty) {
                return lhs.unClearTime - rhs.unClearTime;
            } else {
                return lhs.unClearTime - (rhs.unClearTime + 11);
            }
        } else {
            if (rhsIsEmpty) {
                return (lhs.unClearTime + 11) - rhs.unClearTime;
            } else {
                if (lhs.unClearTime == rhs.unClearTime) {
                    if (leftHasSort) {
                        if (rightHasSort) {
                            return lhs.sortNum - rhs.sortNum;
                        } else {
                            return 1;
                        }
                    } else {
                        if (rightHasSort) {
                            return -1;
                        }
                    }
                    if (lhsIsClear) {
                        if (rhsIsClear) {
                            if (lhs.startTime > rhs.startTime) {
                                return 1;
                            } else if (lhs.startTime < rhs.startTime) {
                                return -1;
                            } else {
                                return 0;
                            }
                        } else {
                            return 1;
                        }
                    } else {
                        if (rhsIsClear) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                } else {
                    return lhs.unClearTime - rhs.unClearTime;
                }
            }
        }
    }
}
