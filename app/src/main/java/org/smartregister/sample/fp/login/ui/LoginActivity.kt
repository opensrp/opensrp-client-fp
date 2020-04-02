package org.smartregister.sample.fp.login.ui

import android.content.Intent
import org.smartregister.fp.common.util.ConstantsUtils
import org.smartregister.fp.common.util.Utils
import org.smartregister.fp.common.view.SiteCharacteristicsEnterActivity
import org.smartregister.fp.features.home.view.HomeRegisterActivity
import org.smartregister.sample.fp.R
import org.smartregister.sample.fp.login.presenter.LoginPresenter
import org.smartregister.task.SaveTeamLocationsTask
import org.smartregister.view.activity.BaseLoginActivity
import org.smartregister.view.contract.BaseLoginContract

class LoginActivity : BaseLoginActivity(), BaseLoginContract.View {
    override fun goToHome(remoteLogin: Boolean) {
        // go to main page after success full login
        if (remoteLogin){
            Utils.startAsyncTask(SaveTeamLocationsTask(), null)
            //  start you activity here
        }

        if (mLoginPresenter.isServerSettingsSet) {
            gotToHomeRegister(remoteLogin)
        } else {
            goToSiteCharacteristics(remoteLogin)
        }

        finish()
    }

    override fun getContentView(): Int {
        return R.layout.activity_login
    }

    override fun initializePresenter() {
        mLoginPresenter = LoginPresenter(this);
    }

    private fun gotToHomeRegister(remote: Boolean) {
        val intent = Intent(this, HomeRegisterActivity::class.java)
        intent.putExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN, remote)
        startActivity(intent)
    }

    private fun goToSiteCharacteristics(remote: Boolean) {
        val intent = Intent(this, SiteCharacteristicsEnterActivity::class.java)
        intent.putExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN, remote)
        startActivity(intent)
    }
}
