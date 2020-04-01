package org.smartregister.sample.fp.login.ui

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.smartregister.sample.fp.R
import org.smartregister.sample.fp.login.presenter.LoginPresenter
import org.smartregister.view.activity.BaseLoginActivity
import org.smartregister.view.contract.BaseLoginContract

class LoginActivity : BaseLoginActivity(), BaseLoginContract.View {
    override fun goToHome(isLogin: Boolean) {
        // go to main page after success full login
        if (isLogin) {

            Toast.makeText(this, "Successfully login", Toast.LENGTH_LONG).show()
        } else
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
    }

    override fun getContentView(): Int {
        return R.layout.activity_login
    }

    override fun initializePresenter() {
        mLoginPresenter = LoginPresenter(this);
    }
}
