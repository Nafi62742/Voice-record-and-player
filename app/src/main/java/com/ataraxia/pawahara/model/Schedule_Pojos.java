package com.ataraxia.pawahara.model;

import java.util.List;

public class Schedule_Pojos {
    private Boolean Schedule_switch;
    private Boolean Schedule_push_switch;
    private String Schedule_name;
    private List<Integer> Schedule_days;
    private String start_time;
    private String end_time;


    public Schedule_Pojos() {

    }
    public Schedule_Pojos(Boolean schedule_switch, String schedule_name,  List<Integer> schedule_days, String start_time, String end_time,Boolean schedule_push_switch) {
        Schedule_switch = schedule_switch;
        Schedule_name = schedule_name;
        Schedule_days = schedule_days;
        this.start_time = start_time;
        this.end_time = end_time;
        Schedule_push_switch=schedule_push_switch;
    }

    public Boolean getSchedule_switch() {
        return Schedule_switch;
    }

    public Boolean getSchedule_push_switch() {
        return Schedule_push_switch;
    }

    public void setSchedule_push_switch(Boolean schedule_push_switch) {
        Schedule_push_switch = schedule_push_switch;
    }

    public String getSchedule_name() {
        return Schedule_name;
    }

    public List<Integer> getSchedule_days() {
        return Schedule_days;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setSchedule_switch(Boolean schedule_switch) {
        Schedule_switch = schedule_switch;
    }

    public String getEnd_time() {
        return end_time;
    }

}
