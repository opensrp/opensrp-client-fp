package org.smartregister.sample.fp.app

import com.evernote.android.job.JobManager
import com.flurry.android.FlurryAgent
import org.smartregister.Context
import org.smartregister.CoreLibrary
import org.smartregister.configurableviews.ConfigurableViewsLibrary
import org.smartregister.repository.Repository
import org.smartregister.sample.fp.BuildConfig
import org.smartregister.sample.fp.job.FPJobCreator
import org.smartregister.sample.fp.repository.FPRepository
import org.smartregister.util.Log
import org.smartregister.view.activity.DrishtiApplication

open class FPApplication : DrishtiApplication() {

    private var mPassword: String? = null

    override fun onCreate() {
        super.onCreate()

        mInstance = this
        context = Context.getInstance()
        context.updateApplicationContext(applicationContext)

        //Initialize Modules
        CoreLibrary.init(context, FPConfiguration(), BuildConfig.BUILD_TIMESTAMP)
        ConfigurableViewsLibrary.init(context)

        //init Job Manager
        JobManager.create(this).addJobCreator(FPJobCreator())
        //Only integrate Flurry Analytics for  production. Remove negation to test in debug

        //Only integrate Flurry Analytics for  production. Remove negation to test in debug
        if (!BuildConfig.DEBUG) {
            FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .withCaptureUncaughtExceptions(true)
                    .withContinueSessionMillis(10000)
                    .withLogLevel(android.util.Log.VERBOSE)
                    .build(this, BuildConfig.FLURRY_API_KEY)
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

    }
}