package com.cappu.music.entiy_gai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.webkit.WebChromeClient.CustomViewCallback;

import com.cappu.music.database.MusicProvider;

public class Songgengxin {
    
    Activity mActivity;
    public Songgengxin(Activity mActivity)
    {
        this.mActivity=mActivity;
    }
    
    // 查询数据库中歌曲
    public void selectzongsong() {
        mSong.clear();
        Cursor cursor = mActivity.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null);
        if(cursor!=null)
        {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndex(Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(Media.TITLE));
                Song song = new Song();
                song.setId(id);
                song.setTitle(title);
                mSong.add(song);
 //               Log.i("test", id + "==id===" + title + "==title");
    
            }
        }
        if(cursor!=null)
        {
            cursor.close();
        }
       
    }

    List<Map<String, Object>> mConnect = new ArrayList<Map<String, Object>>();
    List<Song> mSong = new ArrayList<Song>();
    List<ClassifySong> myClassify = new ArrayList<ClassifySong>();

    // 测对应分类下面的歌曲数据
    public void selectfenzongsong() {
        myClassify.clear();
        Cursor cursor = mActivity.getContentResolver()
                .query(MusicProvider.BaseMusicColumns.SONG_URI, null, null,
                        null, null);
        if(cursor!=null)
        {
        
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                int songid = cursor.getInt(cursor.getColumnIndex("songId"));
                int songInventoryId = cursor.getInt(cursor
                        .getColumnIndex("songInventoryId"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                ClassifySong c = new ClassifySong(id, songid, songInventoryId,
                        title);
                myClassify.add(c);
 //                Log.i("test",
   //              id+"==id=songid="+songid+"===songInventoryId=="+songInventoryId+"=="+title);
            }
        
        }
       if(cursor!=null)
       {
           cursor.close();
       }
       
    }

    // /修改分类下面的歌曲id
    public void update() {

        for (ClassifySong c : myClassify) {
            for (Song s : mSong) {
                if(c!=null&&s!=null)
                {
                    if ((c.getTitle()+"").equals(s.getTitle())) {
                        ContentValues values = new ContentValues();
                        values.put("songId", s.getId());
                        mActivity.getContentResolver().update(
                                MusicProvider.BaseMusicColumns.SONG_URI, values,
                                " title = ? ", new String[] { s.getTitle() });

                    } 
                }
            
            }
        }
        Log.i("ytq", "修改成功否");
    }

    // add ytq xiugai没有点歌曲
    public void deletefensong() {
//        Log.i("ytq", "==classifysong==长度==" + myClassify.size());
//        Log.i("ytq", "==mysong==长度==" + mSong.size());

        for (ClassifySong c : myClassify) {
            boolean fage = false;
            for (Song s : mSong) {
                if(c!=null&&s!=null)
                {
                    if ((c.getTitle()+"").equals(s.getTitle() + "")) {
                        fage = true;
                        break;
                    }
                }

            }
            if (!fage) {
                ContentValues values = new ContentValues();
                values.put("songId", 0);
                mActivity.getContentResolver().update(
                        MusicProvider.BaseMusicColumns.SONG_URI, values,
                        " title = ? ", new String[] { c.getTitle() });
                Log.i("ytq", "删除的数据==" + c.getTitle());
            }
        }

        Log.i("ytq", "没有没执行");
    }

}
