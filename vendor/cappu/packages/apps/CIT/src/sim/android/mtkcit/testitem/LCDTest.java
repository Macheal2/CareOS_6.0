package sim.android.mtkcit.testitem;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
//import java.util.Timer;
//import java.util.TimerTask;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class LCDTest extends Activity {
	LCDView lcdView;
	Timer timer;
	private boolean testPass;
    private boolean cyleShow = false ;
	class LCDView extends View {

		public LCDView(Context context) {

			super(context);
			m_nCurrentPage = 0;
		}


//		private void OnKeypressRight() {
//			if (m_nCurrentPage == 4) {
//				finish();
//			} else {
//				m_nCurrentPage++;
//				invalidate();
//			}
//		}
//
//		private void onKeypressLeft() {
//			if (m_nCurrentPage == 0) {
//				finish();
//			} else {
//				m_nCurrentPage--;
//				invalidate();
//			}
//		}

		private void palette_Display(Canvas canvas, int i) {
			switch (i) {
			case C_RED:
				canvas.drawColor(0xffff0000);
				break;
			case C_GREEN:
				canvas.drawColor(0xff00ff00);
				break;
			case C_BLUE:
				canvas.drawColor(0xff0000ff);
				break;
			case C_BLACK:
				canvas.drawColor(0xff000000);
				break;
			case C_WHITE :
				canvas.drawColor(0xFFFFFFFF);
			default:
				break;

			}
		}

		protected void onDraw(Canvas canvas) {
			if (m_nCurrentPage == 5) {
				m_nCurrentPage=0;
				if(!cyleShow) {
					finish();

				}
			}
			palette_Display(canvas, m_nCurrentPage);
		}

		public boolean onTouchEvent(MotionEvent motionevent) {

			int action = motionevent.getAction();
			switch (action) {
			// return true;
			case MotionEvent.ACTION_UP:

				if(timer != null){
					timer.cancel();
					m_nCurrentPage = 0;
					timer = null;
					cyleShow = true ;
				}
				lcdView.postInvalidate();
				lcdView.m_nCurrentPage++;
				break;

			}
			return true;
		}

		private static final int C_BLACK = 3;
		private static final int C_BLUE = 2;
		private static final int C_GREEN = 1;
		private static final int C_RED = 0;
		private static final int C_WHITE = 4;
		private int m_nCurrentPage;

	}
	/*
	int id = button.getId();
	Bundle b = new Bundle();
	Intent intent = new Intent();

	if (id == R.id.btn_success) {
		b.putInt("test_result", 1);
	} else {
		b.putInt("test_result", 0);
	}
	intent.putExtras(b);
	setResult(RESULT_OK, intent);
	finish();
*/
	class timetask extends TimerTask {

		public void run() {
			try {
				Thread.sleep(1000L);
				Log.v("timetask", "run()");
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.v("timetask", "timetask  error");
				return;
			}
			lcdView.postInvalidate();
			lcdView.m_nCurrentPage++;
		}

	}

	public LCDTest() {

		timer = new Timer();
	}

	private void setFullscreen() {
//		requestWindowFeature();
        requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(1024, 1024);
	}

	protected void onCreate(Bundle bundle) {
		lcdView = new LCDView(this);
		setFullscreen();
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		super.onCreate(bundle);
		setContentView(lcdView);
		Toast toast = Toast.makeText(this, R.string.touchScreen, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0, 0);
      	toast.setMargin(0f,0.5f);
		toast.show();
		
		timetask timetask1 = new timetask();
		timer.schedule(timetask1, 0, 500L);
	}

	
	@Override
	protected void onStop() {
		releaseResource();

 		super.onStop();
	}
/*
	public boolean onKeyUp(int i, KeyEvent keyevent) {

		// _L1:
		//
		// _L3:
		switch (keyevent.getAction()) {
		case KeyEvent.ACTION_DOWN:
			lcdView.onKeypressLeft();
			break;
		// _L4:
		case KeyEvent.ACTION_UP:
			lcdView.OnKeypressRight();
			break;

		// finish();
		}
		return super.onKeyUp(i, keyevent);
		// if(true) goto _L1; else goto _L5
		// _L5:
	}
*/

	private void releaseResource() {
     if(timer!=null) {
    	 timer.cancel();
    	 timer=null;
     }		
	}
}