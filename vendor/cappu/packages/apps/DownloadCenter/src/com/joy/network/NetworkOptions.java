package com.joy.network;

import com.joy.network.impl.ProtocalFactory;

/**
 * ������������Ĳ���
 * 
 * @author Administrator mqj
 * 
 */
public class NetworkOptions {

	public static class Builder {

		private String url, downAppUrl, imgUrl, pushUrl, key;

		/**
		 * example: http://client.ansyspu.com/app/api.do
		 * 
		 * @param url
		 * @return
		 */
		public Builder setUrl(String url) {
			this.url = url;
			return this;
		}

		/**
		 * example: http://client.ansyspu.com/app/api.do
		 * 
		 * @param downAppUrl
		 * @return
		 */
		public Builder setDownAppUrl(String downAppUrl) {
			this.downAppUrl = downAppUrl;
			return this;
		}

		/**
		 * example: http://client.ansyspu.com/app/api.do
		 * 
		 * @param imgUrl
		 * @return
		 */
		public Builder setImgUrl(String imgUrl) {
			this.imgUrl = imgUrl;
			return this;
		}

		/**
		 * example: http://client.ansyspu.com/app/api.do
		 * 
		 * @param pushUrl
		 * @return
		 */
		public Builder setPushUrl(String pushUrl) {
			this.pushUrl = pushUrl;
			return this;
		}

		/**
		 * �����ַ�Ҫ�ͷ�����ͳһ��
		 * 
		 * @param key
		 * @return
		 */
		public Builder setSignKey(String key) {
			this.key = key;
			return this;
		}

		public NetworkOptions Build() {
			if (url != null) {
				ProtocalFactory.HOST_MUTUAL = url;
			}
			if (imgUrl != null) {
				ProtocalFactory.HOST_IMG = imgUrl;
			} else {
				ProtocalFactory.HOST_IMG = url;
			}
			if (downAppUrl != null) {
				ProtocalFactory.HOST_DOWN_APP = downAppUrl;
			} else {
				ProtocalFactory.HOST_DOWN_APP = url;
			}
			if (pushUrl != null) {
				ProtocalFactory.HOST_PUSH = pushUrl;
			} else {
				ProtocalFactory.HOST_PUSH = url;
			}
			if (key != null) {
				ProtocalFactory.SIGN_KEY = key;
			}
			return new NetworkOptions();
		}
	}

	public String getUrl() {
		return ProtocalFactory.HOST_MUTUAL;
	}

	public String getSingkey() {
		return ProtocalFactory.SIGN_KEY;
	}
}
