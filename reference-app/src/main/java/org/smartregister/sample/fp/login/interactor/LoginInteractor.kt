package org.smartregister.sample.fp.login.interactor

import org.smartregister.job.ImageUploadServiceJob
import org.smartregister.job.PullUniqueIdsServiceJob
import org.smartregister.job.SyncServiceJob
import org.smartregister.job.SyncSettingsServiceJob
import org.smartregister.login.interactor.BaseLoginInteractor
import org.smartregister.sample.fp.BuildConfig
import org.smartregister.view.contract.BaseLoginContract
import java.util.concurrent.TimeUnit

/**
 * Created by ndegwamartin on 26/06/2018.
 */
class LoginInteractor(loginPresenter: BaseLoginContract.Presenter?) : BaseLoginInteractor(loginPresenter), BaseLoginContract.Interactor {
    override fun scheduleJobsPeriodically() {
        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.DATA_SYNC_DURATION_MINUTES.toLong()),
                getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES))
        PullUniqueIdsServiceJob
                .scheduleJob(PullUniqueIdsServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.PULL_UNIQUE_IDS_MINUTES.toLong()),
                        getFlexValue(BuildConfig.PULL_UNIQUE_IDS_MINUTES))
        ImageUploadServiceJob
                .scheduleJob(ImageUploadServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.IMAGE_UPLOAD_MINUTES.toLong()),
                        getFlexValue(BuildConfig.IMAGE_UPLOAD_MINUTES))
        SyncSettingsServiceJob
                .scheduleJob(SyncSettingsServiceJob.TAG, TimeUnit.MINUTES.toMillis(BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES.toLong()),
                        getFlexValue(BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES))
    }
}