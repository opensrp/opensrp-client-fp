package org.smartregister.fp.common.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.rules.RuleConstant;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.Contact;
import org.smartregister.fp.common.domain.WomanDetail;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.common.rule.ContactRule;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.repository.PatientRepository;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.fp.common.util.Utils.isCheckboxValueEmpty;

public class FinalizeVisitFormTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String baseEntityId;
    private final int contactNo;
    private final Contact contact;
    private final String jsonCurrentState;
    private final FPFormUtils fpFormUtils;
    private final ProgressDialog progressDialog;
    private HashMap<String, String> clientProfileDetail;

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
        progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        contact.setJsonForm(fpFormUtils.addFormDetails(jsonCurrentState));
        contact.setContactNumber(contactNo);
        FPFormUtils.persistPartial(baseEntityId, contact);

        /*Map<String, String> clientProfileDetail = PatientRepository.getClientProfileDetails(baseEntityId);
        ContactInteractor contactInteractor = new ContactInteractor();
        contactInteractor.finalizeContactForm(clientProfileDetail, this);*/

        clientProfileDetail = PatientRepository.getClientProfileDetails(baseEntityId);

        int gestationAge = getGestationAge(clientProfileDetail);
        boolean isFirst = TextUtils.equals("1", clientProfileDetail.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
        ContactRule contactRule = new ContactRule(gestationAge, isFirst, baseEntityId);

        List<Integer> integerList = FPLibrary.getInstance().getFPRulesEngineHelper()
                .getContactVisitSchedule(contactRule, ConstantsUtils.RulesFileUtils.CONTACT_RULES);

        int nextContactVisitWeeks = integerList.get(0);

        LocalDate localDate = new LocalDate(clientProfileDetail.get(DBConstantsUtils.KeyUtils.EDD));
        String nextContactVisitDate = localDate.minusWeeks(ConstantsUtils.DELIVERY_DATE_WEEKS).plusWeeks(nextContactVisitWeeks).toString();
        int nextContact = getNextContact(clientProfileDetail);
        WomanDetail womanDetail = getWomanDetail(baseEntityId, nextContactVisitDate, nextContact);

        PatientRepository.updateContactVisitDetails(womanDetail, true);

        try {
            processFormFieldKeyValues(baseEntityId, new JSONObject(contact.getJsonForm()), String.valueOf(contactNo));
        }
        catch (Exception ex) {
            Timber.e(ex);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Utils.navigateToProfile(context, clientProfileDetail);
    }


    private int getNextContact(Map<String, String> details) {
        int nextContact = details.containsKey(DBConstantsUtils.KeyUtils.NEXT_CONTACT) && details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT) != null ? Integer.valueOf(details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)) : 1;
        nextContact += 1;
        return nextContact;
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
            Iterator<String> keys = object.keys();

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

                            fieldObject.put(PreviousContactRepository.CONTACT_NO, contactNo);
                            fpFormUtils.savePreviousContactItem(baseEntityId, fieldObject);
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
        }
    }

}
