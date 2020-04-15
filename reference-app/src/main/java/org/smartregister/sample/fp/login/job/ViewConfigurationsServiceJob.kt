package org.smartregister.sample.fp.job

import android.content.Intent
import androidx.annotation.NonNull
import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService
import org.smartregister.fp.common.util.ConstantsUtils
import org.smartregister.job.BaseJob

class ViewConfigurationsServiceJob : BaseJob() {

    fun fetchParms(): Params {
        return params
    }

    @NonNull
    override fun onRunJob(@NonNull params: Params): Result {
        val intent = Intent(applicationContext, PullConfigurableViewsIntentService::class.java)
        applicationContext.startService(intent)
        return if (params != null && params.extras.getBoolean(ConstantsUtils.IntentKeyUtils.TO_RESCHEDULE, false)) Result.RESCHEDULE else Result.SUCCESS
    }

    companion object {
        const val TAG = "ViewConfigurationsServiceJob"
    }

}