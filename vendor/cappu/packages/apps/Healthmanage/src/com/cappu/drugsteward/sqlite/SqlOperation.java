package com.cappu.drugsteward.sqlite;

import java.util.ArrayList;
import java.util.List;

import com.cappu.drugsteward.entity.Drug;
import com.cappu.drugsteward.entity.Member;

import android.R.integer;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.Telephony.Sms;
import android.util.Log;
import android.widget.Toast;

public class SqlOperation {
    
    private SQLiteDatabase sqldb;

    public SqlOperation(Context context){
        SqlHelper helper=new SqlHelper(context,"drug.db", null, 1);
        sqldb = helper.getWritableDatabase();
    }
    
    //添加成员
    public void addmember(Member m){
        ContentValues values=new ContentValues();
        values.put("name", m.getName());
        values.put("sex", m.getSex());
        values.put("age", m.getAge());
        sqldb.insert("membertable", "sex", values);
    }
    
    //查询成员
    public List<Member> getmember(){
        List<Member> member=new ArrayList<Member>();
        Cursor cursor=sqldb.rawQuery("select * from membertable ", null);
        if( cursor !=null ){
            while(cursor.moveToNext()){
                String name=cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String sex=cursor.getString(cursor.getColumnIndexOrThrow("sex"));
                int age=cursor.getInt(cursor.getColumnIndexOrThrow("age"));
                Member m=new Member(name, sex, age);
                member.add(m);
            }
            cursor.close();
        }
        return member;
    }
    
    //删除成员
    public void deletemember(Member m){
        sqldb.execSQL("delete from membertable where name = '"+m.getName()+"'");
    }
    
    //添加药物
    public void adddrug(Drug d){
        ContentValues values=new ContentValues();
        //dname , producttime, duetime,number, member , company,remark 
        values.put("usergroup", d.getGroup());
        values.put("dname", d.getDname());
        values.put("number", d.getNumber());
        values.put("unit", d.getUnit());
//        values.put("producttime", d.getProducttime());
        values.put("duetime", d.getDuetime());
//        values.put("member", d.getMember());
//        values.put("company", d.getCompany());
//        values.put("remark", d.getRemark());
        sqldb.insert("drugtable","company", values);
    }
    
    //查询药物
    public List<Drug> getdurg(int user_group){
        return getdurg(user_group, "");
    }
    
    public List<Drug> getdurg(int user_group, String sql){
        List<Drug> md=new ArrayList<Drug>();
        String group_s = String.valueOf(user_group);
        Cursor cursor=sqldb.rawQuery(" select * from drugtable WHERE usergroup = ? "+sql, new String[]{group_s});
        int i = 0;
        if(cursor!=null){
            while(cursor.moveToNext()){
                int id=cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                int usergroup = cursor.getInt(cursor.getColumnIndexOrThrow("usergroup"));
                String dname = cursor.getString(cursor.getColumnIndexOrThrow("dname"));
                int number = cursor.getInt(cursor.getColumnIndexOrThrow("number"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));
//                String producttime=cursor.getString(cursor.getColumnIndexOrThrow("producttime"));
                String duetime = cursor.getString(cursor.getColumnIndexOrThrow("duetime"));
//                String member=cursor.getString(cursor.getColumnIndexOrThrow("member"));
//                String company=cursor.getString(cursor.getColumnIndexOrThrow("company"));
//                String remark=cursor.getString(cursor.getColumnIndexOrThrow("remark"));
                Drug d=new Drug(usergroup, dname, number, unit, duetime);
                d.setId(id);
                md.add(d);
                i++;
            }
            cursor.close();
        }
        return md;
    }
    
    
//    private void startQuery() {
//        Uri uri = Sms.CONVESATION_URI;
//        QueryHandler = new QueryHandler();
//        mQueryHandler.startQuery(0, null, uri, CONVERSATION_PROJECTION, null, null, "sms.date desc");
//    }
//
//    // 写一个异步查询类
//
//    private final class QueryHandler extends AsyncQueryHandler {
//        public QueryHandler(ContentResolver cr) {
//            super(cr);
//        }
//
//        @Override
//        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
//            super.onQueryComplete(token, cookie, cursor);
//            // 更新mAdapter的Cursor
//            mAdapter.changeCursor(cursor);
//        }
//    }
    
    
    //删除药物
    public void deletedrug(Drug d){
        if (d.getId() == -1){
            Log.e("hmq","无法删除 dont delete");
            return;
        }
        sqldb.execSQL(" delete from drugtable where _id = '"+d.getId()+"' ");
    }
    
    //查询是不是有药品了
    public boolean  selectsingle(String dname){
        Cursor cursor=sqldb.rawQuery(" select * from drugtable  where dname = '"+dname+"' ", null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                return true;
            }
            cursor.close();
        }
        return false;
    }
    
    public void updatedurg(Drug d){
         //producttime, duetime,number, member , company,remark 
        
        //update drugtable set    dname = 'cao222'  , producttime = '2015-8-25'  , duetime = '2015-8-25'  , number = 1  , member = '哈哈'  , company = 'xxx公司'  where _id = 1
        String sql="update drugtable set  ";
        if(d.getDname()!=null && !d.getDname().equals("")){
            sql +="  dname = '"+d.getDname()+"' ";
        }
        
        if(d.getGroup()!= 0){
            sql +=" , usergroup = "+d.getGroup()+" ";
        }
        
        if(d.getNumber()!=0){
            sql+=" , number = "+d.getNumber()+" ";
        }
        
        if(d.getUnit()!=null && !d.getUnit().equals("")){
            sql+=" , unit = '"+d.getUnit()+"' ";
        }
        
//        if(d.getProducttime()!=null && !d.getProducttime().equals("")){
//            sql+=" , producttime = '"+d.getProducttime()+"' ";
//        }
        
        if(d.getDuetime()!=null && !d.getDuetime().equals("")){
            sql+=" , duetime = '"+d.getDuetime()+"' ";
        }
        
//        if(d.getMember()!=null && !d.getMember().equals("")){
//            sql+=" , member = '"+d.getMember()+"' ";
//        }
//        if(d.getCompany()!=null && !d.getCompany().equals("")){
//            sql+=" , company = '"+d.getCompany()+"' ";
//        }
//        if(d.getRemark()!=null && !d.getRemark().equals("")){
//            sql+=" , remark = '"+d.getRemark()+"' ";
//        }
        sql+=" where _id = "+d.getId();
        Log.i("hmq", sql+"==");
        try {
            sqldb.execSQL(sql);
        } catch (Exception e) {
           e.printStackTrace();
           Log.e("hmq","==修改失败=="+e);
        }
    }

    public void updatedurg(String item, Drug d){
        //producttime, duetime,number, member , company,remark 
       
       //update drugtable set    dname = 'cao222'  , producttime = '2015-8-25'  , duetime = '2015-8-25'  , number = 1  , member = '哈哈'  , company = 'xxx公司'  where _id = 1
       String sql="update drugtable set  ";
       
       if (item.equals("dname")){
           if(d.getDname()!=null && !d.getDname().equals("")){
               sql +="dname = '"+d.getDname()+"' ";
           }
       } else if (item.equals("usergroup")) {
           if(d.getGroup()!= 0){
               sql +="usergroup = "+d.getGroup()+" ";
           }
       } else if (item.equals("number")) {
           if(d.getNumber()!=0){
               sql+="number = "+d.getNumber()+" ";
           }
       } else if (item.equals("unit")) {
           if(d.getUnit()!=null && !d.getUnit().equals("")){
               sql+="unit = '"+d.getUnit()+"' ";
           }
//       } else if (item.equals("producttime")) {
//           if(d.getProducttime()!=null && !d.getProducttime().equals("")){
//               sql+="producttime = '"+d.getProducttime()+"' ";
//           }
       } else if (item.equals("duetime")){
           if(d.getDuetime()!=null && !d.getDuetime().equals("")){
               sql+="duetime = '"+d.getDuetime()+"' ";
           }
       }// else if (item.equals("member")) {
//           if(d.getMember()!=null && !d.getMember().equals("")){
//               sql+="member = '"+d.getMember()+"' ";
//           }
//       } else if (item.equals("company")) {
//           if(d.getCompany()!=null && !d.getCompany().equals("")){
//               sql+="company = '"+d.getCompany()+"' ";
//           }
//       } else if (item.equals("remark")) {
//           if(d.getRemark()!=null && !d.getRemark().equals("")){
//               sql+="remark = '"+d.getRemark()+"' ";
//           }
//       }
       
       sql+=" where _id = "+d.getId();
       try {
           sqldb.execSQL(sql);
       } catch (Exception e) {
          e.printStackTrace();
          Log.i("hmq","==修改失败=="+e);
       }
   }
}
