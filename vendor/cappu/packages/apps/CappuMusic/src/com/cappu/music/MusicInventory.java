package com.cappu.music;

public class MusicInventory {

    public String inventoryName = null;
    public int iconRes = -1;
    public int iconBg = -1;
    public int type = -1;
    public long id = -1;
    private boolean isShow = true;
    
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIconBg() {
        return iconBg;
    }

    public void setIconBg(int iconBg) {
        this.iconBg = iconBg;
    }

    

    public MusicInventory() {
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }
    
    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    @Override
    public String toString() {
        return "MusicInventory [inventoryName=" + inventoryName + ", iconRes=" + iconRes + ", iconBg=" + iconBg + ", type=" + type + ", id=" + id
                + ", isShow=" + isShow + "]";
    }


}