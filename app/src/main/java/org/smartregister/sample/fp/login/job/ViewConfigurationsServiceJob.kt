package org.smartregister.sample.fp.login.job

import android.content.Intent
import org.smartregister.configurableviews.service.PullConfigurableViewsIntentService
import org.smartregister.fp.common.util.ConstantsUtils
import org.smartregister.job.BaseJob

class ViewConfigurationsServiceJob : BaseJob() {
    override fun onRunJob(params: Params): Result {
        val intent = Intent(applicationContext, PullConfigurableViewsIntentService::class.java)
        applicationContext.startService(intent)
        return if (params.extras.getBoolean(ConstantsUtils.IntentKeyUtils.TO_RESCHEDULE, false)) Result.RESCHEDULE else Result.SUCCESS
    }

    companion object {
        const val TAG = "ViewConfigurationsServiceJob"
    }
}