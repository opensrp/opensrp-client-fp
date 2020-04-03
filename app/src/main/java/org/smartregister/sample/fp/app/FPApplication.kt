package org.smartregister.sample.fp.app

import com.evernote.android.job.JobManager
import org.smartregister.BuildConfig
import org.smartregister.Context
import org.smartregister.CoreLibrary
import org.smartregister.configurableviews.ConfigurableViewsLibrary
import org.smartregister.repository.Repository
import org.smartregister.sample.fp.login.job.FPJobCreator
import org.smartregister.sample.fp.repository.FPRepository
import org.smartregister.util.Log
import org.smartregister.view.activity.DrishtiApplication

class FPApplication : DrishtiApplication() {

    private var mPassword: String? = null

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        context = Context.getInstance()
        context.updateApplicationContext(applicationContext)

        //Initialize Modules
        CoreLibrary.init(context, AncSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP)
//        FPLibrary.init(context, org.smartregister.sample.fp.BuildConfig.DATABASE_VERSION/*, EventBusException()*/)
        ConfigurableViewsLibrary.init(context)

        //init Job Manager
        JobManager.create(this).addJobCreator(FPJobCreator())
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

    }
}