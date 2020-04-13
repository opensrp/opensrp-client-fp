package org.smartregister.sample.fp.app

import android.content.Intent
import com.evernote.android.job.JobManager
import org.smartregister.Context
import org.smartregister.CoreLibrary
import org.smartregister.commonregistry.CommonFtsObject
import org.smartregister.configurableviews.ConfigurableViewsLibrary
import org.smartregister.fp.common.library.FPLibrary
import org.smartregister.fp.common.util.DBConstantsUtils
import org.smartregister.fp.common.util.Utils
import org.smartregister.location.helper.LocationHelper
import org.smartregister.receiver.SyncStatusBroadcastReceiver
import org.smartregister.repository.Repository
import org.smartregister.sample.fp.BuildConfig.BUILD_TIMESTAMP
import org.smartregister.sample.fp.BuildConfig.DATABASE_VERSION
import org.smartregister.sample.fp.R
import org.smartregister.sample.fp.login.job.FPJobCreator
import org.smartregister.sample.fp.login.ui.LoginActivity
import org.smartregister.sample.fp.repository.FPRepository
import org.smartregister.sync.DrishtiSyncScheduler
import org.smartregister.util.Log
import org.smartregister.view.activity.DrishtiApplication
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver
import timber.log.Timber

class FPApplication : DrishtiApplication(), TimeChangedBroadcastReceiver.OnTimeChangedListener {
    private var commonFtsObject: CommonFtsObject? = null
    private var mPassword: String? = null

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        context = Context.getInstance()
        context.updateApplicationContext(applicationContext)
        context.updateCommonFtsObject(createCommonFtsObject())

        //Initialize Modules
        CoreLibrary.init(context, FPSyncConfiguration(), BUILD_TIMESTAMP)
        FPLibrary.init(context, DATABASE_VERSION)
        ConfigurableViewsLibrary.init(context)
        SyncStatusBroadcastReceiver.init(this)
        TimeChangedBroadcastReceiver.init(this)
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this)
        LocationHelper.init(Utils.ALLOWED_LEVELS, Utils.DEFAULT_LOCATION_LEVEL)

        try {
            Utils.saveLanguage("en")
        } catch (e: Exception) {
            Timber.e(e, " --> saveLanguage")
        }

        //init Job Manager
        JobManager.create(this).addJobCreator(FPJobCreator())
    }

    private fun createCommonFtsObject(): CommonFtsObject {
        if (commonFtsObject == null) {
            commonFtsObject = CommonFtsObject(getFtsTables())
            for (ftsTable in commonFtsObject!!.getTables()) {
                commonFtsObject!!.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable))
                commonFtsObject!!.updateSortFields(ftsTable, getFtsSortFields(ftsTable))
            }
        }
        return commonFtsObject!!
    }

    private fun getFtsTables(): Array<String?>? {
        return arrayOf(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME)
    }

    private fun getFtsSearchFields(tableName: String): Array<String?>? {
        return if (tableName == DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME) {
            arrayOf(DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME, DBConstantsUtils.KeyUtils.FP_ID)
        } else {
            null
        }
    }

    private fun getFtsSortFields(tableName: String): Array<String?>? {
        return if (tableName == DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME) {
            arrayOf(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME,
                    DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, DBConstantsUtils.KeyUtils.DATE_REMOVED)
        } else {
            null
        }
    }


    override fun getRepository(): Repository? {
        try {
            if (repository == null) {
                repository = FPRepository(getInstance()?.applicationContext, context)
            }
        } catch (e: UnsatisfiedLinkError) {
            Log.logError("Error on getRepository: $e")
        }
        return repository
    }

    override fun getPassword(): String? {
        if (mPassword == null) {
            val username: String? = getContext()?.userService()?.allSharedPreferences?.fetchRegisteredANM()
            mPassword = getContext()?.userService()?.getGroupId(username)
        }
        return mPassword
    }

    fun getContext(): Context? {
        return context
    }

    override fun logoutCurrentUser() {
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        applicationContext.startActivity(intent)
        context.userService().logoutSession()
    }

    companion object {
        fun getInstance() : FPApplication = mInstance as FPApplication
    }

    override fun onTerminate() {
        Log.logInfo("Application is terminating. Stopping Sync scheduler and resetting isSyncInProgress setting.")
        cleanUpSyncState()
        TimeChangedBroadcastReceiver.destroy(this)
        super.onTerminate()
    }

    protected fun cleanUpSyncState() {
        try {
            DrishtiSyncScheduler.stop(applicationContext)
            context.allSharedPreferences().saveIsSyncInProgress(false)
        } catch (e: java.lang.Exception) {
            Timber.e(e, " --> cleanUpSyncState")
        }
    }

    override fun onTimeZoneChanged() {
        Utils.showToast(this, this.getString(R.string.device_time_changed))
        context.userService().forceRemoteLogin()
        logoutCurrentUser()
    }

    override fun onTimeChanged() {
        Utils.showToast(this, this.getString(R.string.device_timezone_changed))
        context.userService().forceRemoteLogin()

        logoutCurrentUser()
    }
}