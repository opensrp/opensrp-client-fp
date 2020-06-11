package org.smartregister.fp.common.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.fp.common.helper.ECSyncHelper;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PartialContact;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.MiniClientProcessorForJava;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class BaseFPClientProcessorForJava extends ClientProcessorForJava implements MiniClientProcessorForJava {
    private HashSet<String> eventTypes = new HashSet<>();
    private static BaseFPClientProcessorForJava instance;

    public BaseFPClientProcessorForJava(Context context) {
        super(context);
    }

    public static BaseFPClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new BaseFPClientProcessorForJava(context);
        }

        return instance;
    }

    @Override
    public void processClient(List<EventClient> eventClients) throws Exception {
        Timber.d("Inside the BaseAncClientProcessorForJava");
        ClientClassification clientClassification =
                assetJsonToJava(ConstantsUtils.EcFileUtils.CLIENT_CLASSIFICATION, ClientClassification.class);

        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                processEventClient(eventClient, unsyncEvents, clientClassification);
            }
            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }
    }

    private void processVisit(Event event) {
        //Attention flags
        getDetailsRepository()
                .add(event.getBaseEntityId(), ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS,
                        event.getDetails().get(ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS),
                        Calendar.getInstance().getTimeInMillis());
        processPreviousContacts(event);
    }

    private boolean unSync(ECSyncHelper ecSyncHelper, DetailsRepository detailsRepository, List<Table> bindObjects,
                           Event event, String registeredAnm) {
        try {
            String baseEntityId = event.getBaseEntityId();
            String providerId = event.getProviderId();

            if (providerId.equals(registeredAnm)) {
                ecSyncHelper.deleteEventsByBaseEntityId(baseEntityId);
                ecSyncHelper.deleteClient(baseEntityId);
                detailsRepository.deleteDetails(baseEntityId);

                for (Table bindObject : bindObjects) {
                    String tableName = bindObject.name;
                    deleteCase(tableName, baseEntityId);
                }

                return true;
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    public DetailsRepository getDetailsRepository() {
        return FPLibrary.getInstance().getDetailsRepository();
    }

    private void processPreviousContacts(Event event) {
        //Previous contact state
        String previousContactsRaw = event.getDetails().get(ConstantsUtils.DetailsKeyUtils.PREVIOUS_CONTACTS);
        Map<String, String> previousContactMap = getPreviousContactMap(previousContactsRaw);

        if (previousContactMap != null) {
            int contactNo = getContact(event);
            for (Map.Entry<String, String> entry : previousContactMap.entrySet()) {
                FPLibrary.getInstance().getPreviousContactRepository().savePreviousContact(
                        new PreviousContact(event.getBaseEntityId(), entry.getKey(), entry.getValue(), String.valueOf(contactNo)));
            }
        }
    }

    public PartialContact getPartialObject(String partialContactsRaw) {
        return FPLibrary.getInstance().getGsonInstance().fromJson(partialContactsRaw, PartialContact.class);
    }

    public Map<String, String> getPreviousContactMap(String previousContactsRaw) {
        return FPLibrary.getInstance().getGsonInstance().fromJson(previousContactsRaw, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    private int getContact(Event event) {
        int contactNo = 0;
        if (!TextUtils.isEmpty(event.getDetails().get(ConstantsUtils.CONTACT))) {
            String contact = event.getDetails().get(ConstantsUtils.CONTACT);
            try {
                if (contact != null) {
                    contactNo = Integer.parseInt(contact);
                }
            } catch (NumberFormatException ignore) {
            }
        }
        return contactNo;
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {
        AllCommonsRepository allCommonsRepository = org.smartregister.CoreLibrary.getInstance().context().allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }
    }

    @Override
    public String[] getOpenmrsGenIds() {
        /*
        This method is not currently used because the ANC_ID is always a number and does not contain hyphens.
        This method is used to get the identifiers used by OpenMRS so that we remove the hyphens in the
        content values for such identifiers
         */
        return new String[]{DBConstantsUtils.KeyUtils.FP_ID, ConstantsUtils.JsonFormKeyUtils.CLIENT_ID};
    }

    @Override
    public void processEventClient(@NonNull EventClient eventClient, @NonNull List<Event> unsyncEvents, @Nullable ClientClassification clientClassification) throws Exception {
        Event event = eventClient.getEvent();
        if (event == null) {
            return;
        }

        String eventType = event.getEventType();
        if (StringUtils.isBlank(eventType)) {
            return;
        }
        Client client = eventClient.getClient();
        switch (eventType) {
            case ConstantsUtils.EventTypeUtils.CLOSE:
            case ConstantsUtils.EventTypeUtils.REGISTRATION:
            case ConstantsUtils.EventTypeUtils.UPDATE_REGISTRATION:
                if (clientClassification == null) {
                    return;
                }
                //iterate through the events
                if (client != null) {
                    processEvent(event, client, clientClassification);
                }
                break;
            case ConstantsUtils.EventTypeUtils.CONTACT_VISIT:
                processVisit(event);
                break;
            case ConstantsUtils.EventTypeUtils.VISIT_FORM_JSON:
                processJsonVisitFrom(event);
                break;
            default:
                break;
        }
    }

    private void processJsonVisitFrom(Event event) {
        processPartialVisitForm(event);
    }

    private void processPartialVisitForm(Event event) {
        //Previous contact state
        String previousContactsRaw = event.getDetails().get(ConstantsUtils.DetailsKeyUtils.PREVIOUS_CONTACTS);
        PartialContact partialContact = getPartialObject(previousContactsRaw);

        if (partialContact != null) {
            FPLibrary.getInstance().getPartialContactRepository().insertPartialContact(partialContact);
        }
    }

    @Override
    public boolean unSync(@Nullable List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String registeredAnm = allSharedPreferences.fetchRegisteredANM();

            ClientField clientField = assetJsonToJava(ConstantsUtils.EcFileUtils.CLIENT_FIELDS, ClientField.class);
            if (clientField == null) {
                return false;
            }

            List<Table> bindObjects = clientField.bindobjects;
            DetailsRepository detailsRepository = FPLibrary.getInstance().getContext().detailsRepository();
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(getContext());

            for (Event event : events) {
                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Timber.e(e, " --> unSync");
        }

        return false;
    }


    @NonNull
    @Override
    public HashSet<String> getEventTypes() {
        if (eventTypes.isEmpty()) {
            eventTypes.add(ConstantsUtils.EventTypeUtils.REGISTRATION);
            eventTypes.add(ConstantsUtils.EventTypeUtils.UPDATE_REGISTRATION);
            eventTypes.add(ConstantsUtils.EventTypeUtils.QUICK_CHECK);
            eventTypes.add(ConstantsUtils.EventTypeUtils.CONTACT_VISIT);
            eventTypes.add(ConstantsUtils.EventTypeUtils.CLOSE);
            eventTypes.add(ConstantsUtils.EventTypeUtils.SITE_CHARACTERISTICS);
        }

        return eventTypes;
    }

    @Override
    public boolean canProcess(@NonNull String eventType) {
        return getEventTypes().contains(eventType);
    }
}
