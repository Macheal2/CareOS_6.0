package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.location.*;
import android.os.*;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import sim.android.mtkcit.R;

//import sim.android.mtkcit.TestActivity;

public class GPSTest extends TestBase {
	private static final int MAX_SATELITE_COUNT = 10;
	private static final String TAG_GPS = "GPSTest";
	Handler hGpsHand;
	private final LocationListener locationListener;
	private int mStateliteCount;
	private String mStrAzimuth;
	private String mStrElevation;
	private String mStrPrn;
	private String mStrSnr;
	private String m_bestProvider;
	private GpsStatus m_gpsStatus;
	boolean m_isGpsOpen;
	private double m_lat;
	private double m_lng;
	private Location m_location;
	private LocationManager m_mgr;
	private GpsStatus.Listener m_statusListener;
	private TextView mtv_LocationSuccess;
	private TextView mtv_Testtime;
	private GpsStatus.Listener statusListener;
	private final GpsStatus.NmeaListener nmeaListener;
	TextView tvSatelite[];
	PowerManager.WakeLock wakeLock;
	File gps_data_file;
	FileOutputStream gps_data_output;
	private float averageSignal = 0;
	private int sateliteSignalNum = 0;
	boolean loc_success = false;
	private Timer mtimer;
	private MTimerTask mtimertask;
	GpsInfo mGpsInfo[];
	GpsInfo mGpsinfo_sort[];
	String TAG = "GPSTest";
	int mSecond;

	class GpsInfo {

		private float fAzimuth;
		private float fElevation;
		int iID;
		int prn;
		private float snr;
		boolean sort;
		final GPSTest gpsTest;

		public GpsInfo() {
			gpsTest = GPSTest.this;
			prn = 0;
			iID = 0;
			fAzimuth = 0F;
			fElevation = 0F;
			snr = 0F;
		}
	}

	public GPSTest() {
		Log.v(TAG, "GPSTest() ");
		mGpsInfo = new GpsInfo[20];
		mGpsinfo_sort = new GpsInfo[20];
		m_isGpsOpen = false;
		hGpsHand = new GHandler();
		statusListener = new GpsStatusListner();
		locationListener = new LocaltionLis();
		nmeaListener = new NmeaLis();
	}

	private void alert(String s, final boolean exit) {
		AlertDialog.Builder builder = (new AlertDialog.Builder(this))
				.setTitle(s);
		String s1 = getString(R.string.alert_dialog_ok);
		AlertDialog.Builder builder1 = builder.setPositiveButton(s1,
				new DialogListener());
		builder1.setNegativeButton(R.string.alert_dialog_cancel, null).show();
	}

	private void closeGPS() {
		android.provider.Settings.Secure.setLocationProviderEnabled(
				getContentResolver(), "gps", false);
	}

	private void initAllControl() {
		for (int i = 0; i < 20; i++)
			mGpsInfo[i] = new GpsInfo();

		tvSatelite = new TextView[MAX_SATELITE_COUNT + 2];
		tvSatelite[0] = (TextView) findViewById(R.id.tv_st_1);
		tvSatelite[1] = (TextView) findViewById(R.id.tv_st_2);
		tvSatelite[2] = (TextView) findViewById(R.id.tv_st_3);
		tvSatelite[3] = (TextView) findViewById(R.id.tv_st_4);
		tvSatelite[4] = (TextView) findViewById(R.id.tv_st_5);
		tvSatelite[5] = (TextView) findViewById(R.id.tv_st_6);
		tvSatelite[6] = (TextView) findViewById(R.id.tv_st_7);
		tvSatelite[7] = (TextView) findViewById(R.id.tv_st_8);
		tvSatelite[8] = (TextView) findViewById(R.id.tv_st_9);
		tvSatelite[9] = (TextView) findViewById(R.id.tv_st_10);
		tvSatelite[10] = (TextView) findViewById(R.id.tv_st_11);
		tvSatelite[11] = (TextView) findViewById(R.id.tv_st_12);
		/*tvSatelite[12] = (TextView) findViewById(R.id.tv_st_13);
		tvSatelite[13] = (TextView) findViewById(R.id.tv_st_14);
		tvSatelite[14] = (TextView) findViewById(R.id.tv_st_15);
		tvSatelite[15] = (TextView) findViewById(R.id.tv_st_16);*/
		mtv_LocationSuccess = (TextView) findViewById(R.id.tv_st_success);
		mtv_Testtime = (TextView) findViewById(R.id.tv_st_time);
		mStrSnr = getString(R.string.gps_st_info_snr);
		mStrPrn = getString(R.string.gps_st_info_prn);
		mStrAzimuth = getString(R.string.gps_st_info_azimuth);
		mStrElevation = getString(R.string.gps_st_info_elevation);
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_success.setOnClickListener(this);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_fail.setOnClickListener(this);
		ct.initButton(btn_success);

	}

	private void openGPS() {
		android.provider.Settings.Secure.setLocationProviderEnabled(
				getContentResolver(), "gps", true);
	}

	private void sortStateliteinfo(int i) {
		int j = 0, k = 0;
		for (j = 0; j < i; j++) {
			mGpsInfo[j].sort = false;
		}
		for (j = 0; j < i; j++) {
			float snr_temp = 0;
			int tag = 0;
			for (k = 0; k < i; k++) {
				if ((mGpsInfo[k].sort == false) && (mGpsInfo[k].snr > snr_temp)) {
					snr_temp = mGpsInfo[k].snr;
					tag = k;
				}
			}
			mGpsinfo_sort[j] = mGpsInfo[tag];
			mGpsInfo[tag].sort = true;
		}
	}

	private void sortsetStateliteinfo(int i) {
		if (i > 2) {
			btn_success.setEnabled(true);
		}
		int j = i;
		int k;
		if (j > MAX_SATELITE_COUNT)
			j = MAX_SATELITE_COUNT;
		for (k = 0; k < j; k++) {
			tvSatelite[k].setText("ID:" + (k + 1) + ", " + mStrSnr
					+ mGpsinfo_sort[k].snr + ", " + mStrPrn
					+ mGpsinfo_sort[k].prn + ", " + mStrAzimuth
					+ +mGpsinfo_sort[k].fAzimuth);

			averageSignal = (averageSignal * sateliteSignalNum + mGpsinfo_sort[k].snr)
					/ (sateliteSignalNum + 1);
			sateliteSignalNum++;
		}
		if (k < MAX_SATELITE_COUNT) {
			for (; k < MAX_SATELITE_COUNT; k++)
				tvSatelite[k].setText("");
		}
		String averageSignalIntensity = getString(R.string.gps_info_averageSignalIntensity)
				+ averageSignal;
		tvSatelite[MAX_SATELITE_COUNT].setText(averageSignalIntensity);

		if (loc_success == true) {
			Location location = m_mgr.getLastKnownLocation("gps");
			if (location != null) {
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				float speed = location.getSpeed();

				String latLongString = getString(R.string.gps_info_lat) + lat
						+ "\n" + getString(R.string.gps_info_lon) + lng + "\n"
						+ getString(R.string.gps_info_speed) + speed + ", "
						+ getString(R.string.gps_info_elevation)
						+ (float) location.getAltitude() + ", "
						+ getString(R.string.gps_info_accuracy)
						+ (float) location.getAccuracy();
				tvSatelite[MAX_SATELITE_COUNT + 1].setText(latLongString);
			} else {
				String latLongString = getString(R.string.gps_info_null);
				tvSatelite[MAX_SATELITE_COUNT + 1].setText(latLongString);
			}
		}

	}

	private void setStateliteinfo(int i) {
		int j = i;
		if (j > 10)
			j = 10;
		for (int k = 0; k < j; k++) {
			TextView textview = tvSatelite[k];
			StringBuilder stringbuilder = (new StringBuilder()).append("ID:");
			int l = k + 1;
			StringBuilder stringbuilder1 = stringbuilder.append(l).append("  ");
			String s = mStrSnr;
			StringBuilder stringbuilder2 = stringbuilder1.append(s);
			float f = mGpsInfo[k].snr;
			StringBuilder stringbuilder3 = stringbuilder2.append(f)
					.append("  ");
			String s1 = mStrPrn;
			StringBuilder stringbuilder4 = stringbuilder3.append(s1);
			int i1 = mGpsInfo[k].prn;
			StringBuilder stringbuilder5 = stringbuilder4.append(i1).append(
					"\n");
			String s2 = mStrAzimuth;
			StringBuilder stringbuilder6 = stringbuilder5.append(s2).append(
					"  ");
			float f1 = mGpsInfo[k].fAzimuth;
			StringBuilder stringbuilder7 = stringbuilder6.append(f1).append(
					"  ");
			String s3 = mStrElevation;
			StringBuilder stringbuilder8 = stringbuilder7.append(s3);
			float f2 = mGpsInfo[k].fElevation;
			String s4 = stringbuilder8.append(f2).toString();
			textview.setText(s4);
		}

	}

	private void updateWithNewLocation(Location location) {
		/*
		 * String latLongString;
		 * 
		 * if (location != null) { double lat = location.getLatitude(); double
		 * lng = location.getLongitude(); float speed = location.getSpeed();
		 * 
		 * latLongString = getString(R.string.gps_info_lat) + lat +
		 * "\n"+getString(R.string.gps_info_lon) + lng + "\n"+
		 * getString(R.string.gps_info_speed) + speed+
		 * ", "+getString(R.string.gps_info_elevation) +
		 * (float)location.getAltitude()+
		 * ", "+getString(R.string.gps_info_accuracy) +
		 * (float)location.getAccuracy(); tvSatelite[15].setText(latLongString);
		 * } else { latLongString = getString(R.string.gps_info_null);
		 * tvSatelite[15].setText(latLongString); }
		 */

	}

	public void onCreate(Bundle bundle) {
		Log.v(TAG, "onCreate ");

		super.onCreate(bundle);
		super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.test_gps);
		initAllControl();

	}

	@Override
	protected void onStart() {
		Log.v(TAG, "onStart ");
		mtimer = new Timer();
		mtimertask = new MTimerTask();
		m_mgr = (LocationManager) getSystemService("location");
		if (!m_mgr.isProviderEnabled("gps")) {
			m_isGpsOpen = true;
			openGPS();
		}
		mtv_LocationSuccess.setText(R.string.tv_st_success);
		String latLongString = getString(R.string.gps_info_null);
		tvSatelite[MAX_SATELITE_COUNT + 1].setText(latLongString);

		Log.v(TAG, "mtimertask=" + mtimertask + ";mtimer=" + mtimer);
		mtimer.schedule(mtimertask, 1000L, 1000L);
		super.onStart();
	}

	public boolean onKeyDown(int i, KeyEvent keyevent) {
		boolean flag;
		if (i == KeyEvent.KEYCODE_BACK) {
			m_mgr.removeGpsStatusListener(statusListener);
			m_mgr.removeNmeaListener(nmeaListener);
			mtimer.cancel();
			if (m_isGpsOpen)
				closeGPS();
			flag = true;
			this.finish();
		} else {
			flag = false;
		}
		return flag;
	}

	protected void onPause() {
		super.onPause();
		m_mgr.removeUpdates(locationListener);
		if (wakeLock != null) {
			wakeLock.release();
		}
		if (gps_data_output != null) {
			try {
				gps_data_output.close();
			} catch (IOException e) {
				Log.e("GPSTest", "can not close GPS data file");
			}
		}
	}

	protected void onResume() {
		super.onResume();
		m_mgr.requestLocationUpdates("gps", 1000L, 1F, locationListener);
		boolean flag = m_mgr.addGpsStatusListener(statusListener);
		String s = (new StringBuilder()).append("Add the statusListner is")
				.append(flag).toString();
		Log.e("GPSTest", s);
		boolean flag1 = m_mgr.addNmeaListener(nmeaListener);
		String s1 = (new StringBuilder()).append("Add the nmeaListener is")
				.append(flag1).toString();
		Log.e("GPSTest", s1);
		wakeLock = ((PowerManager) getSystemService(POWER_SERVICE))
				.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ON_AFTER_RELEASE, "GPSTest");
		wakeLock.acquire();
		SimpleDateFormat sDataFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");
		Date curDate = new Date(System.currentTimeMillis());
		String stime = sDataFormat.format(curDate);
		File gps_dir = new File("/sdcard");
		String gpsdata_file_name = "gpsdata" + stime + ".txt";
		gps_data_file = new File(gps_dir, gpsdata_file_name);
		try {
			gps_data_file.createNewFile();
		} catch (IOException e) {
			Log.e("GPSTest", "can not create GPS data file");
		}
		try {
			gps_data_output = new FileOutputStream(gps_data_file);
		} catch (FileNotFoundException e) {
			Log.e("GPSTest", "can not open GPS data file");
		}
	}

	@Override
	protected void onStop() {
		releaseResource();
		super.onStop();
	}

	private void releaseResource() {
		m_mgr.removeGpsStatusListener(statusListener);
		m_mgr.removeNmeaListener(nmeaListener);
		mSecond = 0;
		if (mtimer != null) {
			mtimer.cancel();

		}
		if (m_isGpsOpen)
			closeGPS();
	}

	private class GHandler extends Handler {
		public void handleMessage(Message message) {
			TextView textview = mtv_Testtime;
			StringBuilder stringbuilder = (new StringBuilder()).append("");
			String s = stringbuilder.append(mSecond).toString();
			textview.setText(s);
		}
	}

	private class GpsStatusListner implements GpsStatus.Listener {

		public void onGpsStatusChanged(int status) {
			GPSTest gpstest = GPSTest.this;
			GpsStatus gpsstatus = m_mgr.getGpsStatus(null);
			gpstest.m_gpsStatus = gpsstatus;
			switch (status) {
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				int j = m_gpsStatus.getTimeToFirstFix();
				mtv_LocationSuccess.setText(R.string.tv_st_located);
				mtimer.cancel();
				loc_success = true;
				String s = (new StringBuilder())
						.append("GpsStatus.GPS_EVENT_FIRST_FIX the fix Time is ")
						.append(j).toString();
				Log.d("GPSTest", s);
				break;
			// _L5:
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				Log.d("GPSTest", "GpsStatus.GPS_EVENT_SATELLITE_STATUS");
				Iterator iterator = m_gpsStatus.getSatellites().iterator();
				int k = 0;
				String s5;
				for (; iterator.hasNext(); Log.d("GPSTest", s5)) {
					GpsSatellite gpssatellite = (GpsSatellite) iterator.next();
					GpsInfo gpsinfo = mGpsInfo[k];
					int l = gpssatellite.getPrn();
					gpsinfo.prn = l;
					GpsInfo gpsinfo1 = mGpsInfo[k];
					float f = gpssatellite.getAzimuth();
					gpsinfo1.fAzimuth = f;
					GpsInfo gpsinfo2 = mGpsInfo[k];
					float f1 = gpssatellite.getElevation();
					gpsinfo2.fElevation = f1;
					GpsInfo gpsinfo3 = mGpsInfo[k];
					float f2 = gpssatellite.getSnr();
					gpsinfo3.snr = f2;
					mGpsInfo[k].iID = k;
					StringBuilder stringbuilder = (new StringBuilder())
							.append("mGpsInfo[iCount].prn is ");
					int i1 = mGpsInfo[k].prn;
					String s1 = stringbuilder.append(i1).toString();
					Log.d("GPSTest", s1);
					StringBuilder stringbuilder1 = (new StringBuilder())
							.append("mGpsInfo[iCount].fAzimuth is ");
					float f3 = mGpsInfo[k].fAzimuth;
					String s2 = stringbuilder1.append(f3).toString();
					Log.d("GPSTest", s2);
					StringBuilder stringbuilder2 = (new StringBuilder())
							.append("mGpsInfo[iCount].fElevation");
					float f4 = mGpsInfo[k].fElevation;
					String s3 = stringbuilder2.append(f4).toString();
					Log.d("GPSTest", s3);
					StringBuilder stringbuilder3 = (new StringBuilder())
							.append("mGpsInfo[iCount].snr");
					float f5 = mGpsInfo[k].snr;
					String s4 = stringbuilder3.append(f5).toString();
					Log.d("GPSTest", s4);
					StringBuilder stringbuilder4 = (new StringBuilder())
							.append("mGpsInfo[iCount].iID");
					int j1 = mGpsInfo[k].iID;
					s5 = stringbuilder4.append(j1).toString();

					k++;
				}

				mStateliteCount = k;
				StringBuilder stringbuilder5 = (new StringBuilder())
						.append("the mStateliteCount is");
				int k1 = mStateliteCount;
				String s6 = stringbuilder5.append(k1).toString();
				Log.d("GPSTest", s6);
				sortStateliteinfo(k);
				sortsetStateliteinfo(k);
				// setStateliteinfo(k);
				break;
			// _L2:
			case GpsStatus.GPS_EVENT_STARTED:
				Log.d("GPSTest", "GpsStatus.GPS_EVENT_STARTED");
				break;
			// _L3:
			case GpsStatus.GPS_EVENT_STOPPED:
				Log.d("GPSTest", "GpsStatus.GPS_EVENT_STOPPED");
				break;
			/*
			 * _L6:
			 */
			}
		}

	}

	private class LocaltionLis implements LocationListener {

		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		public void onProviderDisabled(String s) {
			updateWithNewLocation(null);
		}

		public void onProviderEnabled(String s) {
		}

		public void onStatusChanged(String s, int i, Bundle bundle) {
		}

	}

	private class NmeaLis implements GpsStatus.NmeaListener {

		public void onNmeaReceived(long timestamp, String nmea) {
			StringBuilder stringbuilder = (new StringBuilder())
					.append("the timestamp is");
			String s = stringbuilder.append(timestamp).toString();
			// Log.d("GPSTest", s);
			StringBuilder stringbuilder1 = (new StringBuilder())
					.append("the nmea is");
			String s1 = stringbuilder1.append(nmea).toString();
			Log.d("GPSTest", s1);
			if ((nmea != null) && (gps_data_output != null)) {
				try {
					int nmea_len = nmea.length();
					byte[] gps_data = new byte[nmea_len];
					nmea.getBytes(0, nmea_len, gps_data, 0);
					gps_data_output.write(gps_data);
					// gps_data_output.write( '\n' );
				} catch (IOException e) {
					Log.e("GPSTest", "can not write NMEA data to file");
				}
			}
		}

	}

	private class DialogListener implements
			android.content.DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialoginterface, int i) {
			setResult(-1);
			if (i == 1) {
				mtimer.cancel();
				// Process.killProcess(Process.myPid());
			}
		}

	}

	class MTimerTask extends TimerTask {

		public void run() {
			// TODO Auto-generated method stub
			mSecond++;
			hGpsHand.sendEmptyMessage(0);
		}

	}

}
