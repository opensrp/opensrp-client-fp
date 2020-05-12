package org.smartregister.fp.common.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.rules.RuleConstant;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.Contact;
import org.smartregister.fp.common.domain.WomanDetail;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.repository.PatientRepository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.fp.common.util.Utils.isCheckboxValueEmpty;

public class FinalizeVisitFormTask extends AsyncTask<Void, Void, HashMap<String, String>> {

    private final Context context;
    private final String baseEntityId;
    private final int contactNo;
    private final Contact contact;
    private final String jsonCurrentState;
    private final FPFormUtils fpFormUtils;
    private final ProgressDialog progressDialog;

    public FinalizeVisitFormTask(Context context, String baseEntityId, int contactNo, Contact contact, String jsonCurrentState) {
        this.context = context;
        this.baseEntityId = baseEntityId;
        this.contactNo = contactNo;
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

        contact.setJsonForm(fpFormUtils.addFormDetails(jsonCurrentState));
        contact.setContactNumber(contactNo);
        FPFormUtils.persistPartial(baseEntityId, contact);

        HashMap<String, String> clientProfileDetail = PatientRepository.getClientProfileDetails(baseEntityId);
        if (clientProfileDetail == null) return null;

        try {
            JSONObject formObject = new JSONObject(jsonCurrentState);
            processFormFieldKeyValues(baseEntityId, new JSONObject(contact.getJsonForm()), String.valueOf(contactNo));

            int nextContact = getNextContact(clientProfileDetail);
            LocalDate nextContactVisitDate = Utils.getNextContactVisitDate(formObject);
            String formattedDate = nextContactVisitDate == null ? null : nextContactVisitDate.toString();
            WomanDetail patientDetail = getWomanDetail(baseEntityId, formattedDate, nextContact);
            PatientRepository.updateContactVisitDetails(patientDetail, true);
        }
        catch (Exception ex) {
            Timber.e(ex);
        }

        return clientProfileDetail;
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


    private int getNextContact(Map<String, String> details) {
        String nextContactRaw = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        int nextContact = nextContactRaw == null ? 1 : Integer.parseInt(nextContactRaw);
        return ++nextContact;
    }

    public int getGestationAge(Map<String, String> details) {
        return details.containsKey(DBConstantsUtils.KeyUtils.EDD) && details.get(DBConstantsUtils.KeyUtils.EDD) != null ? Utils.getGestationAgeFromEDDate(details.get(DBConstantsUtils.KeyUtils.EDD)) : 4;
    }

    private WomanDetail getWomanDetail(String baseEntityId, String nextContactVisitDate, Integer nextContact) {
        WomanDetail womanDetail = new WomanDetail();
        womanDetail.setBaseEntityId(baseEntityId);
        womanDetail.setNextContact(nextContact);
        womanDetail.setNextContactDate(nextContactVisitDate);
        womanDetail.setContactStatus(ConstantsUtils.AlertStatusUtils.TODAY);
        womanDetail.setPreviousContactStatus(ConstantsUtils.AlertStatusUtils.TODAY);
        return womanDetail;
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
                            sql.append("'").append(previousContact.getValue()).append("'").append(",");
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
