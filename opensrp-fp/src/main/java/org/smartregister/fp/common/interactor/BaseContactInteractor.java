package org.smartregister.fp.common.interactor;

import org.smartregister.fp.common.contact.BaseContactContract;
import org.smartregister.fp.common.util.AppExecutors;
import org.smartregister.fp.features.home.repository.PatientRepository;

import java.util.Map;

public abstract class BaseContactInteractor {
    protected AppExecutors appExecutors;

    BaseContactInteractor(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
    }

    protected void fetchWomanDetails(final String baseEntityId, final BaseContactContract.InteractorCallback callBack) {
        Runnable runnable = () -> {
            final Map<String, String> womanDetails = PatientRepository.getClientProfileDetails(baseEntityId);
            appExecutors.mainThread().execute(() -> callBack.onWomanDetailsFetched(womanDetails));
        };

        appExecutors.diskIO().execute(runnable);
    }

    public AppExecutors getAppExecutors() {
        return appExecutors;
    }
}
