package org.smartregister.fp.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.common.reflect.TypeToken;
import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jeasy.rules.api.Facts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.FormEntityConstants;
import org.smartregister.domain.ProfileImage;
import org.smartregister.domain.form.FormLocation;
import org.smartregister.domain.tag.FormTag;
import org.smartregister.fp.BuildConfig;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.YamlConfigItem;
import org.smartregister.fp.common.domain.YamlConfigWrapper;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ContactSummaryModel;
import org.smartregister.fp.common.model.RegisterModel;
import org.smartregister.fp.common.model.Task;
import org.smartregister.fp.features.home.repository.PatientRepository;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.FormUtils;
import org.smartregister.view.LocationPickerView;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

import static com.vijay.jsonwizard.constants.JsonFormConstants.PERFORM_FORM_TRANSLATION;

/**
 * Created by keyman on 27/06/2018.
 */
public class FPJsonFormUtils extends org.smartregister.util.JsonFormUtils {
    public static final String METADATA = "metadata";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    public static final String READ_ONLY = "read_only";
    public static final int REQUEST_CODE_GET_JSON = 3432;
    public static final String TYPE = "type";
    public static final String HIDDEN = "hidden";
    private static final String TAG = FPJsonFormUtils.class.getCanonicalName();

    public static boolean isFieldRequired(JSONObject fieldObject) throws JSONException {
        boolean isValueRequired = false;
        if (fieldObject.has(JsonFormConstants.V_REQUIRED)) {
            JSONObject valueRequired = fieldObject.getJSONObject(JsonFormConstants.V_REQUIRED);
            String value = valueRequired.getString(JsonFormConstants.VALUE);
            isValueRequired = Boolean.parseBoolean(value);
        }
        //Don't check required for hidden, toaster notes, spacer and label widgets
        return (!fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.LABEL) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.SPACER) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.TOASTER_NOTES) &&
                !fieldObject.getString(JsonFormConstants.TYPE).equals(JsonFormConstants.HIDDEN)) &&
                isValueRequired;
    }

    public static JSONObject getFormAsJson(JSONObject form, String formName, String id, String currentLocationId)
            throws Exception {
        if (form == null) {
            return null;
        }

        String entityId = id;
        form.getJSONObject(METADATA).put(FPJsonFormUtils.ENCOUNTER_LOCATION, currentLocationId);

        if (ConstantsUtils.JsonFormUtils.FP_REGISTER.equals(formName)) {
            if (StringUtils.isNotBlank(entityId)) {
                entityId = entityId.replace("-", "");
            }

            // Inject opensrp id into the form
            JSONArray field = FPJsonFormUtils.fields(form);
            JSONObject clientId = getFieldJSONObject(field, ConstantsUtils.JsonFormKeyUtils.CLIENT_ID);
            if (clientId != null) {
                clientId.remove(FPJsonFormUtils.VALUE);
                clientId.put(FPJsonFormUtils.VALUE, entityId);
            }

        } else if (ConstantsUtils.JsonFormUtils.FP_CLOSE.equals(formName)) {
            if (StringUtils.isNotBlank(entityId)) {
                // Inject entity id into the remove form
                form.remove(FPJsonFormUtils.ENTITY_ID);
                form.put(FPJsonFormUtils.ENTITY_ID, entityId);
            }
        } else {
            Timber.tag(TAG).w("Unsupported form requested for launch " + formName);
        }
        Timber.d("form is " + form.toString());
        return form;
    }

    public static JSONObject getFormAsJson(String formName, String entityId, String currentLocationId) throws Exception {
        JSONObject form = getFormUtils().getFormJson(formName);
        if (form == null) {
            return null;
        }
        return FPJsonFormUtils.getFormAsJson(form, formName, entityId, currentLocationId);
    }

    public static FormUtils getFormUtils() {
        FormUtils formUtils = null;
        try {
            formUtils = FormUtils.getInstance(FPLibrary.getInstance().getApplicationContext());
        } catch (Exception e) {
            Timber.e(e, RegisterModel.class.getCanonicalName(), e.getMessage());
        }
        return formUtils;
    }

    public static JSONObject getFieldJSONObject(JSONArray jsonArray, String key) {
        if (jsonArray == null || jsonArray.length() == 0) {
            return null;
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = FPJsonFormUtils.getJSONObject(jsonArray, i);
            String keyVal = FPJsonFormUtils.getString(jsonObject, FPJsonFormUtils.KEY);
            if (keyVal != null && keyVal.equals(key)) {
                return jsonObject;
            }
        }
        return null;
    }

    public static Pair<Client, Event> processRegistrationForm(AllSharedPreferences allSharedPreferences, String jsonString) {
        try {
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String entityId = getEntityId(jsonForm);
            String encounterType = FPJsonFormUtils.getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = FPJsonFormUtils.getJSONObject(jsonForm, METADATA);

            // String lastLocationName = null;
            // String lastLocationId = null;
            // TODO Replace values for location questions with their corresponding location IDs


            addLastInteractedWith(fields);
            getDobStrings(fields);
//            initializeFirstContactValues(fields);
            FormTag formTag = getFormTag(allSharedPreferences);


            Client baseClient = org.smartregister.util.JsonFormUtils.createBaseClient(fields, formTag, entityId);
            Event baseEvent = org.smartregister.util.JsonFormUtils
                    .createEvent(fields, metadata, formTag, entityId, encounterType, DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME);

            tagSyncMetadata(allSharedPreferences, baseEvent);// tag docs

            return Pair.create(baseClient, baseEvent);
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> processRegistrationForm");
            return null;
        }
    }

    public static Triple<Boolean, JSONObject, JSONArray> validateParameters(String jsonString) {

        JSONObject jsonForm = FPJsonFormUtils.toJSONObject(jsonString);
        JSONArray fields = FPJsonFormUtils.fields(jsonForm);

        return Triple.of(jsonForm != null && fields != null, jsonForm, fields);
    }

    @NotNull
    private static String getEntityId(JSONObject jsonForm) {
        String entityId = FPJsonFormUtils.getString(jsonForm, FPJsonFormUtils.ENTITY_ID);
        if (StringUtils.isBlank(entityId)) {
            entityId = FPJsonFormUtils.generateRandomUUIDString();
        }
        return entityId;
    }

    private static void addLastInteractedWith(JSONArray fields) throws JSONException {
        JSONObject lastInteractedWith = new JSONObject();
        lastInteractedWith.put(ConstantsUtils.KeyUtils.KEY, DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH);
        lastInteractedWith.put(ConstantsUtils.KeyUtils.VALUE, Calendar.getInstance().getTimeInMillis());
        fields.put(lastInteractedWith);
    }

    private static void getDobStrings(JSONArray fields) throws JSONException {
        JSONObject dobUnknownObject = getFieldJSONObject(fields, DBConstantsUtils.KeyUtils.DOB_UNKNOWN);
        JSONArray options = FPJsonFormUtils.getJSONArray(dobUnknownObject, ConstantsUtils.JsonFormKeyUtils.OPTIONS);
        JSONObject option = FPJsonFormUtils.getJSONObject(options, 0);
        String dobUnKnownString = option != null ? option.getString(FPJsonFormUtils.VALUE) : null;

        if (StringUtils.isNotBlank(dobUnKnownString)) {
            dobUnknownObject.put(FPJsonFormUtils.VALUE, Boolean.parseBoolean(dobUnKnownString) ? 1 : 0);
        }
    }

    private static void initializeFirstContactValues(JSONArray fields) throws JSONException {
        //initialize first contact values
        JSONObject nextContactJSONObject = getFieldJSONObject(fields, DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        if (nextContactJSONObject.has(JsonFormConstants.VALUE) &&
                "".equals(nextContactJSONObject.getString(JsonFormConstants.VALUE))) {
            nextContactJSONObject.put(FPJsonFormUtils.VALUE, 1);
        }

        JSONObject nextContactDateJSONObject = getFieldJSONObject(fields, DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE);
        if (nextContactDateJSONObject.has(JsonFormConstants.VALUE) &&
                "".equals(nextContactDateJSONObject.getString(JsonFormConstants.VALUE))) {
            nextContactDateJSONObject.put(FPJsonFormUtils.VALUE, Utils.convertDateFormat(Calendar.getInstance().getTime(), Utils.DB_DF));
        }
    }

    @NotNull
    private static FormTag getFormTag(AllSharedPreferences allSharedPreferences) {
        FormTag formTag = new FormTag();
        formTag.providerId = allSharedPreferences.fetchRegisteredANM();
        formTag.appVersion = BuildConfig.VERSION_CODE;
        formTag.databaseVersion = FPLibrary.getInstance().getDatabaseVersion();
        return formTag;
    }

    private static void tagSyncMetadata(AllSharedPreferences allSharedPreferences, Event event) {
        String providerId = allSharedPreferences.fetchRegisteredANM();
        event.setProviderId(providerId);
        event.setLocationId(allSharedPreferences.fetchDefaultLocalityId(providerId));
        event.setChildLocationId(getChildLocationId(event.getLocationId(), allSharedPreferences));
        event.setTeam(allSharedPreferences.fetchDefaultTeam(providerId));
        event.setTeamId(allSharedPreferences.fetchDefaultTeamId(providerId));
        //event.setVersion(BuildConfig.EVENT_VERSION);
        event.setClientApplicationVersion(BuildConfig.VERSION_CODE);
        event.setClientDatabaseVersion(FPLibrary.getInstance().getDatabaseVersion());
    }

    @Nullable
    public static String getChildLocationId(@NonNull String defaultLocationId, @NonNull AllSharedPreferences allSharedPreferences) {
        String currentLocality = allSharedPreferences.fetchCurrentLocality();

        if (StringUtils.isNotBlank(currentLocality)) {
            String currentLocalityId = LocationHelper.getInstance().getOpenMrsLocationId(currentLocality);
            if (StringUtils.isNotBlank(currentLocalityId) && !defaultLocationId.equals(currentLocalityId)) {
                return currentLocalityId;
            }
        }

        return null;
    }

    public static void mergeAndSaveClient(Client baseClient) throws Exception {
        JSONObject updatedClientJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(baseClient));

        JSONObject originalClientJsonObject =
                FPLibrary.getInstance().getEcSyncHelper().getClient(baseClient.getBaseEntityId());

        JSONObject mergedJson = org.smartregister.util.JsonFormUtils.merge(originalClientJsonObject, updatedClientJson);

        //TODO Save edit log ?

        FPLibrary.getInstance().getEcSyncHelper().addClient(baseClient.getBaseEntityId(), mergedJson);
    }

    public static void saveImage(String providerId, String entityId, String imageLocation) {
        OutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(imageLocation)) {
                return;
            }

            File file = FileUtil.createFileFromPath(imageLocation);
            if (!file.exists()) {
                return;
            }

            Bitmap compressedBitmap = FPLibrary.getInstance().getCompressor().compressToBitmap(file);
            if (compressedBitmap == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
                return;
            }

            if (!entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = FileUtil.createFileFromPath(absoluteFileName);
                outputStream = FileUtil.createFileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                compressedBitmap.compress(compressFormat, 100, outputStream);
                // insert into the db
                ProfileImage profileImage = getProfileImage(providerId, entityId, absoluteFileName);
                ImageRepository imageRepository = FPLibrary.getInstance().getContext().imageRepository();
                imageRepository.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Timber.e("Failed to save static image to disk");
        } catch (IOException e) {
            Timber.e(e, " --> saveImage");
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Timber.e("Failed to close static images output stream after attempting to write image");
                }
            }
        }
    }

    @NotNull
    private static ProfileImage getProfileImage(String providerId, String entityId, String absoluteFileName) {
        ProfileImage profileImage = new ProfileImage();
        profileImage.setImageid(UUID.randomUUID().toString());
        profileImage.setAnmId(providerId);
        profileImage.setEntityID(entityId);
        profileImage.setFilepath(absoluteFileName);
        profileImage.setFilecategory(ConstantsUtils.FileCategoryUtils.PROFILE_PIC);
        profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
        return profileImage;
    }

    public static String getString(String jsonString, String field) {
        return FPJsonFormUtils.getString(FPJsonFormUtils.toJSONObject(jsonString), field);
    }

    public static String getFieldValue(String jsonString, String key) {
        JSONObject jsonForm = FPJsonFormUtils.toJSONObject(jsonString);
        if (jsonForm == null) {
            return null;
        }

        JSONArray fields = FPJsonFormUtils.fields(jsonForm);
        if (fields == null) {
            return null;
        }

        return FPJsonFormUtils.getFieldValue(fields, key);

    }

    public static String getAutoPopulatedJsonEditRegisterFormString(Context context, Map<String, String> registeredClient) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(ConstantsUtils.JsonFormUtils.FP_REGISTER);
            LocationPickerView lpv = createLocationPickerView(context);
            if (lpv != null) {
                lpv.init();
            }
            FPJsonFormUtils.addWomanRegisterHierarchyQuestions(form);
            Timber.d("Form is %s", form.toString());
            form.put(FPJsonFormUtils.ENTITY_ID, registeredClient.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID));
            form.put(FPJsonFormUtils.ENCOUNTER_TYPE, ConstantsUtils.EventTypeUtils.UPDATE_REGISTRATION);

            JSONObject metadata = form.getJSONObject(FPJsonFormUtils.METADATA);
            String lastLocationId =
                    lpv != null ? LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem()) : "";

            metadata.put(FPJsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

            form.put(ConstantsUtils.CURRENT_OPENSRP_ID, registeredClient.get(DBConstantsUtils.KeyUtils.FP_ID).replace("-", ""));

            //inject opensrp id into the form
            JSONObject stepOne = form.getJSONObject(FPJsonFormUtils.STEP1);
            JSONArray jsonArray = stepOne.getJSONArray(FPJsonFormUtils.FIELDS);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                processPopulatableFields(registeredClient, jsonObject);

            }

            // removing referred by from form
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.REFERRED_BY)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        jsonArray.remove(i);
                    }
                    break;
                }

            }

            return form.toString();
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> getAutoPopulatedJsonEditRegisterFormString");
        }

        return "";
    }

    private static LocationPickerView createLocationPickerView(Context context) {
        try {
            return new LocationPickerView(context);
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> createLocationPickerView");
            return null;
        }
    }

    public static void addWomanRegisterHierarchyQuestions(JSONObject form) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("Province");
            allLevels.add("District");
            allLevels.add("City/Town");
            allLevels.add("Health Facility");
            allLevels.add(Utils.HOME_ADDRESS);


            ArrayList<String> healthFacilities = new ArrayList<>();
            healthFacilities.add(Utils.HOME_ADDRESS);


            List<String> defaultFacility = LocationHelper.getInstance().generateDefaultLocationHierarchy(healthFacilities);
            List<FormLocation> upToFacilities =
                    LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);

            String defaultFacilityString = AssetHandler.javaToJsonString(defaultFacility, new TypeToken<List<String>>() {
            }.getType());

            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities, new TypeToken<List<FormLocation>>() {
            }.getType());

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString(ConstantsUtils.KeyUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.HOME_ADDRESS)) {
                    if (StringUtils.isNotBlank(upToFacilitiesString)) {
                        questions.getJSONObject(i).put(ConstantsUtils.KeyUtils.TREE, new JSONArray(upToFacilitiesString));
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        questions.getJSONObject(i).put(ConstantsUtils.KeyUtils.DEFAULT, defaultFacilityString);
                    }
                }
            }

        } catch (JSONException e) {
            Timber.e(e, "JsonFormUtils --> addWomanRegisterHierarchyQuestions");
        }
    }

    protected static void processPopulatableFields(Map<String, String> client, JSONObject jsonObject)
            throws JSONException {

        if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(ConstantsUtils.JsonFormKeyUtils.DOB_ENTERED)) {
            getDobUsingEdd(client, jsonObject, DBConstantsUtils.KeyUtils.DOB);
            jsonObject.put(FPJsonFormUtils.READ_ONLY, false);
        } else if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.HOME_ADDRESS)) {
            String homeAddress = client.get(DBConstantsUtils.KeyUtils.HOME_ADDRESS);
            jsonObject.put(FPJsonFormUtils.VALUE, homeAddress);

        } else if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.DOB_UNKNOWN)) {
            jsonObject.put(FPJsonFormUtils.READ_ONLY, false);
            JSONObject optionsObject = jsonObject.getJSONArray(ConstantsUtils.JsonFormKeyUtils.OPTIONS).getJSONObject(0);
            optionsObject.put(FPJsonFormUtils.VALUE, client.get(DBConstantsUtils.KeyUtils.DOB_UNKNOWN));

        } else if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(ConstantsUtils.KeyUtils.AGE_ENTERED)) {
            jsonObject.put(FPJsonFormUtils.READ_ONLY, false);
            if (StringUtils.isNotBlank(client.get(DBConstantsUtils.KeyUtils.AGE_ENTERED))) {
                jsonObject.put(FPJsonFormUtils.VALUE, client.get(DBConstantsUtils.KeyUtils.AGE_ENTERED));
            }
        } else if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(ConstantsUtils.JsonFormKeyUtils.CLIENT_ID)) {
            jsonObject.put(FPJsonFormUtils.READ_ONLY, true);
            jsonObject.put(FPJsonFormUtils.VALUE, client.get(DBConstantsUtils.KeyUtils.FP_ID).replace("-", ""));

        } else if (jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.UNIVERSAL_ID)
                || jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.REFERRAL)
                || jsonObject.getString(FPJsonFormUtils.KEY).equalsIgnoreCase(DBConstantsUtils.KeyUtils.REFERRED_BY)) {
            jsonObject.put(FPJsonFormUtils.READ_ONLY, true);
            jsonObject.put(FPJsonFormUtils.VALUE, client.get(jsonObject.getString(FPJsonFormUtils.KEY)));
        } else if (client.containsKey(jsonObject.getString(FPJsonFormUtils.KEY))) {
            jsonObject.put(FPJsonFormUtils.READ_ONLY, false);
            jsonObject.put(FPJsonFormUtils.VALUE, client.get(jsonObject.getString(FPJsonFormUtils.KEY)));
        } else {
            Timber.e("ERROR:: Unprocessed Form Object Key %s", jsonObject.getString(FPJsonFormUtils.KEY));
        }
    }

    private static void getDobUsingEdd(Map<String, String> womanClient, JSONObject jsonObject, String birthDate)
            throws JSONException {
        String dobString = womanClient.get(birthDate);
        if (StringUtils.isNotBlank(dobString)) {
            Date dob = Utils.dobStringToDate(dobString);
            if (dob != null) {
                jsonObject.put(FPJsonFormUtils.VALUE, DATE_FORMAT.format(dob));
            }
        }
    }

    public static void startFormForEdit(Activity context, int jsonFormActivityRequestCode, String metaData) {
        Intent intent = new Intent(context, JsonWizardFormActivity.class);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.JSON, metaData);
        intent.putExtra("form", getFormMetadata(context));
        intent.putExtra(PERFORM_FORM_TRANSLATION, true);
        context.startActivityForResult(intent, jsonFormActivityRequestCode);
    }

    private static Form getFormMetadata(Context context) {
        Form form = new Form();
        form.setHomeAsUpIndicator(R.drawable.ic_action_close);
        form.setActionBarBackground(R.color.black);
        form.setSaveLabel(context.getResources().getString(R.string.save_label));
        return form;
    }

    public static Triple<Boolean, Event, Event> saveRemovedFromFPRegister(AllSharedPreferences allSharedPreferences, String jsonString, String providerId) {
        try {
            boolean isDeath = false;
            Triple<Boolean, JSONObject, JSONArray> registrationFormParams = validateParameters(jsonString);

            if (!registrationFormParams.getLeft()) {
                return null;
            }

            JSONObject jsonForm = registrationFormParams.getMiddle();
            JSONArray fields = registrationFormParams.getRight();

            String encounterType = FPJsonFormUtils.getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = FPJsonFormUtils.getJSONObject(jsonForm, METADATA);

            String encounterLocation = null;

            try {
                encounterLocation = metadata.getString(ConstantsUtils.JsonFormKeyUtils.ENCOUNTER_LOCATION);
            } catch (JSONException e) {
                Timber.e(e, "JsonFormUtils --> saveRemovedFromFPRegister --> getEncounterLocation");
            }

            Date encounterDate = new Date();
            String entityId = FPJsonFormUtils.getString(jsonForm, FPJsonFormUtils.ENTITY_ID);

            Event event = (Event) new Event().withBaseEntityId(entityId) //should be different for main and subform
                    .withEventDate(encounterDate).withEventType(encounterType).withLocationId(encounterLocation)
                    .withProviderId(providerId).withEntityType(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME)
                    .withFormSubmissionId(FPJsonFormUtils.generateRandomUUIDString()).withDateCreated(new Date());
            tagSyncMetadata(allSharedPreferences, event);

            for (int i = 0; i < fields.length(); i++) {
                JSONObject jsonObject = FPJsonFormUtils.getJSONObject(fields, i);

                String value = FPJsonFormUtils.getString(jsonObject, FPJsonFormUtils.VALUE);
                if (StringUtils.isNotBlank(value)) {
                    FPJsonFormUtils.addObservation(event, jsonObject);
                    if (jsonObject.get(FPJsonFormUtils.KEY).equals(ConstantsUtils.JsonFormKeyUtils.RECORD_CLOSE_REASON)) {
                        isDeath = "client_died".equalsIgnoreCase(value);
                    }
                }
            }

            Iterator<?> keys = metadata.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = FPJsonFormUtils.getJSONObject(metadata, key);
                String value = FPJsonFormUtils.getString(jsonObject, FPJsonFormUtils.VALUE);
                if (StringUtils.isNotBlank(value)) {
                    String entityVal = FPJsonFormUtils.getString(jsonObject, FPJsonFormUtils.OPENMRS_ENTITY);
                    if (entityVal != null) {
                        if (entityVal.equals(FPJsonFormUtils.CONCEPT)) {
                            FPJsonFormUtils.addToJSONObject(jsonObject, FPJsonFormUtils.KEY, key);
                            FPJsonFormUtils.addObservation(event, jsonObject);

                        } else if (entityVal.equals(FPJsonFormUtils.ENCOUNTER)) {
                            String entityIdVal = FPJsonFormUtils.getString(jsonObject, FPJsonFormUtils.OPENMRS_ENTITY_ID);
                            if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                Date eDate = FPJsonFormUtils.formatDate(value, false);
                                if (eDate != null) {
                                    event.setEventDate(eDate);
                                }
                            }
                        }
                    }
                }
            }

            //Update Child Entity to include death date
            Event updateChildDetailsEvent =
                    (Event) new Event().withBaseEntityId(entityId) //should be different for main and subform
                            .withEventDate(encounterDate).withEventType(ConstantsUtils.EventTypeUtils.UPDATE_REGISTRATION)
                            .withLocationId(encounterLocation).withProviderId(providerId)
                            .withEntityType(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME).withFormSubmissionId(FPJsonFormUtils.generateRandomUUIDString())
                            .withDateCreated(new Date());
            tagSyncMetadata(allSharedPreferences, updateChildDetailsEvent);

            return Triple.of(isDeath, event, updateChildDetailsEvent);
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> saveRemovedFromFPRegister");
        }
        return null;
    }

    public static void launchFPCloseForm(Activity activity) {
        try {
            Intent intent = new Intent(activity, JsonWizardFormActivity.class);
            intent.putExtra(PERFORM_FORM_TRANSLATION,true);
            intent.putExtra("no_parms", false);
            intent.putExtra("form", getFormMetadata(activity));
            JSONObject form = FormUtils.getInstance(activity).getFormJson(ConstantsUtils.JsonFormUtils.FP_CLOSE);
            if (form != null) {
                form.put(ConstantsUtils.JsonFormKeyUtils.ENTITY_ID,
                        activity.getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID));
                intent.putExtra(ConstantsUtils.IntentKeyUtils.JSON, form.toString());
                activity.startActivityForResult(intent, FPJsonFormUtils.REQUEST_CODE_GET_JSON);
            }
        } catch (Exception e) {
            Timber.e(e, "JsonFormUtils --> launchFPCloseForm");
        }
    }

    public static Triple<Event, Event,Event> createContactVisitEvent(Map<String, String> clientDetail) {

        try {
            String contactNo = clientDetail.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
            String contactStartDate = clientDetail.get(DBConstantsUtils.KeyUtils.VISIT_DATE);
            String baseEntityId = clientDetail.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID);

            Event contactVisitEvent = (Event) new Event().withBaseEntityId(baseEntityId).withEventDate(new Date())
                    .withEventType(ConstantsUtils.EventTypeUtils.CONTACT_VISIT).withEntityType(DBConstantsUtils.CONTACT_ENTITY_TYPE)
                    .withFormSubmissionId(FPJsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(getContactStartDate(contactStartDate));

            Event partialVisitEvent = (Event) new Event().withBaseEntityId(baseEntityId).withEventDate(new Date())
                    .withEventType(ConstantsUtils.EventTypeUtils.VISIT_FORM_JSON).withEntityType(DBConstantsUtils.CONTACT_ENTITY_TYPE)
                    .withFormSubmissionId(FPJsonFormUtils.generateRandomUUIDString())
                    .withDateCreated(getContactStartDate(contactStartDate));

            contactVisitEvent.addDetails(ConstantsUtils.CONTACT, getPreviousContact(contactNo));
            partialVisitEvent.addDetails(ConstantsUtils.CONTACT, getPreviousContact(contactNo));

            tagSyncMetadata(FPLibrary.getInstance().getContext().userService().getAllSharedPreferences(), contactVisitEvent);
            tagSyncMetadata(FPLibrary.getInstance().getContext().userService().getAllSharedPreferences(), partialVisitEvent);

            PatientRepository.updateContactVisitStartDate(baseEntityId, null);//reset contact visit date

            //Update client
            EventClientRepository db = FPLibrary.getInstance().getEventClientRepository();
            JSONObject clientForm = db.getClientByBaseEntityId(baseEntityId);

            JSONObject attributes = clientForm.getJSONObject(ConstantsUtils.JsonFormKeyUtils.ATTRIBUTES);
            attributes.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, contactNo);
            attributes.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, clientDetail.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE));
            attributes.put(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE,
                    clientDetail.get(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE));
            attributes.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, clientDetail.get(DBConstantsUtils.KeyUtils.CONTACT_STATUS));
            attributes.put(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, clientDetail.get(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT));
            attributes.put(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, clientDetail.get(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT));
            clientForm.put(ConstantsUtils.JsonFormKeyUtils.ATTRIBUTES, attributes);

            FormTag formTag = getFormTag(FPLibrary.getInstance().getContext().allSharedPreferences());
            formTag.childLocationId = LocationHelper.getInstance().getChildLocationId();
            formTag.locationId = LocationHelper.getInstance().getParentLocationId();

            db.addorUpdateClient(baseEntityId, clientForm);

            Event updateClientEvent = createUpdateClientDetailsEvent(baseEntityId);
            return Triple.of(contactVisitEvent, updateClientEvent,partialVisitEvent);

        } catch (Exception e) {
            Timber.e(e, " --> createContactVisitEvent");
            return null;
        }

    }

    private static String getPreviousContact(String contactNo) {
        int nextContact = contactNo == null ? 1 : Integer.parseInt(contactNo);
        return String.valueOf(--nextContact);
    }

    private static Date getContactStartDate(String contactStartDate) {
        try {
            return new LocalDate(contactStartDate).toDate();
        } catch (Exception e) {
            return new LocalDate().toDate();
        }
    }

    private static JSONArray getOpenTasks(String baseEntityId) {
        List<Task> openTasks = FPLibrary.getInstance().getContactTasksRepository().getOpenTasks(baseEntityId);
        JSONArray openTaskArray = new JSONArray();
        if (openTasks != null && openTasks.size() > 0) {
            for (Task task : openTasks) {
                openTaskArray.put(task.getValue());
            }
        }
        return openTaskArray;
    }

    protected static Event createUpdateClientDetailsEvent(String baseEntityId) {

        Event updateChildDetailsEvent = (Event) new Event().withBaseEntityId(baseEntityId).withEventDate(new Date())
                .withEventType(ConstantsUtils.EventTypeUtils.UPDATE_REGISTRATION).withEntityType(DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME)
                .withFormSubmissionId(FPJsonFormUtils.generateRandomUUIDString()).withDateCreated(new Date());

        FPJsonFormUtils
                .tagSyncMetadata(FPLibrary.getInstance().getContext().allSharedPreferences(), updateChildDetailsEvent);

        return updateChildDetailsEvent;
    }

    public static Event processContactFormEvent(JSONObject jsonForm, String baseEntityId) {
        AllSharedPreferences allSharedPreferences = FPLibrary.getInstance().getContext().allSharedPreferences();
        JSONArray fields = FPJsonFormUtils.getMultiStepFormFields(jsonForm);

        String entityId = FPJsonFormUtils.getString(jsonForm, FPJsonFormUtils.ENTITY_ID);
        if (StringUtils.isBlank(entityId)) {
            entityId = baseEntityId;
        }

        String encounterType = FPJsonFormUtils.getString(jsonForm, ENCOUNTER_TYPE);
        JSONObject metadata = FPJsonFormUtils.getJSONObject(jsonForm, METADATA);

        FormTag formTag = getFormTag(allSharedPreferences);
        Event baseEvent = org.smartregister.util.JsonFormUtils
                .createEvent(fields, metadata, formTag, entityId, encounterType, DBConstantsUtils.DEMOGRAPHIC_TABLE_NAME);

        tagSyncMetadata(allSharedPreferences, baseEvent);// tag docs

        return baseEvent;
    }

    public static JSONObject readJsonFromAsset(Context context, String filePath) throws Exception {
        InputStream inputStream = context.getAssets().open(filePath + ".json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String jsonString;
        StringBuilder stringBuilder = new StringBuilder();
        while ((jsonString = reader.readLine()) != null) {
            stringBuilder.append(jsonString);
        }
        inputStream.close();
        return new JSONObject(stringBuilder.toString());
    }

    public List<ContactSummaryModel> generateNextContactSchedule(String edd, List<String> contactSchedule,
                                                                 Integer lastContactSequence) {
        List<ContactSummaryModel> contactDates = new ArrayList<>();
        Integer contactSequence = lastContactSequence;
        if (StringUtils.isNotBlank(edd)) {
            LocalDate localDate = new LocalDate(edd);
            LocalDate lmpDate = localDate.minusWeeks(ConstantsUtils.DELIVERY_DATE_WEEKS);

            for (String contactWeeks : contactSchedule) {
                contactDates.add(new ContactSummaryModel(String.format(
                        FPLibrary.getInstance().getContext().getStringResource(R.string.contact_number),
                        contactSequence++),
                        Utils.convertDateFormat(lmpDate.plusWeeks(Integer.valueOf(contactWeeks)).toDate(),
                                Utils.CONTACT_SUMMARY_DF), lmpDate.plusWeeks(Integer.valueOf(contactWeeks)).toDate(),
                        contactWeeks));
            }
        }
        return contactDates;
    }

    /**
     * Creates and populates a constraint view to add the contacts tab view instead of using recycler views which introduce
     * lots of scroll complexities
     *
     * @param data
     * @param facts
     * @param position
     * @param context
     * @return constraintLayout
     */
    @NonNull
    public ConstraintLayout createListViewItems(List<YamlConfigWrapper> data, Facts facts, int position, Context context) {
        YamlConfigItem yamlConfigItem = data.get(position).getYamlConfigItem();

        FPJsonFormUtils.Template template = getTemplate(yamlConfigItem.getTemplate());
        String output = "";
        if (!TextUtils.isEmpty(template.detail)) {
            output = Utils.fillTemplate(template.detail, facts);
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout constraintLayout =
                (ConstraintLayout) inflater.inflate(R.layout.previous_contacts_preview_row, null);
        TextView sectionDetailTitle = constraintLayout.findViewById(R.id.overview_section_details_left);
        TextView sectionDetails = constraintLayout.findViewById(R.id.overview_section_details_right);


        sectionDetailTitle.setText(template.title);
        sectionDetails.setText(output);//Perhaps refactor to use Json Form Parser Implementation

        if (FPLibrary.getInstance().getFPRulesEngineHelper().getRelevance(facts, yamlConfigItem.getIsRedFont())) {
            sectionDetailTitle.setTextColor(context.getResources().getColor(R.color.overview_font_red));
            sectionDetails.setTextColor(context.getResources().getColor(R.color.overview_font_red));
        } else {
            sectionDetailTitle.setTextColor(context.getResources().getColor(R.color.overview_font_left));
            sectionDetails.setTextColor(context.getResources().getColor(R.color.overview_font_right));
        }

        sectionDetailTitle.setVisibility(View.VISIBLE);
        sectionDetails.setVisibility(View.VISIBLE);
        return constraintLayout;
    }

    public Template getTemplate(String rawTemplate) {
        Template template = new Template();

        if (rawTemplate.contains(":")) {
            String[] templateArray = rawTemplate.split(":");
            if (templateArray.length == 1) {
                template.title = templateArray[0].trim();
            } else if (templateArray.length > 1) {
                template.title = templateArray[0].trim();
                template.detail = templateArray[1].trim();
            }
        } else {
            template.title = rawTemplate;
            template.detail = "Yes";
        }

        return template;
    }

    public class Template {
        public String title = "";
        public String detail = "";
    }
}
