package org.smartregister.fp.common.contact;

import java.util.Map;

public interface BaseContactContract {
    interface Interactor {
        void fetchWomanDetails(String baseEntityId, InteractorCallback callBack);
    }

    interface InteractorCallback {
        void onWomanDetailsFetched(Map<String, String> womanDetails);
    }
}
