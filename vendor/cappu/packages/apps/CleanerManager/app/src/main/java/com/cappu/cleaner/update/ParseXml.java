package com.cappu.cleaner.update;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;
import android.util.Xml;

import com.cappu.cleaner.Util;

public class ParseXml {

    private final static String APP = "DownloadCenter.apk"; // 当前软件
    private final static String APP_NAME = "magcomm:AppName";// 软件名称
    private final static String VERSION = "magcomm:Version";// 版本号
    private final static String URL = "magcomm:DownloadUrl";// 下载地址

    private final static String XML_URL = "http://app.careos.cn/download/cellLayout/expand_default_workspace.xml";// 服务器配置xml

    // doc 解析
    public HashMap<String, String> parseXmlByDoc(InputStream inStream) {

        HashMap<String, String> hashMap = new HashMap<String, String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();// 实例化一个文档构建器工厂

        DocumentBuilder builder;
        Document document = null;

        try {
            builder = factory.newDocumentBuilder();// 通过文档构建器工厂获取一个文档构建器
            document = builder.parse(inStream);// 通过文档通过文档构建器构建一个文档实例
        } catch (Exception e) {
            e.printStackTrace();
        }

        Element root = document.getDocumentElement();// 根节点

        String rootName = root.getNodeName();
        if(Util.DEBUG)Log.e(Util.TAG, "ParseXml ----rootName=" + rootName);

        NodeList childNodes = root.getChildNodes();// 子节点 // NodeList items =
                                                   // root.getElementsByTagName("favorite");

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = (Node) childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {

                Element childElement = (Element) childNode;// 子节点标签

                String elementAppName = childElement.getAttribute(APP_NAME);
                String elementVersion = childElement.getAttribute(VERSION);
                String elementUrl = childElement.getAttribute(URL);

                if(Util.DEBUG)Log.e(Util.TAG, "ParseXml -----childElementName=" + elementAppName + ",elementVersion=" + elementVersion + ",elementUrl=" + elementUrl);

                if (elementAppName.equals(APP)) {
                    hashMap.put("name", elementAppName);
                    hashMap.put("version", elementVersion);
                    hashMap.put("url", elementUrl);
                }
            }
        }

        return hashMap;
    }

    // pull 解析
    public HashMap<String, String> parseXmlByPull(InputStream inStream) throws XmlPullParserException, IOException {

        HashMap<String, String> hashMap = new HashMap<String, String>();

        XmlPullParser parser = Xml.newPullParser(); // 由android.util.Xml创建一个XmlPullParser实例
        parser.setInput(inStream, "UTF-8"); // 设置输入流 并指明编码方式

        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_DOCUMENT:
                break;

            case XmlPullParser.START_TAG:
                // 解析XML节点数据 ，获取当前标签名字
                String tagName = parser.getName();

                if(Util.DEBUG)Log.e(Util.TAG, "ParseXml ----parseXmlByPull TAG," + tagName);

                if (tagName.equals("favorite")) {
                    // getAttributeValue解析节点的属性值
                    // parser.next 获取下一个节点 =

                    parser.getAttributeValue(null, APP_NAME);

                    String tagAppName = parser.getAttributeValue(14);
                    String tagVersion = parser.getAttributeValue(18);
                    String tagUrl = parser.getAttributeValue(17);// parser.getAttributeValue(null,URL);

                    if(Util.DEBUG)Log.e(Util.TAG, "ParseXml ----tagAppName=" + tagAppName + ",tagVersion=" + tagVersion + ",tagUrl=" + tagUrl);

                    if (tagAppName != null && tagAppName.equals(APP)) {
                        hashMap.put("name", tagAppName);
                        hashMap.put("version", tagVersion);
                        hashMap.put("url", tagUrl);
                    }
                }

                break;
            case XmlPullParser.END_TAG:

                break;
            }
            eventType = parser.next();
        }

        return hashMap;
    }

    public InputStream getXmlByUrl() {
        String url = XML_URL;
        InputStream in = null;

        try {
            URL u = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("GET");

            // 定义请求时间，在ANDROID中最好是不好超过10秒。否则将被系统回收。
            conn.setConnectTimeout(3 * 1000);// 设置连接主机超时（单位：毫秒）
            conn.setReadTimeout(3 * 1000);// 设置从主机读取数据超时（单位：毫秒）
            if (conn.getResponseCode() == 200) {
                in = conn.getInputStream();
            }
        } catch (Exception e) {
            if(Util.DEBUG)Log.e(Util.TAG, "ERROR ---ParseXml ----getXmlByUrl e:" + e.toString());
        }

        return in;
    }
}
