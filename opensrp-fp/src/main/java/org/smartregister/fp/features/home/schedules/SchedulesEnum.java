package org.smartregister.fp.features.home.schedules;

import org.apache.commons.lang3.tuple.Triple;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.features.home.schedules.model.ScheduleModel;

public enum SchedulesEnum {
    COPPER_BEARING_INTRAUTERINE_DEVICES(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.COPPER_BEARING_INTRAUTERINE_DEVICES, ConstantsUtils.SchedulesTriggerDates.IUD_INSERTION_DATE, Triple.of(21, 42, 49), null)),
    LEVONORGESTREL_IUD(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.LEVONORGESTREL_IUD, ConstantsUtils.SchedulesTriggerDates.IUD_INSERTION_DATE, Triple.of(21, 42, 49), null)),
    DMPA_IM(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.DMPA_IM, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, Triple.of(84, 85, 112), null)),
    DMPA_SC(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.DMPA_SC, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, Triple.of(84, 85, 112), null)),
    NET_EN_NORETHISTERONE_ENANTHATE(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.NET_EN_NORETHISTERONE_ENANTHATE, ConstantsUtils.SchedulesTriggerDates.LAST_INJECTION_DATE, Triple.of(56, 57, 84), null)),
    PROGESTOGEN_ONLY_PILLS(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.PROGESTOGEN_ONLY_PILLS, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, Triple.of(84, 85, 92), null)),
    COMBINED_ORAL_CONTRACEPTIVES(new ScheduleModel(ConstantsUtils.ScheduleUtils.RECURRING, ConstantsUtils.SchedulesTriggerEvents.COMBINED_ORAL_CONTRACEPTIVES, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, Triple.of(84, 85, 92), Triple.of(365, 366, 373))),
    COMBINED_CONTRACEPTIVE_PATCH(new ScheduleModel(ConstantsUtils.ScheduleUtils.RECURRING, ConstantsUtils.SchedulesTriggerEvents.COMBINED_CONTRACEPTIVE_PATCH, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, Triple.of(84, 85, 92), Triple.of(365, 366, 373))),
    COMBINED_CONTRACEPTIVE_VAGINAL_RING(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.COMBINED_CONTRACEPTIVE_VAGINAL_RING, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, Triple.of(84, 85, 92), null)),
    PROGESTERONE_RELEASING_VAGINAL_RING(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.PROGESTERONE_RELEASING_VAGINAL_RING, ConstantsUtils.SchedulesTriggerDates.VISIT_DATE, Triple.of(84, 85, 92), null)),
    MALE_STERILIZATION(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.MALE_STERILIZATION, ConstantsUtils.SchedulesTriggerDates.STERILIZATION_DATE, Triple.of(84, 91, 98), null)),
    FEMALE_STERILIZATION(new ScheduleModel(ConstantsUtils.ScheduleUtils.ONCE_OFF, ConstantsUtils.SchedulesTriggerEvents.FEMALE_STERILIZATION, ConstantsUtils.SchedulesTriggerDates.STERILIZATION_DATE, Triple.of(7, 14, 28), null));

    private ScheduleModel scheduleModel;

    SchedulesEnum(ScheduleModel scheduleModel) {
        this.scheduleModel = scheduleModel;
    }

    public ScheduleModel getScheduleModel() {
        return scheduleModel;
    }
}
