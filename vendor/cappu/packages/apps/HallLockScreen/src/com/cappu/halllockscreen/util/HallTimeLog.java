package com.cappu.halllockscreen.util;

import android.util.Log;

public class HallTimeLog
{
  public static final String TAG = "bird_hall_window";
  public static final String TAG_PREFIX = "{HallLockScreen}";
  
  public static void d(String paramString1, String paramString2)
  {
    Log.d("bird_hall_window", "{HallLockScreen}" + paramString1 + ":" + paramString2);
  }
  
  public static void e(String paramString1, String paramString2)
  {
    Log.e("bird_hall_window", "{HallLockScreen}" + paramString1 + ":" + paramString2);
  }
  
  public static void i(String paramString1, String paramString2)
  {
    Log.i("bird_hall_window", "{HallLockScreen}" + paramString1 + ":" + paramString2);
  }
  
  public static void v(String paramString1, String paramString2)
  {
    Log.v("bird_hall_window", "{HallLockScreen}" + paramString1 + ":" + paramString2);
  }
}

