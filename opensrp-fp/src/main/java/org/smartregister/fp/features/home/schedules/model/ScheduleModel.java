package org.smartregister.fp.features.home.schedules.model;

public class ScheduleModel {
    private String frequency;
    private String triggerEventTag;
    private String triggerDateTag;
    private Integer dueDays;
    private Integer overdueDays;
    private Integer expiryDays;

    public ScheduleModel(String frequency, String triggerEventTag, String triggerDateTag, Integer dueDays, Integer overdueDays, Integer expiryDays) {
        this.frequency = frequency;
        this.triggerEventTag = triggerEventTag;
        this.dueDays = dueDays;
        this.overdueDays = overdueDays;
        this.expiryDays = expiryDays;
        this.triggerDateTag = triggerDateTag;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getTriggerEventTag() {
        return triggerEventTag;
    }

    public Integer getDueDays() {
        return dueDays;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public String getTriggerDateTag() {
        return triggerDateTag;
    }
}
