package com.possible.dhis2int.web;
import java.util.Date;

public class Schedules {
    private int id;
    private String programName;
    private String lastRun;
    private String status;

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
    }
    public String getProgramName(){
        return programName;
    }
    public void setProgName(String programName){
        this.programName=programName;
    }
    public String getLastRun(){
        return lastRun;
    }
    public void setLastRun(String lastRun){
        this.lastRun=lastRun;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status=status;
    }
     
}
