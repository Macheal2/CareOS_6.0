package com.cappu.drugsteward.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

public class JsonParse {
    
    public static Map<String, Object> getdrug(InputStream is){
        Map<String, Object> maa=new HashMap<String, Object>();
       
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(is));
            StringBuffer sb=new StringBuffer();
            String str=null;
            while((str=br.readLine())!=null){
                sb.append(str);
            }
            br.close();
            Log.i("test","==内容=="+sb.toString());
            JSONObject obj=new JSONObject(sb.toString());
            int result=obj.getInt("error_code");
            if(result == 0){
                JSONObject summary=obj.getJSONObject("result").getJSONObject("summary");
                String bardcode = summary.getString("barcode");
                String name = summary.getString("name");
                maa.put("bardcode", bardcode);
                maa.put("name", name);
            }else{
                return null;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return maa;
    }
 
}
