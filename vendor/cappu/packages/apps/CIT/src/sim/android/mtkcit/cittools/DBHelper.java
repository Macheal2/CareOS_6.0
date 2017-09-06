package sim.android.mtkcit.cittools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "cit.db";
	private static final String TBL_NAME = "failItem";
	private static final String CREATE_TBL = " create table "
			+ " failItem(_id integer primary key ,itemid integer,name text) ";

	private SQLiteDatabase db;

	public DBHelper(Context c) {
		super(c, DB_NAME, null, 2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		this.db = db;
		db.execSQL(CREATE_TBL);
	}

	public void insert(ContentValues values) {
		SQLiteDatabase db = getWritableDatabase();
		// db.insert(table, nullColumnHack, values)
		db.insert(TBL_NAME, null, values);
		db.close();
	}

	public Cursor getAll() {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TBL_NAME, null, null, null, null, null, null);
		return c;
	}

	public Cursor Query(String id) {
		SQLiteDatabase db = getWritableDatabase();
		Cursor c = db.query(TBL_NAME, null, "itemid=?",
				new String[] { String.valueOf(id) }, null, null, null);
		return c;
	}

	public void del(int id) {
		if (db == null)
			db = getWritableDatabase();
		Log.v("feng", "db.delete itemid = " + id);
		db.delete(TBL_NAME, "itemid=?", new String[] { String.valueOf(id) });
		// db.delete(TBL_NAME, "_id=?", new String[] { String.valueOf(id)
		// });
	}

	public void close() {
		if (db != null)
			db.close();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
}
