package com.cappu.launcherwin;

import java.io.File;
import java.util.UUID;

import android.util.Log;

public class JavaNameOperating {
    /**
     * Java文件操作 获取文件扩展名
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
    
    /**
     * 获取文件名
     */
    public static String getFileNameToUrl(String url) {
        String filename = url.substring(url.lastIndexOf('/') + 1);
        if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
            filename = UUID.randomUUID() + ".tmp";// 默认取一个文件名
        }
        return filename;
    }
    
    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }
    
    /**
     * Java文件操作 获取文件所在目录的文件夹名字
     */
    public static String getFileAtFolder(String filename) {
        if(filename!=null){
            String[] files = filename.split("/");
            /*for (int i = 0; i < files.length; i++) {
                //Log.i("HHJ", "==============:"+files.length+"  i = "+i+"  "+files[i]);
            }
            */
            return files[0];
        }
        return null;
    }
    
    /**
     * Java文件操作 获取文件所在目录
     */
    public static String getFileName(String filename) {
        if(filename!=null){
            String[] files = filename.split("/");
            /*for (int i = 0; i < files.length; i++) {
            }*/
            return files[files.length-1];
        }
        return null;
    }
    
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                boolean f = file.delete();
                Log.i("HHJ", "66 delete file file.delete():"+f);
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            boolean f = file.delete();
            Log.i("HHJ", "74 delete file file.delete():"+f);
        } else {
            Log.i("HHJ", "delete file fail");
        }
    }
}
