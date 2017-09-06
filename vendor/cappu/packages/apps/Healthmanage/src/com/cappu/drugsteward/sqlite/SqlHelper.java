package com.cappu.drugsteward.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlHelper extends SQLiteOpenHelper {

    public SqlHelper(Context context, String name, CursorFactory factory,
            int version) {
        super(context, name, factory, version);
     
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table membertable (_id integer primary key autoincrement , name , sex, age ) ");
        //db.execSQL("create table drugtable (_id integer primary key autoincrement, usergroup integer, dname, producttime, duetime, number, member, company,remark) ");
        db.execSQL("create table drugtable (_id integer primary key autoincrement, usergroup integer, dname, number, unit, duetime) ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("drop if exists membertable ");
        db.execSQL("drop if exists drugtable ");
        onCreate(db);
    }

}
