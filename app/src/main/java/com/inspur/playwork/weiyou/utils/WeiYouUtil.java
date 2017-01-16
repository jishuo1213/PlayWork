package com.inspur.playwork.weiyou.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.playwork.R;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.db.bean.MailContacts;
import com.inspur.playwork.weiyou.view.WordWrapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 孙 on 2015/11/30 0030.
 */
public class WeiYouUtil {

    private static final String TAG = "WeiYouUtil";

    public static List<MailContacts> parseContactListFromJSON(String arrStr){
        List<MailContacts> mcList = new ArrayList<>();
        if (arrStr != null && arrStr.length() > 0) {
            try {
                JSONArray toArr = new JSONArray(arrStr);
                int toArrLength = toArr.length();
                for (int i = 0; i < toArrLength; i++) {
                    JSONObject jobj = (JSONObject) toArr.get(i);
                    String _email = jobj.getString("email");
                    String name = jobj.getString("name");
                    MailContacts mc = new MailContacts();
                    if (TextUtils.isEmpty(name)) {
                       name = _email;
                    }
                    mc.setAddress(_email.toLowerCase());
                    mc.setPersonal(name.toLowerCase());
                    mcList.add(mc);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return mcList;
    }

    public static MailContacts parseContactFromJSON(String contactJSON) {
        MailContacts mc = null;
        try {
            mc = new MailContacts();
            JSONObject from = new JSONObject(contactJSON);
            String name = from.getString("name");
            String email = from.getString("email");
            if (TextUtils.isEmpty(name)) {
                name = email;
            }
            mc.setPersonal(name.toLowerCase());
            mc.setAddress(email.toLowerCase());
        } catch (JSONException e) {
            Log.e("parseContactJSON error", "contactJSON = [" + contactJSON + "]");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return mc;
    }

    public static void fillWordWrapView(WordWrapView alfv, String arrStr, Context context,boolean showParent) {
        List<MailContacts> mcList = parseContactListFromJSON(arrStr);
        if (mcList.size() > 0) {
            if(showParent) ((RelativeLayout)alfv.getParent()).setVisibility(View.VISIBLE);
            for (MailContacts mc : mcList) {
//                LinearLayout ll = new LinearLayout(context);
                TextView nametv = new TextView(context);
//                TextView emailtv = new TextView(context);
                nametv.setTextColor(alfv.getResources().getColor(R.color.wy_common_text_color));
                nametv.setTextSize(16);
//                nametv.setPadding(0, 0, 4, 2);
//                        nametv.setBackgroundColor(alfv.getResources().getColor(R.color.md_contact_bg));
                nametv.setText(mc.getPersonal()+" ;");
//                nametv.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        LinearLayout ll = (LinearLayout)view.getParent();
//                        String email = ((TextView)ll.getChildAt(ll.getChildCount()-1)).getText().toString();
//                        Log.i("ren bei dian le--",email);
//                    }
//                });
//                emailtv.setText(mc.getAddress());
//                emailtv.setVisibility(View.GONE);
//                ll.addView(nametv);
//                ll.addView(emailtv);
                alfv.addView(nametv);
            }
        }else {
            if(showParent) ((RelativeLayout)alfv.getParent()).setVisibility(View.GONE);
        }
    }

    public static UserInfoBean parseJSONStrToUserInfoBean(String jsonStr){
        UserInfoBean u = new UserInfoBean();
        try {
            JSONObject jo = new JSONObject(jsonStr);
            u = parseJSONObjToUserInfoBean(jo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }

    public static UserInfoBean parseJSONObjToUserInfoBean(JSONObject jo){
        UserInfoBean u = new UserInfoBean();
        try {
            u.email = jo.getString("email");
            u.id = jo.getString("id");
            u.name = jo.getString("name");
            if(TextUtils.isEmpty(u.id)){
                u.id = u.email.split("@")[0];
            }
            if(TextUtils.isEmpty(u.name)){
                u.name = u.email;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }

    public static ArrayList<UserInfoBean> parseJSONStrToUserInfoBeanList(String jsonStr){
        ArrayList<UserInfoBean> uibList = new ArrayList<>();
        if (jsonStr != null && jsonStr.length() > 0) {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(jsonStr);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = (JSONObject) jsonArray.get(i);
                    UserInfoBean ccu = parseJSONObjToUserInfoBean(jo);
                    uibList.add(ccu);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return uibList;
    }

    /**
     * 把UserInfoBean对象转化成字符串
     * @param u
     * @return
     */
    public static String getUserJSONStr(UserInfoBean u) {
//        Log.i(TAG, "getUserJSONStr: "+u.toString());
        if (TextUtils.isEmpty(u.name))
            u.name = u.email;
        return "{\"id\":\"" + u.id + "\",\"name\":\"" + u.name + "\",\"email\":\"" + u.email + "\"}";
    }

    public static String getUserJSON(String email,String name){
        if (TextUtils.isEmpty(name)) name = email;
        return "{\"id\":\"" + email.split("@")[0] + "\",\"name\":\"" + name + "\",\"email\":\"" + email + "\"}";
    }

    /**
     * 把UserInfoBean 对象组成的list 拼接成字符串
     * @param uList
     * @return
     */
    public static String getUserArrJSONStr(List<UserInfoBean> uList) {
        String res = "[";
        if (uList.size() > 0) {
            for (UserInfoBean u : uList) {
//                Log.i(TAG, "getUserJSONStr(u): " + getUserJSONStr(u));
                res += getUserJSONStr(u) + ",";
            }
            res = res.substring(0, res.length() - 1);
        }
        return res + "]";
    }


    /**
     * 动态设置ListView的高度
     * @param listView
     */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        if(listView == null) return;
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
            Log.d(TAG, "setListViewHeightBasedOnChildren() listItem.getMeasuredHeight() = [" + listItem.getMeasuredHeight() + " totalHeight = "+totalHeight);
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    /**
     * 检测邮箱地址是否合法
     * @param email
     * @return true合法 false不合法
     */
    public static boolean isEmail(String email){
        if (null==email || "".equals(email)) return false;
//        Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
        Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static String readTxtFile(String strFilePath){
        String path = strFilePath;
        String content = ""; //文件内容字符串
        //打开文件
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()||!file.exists()){
            Log.d(TAG, "ReadTxtFile错误，文件不存在");
        }
        else{
            try {
                InputStream instream = new FileInputStream(file);
                if (instream != null)
                {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    //分行读取
                    while (( line = buffreader.readLine()) != null) {
                        content += line + "\n";
                    }
                    instream.close();
                }
            }
            catch (java.io.FileNotFoundException e)
            {
                Log.d("TestFile", "The File doesn't not exist.");
            }
            catch (IOException e)
            {
                Log.d("TestFile", e.getMessage());
            }
        }
        return content;
    }
//
//    /**
//     * 把邮件内的内嵌资源（图片等）引用地址 由cid 换成 base64形式的
//     * 如：src=3D"cid:image001.png@01D1A61A.F6783A90"  换成 src=3D"data:image/png;base64,KXKLHWOLZJNXKL..."
//     * @param email
//     * @return
//     */
//    public static String getFullHtmlBody(CkEmail email, String mailAddress,List<MailAttachment> currAttachmentList){
//        String body = email.htmlBody();
//        int numRelatedItems = email.get_NumRelatedItems();
//        Log.i(TAG, "getFullHtmlBody: numRelatedItems = "+numRelatedItems);
//        if(numRelatedItems>0){
//            for(int i = 0;i<numRelatedItems;i++){
//                CkByteData cbd = new CkByteData();
//                boolean success = email.GetRelatedData(i,cbd);
//                if(success){
//                    String innerSrcFileName = email.getRelatedFilename(i);//内嵌资源 文件名
//                    String innerSrcFileType = innerSrcFileName.substring(innerSrcFileName.lastIndexOf(".")+1,innerSrcFileName.length()).toLowerCase();
//
//                    if(body.contains(innerSrcFileName)){
//                        String innerSrcId = email.getRelatedContentID(i);//内嵌资源 ID
//                        Log.i(TAG, "getFullHtmlBody: innerSrcId = "+innerSrcId);
//                        String base64Source = Base64.encodeToString(cbd.toByteArray(), Base64.DEFAULT);//base64化的文件（通常是图片）
//                        body = body.replace("cid:"+innerSrcId,"data:image/*"+innerSrcFileType+";base64,"+base64Source);//开始替换
//                    }else{//如果内嵌资源未被邮件正文所引用，则把其当成是附件并保存到本地
//                        String filePath = FileUtil.getCurrMailAttachmentsPath(mailAddress) + "/" + innerSrcFileName;
//                        cbd.saveFile(filePath);
//                        File af = new File(filePath);
//                        MailAttachment attachment = new MailAttachment(null, af.getName(), af.getPath(), null, af.length(), mailAddress, new Date(), 0);
//                        currAttachmentList.add(attachment);
//                    }
//                }else{
//                    Log.e(TAG, "获取内嵌资源失败，getFullHtmlBody: "+email.lastErrorText() );
//                }
//            }
//        }
//        return body;
//    }

}
