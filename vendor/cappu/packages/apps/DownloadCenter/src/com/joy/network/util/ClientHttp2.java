package com.joy.network.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.util.Log;

import com.joy.network.impl.ProtocalFactory;
import com.joy.util.Constants;
import com.joy.util.Logger;
import com.joy.util.Util;

/**
 * ���紦��
 * 
 * @author wanghao
 * 
 */
public class ClientHttp2 implements ClientInterface {

	@Override
	public JSONObject request(Protocal protocal) throws Exception {
		String string = getString(protocal);
		if (string == null) {
			return null;
		}
		JSONObject data = new JSONObject(string);
		return data;
	}

	public String getString(Protocal protocal) throws Exception {

		StringBuffer buffer = new StringBuffer();
		BufferedReader reader = null;
		InputStream in = null;
		try {
			in = getInputStream(protocal);
			Logger.info(this, " in : " + in);
			if (in == null) {
				return null;
			}

			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			String line = null;
			while ((line = reader.readLine()) != null) {
				Logger.info(this, " line : " + line);
				buffer.append(line);
			}

			return buffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.info(this, "getString -> e : " + e);
		} finally {
			if (reader != null) {
				reader.close();
				reader = null;
			}
			if (in != null) {
				in.close();
				in = null;
			}
			buffer = null;
		}
		return null;
	}

	public long getDownloadFileSize(Protocal protocal) {

		if (!Util.isNetworkConnected()) {

			return -1;
		}
		DefaultHttpClient httpClient = new DefaultHttpClient();
		long ilength = -1;

		try {

			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT); // ���ӳ�ʱ

			String urlStrl = null;
			if (protocal.getHost() == null) {
				urlStrl = Constants.BASE_URL;
			} else {
				urlStrl = protocal.getHost();
			}
			String randomTS = Util.getTS();
			// url
			if (protocal.getGetData() != null) {
				if (protocal.getHost() == "")
					urlStrl += protocal.getGetData();
				else
					urlStrl += protocal.getGetData() + ProtocalFactory.getSign(randomTS);
			}

			/*
			 * URL url=new URL( urlStrl); HttpURLConnection
			 * urlcon=(HttpURLConnection)url.openConnection(); //�����Ӧ��ȡ�ļ���С
			 * ilength =urlcon.getContentLength();
			 * 
			 * if(ilength < 102400)//����100k { ilength = -1; }
			 * urlcon.disconnect();
			 */

			HttpRequestBase httpRequest = null;
			// post
			if (protocal.getPostData() != null) {
				httpRequest = new HttpPost(urlStrl);
				byte[] sendData = protocal.getPostData().toString().getBytes("UTF-8");
				((HttpPost) httpRequest).setEntity(new ByteArrayEntity(sendData));
			} else {
				httpRequest = new HttpGet(urlStrl);
			}

			httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT);

			httpRequest.addHeader("ts", randomTS);
			httpRequest.addHeader("deviceId", SystemInfo.deviceid);
			httpRequest.addHeader("Accept-Encoding", "gzip");
			httpRequest.addHeader("Content-Type", "text/json;charset=UTF-8");

			HttpResponse httpResponse = httpClient.execute(httpRequest);

			Header[] contentHeads = httpResponse.getHeaders("Content-Range");

			int httpCode = httpResponse.getStatusLine().getStatusCode();

			if (httpCode == HttpURLConnection.HTTP_OK || httpCode == Constants.DOWNLOAD_APK_HTTP_OK) {

				ilength = httpResponse.getEntity().getContentLength();

			} else {

			}

			httpResponse.getEntity().getContent().close();

		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.warn(this, "getInputStream -> �����쳣2 e:" + ex);
		}
		return ilength;

	}

	/**
	 * ��ȡ������
	 */
	public InputStream getInputStream(Protocal protocal) {
		if (!Util.isNetworkConnected()) {
			Logger.info(this, "getInputStream û�д�����");
			return null;
		}
		if (protocal.getGetData().startsWith("?op=9005") || protocal.getGetData().startsWith("http://")) {
			return download(protocal);
		}
		DefaultHttpClient httpClient = new DefaultHttpClient();

		InputStream result = null;
		try {
			if (protocal.isReTry()) {
				httpClient.setHttpRequestRetryHandler(new RetryHandler());
			}
			httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT); // ���ӳ�ʱ

			String urlStrl = null;
			if (protocal.getHost() == null) {
				urlStrl = Constants.BASE_URL;
			} else {
				urlStrl = protocal.getHost();
			}
			String randomTS = Util.getTS();
			// url
			if (protocal.getGetData() != null) {
				if (protocal.getHost() == "")
					urlStrl += protocal.getGetData();
				else
					urlStrl += protocal.getGetData() + ProtocalFactory.getSign(randomTS);
			}
			Logger.info(this, "getInputStream -> urlStrl��" + urlStrl);
			HttpRequestBase httpRequest = null;
			// post
			if (protocal.getPostData() != null) {
				httpRequest = new HttpPost(urlStrl);
				byte[] sendData = protocal.getPostData().toString().getBytes("UTF-8");
				((HttpPost) httpRequest).setEntity(new ByteArrayEntity(sendData));
			} else {
				httpRequest = new HttpGet(urlStrl);
			}

			httpRequest.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					protocal.getSoTimeout() > 0 ? protocal.getSoTimeout() : Constants.TIMEOUT);

			httpRequest.addHeader("ts", randomTS);
			httpRequest.addHeader("deviceId", SystemInfo.deviceid);
			httpRequest.addHeader("Accept-Encoding", "gzip");
			httpRequest.addHeader("Content-Type", "text/json;charset=UTF-8");
			Log.e("ClientHttpHeader", "deviceId: " + SystemInfo.deviceid);
			int startPos = protocal.getStartPos();
			int endPos = protocal.getEndPos();
			if (startPos != -1 && endPos != -1) {
				httpRequest.addHeader("Range", "bytes=" + startPos + "-");
				Logger.info(this, "getInputStream -> startPos��" + startPos + "  endPos:" + endPos);
			}

			HttpResponse httpResponse = httpClient.execute(httpRequest);
			Header[] contentHeads = httpResponse.getHeaders("Content-Range");
			HeaderIterator iterator2 = httpResponse.headerIterator();

			long contentLength = httpResponse.getEntity().getContentLength();

			while (iterator2.hasNext()) {
				Log.i("ClientHttpHeader", "ResponseHeader: " + iterator2.next().toString());
			}

			for (Header h : contentHeads) {
				String value = h.getValue();
				if (value != null && value.contains("bytes")) {
					protocal.setIsBreakPoint(true);
					Logger.info(this, "getInputStream -> BreakPoint��" + h.getValue());
				}
			}
			int httpCode = httpResponse.getStatusLine().getStatusCode();
			Logger.info(this, "getInputStream -> httpCode��" + httpCode);

			if (httpCode == HttpURLConnection.HTTP_OK || httpCode == Constants.DOWNLOAD_APK_HTTP_OK) {
				Header encodeHader = httpResponse.getLastHeader("Content-Encoding");
				if (encodeHader != null && "gzip".equals(encodeHader.getValue())) {
					result = handleReponse(httpResponse, true);
				} else {
					result = handleReponse(httpResponse, false);
				}
			} else {
				Logger.warn(this, "getInputStream -> �����쳣1 httpCode:" + httpCode);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			Logger.warn(this, "getInputStream -> �����쳣2 e:" + ex);
		}
		return result;
	}

	private InputStream download(Protocal protocal) {
		InputStream inStream;
		try {
			String urlStrl = null;
			if (protocal.getHost() == null) {
				urlStrl = Constants.BASE_URL;
			} else {
				urlStrl = protocal.getHost();
			}
			String randomTS = Util.getTS();
			if (protocal.getGetData() != null) {
				if (protocal.getHost() == "")
					urlStrl += protocal.getGetData();
				else
					urlStrl += protocal.getGetData() + ProtocalFactory.getSign(randomTS);
			}
			URL downUrl = new URL(urlStrl);
			// ʹ��Get��ʽ����
			HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
			http.setConnectTimeout(5 * 1000);
			http.setRequestMethod("GET");
			http.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			http.setRequestProperty("Accept-Language", "zh-CN");
			http.setRequestProperty("Referer", downUrl.toString());
			http.setRequestProperty("Charset", "UTF-8");

			int startPos = protocal.getStartPos();
			int endPos = protocal.getEndPos();
			// int startPos = block * (threadId - 1) + downLength;// ��ʼλ��
			// int endPos = block * threadId - 1;// ����λ��
			http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);// ���û�ȡʵ����ݵķ�Χ
			http.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			http.setRequestProperty("Connection", "Keep-Alive");
			Log.e("clientHttp2", " start download from position " + startPos + "-" + endPos);
			inStream = http.getInputStream();

//			// Test
//			byte[] buffer = new byte[1024 * 4];
//			int offset = 0;
//			File file = new File(Constants.SDCARD + "/" + "update.apk");
//			RandomAccessFile threadfile = new RandomAccessFile(file, "rwd");
//			threadfile.seek(startPos);
//			int downLength = 0;
//			while ((offset = inStream.read(buffer, 0, 1024 * 4)) != -1) {
//				threadfile.write(buffer, 0, offset);
//				downLength += offset;
//				Log.e("clientHttp2", "downLength :" + downLength);
//			}
//
//			threadfile.close();
//			inStream.close();
			return inStream;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * ת������ HttpResponse->InputStream
	 * 
	 * @param response
	 * @param gzip
	 * @return
	 * @throws IOException
	 */
	private InputStream handleReponse(HttpResponse response, boolean gzip) throws IOException {
		InputStream is = null;
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			if (gzip) {
				is = new GZIPInputStream(entity.getContent());
				BufferedInputStream bis = new BufferedInputStream(is);
				bis.mark(2);
				// ȡǰ�����ֽ�
				byte[] header = new byte[2];
				int result = bis.read(header);
				// reset����������ʼλ��
				bis.reset();
				// �ж��Ƿ���GZIP��ʽ
				int ss = (header[0] & 0xff) | ((header[1] & 0xff) << 8);
				if (result != -1 && ss == GZIPInputStream.GZIP_MAGIC) {
					is = new GZIPInputStream(bis);
				} else {
					is = bis;
				}
			} else {
				is = new BufferedInputStream(entity.getContent());
			}
		}
		return is;
	}

	/**
	 * �����������ƺ��쳣�Զ��ָ�����
	 */
	private class RetryHandler implements HttpRequestRetryHandler {

		@Override
		public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {

			Logger.info(this, "---retryRequest requestServiceResource response executionCount: " + executionCount
					+ " exception:" + exception);
			if (executionCount > 3) {
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				return true;
			}
			return false;
		}

	}

	@Override
	public void shutdownNetwork() {

	}
}
