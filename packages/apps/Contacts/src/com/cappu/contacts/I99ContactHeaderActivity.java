package com.cappu.contacts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.R;
import com.android.contacts.detail.PhotoSelectionHandler.PhotoActionListener;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.editor.PhotoActionPopup.Listener;
import com.android.contacts.util.ContactPhotoUtils;
import com.cappu.contacts.util.I99Utils;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.database.Cursor;

public class I99ContactHeaderActivity extends Activity implements View.OnClickListener , AdapterView.OnItemClickListener{
    private static final String TAG = "I99ContactHeaderActivity";
    public  static final String PATH_HEADER = "header";


    private Button mCameraBu , mGalleryBu;
    private GridView mListPhoto;
    private GridAdapter mAdapter;

    static PhotoActionListener mListener;

    private Bitmap mPhotos[];
    private String[] mHeaders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_contact_header_activty);
        AssetManager asset = getAssets();

        try{
            mHeaders = asset.list(PATH_HEADER);

            mPhotos = new Bitmap[mHeaders.length];
            for( int i= 0 ; i< mHeaders.length ; i++){
                Bitmap photo =  null ;
                InputStream input = null ;
                // input = getResources().getAssets().open(PHOTOS[i]);
                Log.i(TAG,"mHeaders = " + mHeaders[i]);
                input = getClass().getResourceAsStream("/assets/" + PATH_HEADER + "/" + mHeaders[i]);
                photo = BitmapFactory.decodeStream(input);
                mPhotos[i] = photo;
            }

        }catch(Exception e){
            Log.i(TAG,"input == null");
        }


        mAdapter = new GridAdapter();
        mListPhoto = (GridView)findViewById(R.id.grid);
        mCameraBu = (Button)findViewById(R.id.i99_camera);
        mGalleryBu = (Button)findViewById(R.id.i99_gallery);

        mListPhoto.setAdapter(mAdapter);
        mListPhoto.setOnItemClickListener(this);
        mCameraBu.setOnClickListener(this);
        mGalleryBu.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v){
        Intent intent = null ;
        switch(v.getId()){
            case R.id.i99_camera :
                if(mListener != null){
                    mListener.onTakePhotoChosen();
                }
                I99ContactHeaderActivity.this.finish();
            break;

            case R.id.i99_gallery :
                if(mListener != null){
                    mListener.onPickFromGalleryChosen();
                }
                I99ContactHeaderActivity.this.finish();
            break;

        }
    }
    @Override
    public void onItemClick(AdapterView<?> parents, View v, int position, long id) {
        Uri uri =null;
        try{
           // saveToCache(I99ContactHeaderActivity.this ,mHeaders[position] );
           uri = saveToDB(I99ContactHeaderActivity.this ,mHeaders[position]);
        }catch(Exception e){
            android.util.Log.e("wangcunxi","onItemClick:e::"+e);
        }

        if(mListener != null){
            String photoName = mHeaders[position];
            photoName = photoName.substring(0,photoName.lastIndexOf(".png"));
            mListener.onI99PhotoSelected(uri,photoName);
        }

        I99ContactHeaderActivity.this.finish();
    }

    public static void setListener(Listener listenr){
        mListener = (PhotoActionListener)listenr;
    }
    private class GridAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mPhotos.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mPhotos[position];
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
            if(view == null){
                view = new ImageView(I99ContactHeaderActivity.this);
                int size = I99Utils.getScreenSize(I99ContactHeaderActivity.this)[0]/3 - 2;
                view.setLayoutParams(new LayoutParams(size, size));
                view.setBackgroundResource(R.drawable.i99_header_bg);
            }

            ImageView img = (ImageView)view;
            img.setImageBitmap(mPhotos[position]);
            return view;
        }
    }
    public void saveToCache(Context context , String name) throws Exception {

        //InputStream input = getResources().getAssets().open(name);
        InputStream input = getClass().getResourceAsStream("/assets/" + PATH_HEADER + "/" + name);

        File file = new File(context.getExternalCacheDir() + "/tmp");
        file.mkdirs();
        file = new File(file, name);

        FileOutputStream outStream = new FileOutputStream(file);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "Entry_saveToSDCard_startwrite");
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = input.read(buffer)) != -1){
                outStream.write(buffer,0,len);
        }
        outStream.flush();
        Log.i(TAG, "Entry_saveToSDCard_write_over");
        outStream.close();
        input.close();
    }


    
    private static final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
    public Uri saveToDB(Context context , String name) throws Exception {
        //InputStream input = getResources().getAssets().open(name);
        Uri uri = null;
        ContentResolver cv = context.getContentResolver();
        Cursor cursor = cv.query(STORAGE_URI,  
        new String[]{"_id"},  
            Images.Media.DISPLAY_NAME + "=?",new String[]{name}, null);
        if(cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()){
            uri = Uri.withAppendedPath(STORAGE_URI, "" + cursor.getInt(0));
            cursor.close();
            return uri;
        }

        InputStream input = getClass().getResourceAsStream("/assets/" + PATH_HEADER + "/" + name);
        File file = new File(context.getExternalCacheDir() + "/tmp");
        file.mkdirs();
        file = new File(file, name);
        FileOutputStream outStream = new FileOutputStream(file);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] buffer = new byte[1024];
        int len = 0;
        while((len = input.read(buffer)) != -1){
                outStream.write(buffer,0,len);
        }
        outStream.flush();
        Log.i(TAG, "Entry_saveToSDCard_write_over");
        outStream.close();
        input.close();
        ContentValues values = new ContentValues(5);  
        values.put(Images.Media.TITLE, name);  
        values.put(Images.Media.DISPLAY_NAME, name);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");
        values.put(Images.Media.DATA, file.getPath());  
        values.put(Images.Media.SIZE, file.length());
        uri = cv.insert(STORAGE_URI, values);
        return uri;
    }
}
