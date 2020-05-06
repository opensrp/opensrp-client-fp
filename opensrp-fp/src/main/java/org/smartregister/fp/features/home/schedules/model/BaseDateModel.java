package org.smartregister.fp.features.home.schedules.model;

public class BaseDateModel {
    private Integer days;
    private Integer weeks;
    private Integer years;

    public BaseDateModel(Integer days, Integer weeks, Integer years) {
        this.days = days;
        this.weeks = weeks;
        this.years = years;
    }

    public Integer getDays() {
        return days;
    }

    public Integer getWeeks() {
        return weeks;
    }

    public Integer getYears() {
        return years;
    }
}
