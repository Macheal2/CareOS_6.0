package entity;

import java.util.ArrayList;
import java.util.List;

public class ReportDetail {

    private int lrid;
    private int caid;
    private String name;
    private String desc;
    private String content;
    private List<Content> mycontent = new ArrayList<Content>();

    public int getLrid() {
        return lrid;
    }

    public void setLrid(int lrid) {
        this.lrid = lrid;
    }

    public List<Content> getMycontent() {
        return mycontent;
    }

    public void setMycontent(List<Content> mycontent) {
        this.mycontent = mycontent;
    }

    public int getCaid() {
        return caid;
    }

    public void setCaid(int caid) {
        this.caid = caid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ReportDetail(int lrid, int caid, String name, String desc, String content) {
        this.lrid = lrid;
        this.caid = caid;
        this.name = name;
        this.desc = desc;
        this.content = content;
    }

    public ReportDetail() {

    }

}
