package org.smartregister.sample.fp.app

import org.smartregister.BuildConfig
import org.smartregister.Context
import org.smartregister.CoreLibrary
import org.smartregister.commonregistry.CommonFtsObject
import org.smartregister.view.activity.DrishtiApplication

class FPApplication : DrishtiApplication() {

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        context = Context.getInstance()
        context.updateApplicationContext(applicationContext)

        //Initialize Modules
        CoreLibrary.init(context, AncSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP)
    }

    override fun logoutCurrentUser() {

    }
}