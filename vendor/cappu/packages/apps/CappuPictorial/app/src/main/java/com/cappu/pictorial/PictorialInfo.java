/**
 *  
 * Copyright (C) 2016 The Cappu Android Source Project
 * <p>
 * Licensed under the Cappu License, (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.cappu.cn
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @data: 2016年12月1日
 * @author: huangminqi@cappu.cn
 * @company: Cappu Co.,Ltd. 
 */
package com.cappu.pictorial;


public class PictorialInfo {
    private String id;
    private int index = -1;
    private int version;
    private String md5;
    private String resolution;//分辨率
    private String suffixes;//后缀
    private String summary;
    private long time;
    private String url;
    private String fileSavePath;
    private boolean state;//下载状态 false:未下载图片或者正在下载图片中,但是该图片还未被下载完成.true:已经与服务器同步下载完成最新的图片

    public final static boolean ISFINISH = true;
    public final static boolean ISREADY = false;

    public PictorialInfo() {
        setId("");
        setIndex(-1);
        setVersion(-1);
        setMd5("");
        setResolution("");
        setSuffixes("");
        setSummary("");
        setTime(0);
        setUrl("");
        setFileSavePath("");
        setState(ISREADY);
    }

    public PictorialInfo(String id, String md5, String resolution, String suffixes, String summary, long time, String url, String fileSavePath) {
        setId(id);
        setIndex(-1);
        setVersion(-1);
        setMd5(md5);
        setResolution(resolution);
        setSuffixes(suffixes);
        setSummary(summary);
        setTime(time);
        setUrl(url);
        setFileSavePath(fileSavePath);
        setState(ISREADY);
    }

    //id
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    //index
    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    //version
    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

    //md5
    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMd5() {
        return this.md5;
    }


    //resolution
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() {
        return this.resolution;
    }

    //suffixes
    public void setSuffixes(String suffixes) {
        this.suffixes = suffixes;
    }

    public String getSuffixes() {
        return this.suffixes;
    }

    //summary
    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSummary() {
        return this.summary;
    }

    //time
    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    //url
    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    //fileSavePath
    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    public String getFileSavePath() {
        return this.fileSavePath;
    }

    public void setState(boolean state){
        this.state = state;
    }

    public Boolean getState() {
        return state;
    }

    public String toString() {
        String str = "id:" + id + "; idx=" + index + "; v="+version+"; md5:" + md5  + "; px:" + resolution
                + "; suf:" + suffixes + "; sum:" + summary + "; t:" + time + "; u:" + url
                + "; fsp:" + fileSavePath+ "; st=" + state;
        return str;
    }
    
    public String toShortString() {
        String str = "id:" + id + "; idx=" + index + "; v="+version + "; sum:" + summary + "; t:" + time + "; u:" + url + "; fsp:" + fileSavePath+ "; st=" + state;
        return str;
    }
}
