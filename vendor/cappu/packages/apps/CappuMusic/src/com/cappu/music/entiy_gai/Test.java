package com.cappu.music.entiy_gai;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.test.AndroidTestCase;
import android.util.Log;

import com.cappu.music.MusicInventory;
import com.cappu.music.database.MusicProvider;

public class Test extends AndroidTestCase{
	
	
	//是测分类的数据
	public void Ta()
	{
		 Cursor c = getContext().getContentResolver().query(MusicProvider.BaseMusicColumns.INVENTORY_URI,null,null, null, null);
	        List<MusicInventory> inventoryList = new ArrayList<MusicInventory>();
	      
	        try {
	            final int idIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns._ID);
	            final int songInventoryNameIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_NAME);
	            final int songInventoryIconIndex = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_ICON);
	            final int songInventoryIconType = c.getColumnIndexOrThrow(MusicProvider.BaseMusicColumns.SONG_INVENTORY_TYPE);
	            while (c.moveToNext()) {
	                MusicInventory musicInventory = new MusicInventory();
	                musicInventory.inventoryName = c.getString(songInventoryNameIndex);
	                musicInventory.id = c.getLong(idIndex);
	                musicInventory.iconRes = c.getInt(songInventoryIconIndex);
	                musicInventory.type = c.getInt(songInventoryIconType);
	                inventoryList.add(musicInventory);
	            }
	   	 } catch (Exception e) {
	     }finally{
	         c.close();
	     }
	        for (MusicInventory m : inventoryList) {
				Log.i("test",m.toString()+"==长度=="+inventoryList.size());
			} 
	        
	}
	
	//测对应分类下面的歌曲数据
	public void Tb()
	{
		//"songInventoryId = '"+ 1 +"' "
		Cursor cursor = getContext().getContentResolver().query(MusicProvider.BaseMusicColumns.SONG_URI, null, null, null, null);
        
		while(cursor.moveToNext())
		{
			//_id  , songId ,songInventoryId 
			int sid=cursor.getInt(cursor.getColumnIndex("_id"));
			int songid=cursor.getInt(cursor.getColumnIndex("songId"));
			int id=cursor.getInt(cursor.getColumnIndex("songInventoryId"));
			String stitle=cursor.getString(cursor.getColumnIndex("title"));
			Log.i("test",sid+"==sid=="+songid+"===songid==id==="+id+"==title="+stitle);
		}
		cursor.close();
	
		
		
	}
	
	//修改分类下面的歌曲id
	public void update()
	{
		ContentValues values=new ContentValues();
		values.put("songId",1690);
		getContext().getContentResolver().update(MusicProvider.BaseMusicColumns.SONG_URI, values, "_id = ? ", new String[]{28+""});
	}
	
	//查询数据库中歌曲
	public void Tc()
	{
		Cursor cursor=getContext().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
		while(cursor.moveToNext())
		{
			long sid=cursor.getLong(cursor.getColumnIndex(Media._ID));
			String sname=cursor.getString(cursor.getColumnIndex(Media.TITLE));
			int duration=cursor.getInt(cursor.getColumnIndex(Media.DURATION));
			String uri=cursor.getString(cursor.getColumnIndex(Media.DATA));
			Log.i("test", sid+"==sid==sname=="+sname+"===duration=="+duration+"==uri="+uri);
		}
		cursor.close();
		
	}

	
	//删除音乐表中的数据
	public void delete()
	{
		int sa=getContext().getContentResolver().delete(MusicProvider.BaseMusicColumns.SONG_URI, null, null);
		Log.i("test", "长度=="+sa);
		
	}
	
	public void test()
	{
		MusicProvider provider=new MusicProvider();
		
	}

}
