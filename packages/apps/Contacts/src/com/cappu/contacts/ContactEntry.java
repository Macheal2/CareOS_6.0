package com.cappu.contacts;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ContactEntry implements Parcelable {

    int id;
    Bitmap photo;
    String number;
    String name;
    String photoUri;
    boolean state;
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhotoUri(){
        return photoUri;
    }

    public void setPhotoUri(String uri){
        this.photoUri = uri;
    }
    public void setState(boolean state){
        this.state = state;
    }
    public boolean getState(){
        return state;
    }
    
    public void setPhoto(Bitmap photo){
        this.photo = photo;
    }
    public Bitmap getPhoto(){
        return photo;
    }

    public static final Parcelable.Creator<ContactEntry> CREATOR = new Creator<ContactEntry>() {
        public ContactEntry createFromParcel(Parcel source) {
            ContactEntry entry = new ContactEntry();
            entry.id = source.readInt();
            entry.name = source.readString();
            entry.number = source.readString();
            entry.photoUri = source.readString();
            return entry;
        }

        public ContactEntry[] newArray(int size) {
            return new ContactEntry[size];
        }
    };
    
    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(photoUri);
    }

}
