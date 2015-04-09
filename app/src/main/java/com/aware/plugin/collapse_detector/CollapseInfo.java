package com.aware.plugin.collapse_detector;


public class CollapseInfo {
    Long timestamp;
    String info;

    public CollapseInfo(){}

    public CollapseInfo(Long timestamp, String coordinate){
        this.timestamp = timestamp;
        this.info = coordinate;
    }
    //Setters
    public void setTimestamp(Long pTimestamp){
        this.timestamp = pTimestamp;
    }

    public void setInfo(String pCoordinates){
        this.info = pCoordinates;
    }

    //Getters
    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getInfo(){
        return this.info;
    }

}
