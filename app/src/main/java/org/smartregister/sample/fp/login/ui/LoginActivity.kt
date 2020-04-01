package org.smartregister.sample.fp.login.ui

import android.widget.Toast
import org.smartregister.sample.fp.R
import org.smartregister.sample.fp.login.presenter.LoginPresenter
import org.smartregister.view.activity.BaseLoginActivity
import org.smartregister.view.contract.BaseLoginContract

class LoginActivity : BaseLoginActivity(), BaseLoginContract.View {
    override fun goToHome(remoteLogin: Boolean) {
        // go to main page after success full login
        if (remoteLogin){
            Toast.makeText(this, "Successfully Remote Login", Toast.LENGTH_LONG).show()
            //  start you activity here
        }
    }

    override fun getContentView(): Int {
        return R.layout.activity_login
    }

    override fun initializePresenter() {
        mLoginPresenter = LoginPresenter(this);
    }
}
