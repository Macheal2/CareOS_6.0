package com.joy.network.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.joy.network.impl.Service;
import com.joy.util.Logger;
import com.joy.util.Util;

/**
 * ������Ϣ��id��mac��IME, sd operate��network status ��
 * 
 * @author wanghao
 * 
 */
public class SystemInfo {

	/**
	 * ������
	 */
	public static String channel;
	/**
	 * IMEI��
	 */
	public static String imei;
	/**
	 * IMSI��
	 */
	public static String imsi;
	/**
	 * ����ϵͳ�汾
	 */
	public static String os;
	/**
	 * ����ʡ��
	 */
	public static String province;
	/**
	 * ���ڳ���
	 */
	public static String city;
	/**
	 * IP��ַ
	 */
	public static String ip;
	/**
	 * ��������
	 */
	// public static String sms;
	/**
	 * ��Ļ�ֱ���
	 */
	public static String display;
	/**
	 * �ֻ������̼�
	 */
	public static String product;
	/**
	 * �ֻ�Ʒ��
	 */
	public static String brand = Build.BRAND;
	/**
	 * �ֻ��ͺ�
	 */
	public static String model = Build.MODEL;
	/**
	 * ���� (0 δ֪ 1 �������� 2 �������� 3 Ӣ��)
	 */
	public static String language;
	/**
	 * ��Ӫ��(0 δ֪ 1 �ƶ� 2 ��ͨ 3 ����)
	 */
	public static Integer operators;
	/**
	 * �����ͺţ�0 δ֪ 1 wifi 2G 3G 4G��
	 */
	public static Integer network;
	/**
	 * ����汾
	 */
	public static Integer vcode;
	/**
	 * ����汾
	 */
	public static String vname;
	/**
	 * mac
	 */
	public static String mac;
	/**
	 * ����
	 */
	public static String board;
	/**
	 * cpuָ�
	 */
	public static String abi;
	/**
	 * �豸����
	 */
	public static String device;
	/**
	 * �޶��汾�б�
	 */
	public static String id;
	/**
	 * mf
	 */
	public static String mf;
	/**
	 * ����build�ı�ǩ
	 */
	public static String tags;
	/**
	 * builder����
	 */
	public static String type;
	/**
	 * user
	 */
	public static String user;
	/**
	 * �豸ID
	 */
	public static String deviceid;
	/**
	 * ����λ����Ϣ
	 */
	private LocationInfo locInfo;

	private static boolean enableLocation = true;
	// private SmsCenter msmCenter;

	static SystemInfo mSystemInfo;

	private SystemInfo() {
		if (enableLocation)
			locInfo = new LocationInfo();
		// msmCenter = new SmsCenter();
		initSystemInfo();
	}

	public static SystemInfo getInstance(String channel, int vcode, boolean enableLocation) {
		if (mSystemInfo == null) {
			SystemInfo.enableLocation = enableLocation;
			mSystemInfo = new SystemInfo();
			mSystemInfo.channel = channel;
			mSystemInfo.vcode = vcode;
		}
		return mSystemInfo;
	}

	public static SystemInfo getInstance(String channel, int vcode) {
		if (mSystemInfo == null) {
			mSystemInfo = new SystemInfo();
			mSystemInfo.channel = channel;
			mSystemInfo.vcode = vcode;
		}
		return mSystemInfo;
	}

	public static SystemInfo getInstance() {
		return mSystemInfo;
	}

	public void setLocationListennerListenner(LocationListennerListenner locationListenner) throws Exception {
		mSystemInfo.locInfo.setLocationListenner(locationListenner);
	}

	private void initSystemInfo() {

		vname = "my_launcher";

		brand = Build.BRAND;
		model = Build.MODEL;
		board = Build.BOARD;
		abi = Build.CPU_ABI;
		device = Build.DEVICE;
		id = Build.ID;
		tags = Build.TAGS;
		type = Build.TYPE;
		user = Build.USER;
		product = Build.PRODUCT;
		mf = Build.MANUFACTURER;
		os = Build.VERSION.RELEASE;

		imei = getImei();
		imsi = getImsi();

		mac = getMac();
		language = getLanguage();
		network = getNetwork();
		display = getDisplay();
		operators = getOperators();
		ip = getLocalIpAddress();

		// sms = msmCenter.getSmsCenter();

		province = "";
		city = "";

		if (SystemInfo.imei == null || SystemInfo.imei.equals("")) {
			SystemInfo.imei = "1234567890";
		}
		if (SystemInfo.imsi == null || SystemInfo.imsi.equals("")) {
			SystemInfo.imsi = "1234567890";
		}

		deviceid = getDeviceID();
		if (locInfo != null)
			locInfo.getLocationInfo();

	}

	/**
	 * ��ȡIMEI��ʶ
	 * 
	 * @param context
	 * @return
	 */
	private String getImei() {
		TelephonyManager tm = (TelephonyManager) Service.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * 
	 * @param imsi
	 * @return 0 δ֪ 1 �ƶ� 2 ��ͨ 3 ����
	 */
	private int getOperators() {
		String imsi = getImsi();
		int operator = 0;
		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				operator = 1;
			} else if (imsi.startsWith("46001")) {
				operator = 2;
			} else if (imsi.startsWith("46003")) {
				operator = 3;
			} else {
				operator = 0;
			}
		}
		return operator;
	}

	/**
	 * ��ȡ��������
	 * 
	 * @return 0 δ֪ 1 wifi 2G 3G 4G
	 */
	private int getNetwork() {

		ConnectivityManager connectMgr = (ConnectivityManager) Service.mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectMgr.getActiveNetworkInfo();
		if (info == null) {
			return 0;
		}

		int type = info.getType();
		if (type == ConnectivityManager.TYPE_WIFI) {
			return 1;
		}
		int substype = info.getSubtype();
		switch (substype) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return 2;// 2G
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return 3;// 3G
		case TelephonyManager.NETWORK_TYPE_LTE:
			return 4;// 4G
		default:
			return 0;
		}
	}

	/**
	 * ��ȡ�ֻ�ֱ���
	 * 
	 * @return
	 */
	private String getDisplay() {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager mWm = (WindowManager) Service.mContext.getSystemService(Context.WINDOW_SERVICE);
		mWm.getDefaultDisplay().getMetrics(dm);
		String display = dm.widthPixels + "*" + dm.heightPixels;
		return display;
	}

	/**
	 * ��ȡ�ֻ�ǰ��������
	 * 
	 * @return
	 */
	private String getLanguage() {

		String language = Locale.getDefault().getLanguage();
		return language;
	}

	/**
	 * ��ȡip��
	 * 
	 * @return
	 */
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Logger.info(this, ex.toString());
		}
		return null;
	}

	/**
	 * ��ȡmac��
	 * 
	 * @return
	 */
	private String getMac() {
		WifiManager wifi = (WifiManager) Service.mContext.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * ��ȡIMSI��ʶ
	 * 
	 * @param context
	 * @return
	 */
	private String getImsi() {
		TelephonyManager tm = (TelephonyManager) Service.mContext.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}

	/**
	 * ��ȡ�ֻ����
	 * 
	 * @param context
	 * @return
	 */
	private String getPhone(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}

	/**
	 * ��ȡ�豸��
	 * 
	 * @return
	 */
	private String getDeviceID() {
		if (deviceid == null) {

			StringBuffer sb = new StringBuffer(200);
			sb.append(imei).append(board).append(brand).append(abi).append(device).append(display).append(id)
					.append(mf).append(model).append(product).append(tags).append(type).append(user);
			deviceid = Util.md5Encode(sb.toString());
			Logger.info(this, "deviceid:" + deviceid);
		}

		return deviceid;
	}

	/**
	 * ��ȡ��ַ��Ϣ
	 * 
	 */
	private class LocationInfo {

		private LocationClient mLocationClient = null;
		private MyLocationListenner myListener = new MyLocationListenner();
		private LocationListennerListenner mLocationListennerListenner = null;

		public LocationInfo() {
			mLocationClient = new LocationClient(Service.mContext);
			mLocationClient.registerLocationListener(myListener);

		}

		public void setLocationListenner(LocationListennerListenner l) {
			mLocationListennerListenner = l;
		}

		private void getLocationInfo() {
			LocationClientOption option = new LocationClientOption();
			option.setOpenGps(true);
			option.setCoorType("bd09ll");
			option.setAddrType("all");
			// option.setScanSpan(1000);
			option.disableCache(true);
			mLocationClient.setLocOption(option);
			mLocationClient.start();

		}

		private class MyLocationListenner implements BDLocationListener {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					return;
				StringBuffer sb = new StringBuffer(256);
				if (location.getCity() == null) {
					int type = mLocationClient.requestLocation();
				}

				province = location.getProvince();
				city = location.getCity();
				mLocationClient.stop();

				if (mLocationListennerListenner != null) {
					if (province != null || city != null) {
						mLocationListennerListenner.onReceiveCompleted();
					} else {
						mLocationListennerListenner.onReceiveFailed();
					}

				}
				String localinfoString = location.getLatitude() + " | " + location.getLongitude() + " | "
						+ location.getRadius() + " | " + location.getProvince() + " | " + location.getCity() + " | "
						+ location.getDistrict() + " | " + location.getAddrStr();

				Logger.info(this, "localinfoString:" + localinfoString);

			}

			public void onReceivePoi(BDLocation poiLocation) {
				if (poiLocation == null) {
					return;
				}
				province = poiLocation.getProvince();
				city = poiLocation.getCity();

				String localinfoString = poiLocation.getLatitude() + " | " + poiLocation.getLongitude() + " | "
						+ poiLocation.getRadius() + " | " + poiLocation.getProvince() + " | " + poiLocation.getCity()
						+ " | " + poiLocation.getDistrict() + " | " + poiLocation.getAddrStr();

				Logger.info(this, "localinfoString:" + localinfoString);
			}
		}
	}

	public interface LocationListennerListenner {
		public void onReceiveCompleted();

		public void onReceiveFailed();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "channel: " + channel + "--imei: " + imei + "--imsi: " + imsi + "--os: " + os + "--province: "
				+ province + "--city: " + city + "--ip: " + ip + "--display: " + display + "--product: " + product
				+ "--brand: " + brand + "--model: " + model + "--language: " + language + "--operators: " + operators
				+ "--network: " + network + "--vcode: " + null + "--vname: " + vname + "--mac: " + mac + "--board: "
				+ board + "--abi: " + abi + "--device: " + device + "--id: " + id + "--mf: " + mf + "--tags: " + tags
				+ "--type: " + type + "--user: " + user;
	}
}
