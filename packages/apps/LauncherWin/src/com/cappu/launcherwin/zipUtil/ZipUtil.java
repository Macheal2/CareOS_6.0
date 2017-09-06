package com.cappu.launcherwin.zipUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
//import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;
import com.cappu.launcherwin.basic.theme.ThemeManager;
import com.cappu.launcherwin.widget.LauncherLog;
import com.cappu.launcherwin.zipUtil.XMLParse.Skin;
//import com.cappu.launcherwin.zipUtil.core.ZipFile;
//import com.cappu.launcherwin.zipUtil.model.FileHeader;


public class ZipUtil {
    
	private static final String TAG="ZipUtil";
    private Context mContext;
    private HashMap<String,Skin> mMapSkin;
    private XMLParse mXMLParse;
//    /**第一个参数为解压的压缩包*/
//    private String mZipFile;
//    /**第二个为解压的路径*/
//    private String UnZipPath;
    
    public ZipUtil(Context context){
        this.mContext = context;
    } 

    /**
     * 第一个参数为解压的压缩包，
     * 第二个为解压的路径
     * */
    public boolean UnZipAllfile(String zipFiel,String UnZipPath) throws Exception{
//        this.mZipFile = zipFiel;
//        this.UnZipPath = UnZipPath;
//        ZipFile zipFile = new ZipFile(zipFiel);
//        if(zipFile.isEncrypted()){
//            zipFile.setPassword("cappu");
//        }
        LauncherLog.v(TAG, "UnZipAllfile, jeff start");
//        zipFile.extractAll(UnZipPath);
        //hejianfeng add start
		// 获取压缩包中的所有条目
        ZipFile zipFile = new ZipFile(zipFiel);
        File dirFile = new File(UnZipPath);
		if (!dirFile.exists())
		{
			dirFile.mkdirs();
		}
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		ZipEntry entry = null;
		String unzipAbpath = dirFile.getAbsolutePath();

		// 遍历条目,并读取条目流输出到文件流中(即开始解压)
		while (entries.hasMoreElements()) {
			entry = entries.nextElement();
			unzipEachFile(zipFile, entry, unzipAbpath);
		}
		//hejianfeng add end
        LauncherLog.v(TAG, "UnZipAllfile, jeff end");
//        List fileHeaderList = zipFile.getFileHeaders();
//        for(int m = 0; m <fileHeaderList.size();m++){
//            FileHeader fileHeader = (FileHeader) fileHeaderList.get(m);
//            zipFile.extractFile(fileHeader, UnZipPath);
//            LauncherLog.v(TAG, "UnZipAllfile, jeff fileHeader.getFileName()="+fileHeader.getFileName());
//        }
        File[] files=ThemeManager.getInstance().getDefaultThemeListFile();
        for(File file :files){
        	String configSkinPath=file.getPath()+"/theme_appIcon_info.xml";
        	LauncherLog.v(TAG, "UnZipAllfile, jeff configSkinPath="
    				+ configSkinPath);
        	setThemesConfigXML(configSkinPath);
        }
        mXMLParse.CompleteConfigurationXML();
        return true;
    }
    /**
	 * 对某一条目进行解压
	 * @param zipFile ZipFile 需要解压的文件
	 * @param entry ZipEntry 压缩文件中的条目
	 * @param unzipAbpath String 解压文件的绝对路径
	 */
	private void unzipEachFile(ZipFile zipFile, ZipEntry entry, String unzipAbpath)
	{
		byte[] buffer = new byte[1024 * 5];
		int readSize = 0;
		String name = entry.getName();
		String fileName = name;
		int index = 0;
		String tempDir = "";

		// 如果条目为目录,则在解压文件路径中创建此目录
		if (entry.isDirectory())
		{
			File tempFile = new File(unzipAbpath + File.separator + name);
			if (!tempFile.exists())
			{
				tempFile.mkdirs();
			}

			return;
		}

		// 不是目录时,由于压缩文件中目录里的文件名为:DIR/DIR/xxxFILE,所以可能文件前的目录并不存在于解压目录中,也需要创建
		if ((index = name.lastIndexOf(File.separator)) != -1)
		{
			fileName = name.substring(index, name.length());
			tempDir = name.substring(0, index);
			File tempDirFile = new File(unzipAbpath + File.separator + tempDir);
			if (!tempDirFile.exists())
			{
				tempDirFile.mkdirs();
			}
		}

		// 使用输入输出流把条目读出并写到解压目录中
		String zipPath = unzipAbpath + File.separator + tempDir + fileName;
		File tempFile = new File(zipPath);
		InputStream is = null;
		FileOutputStream fos = null;
		try
		{
			is = zipFile.getInputStream(entry);
			if (!tempFile.exists())
			{
				tempFile.createNewFile();
			}

			fos = new FileOutputStream(tempFile);
			while ((readSize = is.read(buffer)) > 0)
			{
				fos.write(buffer, 0, readSize);
			}
		}
		catch (Exception e)
		{
			new File(unzipAbpath).delete();
			e.printStackTrace();
		}
		finally
		{
			try
			{
				is.close();
				fos.close();
			}
			catch (IOException e)
			{
				new File(unzipAbpath).delete();
				e.printStackTrace();
			}
		}

	}
    private void setThemesConfigXML(String configSkinPath){
        if(mXMLParse == null){
            mXMLParse = new XMLParse(mContext);
        }
        NodeList nodeList = mXMLParse.getThemesNodeList(configSkinPath);
        if(nodeList != null){
            try {
                mXMLParse.ConfigurationXML(nodeList);
            } catch (IllegalArgumentException e) {
                Log.i("HHJ", "setThemesConfigXML IllegalArgumentException:" + e.toString());
            } catch (TransformerConfigurationException e) {
                Log.i("HHJ", "setThemesConfigXML TransformerConfigurationException:" + e.toString());
            } catch (FileNotFoundException e) {
                Log.i("HHJ", "setThemesConfigXML FileNotFoundException:" + e.toString());
            } catch (SAXException e) {
                Log.i("HHJ", "setThemesConfigXML SAXException:" + e.toString());
            } catch (IOException e) {
                Log.i("HHJ", "setThemesConfigXML IOException:" + e.toString());
            } catch (Exception e) {
                Log.i("HHJ", "setThemesConfigXML TransformerException:" + e.toString());
            }
        }
        
        
    }
    
    private void setListSkin(String configSkin){
        if(mXMLParse == null){
            mXMLParse = new XMLParse(mContext);
        }
        mMapSkin = mXMLParse.getSkinXmlParse(configSkin);
    }
    
    public HashMap<String,Skin> getListSkin(){
        if(mMapSkin == null || mMapSkin.size() == 0){
            setListSkin(XMLParse.CONFIGNAME);
        }
        return mMapSkin;
    }
}
