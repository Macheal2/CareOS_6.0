
package com.cappu.launcherwin.netinfo;

import com.cappu.launcherwin.LauncherSettings;
import com.cappu.launcherwin.R;
import com.cappu.launcherwin.basic.BasicActivity;
//import com.cappu.launcherwin.tools.KookSharedPreferences;
import com.cappu.launcherwin.widget.TopBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class HelpNotesActivity extends BasicActivity{
    Context TextContext;
    int mTextSize = 34;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_notes);
        mTextSize = Settings.Global.getInt(getContentResolver(), "textSize", getResources().getDimensionPixelSize(R.dimen.xl_text_size));
    }
}
