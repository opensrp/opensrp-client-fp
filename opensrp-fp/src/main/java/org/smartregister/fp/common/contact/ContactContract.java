package org.smartregister.fp.common.contact;

import android.content.Context;

import org.json.JSONObject;
import org.smartregister.fp.common.domain.Contact;

import java.util.HashMap;
import java.util.Map;

public interface ContactContract {

    interface View {
        void displayPatientName(String patientName);

        void startFormActivity(JSONObject form, Contact contact);

        void displayToast(int resourceId);

        void loadGlobals(Contact contact);

    }

    interface Presenter {
        void fetchPatient(String baseEntityId);

        void setBaseEntityId(String baseEntityId);

        boolean baseEntityIdExists();

        String getPatientName();

        void startForm(Object tag);

        void onDestroy(boolean isChangingConfiguration);

        void finalizeContactForm(Map<String, String> details, Context context);

        void deleteDraft(String baseEntityId);

        void saveFinalJson(String baseEntityId);

        int getGestationAge();
    }

    interface Model {
        String extractPatientName(Map<String, String> womanDetails);

    }

    interface Interactor extends BaseContactContract.Interactor {
        HashMap<String, String> finalizeContactForm(Map<String, String> details, Context context);

        int getGestationAge(Map<String, String> details);
    }

}
