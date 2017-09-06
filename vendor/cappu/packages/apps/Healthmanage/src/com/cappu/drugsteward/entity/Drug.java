package com.cappu.drugsteward.entity;

import com.cappu.healthmanage.R.string;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;

public class Drug implements Parcelable {

    // dname , producttime, duetime,number, member , company,remark
    private int id = -1;
    private int usergroup;
    private String dname="";
    private int number = -1;
    private String unit;
    //private String producttime;
    private String duetime;
    //private String member;
    //private String company;
    //private String remark;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroup() {
        return usergroup;
    }
    
    public void setGroup(int usergroup) {
        this.usergroup = usergroup;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
    
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
//    public String getProducttime() {
//        return producttime;
//    }
//
//    public void setProducttime(String producttime) {
//        this.producttime = producttime;
//    }

    public String getDuetime() {
        return duetime;
    }

    public void setDuetime(String duetime) {
        this.duetime = duetime;
    }

//    public String getMember() {
//        return member;
//    }
//
//    public void setMember(String member) {
//        this.member = member;
//    }
//
//    public String getCompany() {
//        return company;
//    }
//
//    public void setCompany(String company) {
//        this.company = company;
//    }
//
//    public String getRemark() {
//        return remark;
//    }
//
//    public void setRemark(String remark) {
//        this.remark = remark;
//    }

//    public Drug(int id, String dname,int aa, String producttime, String duetime, int number, String member, String company, String remark) {
//        this.id = id;
//        this.dname = dname;
//        this.number = number;
//        //this.producttime = producttime;
//        this.duetime = duetime;
////        this.member = member;
////        this.company = company;
////        this.remark = remark;
//    }
//
//    public Drug(String dname, String producttime, int aa, String duetime, int number, String member, String company, String remark) {
//
//        this.dname = dname;
////        this.producttime = producttime;
//        this.duetime = duetime;
//        this.number = number;
////        this.member = member;
////        this.company = company;
////        this.remark = remark;
//    }
//
//    public Drug(String dname, String producttime, String duetime, int number, String member, String company, String remark, int usergroup) {
//        this.usergroup = usergroup;
//        this.dname = dname;
////        this.producttime = producttime;
//        this.duetime = duetime;
//        this.number = number;
////        this.member = member;
////        this.company = company;
////        this.remark = remark;
//    }

    public Drug(int usergroup, String dname, int number, String unit, String duetime) {
        this.usergroup = usergroup;
        this.dname = dname;
        this.number = number;
        this.unit = unit;
        this.duetime = duetime;
    }
    
    public Drug() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel p, int arg1) {

        p.writeInt(id);
        p.writeInt(usergroup);
        p.writeString(dname);
        p.writeInt(number);
        p.writeString(unit);
//        p.writeString(producttime);
        p.writeString(duetime);

//        p.writeString(member);
//        p.writeString(company);
//        p.writeString(remark);

    }

    public static final Parcelable.Creator<Drug> CREATOR = new Creator<Drug>() {

        @Override
        public Drug[] newArray(int arg0) {
            // TODO Auto-generated method stub
            return new Drug[arg0];
        }

        @Override
        public Drug createFromParcel(Parcel p) {
            Drug d = new Drug();
            d.setId(p.readInt());
            d.setGroup(p.readInt());
            d.setDname(p.readString());
            d.setNumber(p.readInt());
            d.setUnit(p.readString());
//            d.setProducttime(p.readString());
            d.setDuetime(p.readString());
//            d.setMember(p.readString());
//            d.setCompany(p.readString());
//            d.setRemark(p.readString());
            return d;
        }
    };

}
