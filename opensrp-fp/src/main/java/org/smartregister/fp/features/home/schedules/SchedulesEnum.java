package org.smartregister.fp.features.home.schedules;

public enum SchedulesEnum {
    Copper_bearing_intrauterine_devices(new ScheduleModel());

    private ScheduleModel scheduleModel;

     SchedulesEnum(ScheduleModel scheduleModel) {
        this.scheduleModel = scheduleModel;
    }

}
