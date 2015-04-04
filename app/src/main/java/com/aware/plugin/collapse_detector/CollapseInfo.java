package com.aware.plugin.collapse_detector;


public class CollapseInfo {
    Long timestamp;
    String coordinates;

    public CollapseInfo(){}

    public CollapseInfo(Long timestamp, String coordinate){
        this.timestamp = timestamp;
        this.coordinates = coordinate;
    }
    //Setters
    public void setTimestamp(Long pTimestamp){
        this.timestamp = pTimestamp;
    }

    public void setCoordinates(String pCoordinates){
        this.coordinates = pCoordinates;
    }

    //Getters
    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getCoordinates(){
        return this.coordinates;
    }

}
