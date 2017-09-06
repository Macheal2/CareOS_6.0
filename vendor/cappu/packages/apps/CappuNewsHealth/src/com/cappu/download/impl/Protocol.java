
package com.cappu.download.impl;

import org.json.JSONObject;

/**
 * 协议
 * 
 * @author wanghao
 */
public class Protocol {
    // 交互协议 （eg：op=2000&sign=xx&sjz=xx）
    private String getData;

    // post的数据（json对象） 注：getData与postData的区别
    private JSONObject postData = new JSONObject();

    // 主机
    private String mHost;

    // 超时时间
    private int mTimeOut = -1;

    // 是否启动重连机制
    private boolean mRetry = true;

    boolean isBreakPoint = false;

    private int startPos = -1;

    private int endPos = -1;

    public int getStartPos() {
        return startPos;
    }

    public int getEndPos() {
        return endPos;
    }

    public void setStartPos(int startPos) {
        this.startPos = startPos;
    }

    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }

    public boolean getIsBreakPoint() {
        return isBreakPoint;
    }

    public void setIsBreakPoint(boolean isBreakPoint) {
        this.isBreakPoint = isBreakPoint;
    }

    /**
     * @return the getData
     * 获取数据的一些过滤条件（协议）
     */
    public String getGetData() {
        return getData;
    }

    /**
     * @param getData the getData to set
     * 获取数据的一些过滤条件（协议）
     */
    public void setGetData(String getData) {
        this.getData = getData;
    }

    /**
     * @return the postData
     */
    public JSONObject getPostData() {
        return postData;
    }

    /**
     * @param postData the postData to set
     */
    public void putPostData(String key, Object value) {
        // this.postData = postData;
        try {
            postData.put(key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return the host
     *  请求的主机的域名
     */
    public String getHost() {
        return mHost;
    }

    /**
     * @param host the host to set
     * 请求的主机的域名
     */
    public void setHost(String host) {
        this.mHost = host;
    }

    /**
     * @return the soTimeout
     */
    public int getSoTimeout() {
        return mTimeOut;
    }

    /**
     * @param soTimeout the soTimeout to set
     */
    public void setSoTimeout(int soTimeout) {
        this.mTimeOut = soTimeout;
    }

    /**
     * @return the reTry
     */
    public boolean isReTry() {
        return mRetry;
    }

    /**
     * @param reTry the reTry to set
     */
    public void setReTry(boolean reTry) {
        this.mRetry = reTry;
    }

}
