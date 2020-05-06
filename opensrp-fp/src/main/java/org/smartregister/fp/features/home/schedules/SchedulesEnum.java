package org.smartregister.fp.features.home.schedules;

import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.features.home.schedules.model.ScheduleModel;

public enum SchedulesEnum {
    COPPER_BEARING_INTRAUTERINE_DEVICES(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.COPPER_BEARING_INTRAUTERINE_DEVICES, ConstantsUtils.SchedulesTriggerDates.IUD_INSERTION_DATE, 21, 42, 49)),
    LEVONORGESTREL_IUD(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.LEVONORGESTREL_IUD, ConstantsUtils.SchedulesTriggerDates.IUD_INSERTION_DATE, 21, 42, 49)),
    DMPA_IM(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.DMPA_IM, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, 84, 85, 112)),
    DMPA_SC(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.DMPA_SC, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, 84, 85, 112)),
    NET_EN_NORETHISTERONE_ENANTHATE(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.NET_EN_NORETHISTERONE_ENANTHATE, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, 56, 57, 84)),
    PROGESTOGEN_ONLY_PILLS(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.PROGESTOGEN_ONLY_PILLS, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, 84, 85, 92)),
    COMBINED_ORAL_CONTRACEPTIVES(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.COMBINED_ORAL_CONTRACEPTIVES, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, 84, 85, 92)),
    COMBINED_CONTRACEPTIVE_PATCH(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.COMBINED_CONTRACEPTIVE_PATCH, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, 84, 85, 92)),
    COMBINED_CONTRACEPTIVE_VAGINAL_RING(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.COMBINED_CONTRACEPTIVE_VAGINAL_RING, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, 84, 85, 92)),
    PROGESTERONE_RELEASING_VAGINAL_RING(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.PROGESTERONE_RELEASING_VAGINAL_RING, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, 84, 85, 92)),
    MALE_STERILIZATION(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.MALE_STERILIZATION, ConstantsUtils.SchedulesTriggerDates.STERILIZATION_DATE, 84, 91, 98)),
    FEMALE_STERILIZATION(new ScheduleModel("Once off", ConstantsUtils.SchedulesTriggerEvents.FEMALE_STERILIZATION, ConstantsUtils.SchedulesTriggerDates.STERILIZATION_DATE, 7, 14, 28));

    private ScheduleModel scheduleModel;

    SchedulesEnum(ScheduleModel scheduleModel) {
        this.scheduleModel = scheduleModel;
    }

    public ScheduleModel getScheduleModel() {
        return scheduleModel;
    }
}
