package org.smartregister.fp.features.home.schedules;

import org.smartregister.fp.features.home.schedules.model.DueDateModel;
import org.smartregister.fp.features.home.schedules.model.ExpiryDateModel;
import org.smartregister.fp.features.home.schedules.model.OverdueDateModel;

public class ScheduleModel {
    private String frequency;
    private String triggerEvent;
    private String triggerDate;
    private DueDateModel dueDate;
    private OverdueDateModel overdueDate;
    private ExpiryDateModel expiryDate;

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
    }

    public String getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(String triggerDate) {
        this.triggerDate = triggerDate;
    }

    public DueDateModel getDueDate() {
        return dueDate;
    }

    public void setDueDate(DueDateModel dueDate) {
        this.dueDate = dueDate;
    }

    public OverdueDateModel getOverdueDate() {
        return overdueDate;
    }

    public void setOverdueDate(OverdueDateModel overdueDate) {
        this.overdueDate = overdueDate;
    }

    public ExpiryDateModel getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(ExpiryDateModel expiryDate) {
        this.expiryDate = expiryDate;
    }
}
