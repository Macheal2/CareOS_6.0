package com.joy.network.util;

import java.util.HashMap;

import org.json.JSONObject;

public class Protocal {
    // ����Э�� ��eg��op=2000&sign=xx&sjz=xx��
    private String getData;
    // post����ݣ�json���� ע��getData��postData�����
    private JSONObject postData = new JSONObject();
    // ����
    private String host;
    // ��ʱʱ��
    private int soTimeout = -1;
    // �Ƿ�������������
    private boolean reTry = true;

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
     */
    public String getGetData() {
        return getData;
    }

    /**
     * @param getData
     *            the getData to set
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
     * @param postData
     *            the postData to set
     */
    public void putPostData(String key, Object value) {
        try {
            postData.put(key, value);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @param host
     *            the host to set
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @return the soTimeout
     */
    public int getSoTimeout() {
        return soTimeout;
    }

    /**
     * @param soTimeout
     *            the soTimeout to set
     */
    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    /**
     * @return the reTry
     */
    public boolean isReTry() {
        return reTry;
    }

    /**
     * @param reTry
     *            the reTry to set
     */
    public void setReTry(boolean reTry) {
        this.reTry = reTry;
    }

}
