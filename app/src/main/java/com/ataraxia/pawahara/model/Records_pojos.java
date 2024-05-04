package com.ataraxia.pawahara.model;

import java.util.Date;


public class Records_pojos {

    private Boolean playing;
    private String record_name;
    private String record_file;
    private String record_duration;
    private Date record_date_time;

    public Records_pojos() {
    }

    public Records_pojos(Boolean playing, String record_name, String record_file, String record_duration, Date record_date_time) {
        this.playing = playing;
        this.record_name = record_name;
        this.record_file = record_file;
        this.record_duration = record_duration;
        this.record_date_time = record_date_time;
    }
    public String getRecord_duration() {
        return record_duration;
    }
    public String getRecord_name() {
        return record_name;
    }
    public Boolean getPlaying() {
        return playing;
    }
    public String getRecord_file() {
        return record_file;
    }

    public Date getRecord_date_time() {
        return record_date_time;
    }

    public void setRecord_date_time(Date record_date_time) {
        this.record_date_time = record_date_time;
    }



}
