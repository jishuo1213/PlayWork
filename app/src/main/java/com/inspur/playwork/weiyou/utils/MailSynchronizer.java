package com.inspur.playwork.weiyou.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import com.inspur.fan.decryptmail.DecryptMail;
import com.inspur.playwork.actions.StoreAction;
import com.inspur.playwork.actions.db.DataBaseActions;
import com.inspur.playwork.dispatcher.Dispatcher;
import com.inspur.playwork.utils.ThreadPool;
import com.inspur.playwork.utils.db.bean.MailTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;

/**
 * Created by sunyuan on 2016/12/5 0005 15:29.
 * Email: sunyuan@inspur.com
 */

public class MailSynchronizer {

    private static final String TAG = "MailSynchronizer";
    private static final int SEND_MAIL_RESULT = 0x01;
    private ArrayList<MailTask> mailTaskQueue;
    private MailTask currMailTask;
    private MailTask newTaskCache = null;
    private boolean isRunning = false;
    private DecryptMail dm;
    private SendMailResultListener sendMailResultListener;
    private Handler sychronizerHandler;

    public MailSynchronizer(DecryptMail dm,SendMailResultListener sendMailResultListener){
        Dispatcher.getInstance().register(this);
        this.mailTaskQueue = new ArrayList<>();
        this.dm = dm;
        this.sendMailResultListener = sendMailResultListener;
        sychronizerHandler = new SychronizerHandler(new WeakReference<>(this));
    }

    public void addMailTask(MailTask mailTask){
        Log.i(TAG, "addMailTask:.......... mailTask.getCreateTime() = "+mailTask.getCreateTime());
        newTaskCache = mailTask;
        DBUtil.saveMailTask(mailTask);
    }

    public void removeMailTask(String email,Long createTime){
        if(mailTaskQueue.size() == 0) return;
        if(email == null || createTime == null) return;
        MailTask targetMT = null;
        for(MailTask mt : mailTaskQueue){
            if(mt.getEmail().equals(email) && mt.getCreateTime().equals(createTime)){
                targetMT = mt;
            }
        }
        if(targetMT != null){
            Log.i(TAG, "removeMailTask: 删除数据库中的目标任务数据："+targetMT.getSubject());
            DBUtil.deleteMailTaskById(targetMT.getId());
            if(currMailTask!=null && currMailTask.getCreateTime().equals(targetMT.getCreateTime())){
                currMailTask.setSentRcptCount(currMailTask.getRcptCount() - 1);
                currMailTask.setMailRcpts("[]");
            }else{
                mailTaskQueue.remove(targetMT);
            }
        }
    }

    private void saveMailTaskCB(long taskId){
        Log.i(TAG, "saveMailTaskCB: ");
        if(!isRunning) {
            startMailSychronizer();
        }else {
            newTaskCache.setId(taskId);
            this.mailTaskQueue.add(newTaskCache);
            newTaskCache = null;
        }
    }

    public void removeCurrMailTask(){
        if(mailTaskQueue.size()>0) {
            MailTask _mt = mailTaskQueue.remove(0);
            DBUtil.deleteMailTaskById(_mt.getId());
        }
        syncMailTask();
    }

    public void startMailSychronizer(){
        Log.i(TAG, "startMailSychronizer: ");
        if(!isRunning){
            this.mailTaskQueue.clear();
            DBUtil.getAllMailTask();
        }
    }

    private void getAllMailTaskCB(ArrayList<MailTask> mtAL){
        Log.i(TAG, "getAllMailTaskCB: 现在的任务数量："+mtAL.size());
        this.mailTaskQueue.addAll(mtAL);
        syncMailTask();
    }

    private void syncMailTask(){
        Log.i(TAG, "syncMailTask: ");
        if(mailTaskQueue.size()>0) {
            isRunning = true;
            currMailTask = mailTaskQueue.get(0);
            execSendCmd(currMailTask);
        }else{
            isRunning = false;
        }
    }

    public void clearSyncMailTask(){
        isRunning = false;
        this.mailTaskQueue.clear();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void removeRcptFromMailTask(Address[] addressArr){
        if(currMailTask != null) {
            try {
                JSONArray ja = new JSONArray(currMailTask.getMailRcpts());
                int jaLen = ja.length();
                if(jaLen>0){

                    for(Address addr : addressArr){
                        InternetAddress ia = (InternetAddress)addr;
                        String address = ia.getAddress();
                        Log.i(TAG, "removeRcptFromMailTask: addressArr = "+address);
                        int i,_index = -1;
                        for (i=0;i<jaLen;i++) {
                            JSONObject jo = (JSONObject) ja.opt(i);
                            Log.i(TAG, "removeRcptFromMailTask: for jsonObject address= "+jo.optString("address"));
                            if(address.equals(jo.optString("address"))){
                                _index = i;
                                break;
                            }
                        }
                        Log.i(TAG, "removeRcptFromMailTask: _index = " + _index);
                        if(_index != -1){
                            ja.remove(_index);
                            if(ja.length()>0){
                                currMailTask.setMailRcpts(ja.toString());
//                                Log.i(TAG, "removeRcptFromMailTask: before updateMailTask mailRcpts = " + currMailTask.getMailRcpts());
                                DBUtil.updateMailTask(currMailTask);
                                mailTaskQueue.set(0,currMailTask);
                            }else{
                                removeCurrMailTask();
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 操作数据库数据回调监听
     * @param sa
     */
    public void onEventMainThread(StoreAction sa) {
        int actionType = sa.getActionType();
        SparseArray mlSA = sa.getActiontData();
        switch (actionType) {
            case DataBaseActions.SAVE_ONE_MAIL_TASK_SUCCESS:
                saveMailTaskCB((Long) mlSA.get(0));
                break;
            case DataBaseActions.GET_ALL_MAIL_TASK_RESULT:
                getAllMailTaskCB((ArrayList<MailTask>)mlSA.get(0));
                break;
            case DataBaseActions.DELETE_MAIL_TASK_BY_ID_RESULT:
                break;
            case DataBaseActions.UPDATE_MAIL_TASK_RCPTS_RESULT:
                Log.i(TAG, "onEventMainThread: UPDATE_MAIL_TASK_RCPTS_RESULT "+mlSA.toString());
                break;
        }
    }

    private int execCount = 0;
    private MailTask prevMailTask;
    void execSendCmd(final MailTask mailTask){
        Log.i(TAG, "execSendCmd: currMailTask.getSubject() : "+mailTask.getSubject());
        Log.i(TAG, "execSendCmd: currMailTask.getMailRcpts() : "+mailTask.getMailRcpts());
        prevMailTask = mailTask;
        sendMailResultListener.onBeforeSend(mailTask);
        ThreadPool.exec(new Runnable() {
                    @Override
                    public void run() {
                if(!new File(mailTask.getMessageFilePath()).exists()){
                    sendMailResultListener.onSendSuccess(currMailTask);
                }
                boolean res= false;
                try {
                    res = MailUtil.sendMail(mailTask, dm, new TransportListener() {
                        @Override
                        public void messageDelivered(TransportEvent e) {
                            Address[] succAddressArr = e.getValidSentAddresses();
                            Log.i(TAG, "messageDelivered: ........succAddressArr.length = "+succAddressArr.length);
                            if(succAddressArr.length>0){
                                for(int i=0;i<succAddressArr.length;i++){
                                    InternetAddress iaddr = (InternetAddress)succAddressArr[i];
                                    Log.w(TAG,"邮件“"+currMailTask.getSubject()+"”已成功发送给 "+iaddr.getPersonal()+"<"+iaddr.getAddress()+">");
                                    currMailTask.setSentRcptCount(currMailTask.getSentRcptCount()+1);
                                    sendMailResultListener.onSendSuccess(currMailTask);
                                }
                                removeRcptFromMailTask(succAddressArr);
                            }
                        }

                        @Override
                        public void messageNotDelivered(TransportEvent e) {
                            Log.e(TAG, "messageNotDelivered: ......................" );
                            Address[] addrs = e.getInvalidAddresses();
                            if(addrs.length>0){
                                for (Address addr : addrs) {
                                    Log.e(TAG,"发送邮件失败，无效收件人有："+addr.toString());
                                }
                            }
                            addrs = e.getValidUnsentAddresses();
                            if(addrs.length>0){
                                for (Address addr : addrs) {
                                    Log.e(TAG,"发送邮件失败，未送达的有效收件人有："+addr.toString());
                                }
                            }
                            addrs = e.getValidSentAddresses();
                            if(addrs.length>0){
                                for (Address addr : addrs) {
                                    Log.e(TAG,"发送邮件失败，已送达的有效收件人有："+addr.toString());
                                }
                            }
                            sendMailResultListener.onSendFailed(currMailTask);
                        }

                        @Override
                        public void messagePartiallyDelivered(TransportEvent e) {
                            Log.e(TAG, "messagePartiallyDelivered: ......................" );
                            Address[] addrs = e.getInvalidAddresses();
                            if(addrs.length>0){
                                for (Address addr : addrs) {
                                    Log.e(TAG,"发送邮件部分发送成功，无效收件人有："+addr.toString());
                                }
                            }
                            addrs = e.getValidUnsentAddresses();
                            if(addrs.length>0){
                                for (Address addr : addrs) {
                                    Log.e(TAG,"发送邮件部分发送成功，未送达的有效收件人有："+addr.toString());
                                }
                            }
                            Address[] succAddressArr = e.getValidSentAddresses();
                            Log.i(TAG, "messagePartiallyDelivered: ........succAddressArr.length = "+succAddressArr.length);
                            if(succAddressArr.length>0){
                                for(int i=0;i<succAddressArr.length;i++){
                                    InternetAddress iaddr = (InternetAddress)succAddressArr[i];
                                    Log.w(TAG,"邮件“"+currMailTask.getSubject()+"”已成功发送给 "+iaddr.getPersonal()+"<"+iaddr.getAddress()+">");
                                    currMailTask.setSentRcptCount(currMailTask.getSentRcptCount()+1);
                                    sendMailResultListener.onSendSuccess(currMailTask);
                                }
                                removeRcptFromMailTask(succAddressArr);
                            }
                            sendMailResultListener.onSendPartiallySuccess(currMailTask);
    //                        vuMailHandler.sendMessage(vuMailHandler.obtainMessage(SEND_MAIL_RESULT,SEND_MAIL_PARTIALLY,0,null));
                        }
                    });
                } catch (MessagingException e) {
                    e.printStackTrace();
                    sendMailResultListener.onSendFailed(currMailTask);
                    if(prevMailTask==null||(prevMailTask.getCreateTime().equals(currMailTask.getCreateTime())&&execCount<2)) {
                        sychronizerHandler.sendMessage(sychronizerHandler.obtainMessage(SEND_MAIL_RESULT,0,0,currMailTask));
                        /**
                         * 因为服务器有限制，一分钟发送的邮件数量不能超过5封，
                        * 所以如果发送失败，过20秒后重新尝试发送
                        */
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                execCount ++ ;
//                                execSendCmd(currMailTask);
//                            }
//                        }, 1000 * 20);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMailResultListener.onSendFailed(currMailTask);
                }
                Log.i(TAG,"execSendCmd res: "+res);
            }
        });
    }
    private class SychronizerHandler extends Handler {
        private WeakReference<MailSynchronizer> reference;

        public SychronizerHandler(WeakReference<MailSynchronizer> reference) {
            this.reference = reference;
        }

        @Override
        public void dispatchMessage(@NonNull Message msg) {
            MailSynchronizer mailSynchronizer = reference.get();
//            Log.i(TAG, "dispatchMessage: "+msg.what);
//            Log.i(TAG, "dispatchMessage: "+msg.obj);
            switch (msg.what) {
                case SEND_MAIL_RESULT:
                    final Message finalMsg = msg;
                    mailSynchronizer.sychronizerHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            execSendCmd((MailTask)finalMsg.obj);
                        }
                    },20000);
            }
        }
    }

    public void destroy(){
        clearSyncMailTask();
        this.mailTaskQueue = null;
        Dispatcher.getInstance().unRegister(this);
    }

    public interface SendMailResultListener{
        void onSendSuccess( MailTask currMailTask);
        void onSendFailed( MailTask currMailTask);
        void onSendPartiallySuccess(MailTask currMailTask);
        void onBeforeSend(MailTask mailTask);
    }
}
