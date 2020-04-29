package org.smartregister.sample.fp.login.ui;

import android.content.Intent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.features.home.view.HomeRegisterActivity;
import org.smartregister.sample.fp.R;
import org.smartregister.sample.fp.event.ViewConfigurationSyncCompleteEvent;
import org.smartregister.sample.fp.login.presenter.LoginPresenter;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

import timber.log.Timber;

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
        gotToHomeRegister(remote);
        //String nancyAjram = "2310d5a4-c9b8-454c-9c9b-ea193715d38f";
        //HashMap<String, String> clientDetails = PatientRepository.getClientProfileDetails(nancyAjram);
        //Utils.navigateToProfile(this, clientDetails);
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