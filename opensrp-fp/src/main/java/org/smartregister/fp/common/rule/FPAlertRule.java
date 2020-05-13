package org.smartregister.fp.common.rule;

import org.joda.time.LocalDate;
import org.smartregister.fp.features.home.schedules.model.ScheduleModel;

import static org.smartregister.fp.common.util.ConstantsUtils.AlertStatusUtils.NOT_DUE;
import static org.smartregister.fp.common.util.ConstantsUtils.ScheduleUtils.ONCE_OFF;
import static org.smartregister.fp.common.util.ConstantsUtils.ScheduleUtils.RECURRING;

//All date formats ISO 8601 yyyy-mm-dd

public class FPAlertRule {
    public static final String RULE_KEY = "alertRule";
    public String buttonStatus = NOT_DUE;

    public String triggerDate;
    private LocalDate todayDate;
    public int dueDays;
    public int overDueDays;
    public int expiryDays;

    public FPAlertRule(ScheduleModel scheduleModel, String triggerDate, boolean isFirst) {
        if (scheduleModel.getFrequency().equals(ONCE_OFF)) {
                populateDatesOnceOff(scheduleModel);
        } else if (scheduleModel.getFrequency().equals(RECURRING)) {
            if (isFirst) {
                populateDatesOnceOff(scheduleModel);
            } else {
                if (scheduleModel.getRecurringDays() != null)
                    populateDatesRecurring(scheduleModel);
            }
        }
        this.triggerDate = triggerDate;
        this.todayDate = new LocalDate();
    }

    private void populateDatesRecurring(ScheduleModel scheduleModel) {
        this.dueDays = scheduleModel.getRecurringDays().getLeft();
        this.overDueDays = scheduleModel.getRecurringDays().getMiddle();
        this.expiryDays = scheduleModel.getRecurringDays().getRight();
    }

    private void populateDatesOnceOff(ScheduleModel scheduleModel) {
        this.dueDays = scheduleModel.getNormalDays().getLeft();
        this.overDueDays = scheduleModel.getNormalDays().getMiddle();
        this.expiryDays = scheduleModel.getNormalDays().getRight();
    }

    public boolean isFollowUpDue(Integer dueDays) {
        LocalDate triggerDate = new LocalDate(this.triggerDate);
        return triggerDate.isBefore(todayDate.minusDays(dueDays - 1));
    }

    public boolean isOverdueWithDays(Integer overDueDays) {
        LocalDate triggerDate = new LocalDate(this.triggerDate);
        return triggerDate.isBefore(todayDate.minusDays(overDueDays - 1));
    }

    public boolean isExpiryWithDays(Integer expiryDays) {
        LocalDate triggerDate = new LocalDate(this.triggerDate);
        return triggerDate.isBefore(todayDate.minusDays(expiryDays - 1));
    }

    public String getButtonStatus() {
        return buttonStatus;
    }
}
