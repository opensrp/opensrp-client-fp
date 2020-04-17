package org.smartregister.sample.fp.job;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.evernote.android.job.Job;

import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.job.BaseJob;

/**
 * Created by ndegwamartin on 06/09/2018.
 */
public class ViewConfigurationsServiceJob extends BaseJob {

    public static final String TAG = "ViewConfigurationsServiceJob";

    @NonNull
    @Override
    protected Job.Result onRunJob(@NonNull Job.Params params) {
        Intent intent = new Intent(getApplicationContext(), PullConfigurableViewsIntentService.class);
        getApplicationContext().startService(intent);
        return params != null && params.getExtras().getBoolean(ConstantsUtils.IntentKeyUtils.TO_RESCHEDULE, false) ?
                Result.RESCHEDULE : Result.SUCCESS;
    }
}
