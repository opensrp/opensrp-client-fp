package org.smartregister.sample.fp.app

import com.evernote.android.job.JobManager
import org.smartregister.Context
import org.smartregister.CoreLibrary
import org.smartregister.configurableviews.ConfigurableViewsLibrary
import org.smartregister.fp.FPLibrary
import org.smartregister.sample.fp.BuildConfig.BUILD_TIMESTAMP
import org.smartregister.sample.fp.BuildConfig.DATABASE_VERSION
import org.smartregister.sample.fp.login.job.FPJobCreator
import org.smartregister.view.activity.DrishtiApplication

class FPApplication : DrishtiApplication() {

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        context = Context.getInstance()
        context.updateApplicationContext(applicationContext)

        //Initialize Modules
        CoreLibrary.init(context, FPSyncConfiguration(), BUILD_TIMESTAMP)
        FPLibrary.init(context, DATABASE_VERSION)
        ConfigurableViewsLibrary.init(context)

        //init Job Manager

        //init Job Manager
        JobManager.create(this).addJobCreator(FPJobCreator())
    }

    override fun logoutCurrentUser() {

    }

    companion object {
        fun getInstance() : FPApplication = mInstance as FPApplication
    }
}