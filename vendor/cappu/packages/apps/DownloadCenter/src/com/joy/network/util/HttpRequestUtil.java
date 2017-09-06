package com.joy.network.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import com.joy.network.impl.ProtocalFactory;
import com.joy.util.Util;

public class HttpRequestUtil {

	/**
	 * ����xml���
	 * 
	 * @param path�����ַ
	 * @param xmlxml���
	 * @param encoding����
	 * @return
	 * @throws Exception
	 */
	public static byte[] postXml(String path, String xml, String encoding) throws Exception {
		byte[] data = xml.getBytes(encoding);
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "text/xml; charset=" + encoding);
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		conn.setConnectTimeout(5 * 1000);
		OutputStream outStream = conn.getOutputStream();
		outStream.write(data);
		outStream.flush();
		outStream.close();
		if (conn.getResponseCode() == 200) {
			return readStream(conn.getInputStream());
		}
		return null;
	}
    /** 
     * ֱ��ͨ��HTTPЭ���ύ��ݵ�������,ʵ��������?�ύ����: 
     *   <FORM METHOD=POST ACTION="http://192.168.0.200:8080/ssi/fileload/test.do" enctype="multipart/form-data"> 
            <INPUT TYPE="text" NAME="name"> 
            <INPUT TYPE="text" NAME="id"> 
            <input type="file" name="imagefile"/> 
            <input type="file" name="zip"/> 
         </FORM> 
     * @param path �ϴ�·��(ע������ʹ��localhost��127.0.0.1�����·�����ԣ� 
     *                  ��Ϊ���ָ���ֻ�ģ�����������ʹ��http://www.baidu.com��http://192.168.1.10:8080�����·������) 
     * @param params ������� keyΪ������,valueΪ����ֵ 
     * @param file �ϴ��ļ� 
     */  
	public static boolean post(String path, Map<String, String> params, FormFile[] files) throws Exception {
		// ��ݷָ���
		final String BOUNDARY = "---------------------------7da2137580612";
		// ��ݽ����־"---------------------------7da2137580612--"
		final String endline = "--" + BOUNDARY + "--\r\n";

		// ��������forѭ������Ϊ�˵õ���ݳ��Ȳ������ݱ?�����Ͷ�
		// ���ȵõ��ļ�������ݵ��ܳ���(�����ļ��ָ���)
		int fileDataLength = 0;
		for (FormFile uploadFile : files) {
			StringBuilder fileExplain = new StringBuilder();
			fileExplain.append("--");
			fileExplain.append(BOUNDARY);
			fileExplain.append("\r\n");
			fileExplain.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName()
					+ "\";filename=\"" + uploadFile.getFilname() + "\"\r\n");
			fileExplain.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
			fileExplain.append("\r\n");
			fileDataLength += fileExplain.length();
			if (uploadFile.getInStream() != null) {
				fileDataLength += uploadFile.getFile().length();
			} else {
				fileDataLength += uploadFile.getData().length;
			}
		}
		// �ٹ����ı����Ͳ����ʵ�����
		StringBuilder textEntity = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			textEntity.append("--");
			textEntity.append(BOUNDARY);
			textEntity.append("\r\n");
			textEntity.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
			textEntity.append(entry.getValue());
			textEntity.append("\r\n");
		}

		// ���㴫����������ʵ������ܳ���(�ı��ܳ���+����ܳ���+�ָ���)
		int dataLength = textEntity.toString().getBytes().length + fileDataLength + endline.getBytes().length;

		URL url = new URL(path);
		// Ĭ�϶˿ں���ʵ���Բ�д
		int port = url.getPort() == -1 ? 80 : url.getPort();
		// ����һ��Socket����
		// Socket socket = new Socket(InetAddress.getByName(url.getHost()),
		// port);
		Socket socket = new Socket();
		InetSocketAddress isa = new InetSocketAddress(InetAddress.getByName(url.getHost()), port);
		socket.connect(isa, 15 * 1000);
		// ���һ�����������Android����web��
		OutputStream outStream = socket.getOutputStream();
		// �������HTTP����ͷ�ķ���
		String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
		outStream.write(requestmethod.getBytes());
		// ����accept
		String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
		outStream.write(accept.getBytes());
		// ����language
		String language = "Accept-Language: zh-CN\r\n";
		outStream.write(language.getBytes());
		// ����contenttype
		String contenttype = "Content-Type: multipart/form-data; boundary=" + BOUNDARY + "\r\n";
		outStream.write(contenttype.getBytes());
		// ����contentlength
		String contentlength = "Content-Length: " + dataLength + "\r\n";
		outStream.write(contentlength.getBytes());
		// ����alive
		String alive = "Connection: Keep-Alive\r\n";
		outStream.write(alive.getBytes());
		// ����host
		String host = "Host: " + url.getHost() + ":" + port + "\r\n";
		outStream.write(host.getBytes());
		// д��HTTP����ͷ����HTTPЭ����дһ���س�����
		outStream.write("\r\n".getBytes());
		// �������ı����͵�ʵ����ݷ��ͳ���
		outStream.write(textEntity.toString().getBytes());

		// �������ļ����͵�ʵ����ݷ��ͳ���
		for (FormFile uploadFile : files) {
			StringBuilder fileEntity = new StringBuilder();
			fileEntity.append("--");
			fileEntity.append(BOUNDARY);
			fileEntity.append("\r\n");
			fileEntity.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName()
					+ "\";filename=\"" + uploadFile.getFilname() + "\"\r\n");
			fileEntity.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
			outStream.write(fileEntity.toString().getBytes());
			// �߶���д
			if (uploadFile.getInStream() != null) {
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = uploadFile.getInStream().read(buffer, 0, 1024)) != -1) {
					outStream.write(buffer, 0, len);
				}
				uploadFile.getInStream().close();
			} else {
				outStream.write(uploadFile.getData(), 0, uploadFile.getData().length);
			}
			outStream.write("\r\n".getBytes());
		}
		// ���淢����ݽ����־����ʾ����Ѿ�����
		outStream.write(endline.getBytes());
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// ��ȡweb���������ص���ݣ��ж��������Ƿ�Ϊ200�������200���������ʧ��
		if (reader.readLine().indexOf("200") == -1) {
			return false;
		}
		outStream.flush();
		outStream.close();
		reader.close();
		socket.close();
		return true;
	}

	public static boolean httpPostWithAnnex(String actionUrl, String channel, FormFile[] files) throws IOException {
		// try {

		Map<String, String> params = new HashMap<String, String>();
		String randomTS = Util.getTS();
		String randomString = Util.randomString(6);
		params.put("op", Integer.toString(ProtocalFactory.OP_BACKUP));
		params.put("channel", channel);
		params.put("sign", ProtocalFactory.getSign(randomTS, randomString));
		params.put("sjz", ProtocalFactory.getSjz(randomString));

		String BOUNDARY = "---------7d4a6d158c9"; // ��ݷָ���
		String MULTIPART_FORM_DATA = "multipart/form-data";
		URL url = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);// ��������
		conn.setDoOutput(true);// �������
		conn.setUseCaches(false);// ��ʹ��Cache
		conn.setRequestMethod("POST");
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);

		conn.setRequestProperty("Accept-Encoding", "gzip");
		conn.setRequestProperty("ts", randomTS);
		conn.setRequestProperty("deviceId", SystemInfo.deviceid);
		// User user = new User();
		/*
		 * AppContext ac = (AppContext)mContext.getApplicationContext(); User
		 * user = ac.user ;
		 * 
		 * if(user!=null) conn.setRequestProperty("sid",user.getSessionId());
		 * else conn.setRequestProperty("sid","");
		 */

		StringBuilder sb = new StringBuilder();
		// �ϴ��ı?�����
		for (HashMap.Entry<String, String> entry : params.entrySet()) {// �����?�ֶ�����
			sb.append("--");
			sb.append(BOUNDARY);
			sb.append("\r\n");
			sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
			sb.append(entry.getValue());
			sb.append("\r\n");

		}
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.write(EncodingUtils.getBytes(sb.toString(), "utf-8"));// ���ͱ?�ֶ����
		// �ϴ����ļ�����
		for (FormFile file : files) {
			System.out.println("file:::::::::::::::::::::" + file);
			if (file != null) {

				String srcPath = "";
				srcPath = file.getFilname();

				StringBuilder split = new StringBuilder();
				split.append("--");
				split.append(BOUNDARY);
				split.append("\r\n");
				/*
				 * split.append("Content-Disposition: form-data;name=\"" +
				 * file.getFormname() + "\";filename=\"" + file.getFileName() +
				 * "\"\r\n");
				 */
				split.append("Content-Disposition: form-data;name=\"" + file.getParameterName() + "\";filename=\""
						+ srcPath.substring(srcPath.lastIndexOf("/") + 1) + "\"\r\n");
				split.append("Content-Type: " + file.getContentType() + "\r\n\r\n");
				outStream.write(split.toString().getBytes());
				if (file.getData() == null) {
					if (file.getInStream() != null) {
						byte[] buffer = new byte[1024];
						int length = -1;
						while ((length = file.getInStream().read(buffer)) != -1) {
							outStream.write(buffer, 0, length);
						}
					}
					file.getInStream().close();
				} else {
					outStream.write(file.getData(), 0, file.getData().length);
				}
				outStream.write("\r\n".getBytes());
			}
		}
		// String strResult="";
		byte[] end_data = ("--" + BOUNDARY + "--\r\n").getBytes();// ��ݽ����־
		outStream.write(end_data);
		outStream.flush();
		int cah = conn.getResponseCode();

		System.out.println("conn.getResponseCode()conn.getResponseCode()conn.getResponseCode():" + cah);
		if (cah != 200) {
			throw new RuntimeException("����urlʧ��");
		}

		if (conn.getContentEncoding().equalsIgnoreCase("gzip")) {
			InputStream is = conn.getInputStream();
			InputStream inputStream = new GZIPInputStream(is);

			BufferedInputStream bis = new BufferedInputStream(inputStream);
			bis.mark(2);
			// ȡǰ�����ֽ�
			byte[] header = new byte[2];
			int result = bis.read(header);
			// reset����������ʼλ��
			bis.reset();
			// �ж��Ƿ���GZIP��ʽ
			int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
			if (result != -1 && ss == GZIPInputStream.GZIP_MAGIC) {
				inputStream = new GZIPInputStream(bis);
			} else {
				// ȡǰ�����ֽ�
				inputStream = bis;
			}
			// strResult = Utils.convertStreamToString(inputStream, HTTP.UTF_8);
			// strResult = Util.getBytes(inputStream).toString();
			inputStream.close();

		} else {
			/** ��ѹ����ʽ **/
			InputStream is = conn.getInputStream();
			int ch;
			StringBuilder b = new StringBuilder();
			while ((ch = is.read()) != -1) {
				b.append(ch);
			}
			// strResult = b.toString() ;
		}
		outStream.close();
		conn.disconnect();
		// return strResult;
		return true;
	}

	/**
	 * �ύ��ݵ�������
	 * 
	 * @param path
	 *            �ϴ�·��(ע������ʹ��localhost��127.0.0.1�����·�����ԣ���Ϊ���ָ���ֻ�ģ�����������ʹ��http://
	 *            www.baidu.com��http://192.168.1.10:8080�����·������)
	 * @param params
	 *            ������� keyΪ������,valueΪ����ֵ
	 * @param file
	 *            �ϴ��ļ�
	 */
	public static boolean post(String path, Map<String, String> params, FormFile file) throws Exception {
		return post(path, params, new FormFile[] { file });
	}

	/**
	 * �ύ��ݵ�������
	 * 
	 * @param path
	 *            �ϴ�·��(ע������ʹ��localhost��127.0.0.1�����·�����ԣ���Ϊ���ָ���ֻ�ģ�����������ʹ��http://
	 *            www.baidu.com��http://192.168.1.10:8080�����·������)
	 * @param params
	 *            ������� keyΪ������,valueΪ����ֵ
	 * @param encode
	 *            ����
	 */
	public static byte[] postFromHttpClient(String path, Map<String, String> params, String encode) throws Exception {
		// ���ڴ���������
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			formparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, encode);
		HttpPost httppost = new HttpPost(path);
		httppost.setEntity(entity);
		// �����������
		HttpClient httpclient = new DefaultHttpClient();
		// ����post����
		HttpResponse response = httpclient.execute(httppost);
		return readStream(response.getEntity().getContent());
	}

	/**
	 * ��������
	 * 
	 * @param path
	 *            ����·��
	 * @param params
	 *            ������� keyΪ������� valueΪ����ֵ
	 * @param encode
	 *            �������ı���
	 */
	public static byte[] post(String path, Map<String, String> params, String encode) throws Exception {
		StringBuilder parambuilder = new StringBuilder("");
		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				parambuilder.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), encode))
						.append("&");
			}
			parambuilder.deleteCharAt(parambuilder.length() - 1);
		}
		byte[] data = parambuilder.toString().getBytes();
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// ����������ⷢ���������
		conn.setDoOutput(true);
		// ���ò����л���
		conn.setUseCaches(false);
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("POST");
		// ��������http����ͷ
		conn.setRequestProperty(
				"Accept",
				"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(data.length));
		conn.setRequestProperty("Connection", "Keep-Alive");

		// ���Ͳ���
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.write(data);// �Ѳ����ͳ�ȥ
		outStream.flush();
		outStream.close();
		if (conn.getResponseCode() == 200) {
			return readStream(conn.getInputStream());
		}
		return null;
	}

	/**
	 * ��ȡ��
	 * 
	 * @param inStream
	 * @return �ֽ�����
	 * @throws Exception
	 */
	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = -1;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}

}
