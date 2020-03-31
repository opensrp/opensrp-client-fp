package org.smartregister.fp.features.login.view;

import android.os.Bundle;

import org.smartregister.fp.R;
import org.smartregister.view.activity.BaseLoginActivity;

public class LoginActivity extends BaseLoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected int getContentView() {
        return 0;
    }

    @Override
    protected void initializePresenter() {

    }

    @Override
    public void goToHome(boolean b) {

    }
}
