package com.inspur.playwork.model.timeline;

import java.util.Comparator;

/**
 * Created by Fan on 15-10-29.
 */
public class CrossTaskBeanComparator implements Comparator<TaskBean> {

    @Override
    public int compare(TaskBean lhs, TaskBean rhs) {
        boolean leftHasSort;
        boolean rightHasSort;
        boolean leftIsMine;
        boolean rightIsMine;
        leftHasSort = lhs.sortNum != Integer.MAX_VALUE;
        rightHasSort = rhs.sortNum != Integer.MAX_VALUE;
        leftIsMine = lhs.isMineTask();
        rightIsMine = rhs.isMineTask();

        if (leftHasSort || rightHasSort) {
            if (lhs.sortNum == rhs.sortNum) {
                if (lhs.startTime > rhs.startTime) {
                    return 1;
                } else if (lhs.startTime < rhs.startTime) {
                    return -1;
                }
            }
            return lhs.sortNum - rhs.sortNum;
        } else {
            if (rightIsMine ^ leftIsMine) {
                return leftIsMine ? -1 : 1;
            } else {
                if (lhs.startTime > rhs.startTime) {
                    return 1;
                } else if (lhs.startTime < rhs.startTime) {
                    return -1;
                }
            }
        }
        return 0;
    }

    public static void main(String[] argv) {
    }
}
