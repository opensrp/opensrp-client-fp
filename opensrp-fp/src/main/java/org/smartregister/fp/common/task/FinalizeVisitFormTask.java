package org.smartregister.fp.common.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Pair;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.rules.RuleConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.ClientDetail;
import org.smartregister.fp.common.domain.Contact;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.repository.PatientRepository;
import org.smartregister.util.JsonFormUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.fp.common.util.ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT;
import static org.smartregister.fp.common.util.Utils.isCheckboxValueEmpty;

public class FinalizeVisitFormTask extends AsyncTask<Void, Void, HashMap<String, String>> {

    private final Context context;
    private final String baseEntityId;
    private final int contactNo;
    private final int nextContactNo;
    private final Contact contact;
    private final String jsonCurrentState;
    private final FPFormUtils fpFormUtils;
    private final ProgressDialog progressDialog;

    public FinalizeVisitFormTask(Context context, String baseEntityId, int contactNo, Contact contact, String jsonCurrentState) {
        this.context = context;
        this.baseEntityId = baseEntityId;
        this.contactNo = contactNo;
        this.nextContactNo = contactNo + 1;
        this.contact = contact;
        this.jsonCurrentState = jsonCurrentState;
        fpFormUtils = new FPFormUtils();

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(context.getString(com.vijay.jsonwizard.R.string.loading));
        progressDialog.setMessage(context.getString(R.string.finalizing_form_message));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected HashMap<String, String> doInBackground(Void... voids) {

        // add record to partial contact table
        contact.setJsonForm(fpFormUtils.addFormDetails(jsonCurrentState));
        contact.setContactNumber(contactNo);
        FPFormUtils.persistPartial(baseEntityId, contact);

        // add record to previous contact table
        try {
            processFormFieldKeyValues(baseEntityId, new JSONObject(contact.getJsonForm()), String.valueOf(contactNo));
        } catch (Exception ex) {
            Timber.e(ex);
        }

        HashMap<String, String> clientDetailMap = PatientRepository.getClientProfileDetails(baseEntityId);
        try {
            if (clientDetailMap == null) return null;

            boolean isFirst = TextUtils.equals("1", String.valueOf(contactNo));
            if (!isFirst) isFirst = Utils.isUserFirstVisitForm(baseEntityId);

            String methodExitKey = Utils.getMapValue(METHOD_EXIT, baseEntityId, contactNo);
            String methodName = Utils.getMethodName(methodExitKey);

            String nextContactVisitDate = Utils.getMethodScheduleDate(methodName, isFirst);
            ClientDetail clientDetail = getClientDetails(baseEntityId, nextContactVisitDate, nextContactNo);
            // update patient repo
            PatientRepository.updateContactVisitDetails(clientDetail, true);
            PatientRepository.updateNextContactDate(nextContactVisitDate, baseEntityId);
            updateNextContactDate(clientDetailMap, clientDetail);
            // create event
            Pair<Event, Event> eventPair = FPJsonFormUtils.createContactVisitEvent(clientDetailMap);
            if (eventPair != null) {
                createEvent(baseEntityId, eventPair.first);
                JSONObject updateClientEventJson = new JSONObject(JsonFormUtils.gson.toJson(eventPair.second));
                FPLibrary.getInstance().getEcSyncHelper().addEvent(baseEntityId, updateClientEventJson);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return clientDetailMap;
    }

    private void updateNextContactDate(Map<String, String> details, ClientDetail clientDetail) {
        details.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, clientDetail.getNextContactDate());
        details.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, clientDetail.getNextContact().toString());
    }


    @Override
    protected void onPostExecute(HashMap<String, String> result) {
        super.onPostExecute(result);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (result != null) {
            Utils.navigateToProfile(context, result);
        }
    }

    private void createEvent(String baseEntityId, Event event)
            throws JSONException {
        String currentContactState = getCurrentContactState(baseEntityId);
        if (currentContactState != null) {
            event.addDetails(ConstantsUtils.DetailsKeyUtils.PREVIOUS_CONTACTS, currentContactState);
        }
        JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));
        FPLibrary.getInstance().getEcSyncHelper().addEvent(baseEntityId, eventJson);
    }


    private String getCurrentContactState(String baseEntityId) throws JSONException {
        List<PreviousContact> previousContactList = getPreviousContactRepository().getPreviousContacts(baseEntityId, String.valueOf(contactNo), null);
        JSONObject stateObject = null;
        if (previousContactList != null) {
            stateObject = new JSONObject();

            for (PreviousContact previousContact : previousContactList) {
                stateObject.put(previousContact.getKey(), previousContact.getValue());
            }
        }

        return stateObject != null ? stateObject.toString() : null;
    }


    protected PreviousContactRepository getPreviousContactRepository() {
        return FPLibrary.getInstance().getPreviousContactRepository();
    }

    private int getNextContact(Map<String, String> details) {
        String nextContactRaw = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        int nextContact = nextContactRaw == null ? 1 : Integer.parseInt(nextContactRaw);
        return ++nextContact;
    }

    private ClientDetail getClientDetails(String baseEntityId, String nextContactVisitDate, Integer nextContact) {
        ClientDetail clientDetail = new ClientDetail();
        clientDetail.setBaseEntityId(baseEntityId);
        clientDetail.setNextContact(nextContact);
        clientDetail.setNextContactDate(nextContactVisitDate);
        clientDetail.setContactStatus(ConstantsUtils.AlertStatusUtils.TODAY);
        clientDetail.setPreviousContactStatus(ConstantsUtils.AlertStatusUtils.TODAY);
        return clientDetail;
    }

    private void processFormFieldKeyValues(String baseEntityId, JSONObject object, String contactNo) throws Exception {
        if (object != null) {
            //persistRequiredInvisibleFields(baseEntityId, contactNo, object);

            PreviousContact previousContact = new PreviousContact();
            previousContact.setVisitDate(Utils.getDBDateToday());
            boolean hasRecords = false;
            StringBuilder sql = new StringBuilder("INSERT INTO " + PreviousContactRepository.TABLE_NAME + " (" + PreviousContactRepository.ID + ", " + PreviousContactRepository.CONTACT_NO + ", " + PreviousContactRepository.BASE_ENTITY_ID + ", " + PreviousContactRepository.KEY + ", " + PreviousContactRepository.VALUE + ", " + PreviousContactRepository.CREATED_AT + ") VALUES ");
            /*
            values.put(ID, PreviousContact.getId());
        values.put(CONTACT_NO, PreviousContact.getContactNo());
        values.put(BASE_ENTITY_ID, PreviousContact.getBaseEntityId());
        values.put(VALUE, PreviousContact.getValue());
        values.put(KEY, PreviousContact.getKey());
        values.put(CREATED_AT, PreviousContact.getVisitDate());
            */

            Iterator<String> keys = object.keys();
            boolean addPostFix = false;
            while (keys.hasNext()) {
                String key = keys.next();

                if (key.startsWith(RuleConstant.STEP)) {
                    JSONArray stepArray = object.getJSONObject(key).getJSONArray(JsonFormConstants.FIELDS);

                    for (int i = 0; i < stepArray.length(); i++) {
                        JSONObject fieldObject = stepArray.getJSONObject(i);
                        FPFormUtils.processSpecialWidgets(fieldObject);

                        if (fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.EXPANSION_PANEL)) {
                            //saveExpansionPanelPreviousValues(baseEntityId, fieldObject, contactNo);
                            continue;
                        }

                        //Do not save empty checkbox values with nothing inside square braces ([])
                        if (fieldObject.has(JsonFormConstants.VALUE) &&
                                !TextUtils.isEmpty(fieldObject.getString(JsonFormConstants.VALUE)) &&
                                !isCheckboxValueEmpty(fieldObject)) {
                            hasRecords = true;

                            fieldObject.put(PreviousContactRepository.CONTACT_NO, contactNo);
                            buildPreviousContact(previousContact, baseEntityId, fieldObject);

                            if (addPostFix) sql.append(",");
                            sql.append("(");
                            sql.append(previousContact.getId()).append(",");
                            sql.append(previousContact.getContactNo()).append(",");
                            sql.append("'").append(previousContact.getBaseEntityId()).append("'").append(",");
                            sql.append("'").append(previousContact.getKey()).append("'").append(",");
                            sql.append(DatabaseUtils.sqlEscapeString(previousContact.getValue())).append(",");
                            sql.append("'").append(previousContact.getVisitDate()).append("'");
                            sql.append(")");

                            addPostFix = true;
                        }

                        if (fieldObject.has(ConstantsUtils.KeyUtils.SECONDARY_VALUES) &&
                                fieldObject.getJSONArray(ConstantsUtils.KeyUtils.SECONDARY_VALUES).length() > 0) {
                            JSONArray secondaryValues = fieldObject.getJSONArray(ConstantsUtils.KeyUtils.SECONDARY_VALUES);
                            for (int count = 0; count < secondaryValues.length(); count++) {
                                JSONObject secondaryValuesJSONObject = secondaryValues.getJSONObject(count);
                                secondaryValuesJSONObject.put(PreviousContactRepository.CONTACT_NO, contactNo);
                                fpFormUtils.savePreviousContactItem(baseEntityId, secondaryValuesJSONObject);
                            }
                        }
                    }
                }
            }

            if (hasRecords) {
                FPLibrary.getInstance().getPreviousContactRepository().execRawQuery(sql.toString());
            }
        }
    }

    private void buildPreviousContact(PreviousContact previousContact, String baseEntityId, JSONObject fieldObject) throws JSONException {
        String value = fieldObject.getString(JsonFormConstants.VALUE);
        if ("today".equalsIgnoreCase(value)) {
            value = Utils.convertDateFormat(Calendar.getInstance().getTime(), Utils.DB_DF);
        }

        previousContact.setId(previousContact.getId());
        previousContact.setKey(fieldObject.getString(JsonFormConstants.KEY));
        previousContact.setValue(value);
        previousContact.setBaseEntityId(baseEntityId);
        previousContact.setContactNo(fieldObject.getString(PreviousContactRepository.CONTACT_NO));
    }
}
