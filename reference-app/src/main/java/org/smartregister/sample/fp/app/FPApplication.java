package org.smartregister.sample.fp.app;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.evernote.android.job.JobManager;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.fp.FPEventBusIndex;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.sync.BaseFPClientProcessorForJava;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.repository.Repository;
import org.smartregister.sample.fp.BuildConfig;
import org.smartregister.sample.fp.R;
import org.smartregister.sample.fp.job.FPJobCreator;
import org.smartregister.sample.fp.login.ui.LoginActivity;
import org.smartregister.sample.fp.repository.FPRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.DrishtiSyncScheduler;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import timber.log.Timber;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by ndegwamartin on 21/06/2018.
 */
public class FPApplication extends DrishtiApplication implements TimeChangedBroadcastReceiver.OnTimeChangedListener {
    private static CommonFtsObject commonFtsObject;
    private String password;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        //Initialize Modules
        CoreLibrary.init(context, new FPSyncConfiguration(), BuildConfig.BUILD_TIMESTAMP);
        FPLibrary.init(context, BuildConfig.DATABASE_VERSION, new FPEventBusIndex());
        ConfigurableViewsLibrary.init(context);

        SyncStatusBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);
        LocationHelper.init(Utils.ALLOWED_LEVELS, Utils.DEFAULT_LOCATION_LEVEL);

        try {
            Utils.saveLanguage("en");
        } catch (Exception e) {
            Timber.e(e, " --> saveLanguage");
        }

        //init Job Manager
        JobManager.create(this).addJobCreator(new FPJobCreator());

    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        return commonFtsObject;
    }

    private static String[] getFtsTables() {
        return new String[]{DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME, DBConstantsUtils.WOMAN_DETAILS_TABLE_NAME};
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME)) {
            return new String[]{DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME, DBConstantsUtils.KeyUtils.FP_ID, DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, DBConstantsUtils.KeyUtils.ARCHIVED};
        } else {
            return null;
        }
    }

    private static String[] getFtsSortFields(String tableName) {
        if (tableName.equals(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME)) {
            return new String[]{DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME,
                    DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, DBConstantsUtils.KeyUtils.DATE_REMOVED, DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, DBConstantsUtils.KeyUtils.ARCHIVED};
        } else {
            return null;
        }
    }

    public static synchronized FPApplication getInstance() {
        return (FPApplication) DrishtiApplication.mInstance;
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new FPRepository(getInstance().getApplicationContext(), context);
            }
        } catch (UnsatisfiedLinkError e) {
            logError("Error on getRepository: " + e);

        }
        return repository;
    }

    public String getPassword() {
        if (password == null) {
            String username = getContext().userService().getAllSharedPreferences().fetchRegisteredANM();
            password = getContext().userService().getGroupId(username);
        }
        return password;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        TimeChangedBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void cleanUpSyncState() {
        try {
            DrishtiSyncScheduler.stop(getApplicationContext());
            context.allSharedPreferences().saveIsSyncInProgress(false);
        } catch (Exception e) {
            Timber.e(e, " --> cleanUpSyncState");
        }
    }

    @Override
    public void onTimeChanged() {
        Utils.showToast(this, this.getString(R.string.device_time_changed));
        context.userService().forceRemoteLogin();
        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
        Utils.showToast(this, this.getString(R.string.device_timezone_changed));
        context.userService().forceRemoteLogin();

        logoutCurrentUser();
    }

    @Override
    public void logoutCurrentUser() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        context.userService().logoutSession();
    }

    @NonNull
    @Override
    public ClientProcessorForJava getClientProcessor() {
        return BaseFPClientProcessorForJava.getInstance(this);
    }
}
