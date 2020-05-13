package org.smartregister.fp.features.home.schedules.model;

import org.apache.commons.lang3.tuple.Triple;

public class ScheduleModel {
    private String frequency;
    private String triggerEventTag;
    private String triggerDateTag;
    private Triple<Integer, Integer, Integer> recurringDays;
    private Triple<Integer, Integer, Integer> normalDays;

    public ScheduleModel(String frequency, String triggerEventTag, String triggerDateTag, Triple<Integer, Integer, Integer> normalDays,Triple<Integer, Integer, Integer> recurringDays) {
        this.frequency = frequency;
        this.triggerEventTag = triggerEventTag;
        this.normalDays = normalDays;
        this.recurringDays = recurringDays;
        this.triggerDateTag = triggerDateTag;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getTriggerEventTag() {
        return triggerEventTag;
    }

    public Triple<Integer, Integer, Integer> getRecurringDays() {
        return recurringDays;
    }

    public Triple<Integer, Integer, Integer> getNormalDays() {
        return normalDays;
    }

    public String getTriggerDateTag() {
        return triggerDateTag;
    }


}
