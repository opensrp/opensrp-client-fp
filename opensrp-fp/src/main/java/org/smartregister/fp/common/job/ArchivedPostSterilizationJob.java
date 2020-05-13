package org.smartregister.fp.common.job;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.features.home.repository.PatientRepository;
import org.smartregister.job.BaseJob;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class ArchivedPostSterilizationJob extends BaseJob {

    public static final String TAG = "ArchivedPostSterilizationJob";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        List<HashMap<String, String>> data = FPLibrary.getInstance().getPreviousContactRepository().getLatestSterilizeContacts();

        try {
            int count = 0;
            LocalDate todayDate = LocalDate.now();
            DateTimeFormatter pattern = DateTimeFormat.forPattern("dd-MM-yyyy");
            for (HashMap<String, String> map : data) {
                String baseEntityId = map.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID);
                LocalDate sterilizeDate = LocalDate.parse(map.get(ConstantsUtils.JsonFormFieldUtils.STERILIZATION_DATE), pattern).plusMonths(6);
                if (todayDate.isAfter(sterilizeDate)) {
                    PatientRepository.doArchive(baseEntityId);
                    Timber.d("patient with base_entity_id: %s successfully archived.", baseEntityId);
                    ++count;
                }
            }
            Timber.d("%s patient(s) successfully archived.", count);
        }
        catch (Exception ex) {
            Timber.e(ex);
        }

        return Result.SUCCESS;
    }

    public static void makeSchedule() {
        ArchivedPostSterilizationJob.scheduleJob(ArchivedPostSterilizationJob.TAG, TimeUnit.DAYS.toMillis(1), TimeUnit.DAYS.toMillis(1));
    }
}
