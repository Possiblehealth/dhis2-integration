package com.possible.dhis2int.web;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Date;

import org.joda.time.DateTime;

public class Schedules {
    private int id;
    private String programName;
    private String lastRun;
    private String status;
    private LocalDate created_date;
    private LocalDate target_time;
    private String created_by;
    private String frequency;

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
    public String getFrequency(){
        return frequency;
    }
    public void setFrequency(String frequency){
        this.frequency=frequency;
    }
    public String getCreatedBy(){
        return created_by;
    }
    public void setCreatedBy(String created_by){
        this.created_by=created_by;
    }
    public LocalDate getCreatedDate(){
        return created_date;
    }
    public void setCreatedDate(LocalDate created_date){
        this.created_date=created_date;
    }
    public LocalDate getTargetTime(){
        return target_time;
    }
    public void setTargetTime(LocalDate target_time){
        this.target_time=target_time;
    }
     
}
