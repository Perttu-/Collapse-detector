package com.aware.plugin.collapse_detector;


public class CollapseInfo {
    Long timestamp;
    String info;

    public CollapseInfo(){}

    public CollapseInfo(Long timestamp, String info){
        this.timestamp = timestamp;
        this.info = info;
    }
    //Setters
    public void setTimestamp(Long pTimestamp){
        this.timestamp = pTimestamp;
    }

    public void setInfo(String pInfo){
        this.info = pInfo;
    }

    //Getters
    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getInfo(){
        return this.info;
    }

}
