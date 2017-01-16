package com.inspur.playwork.utils;

import com.inspur.playwork.config.AppConfig;
import com.inspur.playwork.model.message.ImgMsgXmlBean;
import com.inspur.playwork.model.message.MessageBean;
import com.inspur.playwork.model.timeline.TaskAttachmentBean;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


/**
 * Created by Fan on 15-10-9.
 */
public class XmlHelper {

    private static final String TAG = "XmlHelperfan";


    public static ImgMsgXmlBean praseChatImageMsg(String content) {
        return getImgMsgXmlBean(content);
    }

    private static ImgMsgXmlBean getImgMsgXmlBean(String content) {
        ImgMsgXmlBean result = new ImgMsgXmlBean();

        org.jsoup.nodes.Document doc = Jsoup.parse(content);

        Elements img = doc.getElementsByTag("img");
        if (img.hasAttr("imagesrc")) {
            result.src = AppConfig.UPLOAD_FILE_URI_SERVICE + img.attr("imagesrc");
            result.id = img.attr("id");
            return result;
        }
        return null;
    }

    public static TaskAttachmentBean getFileMsgXmlBean(String content) {
        org.jsoup.nodes.Document doc = Jsoup.parse(content);
        Elements file = doc.getElementsByTag("a");
        if (file.hasAttr("_href")) {
            TaskAttachmentBean taskAttachmentBean = new TaskAttachmentBean();
            taskAttachmentBean.attachPath = file.attr("_href");
            taskAttachmentBean.attachmentName = file.attr("_name");
            taskAttachmentBean.attachSize = Long.parseLong(file.attr("_size"));
            taskAttachmentBean.docId = taskAttachmentBean.attachPath.split("&")[0].substring(11);
            if (file.hasAttr("local_path")) {
                taskAttachmentBean.localPath = file.attr("local_path");
            }
            return taskAttachmentBean;
        }
        return null;
    }

    public static String genertImgXmlMsg(String id, String src) {

        org.dom4j.Document document = DocumentHelper.createDocument();

        Element rootElement = document.addElement("p");
        Element imgElement = rootElement.addElement("img");
        imgElement.addAttribute("class", "weiliao_images");
        imgElement.addAttribute("id", "img_index_" + id);
        imgElement.addAttribute("style", "max-width:364px;max-height:400px");
        int index = src.indexOf("doc");
        src = src.substring(index, src.length());
        imgElement.addAttribute("imagesrc", src + "&" + "uid=" + PreferencesHelper.getInstance().getCurrentUser().uid);
        return rootElement.asXML();
    }

    public static String genertFileXmlMsg(MessageBean messageBean) {
        org.dom4j.Document document = DocumentHelper.createDocument();
        Element rootElement = document.addElement("div");
        Element fileElement = rootElement.addElement("a");
        fileElement.addAttribute("class", "attachmentDownload");
        fileElement.addAttribute("ng-click", "clickAttachHandler($envent)");
        fileElement.addAttribute("_size", String.valueOf(messageBean.attachmentBean.attachSize));
        fileElement.addAttribute("_name", messageBean.attachmentBean.attachmentName);
        fileElement.addAttribute("_href", messageBean.attachmentBean.attachPath);
        fileElement.addAttribute("local_path", messageBean.attachmentBean.localPath);
        fileElement.setText(messageBean.attachmentBean.attachmentName);
        rootElement.setText(" 上传成功");
        return rootElement.asXML();
    }

    public static String stripHtml(String content) {
// <p>段落替换为换行
        content = content.replaceAll("<p .*?>", "\r\n");
// <br><br/>替换为换行
        content = content.replaceAll("<br\\s*/?>", "\r\n");
// 去掉其它的<>之间的东西
        content = content.replaceAll("\\<.*?>", "");
// 还原HTML
// content = HTMLDecoder.decode(content);
        return content;
    }

    public static void main(String[] args) {
        String content = "<p><img class=\"weiliao_images\" id=\"img_index_1439513911678\" style=\"max-width:364px;max-height:400px;\" src=\"http://172.30.10.20:8380/wpserver/doc?doc_id=3141&amp;system_name=weiliao&amp;uid=P00000741\"></p>";
        System.out.println(stripHtml(content));
    }
}
