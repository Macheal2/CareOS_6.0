package dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import entity.LaboratoryReport;
import entity.ReportDetail;

public class Sqldao {

    private SQLiteDatabase sd;

    private class sqliteopen extends SQLiteOpenHelper {

        public sqliteopen(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase arg0) {
        }

        @Override
        public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        }

    }

    public Sqldao(Context context) {
        sqliteopen db = new sqliteopen(context, "health_report.db", null, 1);
        sd = db.getReadableDatabase();
    }

    //查询化验单解读
    public List<LaboratoryReport> selectreport() {
        List<LaboratoryReport> myl = new ArrayList<LaboratoryReport>();
        Cursor cursor = sd.rawQuery("select * from laboratory_report order by orderNo  ", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String sort = cursor.getString(cursor.getColumnIndex("sort"));
            LaboratoryReport report = new LaboratoryReport(id, name, sort);
            myl.add(report);
        }
        if (cursor != null) {
            cursor.close();
        }
        return myl;
    }
    
    //查询化验对应下的状态
    public List<ReportDetail> selectreportdetail(int lid) {
        List<ReportDetail> reports = new ArrayList<ReportDetail>();
        Cursor cursor = sd.rawQuery("select *from laboratory_report_detail where lr_id= " + lid, new String[] {});
        while (cursor.moveToNext()) {
            int lrid = cursor.getInt(cursor.getColumnIndex("lr_id"));
            int caid = cursor.getInt(cursor.getColumnIndex("ca_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String desc = cursor.getString(cursor.getColumnIndex("desc"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            ReportDetail detail = new ReportDetail(lrid, caid, name, desc, content);
            reports.add(detail);
        }
        if (cursor != null) {
            cursor.close();
        }
        return reports;
    }

    //查询单个的详情信息
    public ReportDetail selectalonedetail(int lid, int cid) {
        ReportDetail reports = null;
        Cursor cursor = sd.rawQuery("select *from laboratory_report_detail where ca_id=" + cid + " and lr_id=" + lid, new String[] {});
        while (cursor.moveToNext()) {
            int lrid = cursor.getInt(cursor.getColumnIndex("lr_id"));
            int caid = cursor.getInt(cursor.getColumnIndex("ca_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String desc = cursor.getString(cursor.getColumnIndex("desc"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            reports = new ReportDetail(lrid, caid, name, desc, content);

        }
        if (cursor != null) {
            cursor.close();
        }
        return reports;
    }

    //根据二级查询
    public List<ReportDetail> searchreportdetail(String sname) {
        List<ReportDetail> reports = new ArrayList<ReportDetail>();
        Cursor cursor = sd.rawQuery("select *from laboratory_report_detail where name like '%" + sname + "%' ", new String[] {});
        while (cursor.moveToNext()) {
            int lrid = cursor.getInt(cursor.getColumnIndex("lr_id"));
            int caid = cursor.getInt(cursor.getColumnIndex("ca_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String desc = cursor.getString(cursor.getColumnIndex("desc"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            ReportDetail detail = new ReportDetail(lrid, caid, name, desc, content);
            reports.add(detail);
        }
        if (cursor != null) {
            cursor.close();
        }
        return reports;
    }

    //添加你阅读过的信息
    public void addreadreport(ReportDetail detail, long time) {
        ContentValues values = new ContentValues();

        Cursor cursor = sd.rawQuery("select *from read_report_detail where ca_id= " + detail.getCaid(), new String[] {});
        if (!cursor.moveToFirst()) {
            values.put("ca_id", detail.getCaid());
            values.put("name", detail.getName());
            values.put("content", detail.getLrid() + "");
            values.put("readdate", time);
            sd.insert("read_report_detail", "content", values);
        } else {
            values.put("readdate", time);
            sd.update("read_report_detail", values, " ca_id= ? ", new String[] {});
        }

    }

    //查询阅读历史
    public List<ReportDetail> selectreadreport() {
        List<ReportDetail> reports = new ArrayList<ReportDetail>();
        Cursor cursor = sd.rawQuery("select * from read_report_detail ORDER BY readdate desc limit 0,25 ", new String[] {});
        while (cursor.moveToNext()) {
            int caid = cursor.getInt(cursor.getColumnIndex("ca_id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            ReportDetail detail = new ReportDetail();
            detail.setCaid(caid);
            detail.setName(name);
            detail.setContent(content);
            reports.add(detail);
        }
        if (cursor != null) {
            cursor.close();
        }
        return reports;
    }

    public void deletereadhistory() {
        sd.delete("read_report_detail", null, new String[] {});
    }
    /*
     * 显示laboratory_report表把化验单读去出来
     * 然后根据化验单id进行laboratory_report_detail化验单详细表查询
     * 再对应的症状category_detail
     */

}
