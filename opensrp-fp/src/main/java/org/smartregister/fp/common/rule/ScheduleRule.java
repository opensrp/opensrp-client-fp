package org.smartregister.fp.common.rule;

import org.joda.time.LocalDate;
import org.smartregister.fp.features.home.schedules.model.ScheduleModel;

public class ScheduleRule {

    public static final String RULE_KEY = "scheduleRule";
    private final Integer dueDaysAnnually;
    private final Integer dueDays;

    private LocalDate todayDate;
    public boolean isFirst;
    public String nextContactVisitDate;

    public ScheduleRule(ScheduleModel scheduleModel, boolean isFirst) {

        this.dueDays = scheduleModel.getNormalDays().getLeft();
        this.dueDaysAnnually = scheduleModel.getRecurringDays().getLeft();
        this.todayDate = new LocalDate();
        this.isFirst = isFirst;
    }

    public void calculateNextContactVisitDate(boolean isFirst) {
        if (isFirst)
            nextContactVisitDate = todayDate.plusDays(dueDays).toString();
        else {
            nextContactVisitDate = todayDate.plusDays(dueDaysAnnually).toString();
        }
    }

    public String getNextContactVisitDate() {
        return nextContactVisitDate;
    }
}
