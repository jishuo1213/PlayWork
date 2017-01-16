/**
 * Created by yonysun on 2015/12/25.
 */
package com.inspur.playwork.weiyou.adapter;

import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.model.message.GroupInfoBean;


public interface AddMemberListener {
    /**
     * 增加单个人员接口回调方法
     *
     * @param userInfoBean 添加的人员信息
     */
    void addMember(UserInfoBean userInfoBean);

    /**
     * 增加一组人员接口回调方法
     *
     * @param groupInfoBean 添加的群组信息
     */
    void addMember(GroupInfoBean groupInfoBean);
}
