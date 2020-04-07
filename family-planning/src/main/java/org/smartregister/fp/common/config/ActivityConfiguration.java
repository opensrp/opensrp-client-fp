package org.smartregister.fp.common.config;

import android.app.Activity;

import androidx.annotation.NonNull;

import org.smartregister.fp.features.home.view.HomeRegisterActivity;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 2019-07-17
 */

public class ActivityConfiguration {

    private Class<? extends HomeRegisterActivity> homeRegisterActivityClass;
    private Class<? extends Activity> landingPageActivityClass;


    public ActivityConfiguration() {
        setHomeRegisterActivityClass(HomeRegisterActivity.class);
        setLandingPageActivityClass(HomeRegisterActivity.class);
    }

    public Class<? extends HomeRegisterActivity> getHomeRegisterActivityClass() {
        return homeRegisterActivityClass;
    }

    public void setHomeRegisterActivityClass(@NonNull Class<? extends HomeRegisterActivity> homeRegisterActivityClass) {
        this.homeRegisterActivityClass = homeRegisterActivityClass;
    }

    public Class<? extends Activity> getLandingPageActivityClass() {
        return landingPageActivityClass;
    }

    public void setLandingPageActivityClass(Class<? extends Activity> landingPageActivityClass) {
        this.landingPageActivityClass = landingPageActivityClass;
    }
}
