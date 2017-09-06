package com.cappu.launcherwin.zipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.cappu.launcherwin.LauncherApplication;
import com.cappu.launcherwin.widget.LauncherLog;

import android.content.Context;
import android.util.Log;

public class XMLParse {
    
	private String TAG="XMLParse";
    private DocumentBuilderFactory mDocumentBuilderFactory;
    private DocumentBuilder mDocumentBuilder;
    private TransformerFactory mTransformerFactory;
    private Transformer mTransformer;
    private DOMSource mDOMSource;
    private PrintWriter mPrintWriter;
    private StreamResult mStreamResult;
    
    private Document mDocument;
    private Element mRootNode;
    public static final String CONFIGNAME = LauncherApplication.CappuDate+"ThemesConfig.xml";
             
    public void init() {
        try {
            mDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            mDocumentBuilder = mDocumentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    public class Skin{
        public static final String BACKGROUND = "background";
        public static final String CLASSNAME = "className";
        public static final String PACKAGENAME = "packageName";
        public static final String THEMESTYPE = "type";
        
        public String mBackground;
        public String mClassName;
        public String mPackageName;
        public String mThemesType;
        
        public Skin(){
        }
        
        public Skin(String appName,String appNameCN,String background,String className,String packageName,String type){
            
            this.mBackground = background;
            this.mClassName = className;
            this.mPackageName = packageName;
            this.mThemesType = type;
        }
        public String getThemesType() {
            return mThemesType;
        }
        public void setThemesType(String themesType) {
            this.mThemesType = themesType;
        }
        public String getBackground() {
            return mBackground;
        }
        public void setBackground(String mBackground) {
            this.mBackground = mBackground;
        }
        public String getClassName() {
            return mClassName;
        }
        public void setClassName(String mClassName) {
            this.mClassName = mClassName;
        }
        public String getPackageName() {
            return mPackageName;
        }
        public void setPackageName(String mPackageName) {
            this.mPackageName = mPackageName;
        }

        @Override
        public String toString() {
            return "Skin [mBackground=" + mBackground + ", mClassName=" + mClassName
                    + ", mPackageName=" + mPackageName + ", mThemesType=" + mThemesType + "]";
        }
        
        
        
    }
    
    Context mContext;
    public XMLParse(Context context){
        this.mContext = context;
    }

    
    /*
     * 
     * 这种解析xml文件 如果路径是 data/data/com.cappu.launcherwin/cappu/CappuRes/klt_skin.xml 解析会出问题
     * 解决方法是使用file类型协议
     * */
    public HashMap<String,Skin> getSkinXmlParse(String filePath){
        if(filePath == null){
            return null;
        }
        LauncherLog.v(TAG, "getSkinXmlParse,jeff filePath="+filePath);
        HashMap<String,Skin> mapSkin = new HashMap<String, Skin>();
        File pareFile = new File(filePath);
        if(!pareFile.exists()){
            return null;
        }
        
        try {
            if(mDocumentBuilder == null){
                init();
            }
            Document doc = mDocumentBuilder.parse(pareFile);
            doc.normalize();
            NodeList mNodeList = doc.getElementsByTagName("AppIcon");
            for (int i = 0; i < mNodeList.getLength(); i++) {
                Element link = (Element) mNodeList.item(i);
                String background = link.getAttribute(Skin.BACKGROUND);
                String className = link.getAttribute(Skin.CLASSNAME);
                String packageName = link.getAttribute(Skin.PACKAGENAME);
                String themesType = link.getAttribute(Skin.THEMESTYPE);
                Skin skin = new Skin();
                skin.setBackground(background+".png");
                skin.setClassName(className);
                skin.setPackageName(packageName);
                skin.setThemesType(themesType);
                
                if(!mapSkin.containsKey(packageName+"/"+className+"/"+themesType)){
                	LauncherLog.v(TAG, "getSkinXmlParse,jeff mapSkin-->key="+packageName+"/"+className+"/"+themesType);
                    mapSkin.put(packageName+"/"+className+"/"+themesType, skin);
                }
                
            }
            pareFile = null;
            mDocumentBuilder = null;
            mDocumentBuilderFactory = null;
        }  catch (SAXException e) {
            Log.i("HHJ", "SAXException  :"+e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("HHJ", "IOException  :"+e.toString());
        }
        return mapSkin;
    }
    
    /**获取主题文件的所有子节点*/
    public NodeList getThemesNodeList(String filePath){
        File pareFile = new File(filePath);
        if(filePath == null){
            return null;
        }
        Document doc = null;
        try {
            if (mDocumentBuilder == null) {
                init();
            }
            doc = mDocumentBuilder.parse(pareFile);
            NodeList mNodeList = doc.getElementsByTagName("AppIcon");
            return mNodeList;
        } catch (SAXException e) {
            Log.i("HHJ", "获取主题文件的所有子节点  SAXException:"+e.toString());
        } catch (IOException e) {
            Log.i("HHJ", "获取主题文件的所有子节点  IOException:"+e.toString());
        }
        
        return null;
        
    }
    private void initDocument() throws Exception{
		if (mDocument == null) {
			LauncherLog.v(TAG, "initDocument,jeff mDocument == null");
			File configFile = new File(CONFIGNAME);
			if (!configFile.exists()) {
				configFile.createNewFile();
				mDocument = mDocumentBuilder.newDocument();
				mRootNode = mDocument.getDocumentElement();
				mRootNode = mDocument.createElement("Page");// 创建根节点
				mDocument.appendChild(mRootNode);
			} else {
				mDocument = mDocumentBuilder.parse(configFile);
				NodeList pageNode = mDocument.getElementsByTagName("Page");
				if (pageNode.getLength() == 1) {
					mRootNode = (Element) pageNode.item(1);
					mDocument.appendChild(mRootNode);
				} else {
					configFile.delete();
					configFile.createNewFile();
					mDocument = mDocumentBuilder.newDocument();
					mRootNode = mDocument.getDocumentElement();
					mRootNode = mDocument.createElement("Page");// 创建根节点
					mDocument.appendChild(mRootNode);
				}
			}
		}
    }
    /**为主题做的 配置XML 方法
     * @throws IOException 
     * @throws SAXException */
    public Document ConfigurationXML(NodeList childNode) throws SAXException, IOException,IllegalArgumentException,TransformerConfigurationException,FileNotFoundException,Exception {
        if (mDocumentBuilder == null) {
            init();
        }
        initDocument();
        for (int i = 0; i < childNode.getLength(); i++) {
            Element link = (Element) childNode.item(i);
            LauncherLog.v(TAG, "ConfigurationXML, jeff PACKAGE/CLASS="+link.getAttribute(Skin.PACKAGENAME)+"/"+
            		link.getAttribute(Skin.CLASSNAME)+"/"+link.getAttribute(Skin.THEMESTYPE));
            Element appIcon = mDocument.createElement("AppIcon");//创建节点
            appIcon.setAttribute(Skin.BACKGROUND, link.getAttribute(Skin.BACKGROUND));
            appIcon.setAttribute(Skin.CLASSNAME, link.getAttribute(Skin.CLASSNAME));
            appIcon.setAttribute(Skin.PACKAGENAME, link.getAttribute(Skin.PACKAGENAME));
            appIcon.setAttribute(Skin.THEMESTYPE, link.getAttribute(Skin.THEMESTYPE));
            mRootNode.appendChild(appIcon);
        }
        return mDocument;
    }
    
    /**配置完成以后将配置的写入文件
     * @throws FileNotFoundException 
     * @throws TransformerException */
    public void CompleteConfigurationXML() throws FileNotFoundException, TransformerException{
        if(mTransformerFactory == null){
            mTransformerFactory = TransformerFactory.newInstance();
        }
        if(mTransformer == null){
            mTransformer = mTransformerFactory.newTransformer();
            mTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            mTransformer.setOutputProperty(OutputKeys.INDENT, "    ");
        }
        if(mDOMSource == null){
            mDOMSource = new DOMSource(mDocument);
        }
        if(mPrintWriter == null){
            mPrintWriter = new PrintWriter(new FileOutputStream(CONFIGNAME));
        }
        if(mStreamResult == null){
            mStreamResult = new StreamResult(mPrintWriter);
        }
        mTransformer.transform(mDOMSource, mStreamResult);//关键转换  
        mDocument=null;
        mRootNode=null;
    }
}
