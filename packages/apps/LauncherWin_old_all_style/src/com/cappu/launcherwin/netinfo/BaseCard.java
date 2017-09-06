
package com.cappu.launcherwin.netinfo;

import java.io.Serializable;

public class BaseCard implements Serializable{
    
    /**服务器端传过来存在本地的一个唯一去服务器获取的ID值*/
    public static final String TEXT_ID = "id";
    public static final String ID = "_id";
    public static final String TEXT_FLAG = "flag";
    public static final String TEXT_ICON = "icon";
    public static final String TEXT_ICON_PATH = "icon_path";
    /**这边这个状态表示icon下载成功与否 0表示失败，1表示成功*/
    public static final String TEXT_ICON_STATUS = "iconStatus";
    public static final String TEXT_BANNER = "banner";
    public static final String TEXT_BANNER_PATH = "banner_path";
    /**这边这个状态表示banner下载成功与否 0表示失败，1表示成功*/
    public static final String TEXT_BANNER_STATUS = "bannerStatus";
    public static final String TEXT_TYPE = "type";
    public static final String TEXT_TITLE = "title";
    public static final String TEXT_INTRODUCE = "introduce";
    public static final String TEXT_PACKAGENAME = "packageName";
    public static final String TEXT_URL = "url";
    public static final String TEXT_SIZE = "size";
    public static final String TEXT_SITE = "site";
    public static final String TEXT_STATUS = "pushStatus";
    /**这个是收藏的标志位*/
    public static final String TEXT_FAVORITES = "favorites";
    public static final String TEXT_DATE = "date";

    public int id;
    public int  pushID;
    /*这个flag表示是什么类型 新闻健康旅游理财*/
    public int flag;
    public String  date;
    public String  title;
    public String  introduce;
    public String  address;
    public String  icon;
    public String  iconPath;
    public String  banner;
    public String bannerPath;
    public String  favorites;
    
    public BaseCard(int id,int pushID,int flag, String date, String title, String introduce, String address, String icon,String banner,String favorites) {
        this.id = id;
        this.pushID = pushID;
        this.flag = flag;
        this.date = date;
        this.title = title;
        this.introduce = introduce;
        this.address = address;
        this.icon = icon;
        this.banner = banner;
        this.favorites = favorites;
    }
    
/*    public int getFlag() {
        return flag;
    }

    public void setFlag(int type) {
        this.flag = type;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getFavorites() {
        return favorites;
    }

    public void setFavorites(String favorites) {
        this.favorites = favorites;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getIntroduce() {
        return introduce;
    }
    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }*/

    @Override
    public String toString() {
        return "BaseCard [id=" + id + ", date=" + date + ", title=" + title + ", introduce=" + introduce + ", address=" + address + ", icon=" + icon
                + ", banner=" + banner + ", favorites=" + favorites + "]";
    }



}
