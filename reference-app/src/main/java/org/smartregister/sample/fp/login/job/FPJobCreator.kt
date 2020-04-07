package org.smartregister.sample.fp.login.job

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator
import org.smartregister.job.*
import org.smartregister.sync.intent.SyncIntentService
import timber.log.Timber

class FPJobCreator : JobCreator {
    override fun create(tag: String): Job? {
        return when (tag) {
            SyncServiceJob.TAG -> SyncServiceJob(SyncIntentService::class.java)
            ExtendedSyncServiceJob.TAG -> ExtendedSyncServiceJob()
            ImageUploadServiceJob.TAG -> ImageUploadServiceJob()
            PullUniqueIdsServiceJob.TAG -> PullUniqueIdsServiceJob()
            ValidateSyncDataServiceJob.TAG -> ValidateSyncDataServiceJob()
            ViewConfigurationsServiceJob.TAG -> ViewConfigurationsServiceJob()
            SyncSettingsServiceJob.TAG -> SyncSettingsServiceJob()
            else -> {
                Timber.d("Looks like you tried to create a job $tag that is not declared in the Anc Job Creator")
                null
            }
        }
    }
}