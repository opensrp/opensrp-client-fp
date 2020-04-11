package org.smartregister.sample.fp.app

import org.smartregister.SyncConfiguration
import org.smartregister.SyncFilter
import org.smartregister.sample.fp.BuildConfig

<<<<<<< HEAD:reference-app/src/main/java/org/smartregister/sample/fp/app/FPConfiguration.kt
class FPConfiguration : SyncConfiguration() {
=======
class FPSyncConfiguration : SyncConfiguration() {
>>>>>>> issue5-create-main-register-page:reference-app/src/main/java/org/smartregister/sample/fp/app/FPSyncConfiguration.kt
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

    override fun getSynchronizedLocationTags(): MutableList<String> {
        return ArrayList()
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

    override fun getTopAllowedLocationLevel(): String {
        return ""
    }

    override fun updateClientDetailsTable(): Boolean {
        return true
    }

    override fun getSynchronizedLocationTags(): List<String?>? {
        return null
    }

    override fun getTopAllowedLocationLevel(): String? {
        return null
    }
}
