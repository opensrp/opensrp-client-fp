package org.smartregister.fp.application;


import org.smartregister.fp.R;
import org.smartregister.view.activity.DrishtiApplication;

/**
 * Created by ndegwamartin on 27/05/2018.
 */

public class FPTestApplication extends DrishtiApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.FPAppTheme);
    }

    @Override
    public void logoutCurrentUser() {
        // Nothing happens here
    }
}
