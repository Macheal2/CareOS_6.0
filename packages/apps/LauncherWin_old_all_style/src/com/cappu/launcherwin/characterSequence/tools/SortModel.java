package com.cappu.launcherwin.characterSequence.tools;

public class SortModel {

    private int  id;
    private String  name; // 显示的数据
    private String  number;
    private long  group;
    private String  head;
    private String sortLetters; // 显示数据拼音的首字母
    
    public SortModel(int id,String name, String number,long group, String head) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.group = group;
        this.head = head;
    }
    
    public SortModel() {
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }
    public long getGroup() {
        return group;
    }
    public void setGroup(long group) {
        this.group = group;
    }
    public String getHead() {
        return head;
    }
    public void setHead(String head) {
        this.head = head;
    }
    public String getSortLetters() {
        return sortLetters;
    }
    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    @Override
    public String toString() {
        return "SortModel [id=" + id + ", name=" + name + ", number=" + number + ", group=" + group + ", head=" + head + ", sortLetters="
                + sortLetters + "]";
    }

    
}

