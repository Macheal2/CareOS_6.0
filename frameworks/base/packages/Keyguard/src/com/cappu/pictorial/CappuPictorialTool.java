/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.magcomm.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@magcomm.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial;

import java.io.File;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.Log;

public class CappuPictorialTool {
    private static ICappuPictorial iService = null;
    
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
    
    public static ICappuPictorial getCappuPictorialService(){
        return iService;
    }
    
    public static ICappuPictorial setBindService(Context context){
        final Intent intent = new Intent();
        intent.setAction("com.cappu.CappuPictorialService");
        final Intent eintent = new Intent(createExplicitFromImplicitIntent(context, intent));
        context.bindService(eintent, connection, Service.BIND_AUTO_CREATE);
        return iService;
    }
    
    public static ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            iService = ICappuPictorial.Stub.asInterface(service);
            Log.e("hmq","onServiceConnected "+ name);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            iService = null;
            Log.e("hmq","onServiceDisconnected "+ name);
        }
    };
    
    public static Bitmap getDiskBitmap(String pathString) {
        Bitmap bitmap = null;
        try {
            File file = new File(pathString);
            if (file.exists()) {
                bitmap = BitmapFactory.decodeFile(pathString);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return bitmap;
    }
    
    public static Drawable bitmap2Drawable(Bitmap bitmap) {  
        return new BitmapDrawable(bitmap);  
    }
}