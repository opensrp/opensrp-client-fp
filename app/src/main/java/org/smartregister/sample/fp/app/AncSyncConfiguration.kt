package org.smartregister.sample.fp.app

import org.smartregister.SyncConfiguration
import org.smartregister.SyncFilter
import org.smartregister.repository.AllSharedPreferences
import org.smartregister.sample.fp.BuildConfig

class AncSyncConfiguration : SyncConfiguration() {
    override fun getSyncMaxRetries(): Int {
        return BuildConfig.MAX_SYNC_RETRIES
    }

    override fun getSyncFilterParam(): SyncFilter {
        return SyncFilter.TEAM_ID
    }

    override fun getSyncFilterValue(): String {
//        AncLibrary will design that later
        /*val sharedPreferences: AllSharedPreferences = AncLibrary.getInstance().getContext().userService().getAllSharedPreferences()
        return sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM())*/
        return ""
    }

    override fun getUniqueIdSource(): Int {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE
    }

    override fun getUniqueIdBatchSize(): Int {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE
    }

    override fun getUniqueIdInitialBatchSize(): Int {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE
    }

    override fun isSyncSettings(): Boolean {
        return BuildConfig.IS_SYNC_SETTINGS
    }

    override fun getEncryptionParam(): SyncFilter {
        return SyncFilter.TEAM
    }

    override fun updateClientDetailsTable(): Boolean {
        return true
    }
}
