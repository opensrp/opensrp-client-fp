package org.smartregister.fp.common.rule;

import org.joda.time.LocalDate;

import static org.smartregister.fp.common.util.ConstantsUtils.AlertStatusUtils.NOT_DUE;

//All date formats ISO 8601 yyyy-mm-dd

public class FPAlertRule {
    public static final String RULE_KEY = "alertRule";
    public String buttonStatus = NOT_DUE;

    public String triggerDate;
    private LocalDate todayDate;
    public int dueDays;
    public int overDueDays;
    public int expiryDays;

    public FPAlertRule(int dueDays, int overDueDays, int expiryDays, String triggerDate) {

        this.dueDays = dueDays;
        this.overDueDays = overDueDays;
        this.expiryDays = expiryDays;
        this.triggerDate = triggerDate;
        this.todayDate = new LocalDate();
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
