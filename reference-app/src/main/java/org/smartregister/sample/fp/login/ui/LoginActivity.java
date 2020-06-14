package org.smartregister.sample.fp.login.ui;

import android.content.Intent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.fp.common.job.ArchivedPostSterilizationJob;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.features.home.view.HomeRegisterActivity;
import org.smartregister.job.ImageUploadServiceJob;
import org.smartregister.job.PullUniqueIdsServiceJob;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncSettingsServiceJob;
import org.smartregister.sample.fp.BuildConfig;
import org.smartregister.sample.fp.R;
import org.smartregister.sample.fp.event.ViewConfigurationSyncCompleteEvent;
import org.smartregister.sample.fp.login.presenter.LoginPresenter;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import timber.log.Timber;

import static org.smartregister.fp.common.util.Utils.getFlexValue;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (!remote) scheduleJobsPeriodically();
        gotToHomeRegister(remote);
    }

    private void scheduleJobsPeriodically() {
        SyncServiceJob.scheduleJob(SyncServiceJob.TAG, (long) BuildConfig.DATA_SYNC_DURATION_MINUTES,
                getFlexValue(BuildConfig.DATA_SYNC_DURATION_MINUTES));
        PullUniqueIdsServiceJob
                .scheduleJob(PullUniqueIdsServiceJob.TAG, (long) BuildConfig.PULL_UNIQUE_IDS_MINUTES,
                        getFlexValue(BuildConfig.PULL_UNIQUE_IDS_MINUTES));
        ImageUploadServiceJob
                .scheduleJob(ImageUploadServiceJob.TAG, (long) BuildConfig.IMAGE_UPLOAD_MINUTES,
                        getFlexValue(BuildConfig.IMAGE_UPLOAD_MINUTES));
        SyncSettingsServiceJob
                .scheduleJob(SyncSettingsServiceJob.TAG, (long) BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES,
                        getFlexValue(BuildConfig.CLIENT_SETTINGS_SYNC_MINUTES));
        ArchivedPostSterilizationJob.makeSchedule();
    }

    private void gotToHomeRegister(boolean remote) {
        Intent intent = new Intent(this, HomeRegisterActivity.class);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN, remote);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void refreshViews(ViewConfigurationSyncCompleteEvent syncCompleteEvent) {
        if (syncCompleteEvent != null) {
            Timber.d("Refreshing Login View...");
            mLoginPresenter.processViewCustomizations();

        }
    }
}