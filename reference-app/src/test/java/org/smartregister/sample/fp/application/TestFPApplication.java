package org.smartregister.sample.fp.application;


import org.smartregister.sample.fp.R;
import org.smartregister.sample.fp.app.FPApplication;

/**
 * Created by ndegwamartin on 27/05/2018.
 */

public class TestFPApplication extends FPApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.AppTheme); //or just R.style.Theme_AppCompat

    }
}
