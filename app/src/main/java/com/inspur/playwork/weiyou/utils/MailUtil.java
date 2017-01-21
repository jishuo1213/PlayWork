package com.inspur.playwork.weiyou.utils;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebView;

import com.inspur.fan.decryptmail.DecryptMail;
import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.common.UserInfoBean;
import com.inspur.playwork.utils.FileUtil;
import com.inspur.playwork.utils.db.bean.MailAccount;
import com.inspur.playwork.utils.db.bean.MailAttachment;
import com.inspur.playwork.utils.db.bean.MailDetail;
import com.inspur.playwork.utils.db.bean.MailTask;
import com.inspur.playwork.utils.encryptUtil.EncryptUtil;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.util.MailSSLSocketFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

public class MailUtil {

    private static final String TAG = "MailUtil--------";

    /**
     * 填充邮件内容到 MimeMultipart 对象
     * @param content     邮件内容
     * @return 返回结果
     */
    public static void fillContentToMessage(SMTPMessage mailMessage, String content, List<MailAttachment> mailAttachments)
            throws MessagingException, IOException {
        // 发送HTML邮件创建一个MimeMultipart对象
        MimeMultipart mmp = new MimeMultipart();
        MimeBodyPart htmlContent = new MimeBodyPart();
        htmlContent.setContent(content, "text/html; charset=utf-8");
        mmp.addBodyPart(htmlContent);
        if(mailAttachments.size()>0){
            // create the Multipart and add its parts to it
            for(MailAttachment ma : mailAttachments){
                // create the message part for attachment
                MimeBodyPart mbp2 = new MimeBodyPart();
//                Log.i(TAG, "createMessage: filePath = " + ma.getPath());
                File attach = new File(ma.getPath());
                if(attach.exists()){
                    mbp2.attachFile(attach);
                    mbp2.setFileName(ma.getName());
                    mmp.addBodyPart(mbp2);
                }else{
                    Log.e(TAG, "createMessage: attachment file is not existed" );
                }
            }
        }

        // 将MimeMultipart对象设置为邮件内容
        mailMessage.setContent(mmp);
        // 保存邮件内容修改
//        mailMessage.saveChanges();
    }

    public static Session createSmtpSessionObject(MailTask mailTask) {
        final String username = EncryptUtil.aesDecryptAD(mailTask.getAccount());
        final String password = EncryptUtil.aesDecryptAD(mailTask.getPassword());
        Properties properties = new Properties();
        if (mailTask.getOutgoingSSL()) {
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.smtps.host", mailTask.getOutgoingHost());
            properties.put("mail.smtps.port", mailTask.getOutgoingPort());
            properties.put("mail.smtps.connectiontimeout", 30000);
            properties.put("mail.smtps.timeout", 60000);
            properties.put("mail.smtps.ssl.trust", "*");
            //如果有无效的收件人地址  设置成true 会把邮件发送给有效的收件人地址,并把无效的地址生成一个 SendFailedException异常
            //设置成false 则邮件谁都不给发送
            properties.put("mail.smtps.sendpartial", true);
            if (mailTask.getOutgoingTLS()) {
                properties.put("mail.smtps.starttls.enable", "true");
            }
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.host", mailTask.getOutgoingHost());
            properties.put("mail.smtp.port", mailTask.getOutgoingPort());
            properties.put("mail.smtp.connectiontimeout", 30000);
            properties.put("mail.smtp.timeout", 60000);

            properties.put("mail.smtp.ssl.trust", "*");
            //如果有无效的收件人地址  设置成true 会把邮件发送给有效的收件人地址,并把无效的地址生成一个 SendFailedException异常
            //设置成false 则邮件谁都不给发送
            properties.put("mail.smtp.sendpartial", true);
            if (mailTask.getOutgoingTLS()) {
                properties.put("mail.smtp.starttls.enable", "true");
            }
        }
        return Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * 发送（加密、签名）邮件
     * <p/>
     * 1.加密邮件：
     * 1).先取所有收件人抄送人的公钥，没有公钥的提示是否发送明文
     * 2).生成一个只设置了Content的Message对象，并writeTo 进一个临时文件中
     * 3).遍历每个有公钥的收件人抄送人:
     * a.根据每个人的公钥加密之前保存的临时文件，生成各自的加密后的邮件文件，
     * b.生成一个新Message对象，如：
     * MimeMessage smtpMessage = new MimeMessage(session,new FileInputStream(mailPath));
     * c.填充进其他邮件信息，!!!( 提取出方法 )
     * d.最后调用send(message,[new InternalAddress(email)])发送
     * <p/>
     * 2.签名邮件
     * 1).生成一个只设置了Content的Message对象，并writeTo 进一个临时文件中
     * 2).签名临时文件,生成签名后的邮件文件
     * 3).生成一个新Message对象，如：
     * MimeMessage smtpMessage = new MimeMessage(session,new FileInputStream(mailPath));
     * 4).填充进其他邮件信息，!!!( 提取出方法 )
     * 5).发送邮件
     * <p/>
     * 3.普通邮件直接发
     *
     * @return 返回结果
     */
    public static boolean sendMail(MailTask mailTask, DecryptMail dm,
                                   TransportListener transportListener) throws MessagingException, IOException, InterruptedException, JSONException {
        //创建一个邮件发送服务会话
        Session session = createSmtpSessionObject(mailTask);

        Transport transport = session.getTransport();
        transport.addTransportListener(transportListener);
        transport.connect();
        ArrayList<UserInfoBean> toList = WeiYouUtil.parseJSONStrToUserInfoBeanList(mailTask.getTo());
        ArrayList<UserInfoBean> ccList = WeiYouUtil.parseJSONStrToUserInfoBeanList(mailTask.getCc());
        if (!mailTask.getEncrypted()) {
            SMTPMessage mailMessage = new SMTPMessage(session, new FileInputStream(mailTask.getMessageFilePath()));
            fillDataToMessage(mailTask, mailMessage,toList,ccList);
            Address [] rcptArr = mailMessage.getAllRecipients();
//            Log.i(TAG, "send normal Mail: allRcptArr.length = "+rcptArr.length);
            transport.sendMessage(mailMessage,rcptArr);
            Thread.sleep(AppConfig.WY_CFG.SEND_MAIL_INTERVAL);
            return true;
        } else {
            // 创建一个 所有 收邮件的人的集合 包括所有 收件人和所有抄送人
            Map<String, String> publicKeyMap = transferPublicKeysToArrayMap(mailTask.getPublicKeys());
            execSendEncryptedMailToOnRcpt(session,mailTask,publicKeyMap,toList,ccList,dm,transport);
        }
        return true;
    }

    private static void execSendEncryptedMailToOnRcpt(Session session, MailTask mailTask, Map<String, String> publicKeyMap,
                  ArrayList<UserInfoBean> toList, ArrayList<UserInfoBean> ccList, DecryptMail dm, Transport transport) throws
            JSONException, IOException, MessagingException, InterruptedException {
        Log.i(TAG, "execSendEncryptedMailToOnRcpt: mailTask.getMailRcpts() = "+mailTask.getMailRcpts());
        JSONArray ja = new JSONArray(mailTask.getMailRcpts());
        int jalen = ja.length();
        Log.i(TAG, "execSendEncryptedMailToOnRcpt: JSONArray length = "+jalen);
        Log.i(TAG, "execSendEncryptedMailToOnRcpt: mailTask.getRcptCount() = "+mailTask.getRcptCount());
        Log.i(TAG, "execSendEncryptedMailToOnRcpt: mailTask.getSentRcptCount() = "+mailTask.getSentRcptCount());
        if(jalen == 0 || mailTask.getRcptCount() <= mailTask.getSentRcptCount()) return;
        JSONObject jo = ja.optJSONObject(0);
        String _email = jo.optString("address");
//                Log.i(TAG, "sendMail: mailTask.getMailRcpts() >> rcpt jo = "+jo.toString());
        String mailFilePathTemp = mailTask.getMessageFilePath() + "_encrypted";

        File tempFile = new File(mailFilePathTemp);
        tempFile.createNewFile();
        String pbk = publicKeyMap.get(_email.split("@")[0]);
        String msgFilePath = mailTask.getMessageFilePath();
//        Log.i(TAG, "execSendEncryptedMailToOnRcpt: msgFilePath = "+msgFilePath);
        dm.encryptMail(msgFilePath, mailFilePathTemp, pbk);
        // 创建一个新Message对象，并读取加密后的邮件文件，使其Content 为加密后的密文
        SMTPMessage msg = new SMTPMessage(session, new FileInputStream(mailFilePathTemp));
        // 填充其他邮件数据
        fillDataToMessage(mailTask,msg,toList,ccList);
        //把邮件接收者加入邮件接收者缓存集合中
        Address rcpt = new InternetAddress(_email, jo.optString("name"));
        Address[] rcptArr = {rcpt};
        //Send the message to the specified addresses, ignoring any recipients specified in the message itself.
        // The send method calls the saveChanges method on the message before sending it.
        transport.sendMessage(msg, rcptArr);//此处 send方法 必须得添加rcpt参数
        Thread.sleep(AppConfig.WY_CFG.SEND_MAIL_INTERVAL);
        execSendEncryptedMailToOnRcpt(session,mailTask,publicKeyMap,toList,ccList,dm,transport);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static Map<String,String> transferPublicKeysToArrayMap(String pbkJSONStr){
        ArrayMap<String,String> pbkAM = new ArrayMap<>();
        if(pbkJSONStr!=null && pbkJSONStr.length()>0){
            try {
                JSONArray ja = new JSONArray(pbkJSONStr);
                int jaLen = ja.length(),i;
                if(jaLen>0){
                    for(i=0;i<jaLen;i++){
                        JSONObject jo = ja.optJSONObject(i);
                        pbkAM.put(jo.optString("UserId"),jo.optString("RawData"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pbkAM;
    }

    /**
     * 向Message对象里填充其他邮件数据
     * @param mailTask
     * @param message
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static void fillDataToMessage(MailTask mailTask, Message message, ArrayList<UserInfoBean> toList,
             ArrayList<UserInfoBean> ccList) throws MessagingException, UnsupportedEncodingException {

        // 发件人地址
        Address from = (TextUtils.isEmpty(mailTask.getNickName())) ?
                new InternetAddress(mailTask.getEmail()) : new InternetAddress(mailTask.getEmail(), mailTask.getNickName());
        // 设置邮件消息的发送者
        message.setFrom(from);
        // 创建邮件的接收者地址，并设置到邮件消息中
        int toSize = toList.size();
        if (toSize>0) {
            InternetAddress toia[] = new InternetAddress[toSize];
            for (int i = 0; i < toSize; i++) {
                UserInfoBean uib = toList.get(i);
                InternetAddress address = new InternetAddress(uib.email.trim(), uib.name.trim());
                toia[i] = address;
            }
            message.setRecipients(Message.RecipientType.TO, toia);
        } else {
            Log.e(TAG, "sendMail: 没有收件人 toList.size() == 0");
        }

        // 创建邮件的接收者地址，并设置到邮件消息中
        int ccSize = ccList.size();
//        Log.i(TAG, "fillDataToMessage: ccSize = "+ccSize);
        if (ccSize>0) {
            InternetAddress ccia[] = new InternetAddress[ccSize];
            for (int i = 0; i < ccSize; i++) {
                UserInfoBean uib = ccList.get(i);
//                Log.i(TAG, "fillDataToMessage: cc uib.email = "+uib.email+" uib.name = "+uib.name);
                InternetAddress address = new InternetAddress(uib.email.trim(), uib.name.trim());
                ccia[i] = address;
            }
            message.setRecipients(Message.RecipientType.CC, ccia);
        }

        // 设置邮件消息的主题
        message.setSubject(mailTask.getSubject());

        // 设置邮件消息发送的时间
        message.setSentDate(new Date(mailTask.getCreateTime()));
        String references = mailTask.getReferences();
        if(references != null){
            message.setHeader("References",references);
        }
//        // 保存邮件内容修改
//        message.saveChanges();

    }

    /**
     * 替换字符串中的Html特殊字符
     *
     * @param str 字符串
     * @return 替换后的字符串
     */
    private String replaceHtml(String str) {
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("\"", "&quot;");
        str = str.replaceAll("&", "&amp;");
        return str;
    }

    /**
     * 【保存附件】
     */
    public static void saveAttachments(Part part, String email, List<MailAttachment> mal) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                MimeBodyPart mpart = (MimeBodyPart) mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                // 如果该BodyPart对象包含附件，则应该解析出来 Content-Disposition
                if (disposition != null) {
                    if(!disposition.equals("inline")) {
                        fileName = mpart.getFileName();
                        if (fileName != null && fileName.startsWith("=?")) {
                            // 解析文件名
                            fileName = MimeUtility.decodeText(fileName);
                        }

//                    String filePath = FileUtil.getCurrMailAttachmentsPath(email)+fileName;
                        String filePath = FileUtil.getMailCachePath() + fileName;
                        mpart.saveFile(filePath);
                        mal.add(new MailAttachment(null, fileName, filePath, null, new Long(mpart.getSize()), email, new Date(), 0));
                    }
                } else if (mpart.isMimeType("multipart/*")) {
                    saveAttachments(mpart, email, mal);
                } else {
                    fileName = mpart.getFileName();
                    if (fileName != null) {
                        fileName = MimeUtility.decodeText(fileName);
//                        String filePath = FileUtil.getCurrMailAttachmentsPath(email)+fileName;
                        String filePath = FileUtil.getMailCachePath()+fileName;
                        mpart.saveFile(filePath);
                        mal.add(new MailAttachment(null, fileName, filePath, null, new Long(mpart.getSize()), email, new Date(), 0));
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachments((Part) part.getContent(), email, mal);
        }
    }

    /**
     * 获取邮件正文内容
     *
     * @param msg 邮件体
     */
    public static void getMailText(Part msg, MailDetail md, WebView wv) {
        StringBuilder textContent = new StringBuilder();
        List<Map<String, String>> cidList = new ArrayList<Map<String, String>>();
        try {

            // 读取邮件内容
//            Log.i(TAG, "getMailText: ```````````````"+md.getUid());
            setContentTextType("text/plain");
            getMailTextContent(msg, textContent, cidList);

//            Log.i(TAG, "getMailText: ```````````````获取文本完毕 textContent ="+textContent);
//            Log.i(TAG, "getMailText: ```````````````获取文本完毕，开始页面编码转换 hasHtml ="+hasHtml);
            String text = replaceInnerSource(textContent,cidList);
            // 获取邮件内容
            if(text.length()>1024*1024) {
                String filePath = FileUtil.getMailCachePath() + md.getUid() + ".xhw";
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.write(text.getBytes());
                fos.close();
                wv.loadUrl(Uri.fromFile(new File(filePath)).toString());
            }else
                wv.loadDataWithBaseURL(null, text,getContentTextType() , "utf-8", null);
            md.setContent(text);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "获取邮件正文过程中出现异常：", e);
        }
    }

    private static String replaceInnerSource(StringBuilder content,List<Map<String, String>> cidList){
        String text = content.toString();
        text = text.replaceFirst("charset=[A-Za-z0-9\\-]+\"", "charset=utf-8\"");
//            Log.i(TAG, "getMailText: ```````````````页面编码转换完毕，开始替换内嵌资源"+md.getUid());
        int size = cidList.size();
        Map<String, String> cidMap;
        String cid;
        String url;
        for (int j = 0; j < size; j++) {
            cidMap = cidList.get(j);
            cid = cidMap.get("cid");
            url = cidMap.get("url");
            text = text.replaceAll("cid:" + cid, url);
        }
//        Log.i(TAG, "getMailText: ```````````````替换内嵌资源完毕 = ");
        return text;
    }

    private static String contentTextType = "text/html";
    private static void setContentTextType(String flag){
        contentTextType = flag;
    }
    public static String getContentTextType(){
        return contentTextType;
    }

    /**
     * 获取邮件正文内容
     *
     * @param part    邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws Exception
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void getMailTextContent(Part part, StringBuilder content, List<Map<String, String>> cidList) throws Exception {
        // 如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") != -1;
//        Log.i(TAG, isContainTextAttach + "---getMailTextContent: part.getContentType();"+part.getContentType());
        if (part.isMimeType("text/*") && !isContainTextAttach) {// 邮件内容是纯文本
            String textContent="";
            try{
                MimePart mpart = (MimePart)part;
                Log.i(TAG, "getMailTextContent: "+mpart.getEncoding());
                if(mpart.getEncoding().equals("quotedprintable")){
                    mpart.setHeader("Content-Transfer-Encoding","quoted-printable");
                }
                else if(mpart.getEncoding().equals("x-unknown")){
                    mpart.setHeader("Content-Transfer-Encoding","binary");
                }
                textContent = (String)(mpart.getContent());
            }catch(Exception e){
                e.printStackTrace();
//                if(((MimePart) part).getEncoding().equals("x-unknown"))
//                textContent = (String)part.getgetContent();
            }
            content.append(textContent);
//            Log.i(TAG, "getMailTextContent: text/* ");
            if(part.isMimeType("text/html")){
                setContentTextType("text/html");
            }
//            Log.i(TAG, "getMailTextContent: \"text/*\" "+hasHtml);
        } else if (part.isMimeType("message/rfc822") && !isContainTextAttach) {
            getMailTextContent((Part) part.getContent(), content, cidList);
        } else if (part.isMimeType("multipart/alternative")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                // 过滤掉text/plain的重复内容
                if (bodyPart.isMimeType("text/html")) {
//                    getMailTextContent(bodyPart, content, cidList, md);
                    String textContent = bodyPart.getContent().toString();
                    content.append(textContent);
                }
//                Log.i(TAG, "getMailTextContent: text/html  "+bodyPart.getContent().toString());
            }
            setContentTextType("text/html");
//            Log.i(TAG, "getMailTextContent: alternative ");
        } else if (part.isMimeType("multipart/related")) {//内嵌资源
//            Log.i(TAG, "getContentType: multipart/related 内嵌资源");
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            ArrayMap<String, String> cidMap;
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String filename = bodyPart.getFileName();
                if (filename != null) {
                    if (filename.startsWith("=?")) {
                        // 解析文件名
                        filename = MimeUtility.decodeText(filename);
                    }
                    String mimeType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
                    InputStream in = bodyPart.getInputStream();
                    byte[] data = new byte[in.available()];
                    in.read(data);
                    in.close();
                    String base64str = Base64.encodeToString(data, Base64.DEFAULT);
                    String srcStr = "data:" + mimeType + ";base64," + base64str;
                    // 暂存内嵌文件的cid信息，用于后面统一进行替换
                    cidMap = new ArrayMap<>();
                    String cid = ((MimeBodyPart) bodyPart).getContentID();
                    cidMap.put("cid", cid.substring(1, cid.length() - 1));
                    cidMap.put("url", srcStr);
                    cidList.add(cidMap);

                } else {
                    getMailTextContent(bodyPart, content, cidList);
                }
            }
        } else if (part.isMimeType("multipart/*")) {
//            Log.i(TAG, "getMailTextContent: multipart/* :");
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content, cidList);
            }
        }
    }
//    /**
//     * 【真正的保存附件到指定目录里】
//     */
//    private static File saveFile(String storeFilePath, InputStream in) throws Exception {
//        Log.i(TAG, "saveFile: storeFilePath = "+storeFilePath);
//        File storefile = new File(storeFilePath);
//        BufferedOutputStream bos = null;
//        try {
//            bos = new BufferedOutputStream(new FileOutputStream(storefile));
//            byte[] buf = new byte[2048];
//            while (in.read(buf) != -1) {
//                bos.write(buf);
//            }
//            bos.flush();
//        } catch (Exception exception) {
//            exception.printStackTrace();
//            throw new Exception("文件保存失败!");
//        } finally {
//            bos.close();
//            in.close();
//        }
//        return storefile;
//    }

    /**
     * 获取邮件正文内容
     * @param part
     * @param md
     * @return int encType 0：不加密不签名；1：先加密 2：先签名
     * @throws Exception
     */
    public static int getMailEncType(Part part,MailDetail md) throws Exception {
        int encType = 0;
        // 如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        if (part.isMimeType("message/rfc822")) {
            getMailEncType((Part) part.getContent(),md);
        } else if ( part.isMimeType("application/pkcs7-signature")||
                part.isMimeType("application/x-pkcs7-signature")||
                part.getContentType().contains("smime-type=signed-data")) {
            if(encType==0) encType = 2;
            // 是签名邮件
            md.setSigned(true);
        } else if (part.isMimeType("application/pkcs7-mime")
                || part.isMimeType("application/x-pkcs7-mime")) {
            if(encType==0) encType = 1;
            // 是加密邮件
            md.setEncrypted(true);
        } else if (part.isMimeType("multipart/*")) {
            if(part.isMimeType("multipart/signed")) {
                if(encType==0) encType = 2;
                md.setSigned(true);
            }
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailEncType(bodyPart,md);
            }
        }
        return encType;
    }

    /**
     * 根据UID删除服务器上的邮件
     *
     * @param map 参数
     * @return 返回结果
     */
    public static boolean deleteMailByUID(Map<String, String> map) {

        // 获取邮件UID参数
        String pUID = (String) map.get("uid");
        if (pUID == null) {
            Log.e(TAG, "UID不能为空");
            return false;
        }

        // 获取邮件服务器地址
        String host = (String) map.get("receiveServer");
        if (host == null || "".equals(host)) {
            host = "mail.inspur.com";
        }
        // 获取端口
        int port;
        String strPort = (String) map.get("receivePort");
        if (strPort != null && !"".equals(strPort)) {
            port = Integer.parseInt(strPort);
        } else {
            port = 995;
        }

        // 获取接收邮件协议
        String protocol = (String) map.get("protocol");
        if (protocol == null || "".equals(protocol)) {
            protocol = "pop3s";
        }

        // 获取用户账号
        String username = (String) map.get("username");
        String password = (String) map.get("password");

        try {

            // 创建属性对象
            Properties props = new Properties();
            props.put("mail.mime.address.strict", "false");
            if ("pop3s".equals(protocol)) {
                // props.setProperty("mail.pop3.ssl.enable", "true");
                // props.setProperty("mail.protocol.ssl.trust", "mail.inspur.com");
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustedHosts(new String[]{host});
                props.put("mail.pop3s.ssl.socketFactory", sf);
                // props.setProperty("mail.pop3s.port", "995");
            }

            // 获取会话对象
            Session session = Session.getInstance(props);
            // session.setDebug(true);

            // 获取存储对象
            Store store = session.getStore(protocol);
            store.connect(host, port, username, password);

            // 读取收件箱
            POP3Folder folder = (POP3Folder) store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            // 获取收件箱信息
            Message messages[] = folder.getMessages();
            // 解析邮件信息
            MimeMessage message;
            String uid;
            for (int i = 0, n = messages.length; i < n; i++) {
                message = (MimeMessage) messages[i];
                uid = folder.getUID(message);
                if (uid != null && uid.equals(pUID)) {
                    message.setFlag(Flags.Flag.DELETED, true);
                    break;
                }
            }

            // 关闭连接
            folder.close(true);
            store.close();

            Log.i(TAG, "用户" + username + "删除邮件成功！");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "用户" + username + "删除邮件出现异常：", e);
            return false;
        }
    }

    /**
     * 获取邮件主题
     *
     * @param msg 邮件内容
     * @return 解码后的邮件主题
     */
    public static String getSubject(MimeMessage msg) throws UnsupportedEncodingException,
            MessagingException {
        String subject = msg.getSubject();
        if (subject != null) {
            subject = MimeUtility.decodeText(subject);
        } else {
            subject = "";
        }
        return subject;
    }

    /**
     * 获取邮件发件人
     *
     * @param msg 邮件内容
     * @return Map<String, String> 发件人
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public static UserInfoBean getFromMap(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        Address[] froms = msg.getFrom();
        if (froms.length < 1)
            throw new MessagingException("没有发件人!");

        InternetAddress address = (InternetAddress) froms[0];
        String personal = address.getPersonal();
        if (personal != null) {
            personal = MimeUtility.decodeText(personal);
        } else {
            personal = "";
        }
        String email = address.getAddress();
        String id = "";
        if (email != null && !"".equals(email)) {
            id = email.substring(0, email.indexOf("@"));
        }
        UserInfoBean uib = new UserInfoBean();
        uib.name = personal;
        uib.email = email;
        uib.id = id;

        return uib;
    }

    /**
     * 根据收件人类型，获取邮件收件人、抄送和密送地址。如果收件人类型为空，则获取所有的收件人
     * Message.RecipientType.TO 收件人
     * Message.RecipientType.CC 抄送
     * Message.RecipientType.BCC 密送
     *
     * @param msg  邮件内容
     * @param type 收件人类型
     * @return List<Map<String, String>> 收件人
     */
    public static ArrayList<UserInfoBean> getRecipients(MimeMessage msg, Message.RecipientType type) {
        ArrayList<UserInfoBean> recipients = new ArrayList<UserInfoBean>();
        Address[] addresss = null;
        try {
            if (type == null) {
                addresss = msg.getAllRecipients();
            } else {
                addresss = msg.getRecipients(type);
            }
            if (addresss != null && addresss.length > 0) {
                for (Address address : addresss) {
                    InternetAddress internetAddress = (InternetAddress) address;
                    String personal = internetAddress.getPersonal();
                    if (personal != null) {
                        personal = MimeUtility.decodeText(personal);
                    } else {
                        personal = "";
                    }
                    String email = internetAddress.getAddress();
                    String id = "";
                    if (email != null && !"".equals(email)) {
                        id = email.substring(0, email.indexOf("@"));
                    }
                    UserInfoBean uib = new UserInfoBean();
                    uib.name = personal;
                    uib.email = email;
                    uib.id = id;
                    recipients.add(uib);
                }
            }
        } catch (MessagingException e) {
            Log.e(TAG, "获取收件人过程中出现异常：", e);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "获取收件人过程中出现异常：", e);
        }
        return recipients;
    }

    /**
     * 【判断此邮件是否已读，如果未读返回返回false,反之返回true】
     */
    public static boolean isNew(Message mimeMessage) throws MessagingException {
        boolean isnew = false;
        Flags flags = mimeMessage.getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
//        System.out.println("flags's length: " + flag.length);
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isnew = true;
//                System.out.println("seen Message.......");
                break;
            }
        }
        return isnew;
    }


    public static boolean verifyMailAccount(MailAccount mailAccount) throws MessagingException, GeneralSecurityException {
        boolean res = false;
        final String username = EncryptUtil.aesDecryptAD(mailAccount.getAccount());
        final String password = EncryptUtil.aesDecryptAD(mailAccount.getPassword());
        Properties properties = new Properties();
        if (mailAccount.getOutGoingSSL()) {
            properties.put("mail.smtps.auth", "true");
            properties.put("mail.smtps.host", mailAccount.getOutGoingHost());
            properties.put("mail.smtps.port", mailAccount.getOutGoingPort());
            properties.put("mail.smtps.connectiontimeout", 10000);
            properties.put("mail.smtps.timeout", 10000);
            properties.put("mail.smtps.ssl.trust", "*");
            //如果有无效的收件人地址  设置成true 会把邮件发送给有效的收件人地址,并把无效的地址生成一个 SendFailedException异常
            //设置成false 则邮件谁都不给发送
            properties.put("mail.smtps.sendpartial", true);
//                if (mailAccount.getOutGoingTLS()) {
//                    properties.put("mail.smtps.starttls.enable", "true");
//                }
        } else {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.host", mailAccount.getOutGoingHost());
            properties.put("mail.smtp.port", mailAccount.getOutGoingPort());
            properties.put("mail.smtp.connectiontimeout", 10000);
            properties.put("mail.smtp.timeout", 10000);

            properties.put("mail.smtp.ssl.trust", "*");
            //如果有无效的收件人地址  设置成true 会把邮件发送给有效的收件人地址,并把无效的地址生成一个 SendFailedException异常
            //设置成false 则邮件谁都不给发送
            properties.put("mail.smtp.sendpartial", true);
//                if (mailAccount.getOutGoingTLS()) {
//                    properties.put("mail.smtp.starttls.enable", "true");
//                }
        }

        Session smtpSession = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        Transport transport = smtpSession.getTransport();
        transport.connect();
        transport.close();
        smtpSession = null;
        /* -------------------------------------------------------------------------------------- */
        properties.clear();
        // 创建属性对象
        properties.put("mail.mime.address.strict", "false");
        if ("pop3s".equals(mailAccount.getProtocol())) {
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustedHosts(new String[]{mailAccount.getInComingHost()});
            properties.put("mail.pop3s.ssl.socketFactory", sf);
        }

        // 获取会话对象
        Session pop3Session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        POP3Store store = (POP3Store) pop3Session.getStore(mailAccount.getProtocol());
        store.connect(mailAccount.getInComingHost(), Integer.parseInt(mailAccount.getInComingPort()), username, password);
        POP3Folder folder = (POP3Folder) store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        FetchProfile profile = new FetchProfile();
        profile.add(UIDFolder.FetchProfileItem.UID);
        folder.close(false);
        store.close();
        res = true;
        return res;
    }


//
//    public static void toggleDownloadLock(boolean flag){
//        PreferencesHelper.getInstance().writeToPreferences("isDownloadingVUMail",flag);
//    }
//    public static boolean getDownloadLock(){
//        return PreferencesHelper.getInstance().readBooleanPreference("isDownloadingVUMail");
//    }
//    public static void setNewestMailSentDate(String email ,long timeValue){
//        PreferencesHelper.getInstance().writeToPreferences(email+"_newestMailSentDate",timeValue);
//    }
//    public static long getNewestMailSentDate(String email){
//        return PreferencesHelper.getInstance().readLongPreference(email+"_newestMailSentDate");
//    }

}