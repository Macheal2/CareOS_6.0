package com.cappu.launcherwin.netinfo.widget;

import android.os.Parcel;
import android.os.Parcelable;

public class NetDateDao implements Parcelable {// extends Parcelable
    public String date;
    public String title;
    public String introduce;
    public String address;
    public String banner;
    public String icon;
    public int type;

    public NetDateDao(){
        
    }
    public NetDateDao(Parcel in) {
        date = in.readString();
        title = in.readString();
        introduce = in.readString();
        address = in.readString();
        banner = in.readString();
        icon = in.readString();
        type = in.readInt();
    }

    @Override
    public String toString() {
        return "NetDateDao [date=" + date + ", title=" + title + ", introduce=" + introduce + ", address=" + address + ", banner=" + banner
                + ", icon=" + icon + ", type=" + type + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int arg1) {
        out.writeString(date);
        out.writeString(title);
        out.writeString(introduce);
        out.writeString(address);
        out.writeString(banner);
        out.writeString(icon);
        out.writeInt(type);
    }

    public static final Parcelable.Creator<NetDateDao> CREATOR = new Creator<NetDateDao>() {
        @Override
        public NetDateDao[] newArray(int size) {
            return new NetDateDao[size];
        }

        @Override
        public NetDateDao createFromParcel(Parcel in) {
            return new NetDateDao(in);
        }
    };

}