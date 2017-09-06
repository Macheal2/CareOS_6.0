package com.joy.network.util;

import java.io.InputStream;

import org.json.JSONObject;

import android.graphics.Bitmap;

/**
 * ͨ�Žӿ�
 * 
 * @author wanghao
 * 
 */
public interface ClientInterface {
	/**
	 * ����������󣬵õ����ص�JSON����
	 * 
	 * @param protocal
	 * @return
	 * @throws Exception
	 */
	public JSONObject request(Protocal protocal) throws Exception;

	/**
	 * ��ȡ�ַ�
	 * 
	 * @param protocal
	 * @return
	 * @throws Exception
	 */
	public String getString(Protocal protocal) throws Exception;

	/**
	 * ������ݣ��õ����ص���
	 * 
	 * @param protocal
	 * @return
	 */
	public InputStream getInputStream(Protocal protocal);

	/**
	 * �ͷ���Դ
	 * 
	 * @param
	 * @return void
	 */
	public void shutdownNetwork();
	
	
	public long getDownloadFileSize(Protocal protocal);
}
