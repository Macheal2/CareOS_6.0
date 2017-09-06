package sim.android.mtkcit.runintest;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.SystemProperties;
import sim.android.mtkcit.cittools.CITTools;
import sim.android.mtkcit.R;
import sim.android.mtkcit.RunInTest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SubCameraTest extends RuninBaseActivity implements SurfaceHolder.Callback {

	private boolean debugFlag = true;
	private final String TAG = "SubCameraTest";
	private boolean testFlas = true;

    private static String imgPath = Environment.getExternalStorageDirectory().getPath() + "/DCIM/CAMERA"  ;
      
    private SurfaceView surfaceView;   
    private SurfaceHolder surfaceHolder;  
    private Button takePicView;  
	private Button succesButton ;
	private Button failButton ;  
    private Camera mCamera;  
    File file = null; 


	@Override
	protected void onCreate(Bundle bundle) {

		// TODO Auto-generated method stub
		super.onCreate(bundle);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.run_in_camera);

        surfaceView = (SurfaceView)this.findViewById(R.id.camera_view);  
        surfaceHolder = surfaceView.getHolder();  
        surfaceHolder.addCallback(this);  
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
          
        checkSoftStage(); 

	}

	@Override
	protected void onResume() {
		initRec();
		super.onResume();
	}

	private void initRec() {

		new Thread(new mThread()).start();
		
	}

    private void checkSoftStage(){  
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){ 
            file = new File(imgPath);  
            if(!file.exists()){  
                file.mkdir();  
            }  
        }else{  
        	 new AlertDialog.Builder(this).setMessage(getString(R.string.speak_test_hasnot_sdcard))  
             .setPositiveButton(getString(R.string.alert_dialog_ok), new DialogInterface.OnClickListener() {  
                 @Override  
                 public void onClick(DialogInterface dialog, int which) {  
                     finish();  
                 }  
             }).show();  
        }  
    }  

	//modify by even delete files
	//start
	public void deleteFile(File file) {
		if (file.exists()) { // \u5224\u65ad\u6587\u4ef6\u662f\u5426\u5b58\u5728
		if (file.isFile()&& file.getName().startsWith("YY")) { // \u5224\u65ad\u662f\u5426\u662f\u6587\u4ef6
		file.delete(); // delete()\u65b9\u6cd5 \u4f60\u5e94\u8be5\u77e5\u9053 \u662f\u5220\u9664\u7684\u610f\u601d;
		} else if (file.isDirectory()) { // \u5426\u5219\u5982\u679c\u5b83\u662f\u4e00\u4e2a\u76ee\u5f55
		File files[] = file.listFiles(); // \u58f0\u660e\u76ee\u5f55\u4e0b\u6240\u6709\u7684\u6587\u4ef6 files[];
		for (int i = 0; i < files.length; i++) { // \u904d\u5386\u76ee\u5f55\u4e0b\u6240\u6709\u7684\u6587\u4ef6
			if(files[i].getName().startsWith("YY"))
			{
				this.deleteFile(files[i]); // \u628a\u6bcf\u4e2a\u6587\u4ef6 \u7528\u8fd9\u4e2a\u65b9\u6cd5\u8fdb\u884c\u8fed\u4ee3
			}
		}
		}
		file.delete();
		} else {
			//("\u6587\u4ef6\u4e0d\u5b58\u5728\uff01"+"\n");
		}
	}
	//end
      
    private final OnClickListener TakePicListener = new OnClickListener(){  
        @Override  
        public void onClick(View v) {  
           // mCamera.autoFocus(new AutoFoucus());  
 	    mCamera.takePicture(mShutterCallback, null, mPictureCallback);  
            takePicView.setEnabled(false);
        }  
    }; 

    private final class AutoFoucus implements AutoFocusCallback{  
        @Override  
        public void onAutoFocus(boolean success, Camera camera) {  
            if(success && mCamera!=null){  
                mCamera.takePicture(mShutterCallback, null, mPictureCallback);  
            }  
        }  
    }  
    
    private final PictureCallback mPictureCallback = new PictureCallback() {  
        @Override  
        public void onPictureTaken(byte[] data, Camera camera) {  
            try {  
                String fileName = "YY"+ System.currentTimeMillis()+".jpg";  
                file = new File(imgPath,fileName);  
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);  
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));  
                bm.compress(Bitmap.CompressFormat.JPEG, 60, bos);  
                bos.flush();  
                bos.close();  
                takePicView.setEnabled(false);
                //Intent intent = new Intent(CameraTest.this,PictureViewAct.class);  
                //intent.putExtra("imagePath", file.getPath());  
                //startActivity(intent);  
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    };  
       
    private final ShutterCallback mShutterCallback = new ShutterCallback() {    
        public void onShutter() {    
            Log.d("ShutterCallback", "...onShutter...");    
        }    
    };  
      
    @Override  
     
    public void surfaceChanged(SurfaceHolder holder, int format, int width,  
            int height) {  
        Camera.Parameters param = mCamera.getParameters();  
       
        param.setPictureFormat(PixelFormat.JPEG);  
         
        //param.setPreviewSize(320, 240);  
 

        param.setPictureSize(640,480);  
       // mCamera.setDisplayOrientation(180);
        mCamera.setParameters(param);  
        
        mCamera.startPreview();  
    }  
    @Override  
      
    public void surfaceCreated(SurfaceHolder holder) {  
        try {  
            mCamera = Camera.open(1); 
            mCamera.setPreviewDisplay(holder);  
        } catch (IOException e) {  
            mCamera.release();  
            mCamera = null;  
        }  
    }  
      
    @Override  
   
    public void surfaceDestroyed(SurfaceHolder holder) {  
        if(mCamera!=null)
        {
            mCamera.stopPreview();  
            mCamera.release();  
            mCamera = null; 
        }  
	if(null != file)
	{
		deleteFile(file);
	}
    }  
      
    @Override  
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if(keyCode == KeyEvent.KEYCODE_CAMERA){  
            mCamera.autoFocus(new AutoFoucus());  
            return true;  
        }else{  
            return false;  
        }  
    }  

	@Override
	void cyleTest() {
		LOGV(debugFlag, TAG, "cyleTest()");
		Intent intent = new Intent();
		intent.setClass(this, MVPlayerTest.class);
		startActivity(intent);
		this.finish();
	}
	
	class mThread implements Runnable {
		@Override
		public void run() {
			LOGV(debugFlag, TAG, "run() ");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOGV(debugFlag, TAG, "mThread error ");
				return;
			}
//			currentTime = System.currentTimeMillis();
//			LOGV(debugFlag, TAG, "currentTime=" + currentTime);
//			if (currentTime > RunInTest.stopTime) {
//				stopTest();
//
//			} else

                //modify by even
                //start
                if(mCamera!=null)
                {
                    mCamera.stopPreview();  
                    mCamera.release();  
                    mCamera = null; 
                }
                //end
				cyleTest();

		}
	}
}
