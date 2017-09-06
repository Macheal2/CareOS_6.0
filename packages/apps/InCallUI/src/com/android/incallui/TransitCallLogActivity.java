package com.android.incallui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TransitCallLogActivity extends Activity {

    public static final String SHOW_DIALPAD_EXTRA = "InCallActivity.show_dialpad";
    public static final String DIALPAD_TEXT_EXTRA = "InCallActivity.dialpad_text";
    public static final String NEW_OUTGOING_CALL_EXTRA = "InCallActivity.new_outgoing_call";
    public static final String SHOW_CIRCULAR_REVEAL_EXTRA = "InCallActivity.show_circular_reveal";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        final Intent intentCall = new Intent(Intent.ACTION_MAIN,null);
        intentCall.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
       // intentCall.setClassName("com.android.dialer",  "com.android.incallui.InCallActivity");
       // intentCall.setClassName("com.android.dialer",  "com.android.incallui.InCallActivity");
        intentCall.setClass(getApplicationContext(), InCallActivity.class);
        intentCall.putExtra(NEW_OUTGOING_CALL_EXTRA, false);
        intentCall.putExtra(SHOW_CIRCULAR_REVEAL_EXTRA, false);
        startActivity(intentCall);
        
//        Intent intent = new Intent(this, com.android.incallui.InCallActivity.class);
//        startActivity(intent);
    }
}
