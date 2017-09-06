package com.cappu.contacts;

import android.util.TypedValue;

public class I99Font {
   public static final int TYPE = TypedValue.COMPLEX_UNIT_DIP;
   public static final float TITLE_S_PERCENTAGE = 0.8f ; 
   public static float TITLE = 30; // default -- i99_text_size_default
   public static float TITLE_S = TITLE*TITLE_S_PERCENTAGE;
   public static float SUMMERY = 17; // default -- i99_text_summary_size_default
   public static float NORMAL;
   
   public static void updateOtherFont(){
        TITLE_S = TITLE*TITLE_S_PERCENTAGE;
   }
}