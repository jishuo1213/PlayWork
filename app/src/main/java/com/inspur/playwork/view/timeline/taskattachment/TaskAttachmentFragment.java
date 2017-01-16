package com.inspur.playwork.view.timeline.taskattachment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.inspur.playwork.R;
import com.inspur.playwork.actions.UpdateUIAction;
import com.inspur.playwork.actions.timeline.TimeLineActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;
import com.inspur.playwork.model.timeline.TaskBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.UItoolKit;
import com.inspur.playwork.view.common.pulltorefresh.PullToRefreshView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Fan on 15-9-18.
 */
public class TaskAttachmentFragment extends Fragment implements PullToRefreshView.OnRefreshListener {

    private static final String TAG = "AttachmentFragmentFan";

    private static final String TASK_BEAN = "task bean";

    public static Fragment getInstance(TaskBean taskBean) {
        Fragment fragment = new TaskAttachmentFragment();
        Bundle args = new Bundle();
        args.putParcelable(TASK_BEAN, taskBean);
        fragment.setArguments(args);
        return fragment;
    }

    private TaskBean taskBean;

    private PullToRefreshView pullToRefreshView;
    private RecyclerView attachMentListView;

    private ArrayList<TaskAttachmentBean> attachmentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dispatcher.getInstance().register(this);
        taskBean = getArguments().getParcelable(TASK_BEAN);
        checkAttachmentDir();
    }

    private void checkAttachmentDir() {
        File file = new File(FileUtil.getAttachmentPath() + taskBean.taskId + File.separator);
        if (!file.exists()) {
            boolean result = file.mkdir();
            if (!result)
                UItoolKit.showToastShort(getActivity(), "创建附件文件夹失败，是不是没有空间了？");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_task_attach_list, container, false);
        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        pullToRefreshView = (PullToRefreshView) rootView.findViewById(R.id.pull_refresh_task_attach);
        pullToRefreshView.setOnRefreshListener(this);
        attachMentListView = (RecyclerView) rootView.findViewById(R.id.recycler_task_attachment);
        attachMentListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadTaskAttachMent();
        if (attachmentList != null && attachMentListView.getAdapter() == null) {
            setListAdapter();
        }
    }

    @SuppressWarnings({"unused", "unchecked"})
    public void onEventMainThread(UpdateUIAction action) {
        if (action.getActionType() == TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST) {
            attachmentList = (ArrayList<TaskAttachmentBean>) action.getActionData().get(0);
            if (attachMentListView != null) {
                if (attachMentListView.getAdapter() == null) {
                    setListAdapter();
                } else {
                    ((AttachmentRecyclerAdapter) attachMentListView.getAdapter()).setAttachmentList(attachmentList);
                    (attachMentListView.getAdapter()).notifyDataSetChanged();
                    UItoolKit.showToastShort(getActivity(), "刷新成功");
                    pullToRefreshView.postDelayed(refreshCompleteRunnable, 500);
                }
            }
        }
    }

    public void setListAdapter() {
        AttachmentRecyclerAdapter adapter = new AttachmentRecyclerAdapter(attachMentListView);
        adapter.setAttachmentList(attachmentList);
        attachMentListView.setAdapter(adapter);
    }

    private void loadTaskAttachMent() {
        Dispatcher.getInstance().dispatchStoreActionEvent(TimeLineActions.TIME_LINE_GET_TASK_ATTACH_LIST, taskBean.taskId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Dispatcher.getInstance().unRegister(this);
    }

    @Override
    public void onRefresh() {
        loadTaskAttachMent();
    }

    private Runnable refreshCompleteRunnable = new Runnable() {
        @Override
        public void run() {
            pullToRefreshView.setRefreshing(false);
        }
    };
}
