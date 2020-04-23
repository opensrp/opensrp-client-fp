package org.smartregister.fp.common.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.ExpansionPanelValuesModel;
import com.vijay.jsonwizard.rules.RuleConstant;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.jeasy.rules.api.Facts;
import org.jetbrains.annotations.NotNull;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.domain.Contact;
import org.smartregister.fp.common.event.BaseEvent;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PartialContact;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.model.Task;
import org.smartregister.fp.common.rule.AlertRule;
import org.smartregister.fp.features.home.repository.ContactTasksRepository;
import org.smartregister.fp.features.home.view.HomeRegisterActivity;
import org.smartregister.fp.features.profile.view.ProfileActivity;
import org.smartregister.fp.features.visit.view.StartVisitJsonFormActivity;
import org.smartregister.view.activity.DrishtiApplication;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils extends org.smartregister.util.Utils {
    public static final SimpleDateFormat DB_DF = new SimpleDateFormat(ConstantsUtils.SQLITE_DATE_TIME_FORMAT);
    public static final SimpleDateFormat CONTACT_DF = new SimpleDateFormat(ConstantsUtils.CONTACT_DATE_FORMAT);
    public static final SimpleDateFormat CONTACT_SUMMARY_DF = new SimpleDateFormat(ConstantsUtils.CONTACT_SUMMARY_DATE_FORMAT);
    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final String FACILITY = "Facility";
    public static final String HOME_ADDRESS = "Home Address";
    private static final DateTimeFormatter SQLITE_DATE_DF = DateTimeFormat.forPattern(ConstantsUtils.SQLITE_DATE_TIME_FORMAT);
    private static final String OTHER_SUFFIX = ", other]";

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(FACILITY);
    }

    public static void saveLanguage(String language) {
        Utils.getAllSharedPreferences().saveLanguagePreference(language);
        setLocale(new Locale(language));
    }

    public static void setLocale(Locale locale) {
        Resources resources = FPLibrary.getInstance().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            FPLibrary.getInstance().getApplicationContext().createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    public static String getLanguage() {
        return Utils.getAllSharedPreferences().fetchLanguagePreference();
    }

    public static void postEvent(BaseEvent event) {
        EventBus.getDefault().post(event);
    }

    public static void postStickyEvent(
            BaseEvent event) {//Each Sticky event must be manually cleaned by calling Utils.removeStickyEvent
        // after
        // handling
        EventBus.getDefault().postSticky(event);
    }

    public static void removeStickyEvent(BaseEvent event) {
        EventBus.getDefault().removeStickyEvent(event);

    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static void writePrefString(Context context, final String key, final String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static int convertDpToPx(Context context, int dp) {
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

    public static boolean isEmptyMap(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isEmptyCollection(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static String getTodaysDate() {
        return convertDateFormat(Calendar.getInstance().getTime(), DB_DF);
    }

    public static String convertDateFormat(Date date, SimpleDateFormat formatter) {

        return formatter.format(date);
    }

    @Nullable
    public static int getAttributeDrawableResource(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return typedValue.resourceId;
    }

    public static int getProfileImageResourceIdentifier() {
        return R.drawable.avatar_woman;
    }

    public static List<String> getListFromString(String stringArray) {
        List<String> stringList = new ArrayList<>();
        if (!StringUtils.isEmpty(stringArray)) {
            stringList = new ArrayList<>(
                    Arrays.asList(stringArray.substring(1, stringArray.length() - 1).replaceAll("\"", "").split(", ")));
        }
        return stringList;
    }

    /**
     * Check for the quick check form then finds whether it still has pending required fields, If it has pending fields if so
     * it redirects to the quick check page. If not pending required fields then it redirects to the main contact page
     *
     * @param baseEntityId       {@link String}
     * @param personObjectClient {@link CommonPersonObjectClient}
     * @param context            {@link Context}
     * @author martinndegwa
     */
    public static void proceedToContact(String baseEntityId, HashMap<String, String> personObjectClient, Context context) {
        try {
            baseEntityId = "ba9b0b5c-4453-4fc3-8d62-f13fbaf0a342";
            personObjectClient = new HashMap<>();
            personObjectClient.put("dob", "1995-12-18T05:00:00.000+05:00");
            personObjectClient.put("last_interacted_with", "1587569732782");
            personObjectClient.put("base_entity_id", "ba9b0b5c-4453-4fc3-8d62-f13fbaf0a342");
            personObjectClient.put("data_removed", null);
            personObjectClient.put("last_name", "Wilson");
            personObjectClient.put("_id", "ba9b0b5c-4453-4fc3-8d62-f13fbaf0a342");
            personObjectClient.put("first_name", "Jimmy");
            personObjectClient.put("relationid", null);
            personObjectClient.put("client_id", "1341502");
            personObjectClient.put(DBConstantsUtils.KeyUtils.GENDER, "Female");
            personObjectClient.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, "0");

            Intent intent = new Intent(context.getApplicationContext(), StartVisitJsonFormActivity.class);

            Contact startVisit = new Contact();
            startVisit.setName(context.getResources().getString(R.string.family_planning_visit));
            startVisit.setFormName(ConstantsUtils.JsonFormUtils.FP_START_VISIT);
            startVisit.setContactNumber(Integer.valueOf(personObjectClient.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
            //quickCheck.setBackground(R.drawable.quick_check_bg);
            startVisit.setActionBarBackground(R.color.btn_start_visit);
            startVisit.setNavigationBackground(R.color.start_visit_navigation_background);
            startVisit.setBackIcon(R.drawable.ic_clear);
            startVisit.setWizard(true);
            //quickCheck.setHideSaveLabel(true);
            Map<String, String> globals = loadGlobalConfig(context, personObjectClient, baseEntityId, Integer.valueOf(personObjectClient.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)), ConstantsUtils.JsonFormUtils.FP_START_VISIT);
            startVisit.setGlobals(globals);




            //partial contact exists?
            PartialContact partialContactRequest = new PartialContact();
            partialContactRequest.setBaseEntityId(baseEntityId);
            partialContactRequest.setContactNo(startVisit.getContactNumber());
            partialContactRequest.setType(startVisit.getFormName());

            String locationId = FPLibrary.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);

            JSONObject form = FPJsonFormUtils.getFormAsJson(startVisit.getFormName(), baseEntityId, locationId);

            JSONObject defaultGlobal = new JSONObject();
            if (!globals.isEmpty()) {
                for (Map.Entry<String, String> entry : globals.entrySet()) {
                    defaultGlobal.put(entry.getKey(), entry.getValue());
                }
            }
            form.put(JsonFormConstants.JSON_FORM_KEY.GLOBAL, defaultGlobal);

            String processedForm = FPFormUtils.getFormJsonCore(partialContactRequest, form).toString();





            intent.putExtra(ConstantsUtils.JsonFormExtraUtils.JSON, processedForm);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, startVisit);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, partialContactRequest.getBaseEntityId());
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, personObjectClient);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME, partialContactRequest.getType());
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, partialContactRequest.getContactNo());
            Activity activity = (Activity) context;
            activity.startActivityForResult(intent, FPJsonFormUtils.REQUEST_CODE_GET_JSON);


            /*if (hasPendingRequiredFields(new JSONObject(processedForm))) {

            } else {
                intent = new Intent(context, StartVisitJsonFormActivity.class);
                intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
                intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, personObjectClient);
                intent.putExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME, partialContactRequest.getType());
                intent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO,
                        Integer.valueOf(personObjectClient.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
                context.startActivity(intent);
            }*/


        } catch (Exception e) {
            Timber.e(e, " --> proceedToContact");
            Utils.showToast(context,
                    "Error proceeding to contact for client " + personObjectClient.get(DBConstantsUtils.KeyUtils.FIRST_NAME));
        }
    }

    public static Map<String, String>  loadGlobalConfig(Context context, HashMap<String, String> personObjectClient, String baseEntityId, int contactNo, String formName) {
        Map<String, String> globals = new HashMap<>();
        Set<String> defaultValueFields = new HashSet<>();
        JSONObject mainJson;
        try {
            mainJson = FPJsonFormUtils.readJsonFromAsset(context.getApplicationContext(), "json.form/" + formName);
            if (mainJson.has(ConstantsUtils.DEFAULT_VALUES)) {
                JSONArray defaultValuesArray = mainJson.getJSONArray(ConstantsUtils.DEFAULT_VALUES);
                defaultValueFields.addAll(getListValues(defaultValuesArray));
            }

            Map<String, List<String>> formGlobalKeys = new HashMap<>();
            Set<String> globalKeys = new HashSet<>();
            loadContactGlobalsConfig(formGlobalKeys, globalKeys, context);

            Map<String, String> formGlobalValues = new HashMap<>();
            if (contactNo > 1) {
                for (String item : defaultValueFields) {
                    if (globalKeys.contains(item)) {
                        formGlobalValues.put(item, getMapValue(item, baseEntityId, contactNo));
                    }
                }
            }

            List<String> contactGlobals = formGlobalKeys.get(formName);

            if (contactGlobals != null) {

                for (String contactGlobal : contactGlobals) {
                    if (formGlobalValues.containsKey(contactGlobal)) {
                        String some = globals.get(contactGlobal);

                        if (some == null || !some.equals(formGlobalValues.get(contactGlobal))) {
                            globals.put(contactGlobal, formGlobalValues.get(contactGlobal));
                        }

                    } else {
                        globals.put(contactGlobal, "");
                    }
                }

                //Inject some form defaults from client details
                globals.put(ConstantsUtils.KeyUtils.CONTACT_NO, String.valueOf(contactNo));
                globals.put(ConstantsUtils.PREVIOUS_CONTACT_NO, contactNo > 1 ? String.valueOf(contactNo - 1) : "0");
                globals.put(DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE, personObjectClient.get(DBConstantsUtils.KeyUtils.GENDER));


                String lastContactDate = personObjectClient.get(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE);
                globals.put(ConstantsUtils.KeyUtils.LAST_CONTACT_DATE, !TextUtils.isEmpty(lastContactDate) ? Utils.reverseHyphenSeperatedValues(lastContactDate, "-") : "");

            }

        } catch (Exception e) {
            Timber.e(e, "Error reading json from asset file ");
        }

        return globals;
    }

    public static String getMapValue(String key, String baseEntityId, int contactNo) {
        PreviousContact request = new PreviousContact();
        request.setBaseEntityId(baseEntityId);
        request.setKey(key);
        if (contactNo > 1) {
            request.setContactNo(String.valueOf(contactNo - 1));
        }

        PreviousContact previousContact = FPLibrary.getInstance().getPreviousContactRepository().getPreviousContact(request);
        return previousContact != null ? previousContact.getValue() : null;
    }

    public static void loadContactGlobalsConfig(Map<String, List<String>> formGlobalKeys, Set<String> globalKeys, Context context) throws IOException {
        Iterable<Object> contactGlobals = readYaml(FilePathUtils.FileUtils.VISIT_GLOBALS, new Yaml(), context);

        for (Object ruleObject : contactGlobals) {
            Map<String, Object> map = ((Map<String, Object>) ruleObject);
            formGlobalKeys.put(map.get(ConstantsUtils.FORM).toString(), (List<String>) map.get(JsonFormConstants.FIELDS));
            globalKeys.addAll((List<String>) map.get(JsonFormConstants.FIELDS));
        }
    }

    public static Iterable<Object> readYaml(String filename, Yaml yaml, Context context) throws IOException {
        InputStreamReader inputStreamReader =
                new InputStreamReader(context.getApplicationContext().getAssets().open((FilePathUtils.FolderUtils.CONFIG_FOLDER_PATH + filename)));
        return yaml.loadAll(inputStreamReader);
    }

    public static List<String> getListValues(JSONArray jsonArray) {
        if (jsonArray != null) {
            return FPLibrary.getInstance().getGsonInstance()
                    .fromJson(jsonArray.toString(), new TypeToken<List<String>>() {
                    }.getType());
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Checks the pending required fields on the json forms and returns true|false
     *
     * @param object {@link JSONObject}
     * @return true|false {@link Boolean}
     * @throws Exception
     * @author martinndegwa
     */
    public static boolean hasPendingRequiredFields(JSONObject object) throws Exception {
        if (object != null) {
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (key.startsWith(RuleConstant.STEP)) {
                    JSONArray stepArray = object.getJSONObject(key).getJSONArray(JsonFormConstants.FIELDS);

                    for (int i = 0; i < stepArray.length(); i++) {
                        JSONObject fieldObject = stepArray.getJSONObject(i);
                        FPFormUtils.processSpecialWidgets(fieldObject);

                        boolean isRequiredField = FPJsonFormUtils.isFieldRequired(fieldObject);
                        //Do not check for required for fields that are invisible
                        if (fieldObject.has(JsonFormConstants.IS_VISIBLE) && !fieldObject.getBoolean(JsonFormConstants.IS_VISIBLE)) {
                            isRequiredField = false;
                        }

                        if (isRequiredField && ((fieldObject.has(JsonFormConstants.VALUE) && TextUtils.isEmpty(
                                fieldObject.getString(JsonFormConstants.VALUE))) || !fieldObject.has(JsonFormConstants.VALUE))) {
                            //TO DO Remove/ Alter logical condition
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * This finalizes the form and redirects you to the contact summary page for more confirmation of the data added
     *
     * @param context {@link Activity}
     * @author martinndegwa
     */
    /*public static void finalizeForm(Activity context, HashMap<String, String> womanDetails, boolean isRefferal) {
        try {

            Intent contactSummaryFinishIntent = new Intent(context, ContactSummaryFinishActivity.class);
            contactSummaryFinishIntent
                    .putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, womanDetails.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID));
            contactSummaryFinishIntent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, womanDetails);
            contactSummaryFinishIntent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO,
                    Integer.valueOf(womanDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
            if (isRefferal) {
                int contactNo = Integer.parseInt(womanDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
                if (contactNo < 0) {
                    contactSummaryFinishIntent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, Integer.valueOf(contactNo));
                } else {
                    contactSummaryFinishIntent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, Integer.valueOf("-" + contactNo));
                }
            } else {
                contactSummaryFinishIntent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO,
                        Integer.valueOf(womanDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
            }
            context.startActivity(contactSummaryFinishIntent);
        } catch (Exception e) {
            Timber.e(e);
        }

    }*/

    public static String fillTemplate(String stringValue, Facts facts) {
        String stringValueResult = stringValue;
        while (stringValueResult.contains("{")) {
            String key = stringValueResult.substring(stringValueResult.indexOf("{") + 1, stringValueResult.indexOf("}"));
            String value = processValue(key, facts);
            stringValueResult = stringValueResult.replace("{" + key + "}", value).replaceAll(", $", "").trim();
        }
        //Remove unnecessary commas by cleaning the returned string
        return cleanValueResult(stringValueResult);
    }

    private static String processValue(String key, Facts facts) {
        String value = "";
        if (facts.get(key) instanceof String) {
            value = facts.get(key);
            if (value != null && value.endsWith(OTHER_SUFFIX)) {
                Object otherValue = value.endsWith(OTHER_SUFFIX) ? facts.get(key + ConstantsUtils.SuffixUtils.OTHER) : "";
                value = otherValue != null ?
                        value.substring(0, value.lastIndexOf(",")) + ", " + otherValue.toString() + "]" :
                        value.substring(0, value.lastIndexOf(",")) + "]";

            }
        }

        return FPFormUtils.keyToValueConverter(value);
    }

    private static String cleanValueResult(String result) {
        List<String> nonEmptyItems = new ArrayList<>();

        for (String item : result.split(",")) {
            if (item.length() > 1) {
                nonEmptyItems.add(item);
            }
        }
        //Get the first item that usually  has a colon and remove it form list, if list has one item append separator
        String itemLabel = "";
        if (!nonEmptyItems.isEmpty() && nonEmptyItems.get(0).contains(":")) {
            String[] separatedLabel = nonEmptyItems.get(0).split(":");
            itemLabel = separatedLabel[0];
            if (separatedLabel.length > 1) {
                nonEmptyItems.set(0, nonEmptyItems.get(0).split(":")[1]);
            }//replace with extracted value
        }
        return itemLabel + (!TextUtils.isEmpty(itemLabel) ? ": " : "") + StringUtils.join(nonEmptyItems.toArray(), ",");
    }

    public static void navigateToHomeRegister(Context context, boolean isRemote, Class<? extends HomeRegisterActivity> homeRegisterActivityClass) {
        Intent intent = new Intent(context, homeRegisterActivityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.IS_REMOTE_LOGIN, isRemote);
        context.startActivity(intent);
    }

    public static void navigateToProfile(Context context, HashMap<String, String> patient) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, patient.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID));
        intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, patient);
        context.startActivity(intent);
    }

    public static String getColumnMapValue(CommonPersonObjectClient pc, String key) {
        return org.smartregister.util.Utils.getValue(pc.getColumnmaps(), key, false);
    }

    public static String getDBDateToday() {
        return (new LocalDate()).toString(SQLITE_DATE_DF);
    }

    public static ButtonAlertStatus getButtonAlertStatus(Map<String, String> details, Context context, boolean isProfile) {
        String contactStatus = details.get(DBConstantsUtils.KeyUtils.CONTACT_STATUS);

        String nextContactDate = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE);
        String edd = details.get(DBConstantsUtils.KeyUtils.EDD);
        String alertStatus;
        Integer gestationAge = 0;
        if (StringUtils.isNotBlank(edd)) {
            gestationAge = Utils.getGestationAgeFromEDDate(edd);
            AlertRule alertRule = new AlertRule(gestationAge, nextContactDate);
            alertStatus =
                    StringUtils.isNotBlank(contactStatus) && ConstantsUtils.AlertStatusUtils.ACTIVE.equals(contactStatus) ?
                            ConstantsUtils.AlertStatusUtils.IN_PROGRESS : FPLibrary.getInstance().getAncRulesEngineHelper()
                            .getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);
        } else {
            alertStatus = StringUtils.isNotBlank(contactStatus) ? ConstantsUtils.AlertStatusUtils.IN_PROGRESS : "DEAD";
        }

        ButtonAlertStatus buttonAlertStatus = new ButtonAlertStatus();

        //Set text first
        String nextContactRaw = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        Integer nextContact = StringUtils.isNotBlank(nextContactRaw) ? Integer.valueOf(nextContactRaw) : 1;

        nextContactDate =
                StringUtils.isNotBlank(nextContactDate) ? Utils.reverseHyphenSeperatedValues(nextContactDate, "/") : null;

        buttonAlertStatus.buttonText = String.format(getDisplayTemplate(context, alertStatus, isProfile), nextContact, (nextContactDate != null ? nextContactDate :
                Utils.convertDateFormat(Calendar.getInstance().getTime(), Utils.CONTACT_DF)));

        alertStatus =
                Utils.processContactDoneToday(details.get(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE), alertStatus);

        buttonAlertStatus.buttonAlertStatus = alertStatus;
        buttonAlertStatus.gestationAge = gestationAge;
        buttonAlertStatus.nextContact = nextContact;
        buttonAlertStatus.nextContactDate = nextContactDate;

        return buttonAlertStatus;
    }

    public static int getGestationAgeFromEDDate(String expectedDeliveryDate) {
        try {
            if (!"0".equals(expectedDeliveryDate)) {
                LocalDate date = SQLITE_DATE_DF.withOffsetParsed().parseLocalDate(expectedDeliveryDate);
                LocalDate lmpDate = date.minusWeeks(ConstantsUtils.DELIVERY_DATE_WEEKS);
                Weeks weeks = Weeks.weeksBetween(lmpDate, LocalDate.now());
                return weeks.getWeeks();
            } else {
                return 0;
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e, " --> getGestationAgeFromEDDate");
            return 0;
        }
    }

    public static String reverseHyphenSeperatedValues(String rawString, String outputSeparator) {
        if (StringUtils.isNotBlank(rawString)) {
            String resultString = rawString;
            String[] tokenArray = resultString.trim().split("-");
            ArrayUtils.reverse(tokenArray);
            resultString = StringUtils.join(tokenArray, outputSeparator);
            return resultString;
        }
        return "";
    }

    private static String getDisplayTemplate(Context context, String alertStatus, boolean isProfile) {
        String displayTemplate;
        if (StringUtils.isNotBlank(alertStatus) && !isProfile) {
            switch (alertStatus) {
                case ConstantsUtils.AlertStatusUtils.IN_PROGRESS:
                    displayTemplate = context.getString(R.string.contact_in_progress);
                    break;
                case ConstantsUtils.AlertStatusUtils.NOT_DUE:
                    displayTemplate = context.getString(R.string.contact_number_due);
                    break;
                default:
                    displayTemplate = context.getString(R.string.contact_weeks);
                    break;
            }
        } else {
            switch (alertStatus) {
                case ConstantsUtils.AlertStatusUtils.IN_PROGRESS:
                    displayTemplate = context.getString(R.string.contact_in_progress_no_break);
                    break;
                case ConstantsUtils.AlertStatusUtils.NOT_DUE:
                    displayTemplate = context.getString(R.string.contact_number_due_no_break);
                    break;
                default:
                    displayTemplate = context.getString(R.string.contact_weeks_no_break);
                    break;
            }
        }
        return displayTemplate;
    }

    public static String processContactDoneToday(String lastContactDate, String alertStatus) {
        String result = alertStatus;

        if (!TextUtils.isEmpty(lastContactDate)) {
            try {
                result = DateUtils.isToday(DB_DF.parse(lastContactDate).getTime()) ? ConstantsUtils.AlertStatusUtils.TODAY : alertStatus;
            } catch (ParseException e) {
                Timber.e(e, " --> processContactDoneToday");
            }
        }

        return result;
    }

    public static void processButtonAlertStatus(Context context, Button dueButton, ButtonAlertStatus buttonAlertStatus) {
        Utils.processButtonAlertStatus(context, dueButton, null, buttonAlertStatus);
    }

    public static void processButtonAlertStatus(Context context, Button dueButton, TextView contactTextView,
                                                ButtonAlertStatus buttonAlertStatus) {
        if (dueButton != null) {
            dueButton.setVisibility(View.VISIBLE);
            dueButton.setText(buttonAlertStatus.buttonText);
            dueButton.setTag(R.id.GESTATION_AGE, buttonAlertStatus.gestationAge);

            if (buttonAlertStatus.buttonAlertStatus != null) {
                switch (buttonAlertStatus.buttonAlertStatus) {
                    case ConstantsUtils.AlertStatusUtils.IN_PROGRESS:
                        dueButton.setBackgroundColor(context.getResources().getColor(R.color.progress_orange));
                        dueButton.setTextColor(context.getResources().getColor(R.color.white));
                        break;
                    case ConstantsUtils.AlertStatusUtils.DUE:
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_due));
                        dueButton.setTextColor(context.getResources().getColor(R.color.vaccine_blue_bg_st));
                        break;
                    case ConstantsUtils.AlertStatusUtils.OVERDUE:
                        dueButton.setBackgroundColor(context.getResources().getColor(R.color.vaccine_red_bg_st));
                        dueButton.setTextColor(context.getResources().getColor(R.color.white));
                        break;
                    case ConstantsUtils.AlertStatusUtils.NOT_DUE:
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_not_due));
                        dueButton.setTextColor(context.getResources().getColor(R.color.dark_grey));
                        break;
                    case ConstantsUtils.AlertStatusUtils.DELIVERY_DUE:
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_due));
                        dueButton.setTextColor(context.getResources().getColor(R.color.vaccine_blue_bg_st));
                        dueButton.setText(context.getString(R.string.due_delivery));
                        break;
                    case ConstantsUtils.AlertStatusUtils.EXPIRED:
                        dueButton.setBackgroundColor(context.getResources().getColor(R.color.vaccine_red_bg_st));
                        dueButton.setTextColor(context.getResources().getColor(R.color.white));
                        dueButton.setText(context.getString(R.string.due_delivery));
                        break;
                    case ConstantsUtils.AlertStatusUtils.TODAY:
                        if (contactTextView != null) {
                            contactTextView.setText(String.format(context.getString(R.string.contact_recorded_today),
                                    Utils.getTodayContact(String.valueOf(buttonAlertStatus.nextContact))));
                            contactTextView.setPadding(2, 2, 2, 2);
                        }
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_disabled));
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_disabled));
                        dueButton.setTextColor(context.getResources().getColor(R.color.dark_grey));
                        dueButton.setText(String.format(context.getString(R.string.contact_recorded_today_no_break),
                                Utils.getTodayContact(String.valueOf(buttonAlertStatus.nextContact))));
                        break;
                    default:
                        dueButton.setBackground(context.getResources().getDrawable(R.drawable.contact_due));
                        dueButton.setTextColor(context.getResources().getColor(R.color.vaccine_blue_bg_st));
                        break;
                }

                if (contactTextView != null) {
                    contactTextView.setVisibility(View.GONE);
                    if (ConstantsUtils.AlertStatusUtils.TODAY.equals(buttonAlertStatus.buttonAlertStatus)) {
                        dueButton.setVisibility(View.GONE);
                        contactTextView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    public static Integer getTodayContact(String nextContact) {
        int todayContact = 1;
        try {
            todayContact = Integer.valueOf(nextContact) - 1;
        } catch (NumberFormatException nfe) {
            Timber.e(nfe, " --> getTodayContact");
        } catch (Exception e) {
            Timber.e(e, " --> getTodayContact");
        }

        return todayContact;
    }

    /***
     * Save to shared preference
     * @param sharedPref name of shared preference file
     * @param key key to persist
     * @param value value to persist
     */
    public static void saveToSharedPreference(String sharedPref, String key, String value) {
        SharedPreferences.Editor editor = DrishtiApplication.getInstance().getSharedPreferences(
                sharedPref, Context.MODE_PRIVATE).edit();
        editor.putString(key, value).apply();
    }

    /***
     * Save to shared preference
     * @param sharedPref name of shared preference file
     * @param key key used to retrieve the value
     */
    public static String readFromSharedPreference(String sharedPref, String key) {
        SharedPreferences sharedPreferences = DrishtiApplication.getInstance().getSharedPreferences(
                sharedPref, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    /**
     * Checks if a table exists on the table. An {@link Exception} is expected to be thrown by the sqlite
     * database in case of anything weird such as the query being wrongly executed. This method is used
     * to determine critical operations such as migrations that if not done will case data corruption.
     * It is therefore safe to let the app crash instead of handling the error. So that the developer/user
     * can fix the issue before continuing with any other operations.
     *
     * @param sqliteDatabase
     * @param tableName
     * @return
     */
    public static boolean isTableExists(@NonNull SQLiteDatabase sqliteDatabase, @NonNull String tableName) {
        Cursor cursor = sqliteDatabase.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'",
                null
        );

        int nameColumnIndex = cursor.getColumnIndexOrThrow("name");
        while (cursor.moveToNext()) {
            String name = cursor.getString(nameColumnIndex);

            if (name.equals(tableName)) {
                if (cursor != null) {
                    cursor.close();
                }

                return true;
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return false;
    }

    /**
     * Loads yaml files that contain rules for the profile displays
     *
     * @param filename {@link String}
     * @return
     * @throws IOException
     */
    public Iterable<Object> loadRulesFiles(String filename) throws IOException {
        return FPLibrary.getInstance().readYaml(filename);
    }

    /**
     * Creates the {@link Task} partial contact form.  This is done any time we update tasks.
     *
     * @param baseEntityId {@link String} - The patient base entity id
     * @param context      {@link Context} - application context
     * @param contactNo    {@link Integer} - the contact that the partial contact belongs in.
     * @param doneTasks    {@link List<Task>} - A list of all the done/completed tasks.
     */
    public void createTasksPartialContainer(String baseEntityId, Context context, int contactNo, List<Task> doneTasks) {
        try {
            if (contactNo > 0 && doneTasks != null && doneTasks.size() > 0) {
                JSONArray fields = createFieldsArray(doneTasks);

                FPFormUtils FPFormUtils = new FPFormUtils();
                JSONObject jsonForm = FPFormUtils.loadTasksForm(context);
                if (jsonForm != null) {
                    FPFormUtils.updateFormFields(jsonForm, fields);
                }

                createAndPersistPartialContact(baseEntityId, contactNo, jsonForm);
            }
        } catch (JSONException e) {
            Timber.e(e, " --> createTasksPartialContainer");
        }
    }

    @NotNull
    private JSONArray createFieldsArray(List<Task> doneTasks) throws JSONException {
        JSONArray fields = new JSONArray();
        for (Task task : doneTasks) {
            JSONObject field = new JSONObject(task.getValue());
            fields.put(field);
        }
        return fields;
    }

    private void createAndPersistPartialContact(String baseEntityId, int contactNo, JSONObject jsonForm) {
        Contact contact = new Contact();
        contact.setJsonForm(String.valueOf(jsonForm));
        contact.setContactNumber(contactNo);
        contact.setFormName(ConstantsUtils.JsonFormUtils.ANC_TEST_TASKS);

        FPFormUtils.persistPartial(baseEntityId, contact);
    }

    /**
     * Returns the Contact Tasks Repository {@link ContactTasksRepository}
     *
     * @return contactTasksRepository
     */
    public ContactTasksRepository getContactTasksRepositoryHelper() {
        return FPLibrary.getInstance().getContactTasksRepository();
    }

    /**
     * Displays the extra info on the expansion panel widget.
     *
     * @param view {@link View}
     */
    public static void infoAlertDialog(View view) {
        Context context = ((Context) view.getTag(R.id.accordion_context));
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context, R.style.AppThemeAlertDialog);
        builderSingle.setTitle((String) view.getTag(R.id.accordion_info_title));
        builderSingle.setMessage((String) view.getTag(R.id.accordion_info_text));
        builderSingle.setIcon(com.vijay.jsonwizard.R.drawable.dialog_info_filled);
        builderSingle.setNegativeButton(context.getResources().getString(R.string.ok),
                (dialog, which) -> dialog.dismiss());

        builderSingle.show();
    }

    /**
     * Creates the new updated tasks with the the new values
     *
     * @param taskValue {@link JSONObject}
     * @param task      {@link Task}
     * @return task {@link Task}
     */
    public static Task createTask(JSONObject taskValue, Task task) {
        Task newTask = new Task();
        newTask.setId(task.getId());
        newTask.setBaseEntityId(task.getBaseEntityId());
        newTask.setKey(task.getKey());
        newTask.setValue(String.valueOf(taskValue));
        newTask.setUpdated(true);
        newTask.setComplete(FPJsonFormUtils.checkIfTaskIsComplete(taskValue));
        newTask.setCreatedAt(Calendar.getInstance().getTimeInMillis());
        return newTask;
    }

    /**
     * Removes the task values & sets it to empty.
     *
     * @param taskValue {@link JSONObject}
     * @return task {@link JSONObject}
     */
    public static JSONObject removeTestResults(JSONObject taskValue) {
        JSONObject task = new JSONObject();
        if (taskValue != null && taskValue.has(JsonFormConstants.VALUE)) {
            taskValue.remove(JsonFormConstants.VALUE);
            task = taskValue;
        }
        return task;
    }

    /**
     * Returns the expansion panel values which were selected from the forms.
     *
     * @param taskValue {@link JSONObject}
     * @param taskKey   {@link String}
     * @return values {@link JSONArray}
     */
    public static JSONArray getExpansionPanelValues(JSONObject taskValue, String taskKey) {
        JSONArray values = new JSONArray();
        if (taskValue != null && StringUtils.isNotBlank(taskKey)) {
            JSONArray taskValueArray = new JSONArray();
            taskValueArray.put(taskValue);
            values = new FPFormUtils().loadExpansionPanelValues(taskValueArray, taskKey);
        }

        return values;
    }



    /**
     * Returns a map of the expansion panel values
     *
     * @param secondaryValues {@link JSONArray}
     * @return expansionPanelValuesMap = {@link Map}
     */
    public static Map<String, ExpansionPanelValuesModel> getSecondaryValues(JSONArray secondaryValues) {
        Map<String, ExpansionPanelValuesModel> stringExpansionPanelValuesModelMap = new HashMap<>();
        if (secondaryValues != null && secondaryValues.length() > 0) {
            stringExpansionPanelValuesModelMap = new FPFormUtils().createSecondaryValuesMap(secondaryValues);
        }
        return stringExpansionPanelValuesModelMap;
    }

    /**
     * Loads the sub forms using the name on the accordion.  It returns the sub form fields
     *
     * @param taskValue {@link JSONObject}
     * @param context   {@link Context}
     * @return fields  {@link JSONArray}
     */
    public static JSONArray loadSubFormFields(JSONObject taskValue, Context context) {
        JSONArray fields = new JSONArray();
        try {
            if (taskValue != null && taskValue.has(JsonFormConstants.CONTENT_FORM)) {
                String subFormName = taskValue.getString(JsonFormConstants.CONTENT_FORM);
                JSONObject subForm = FPFormUtils.getSubFormJson(subFormName, "", context);
                if (subForm.has(JsonFormConstants.CONTENT_FORM)) {
                    fields = subForm.getJSONArray(JsonFormConstants.CONTENT_FORM);
                }
            }
        } catch (JSONException e) {
            Timber.e(e, " --> loadSubFormFields");
        } catch (Exception e) {
            Timber.e(e, " --> loadSubFormFields");
        }
        return fields;
    }



    /**
     * Get the form title for the accordion text
     *
     * @param taskValue {@link JSONObject}
     * @return title {@link String}
     */
    public static String getFormTitle(JSONObject taskValue) {
        String title = "";
        if (taskValue != null && taskValue.has(JsonFormConstants.TEXT)) {
            title = taskValue.optString(JsonFormConstants.TEXT);
        }
        return title;
    }

    /**
     * Updates the form step1 title to match the test header
     *
     * @param form  {@link JSONObject}
     * @param title {@link String}
     */
    public static void updateFormTitle(JSONObject form, String title) {
        try {
            if (form != null && StringUtils.isNotBlank(title) && form.has(JsonFormConstants.STEP1)) {
                JSONObject stepOne = form.getJSONObject(JsonFormConstants.STEP1);
                stepOne.put(JsonFormConstants.STEP_TITLE, title);
            }
        } catch (JSONException e) {
            Timber.e(e, " --> updateFormTitle");
        }
    }

    public static boolean isCheckboxValueEmpty(JSONObject fieldObject) throws JSONException {
        if (!fieldObject.has(JsonFormConstants.VALUE)) {
            return true;
        }
        String currentValue = fieldObject.getString(JsonFormConstants.VALUE);
        return TextUtils.equals(currentValue, "[]") || (currentValue.length() == 2
                && currentValue.startsWith("[") && currentValue.endsWith("]"));
    }
}
