package org.smartregister.fp.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.utils.FormUtils;

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
import org.smartregister.fp.common.rule.FPAlertRule;
import org.smartregister.fp.features.home.repository.ContactTasksRepository;
import org.smartregister.fp.features.home.repository.PatientRepository;
import org.smartregister.fp.features.home.schedules.SchedulesEnum;
import org.smartregister.fp.features.home.schedules.model.ScheduleModel;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

import static org.smartregister.fp.common.util.ConstantsUtils.CURRENT_CONTACT_NO;
import static org.smartregister.fp.common.util.ConstantsUtils.DateFormatPatternUtils.FOLLOWUP_VISIT_BUTTON_FORMAT;
import static org.smartregister.fp.common.util.ConstantsUtils.DateFormatPatternUtils.YYYY_MM_DD;
import static org.smartregister.fp.common.util.ConstantsUtils.ProfileDateStatusUtils.ANY_NULL_DATE;
import static org.smartregister.fp.common.util.ConstantsUtils.ProfileDateStatusUtils.BOTH_DATE_EQUAL;
import static org.smartregister.fp.common.util.ConstantsUtils.ProfileDateStatusUtils.FIRST_DATE_IS_GREATER;
import static org.smartregister.fp.common.util.ConstantsUtils.ProfileDateStatusUtils.SECOND_DATE_IS_GREATER;
import static org.smartregister.fp.common.util.ConstantsUtils.RulesFileUtils.FP_ALERT_RULES;
import static org.smartregister.fp.common.util.ConstantsUtils.ScheduleUtils.ONCE_OFF;
import static org.smartregister.fp.common.util.ConstantsUtils.ScheduleUtils.RECURRING;
import static org.smartregister.fp.common.util.FPJsonFormUtils.getFieldJSONObject;

/**
 * Created by ndegwamartin on 14/03/2018.
 */

public class Utils extends org.smartregister.util.Utils {
    public static final SimpleDateFormat DB_DF = new SimpleDateFormat(ConstantsUtils.SQLITE_DATE_TIME_FORMAT);
    public static final SimpleDateFormat CONTACT_SUMMARY_DF = new SimpleDateFormat(ConstantsUtils.CONTACT_SUMMARY_DATE_FORMAT);
    public static final ArrayList<String> ALLOWED_LEVELS;
    public static final String DEFAULT_LOCATION_LEVEL = "Health Facility";
    public static final String FACILITY = "Facility";
    public static final String HOME_ADDRESS = "Home Address";
    private static final DateTimeFormatter SQLITE_DATE_DF = DateTimeFormat.forPattern(ConstantsUtils.SQLITE_DATE_TIME_FORMAT);
    private static final String OTHER_SUFFIX = ", other]";

    private static final HashMap<String, String> METHODS;
    private static final int MINIMUM_JOB_FLEX_VALUE = 5;

    static {
        ALLOWED_LEVELS = new ArrayList<>();
        ALLOWED_LEVELS.add(DEFAULT_LOCATION_LEVEL);
        ALLOWED_LEVELS.add(FACILITY);

        METHODS = new HashMap<>();
        METHODS.put("cu_iud", "Copper-bearing intrauterine devices (Cu-IUDs)");
        METHODS.put("lng_iud", "Levonorgestrel IUD (LNG-IUD)");
        METHODS.put("etg_onerod", "Etonogestrel (ETG) one-rod");
        METHODS.put("lng_tworod", "Levonorgestrel (LNG) two-rod");
        METHODS.put("dmpa_im", "DMPA-IM (DMPA, administered intramuscularly)");
        METHODS.put("dmpa_sc", "DMPA-SC (DMPA, administered subcutaneously)");
        METHODS.put("net_en", "NET-EN norethisterone enanthate");
        METHODS.put("pop", "Progestogen-only pills (POP)");
        METHODS.put("coc", "Combined oral contraceptives (COCs)");
        METHODS.put("patch", "Combined contraceptive patch");
        METHODS.put("cvr", "Combined contraceptive vaginal ring (CVR)");
        METHODS.put("pvr", "Progesterone-releasing vaginal ring (PVR)");
        METHODS.put("lam", "Lactational amenorrhea method (LAM)");
        METHODS.put("female_condoms", "Female condoms");
        METHODS.put("male_condoms", "Male condoms");
        METHODS.put("ecp", "Emergency contraceptive pills (ECPs)");
        METHODS.put("fab", "Fertility awareness-based methods (FAB)");
        METHODS.put("female_sterilization", "Female sterilization");
        METHODS.put("male_method", "Female relying on male method");
        METHODS.put("male_sterilization", "Male sterilization");
        METHODS.put("withdrawal", "Withdrawal");
        METHODS.put("female_method", "Male relying on female method");
        METHODS.put("no_method", "No method");

    }

    public static void saveLanguage(String language) {
        Utils.getAllSharedPreferences().saveLanguagePreference(language);
    }

    public static void setLocale(Locale locale) {
        Resources resources = FPLibrary.getInstance().getApplicationContext().getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        FPLibrary.getInstance().getApplicationContext().createConfigurationContext(configuration);
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
        return R.drawable.user_avatar;
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
     * Check for the visit form then finds whether it still has pending required fields, If it has pending fields if so
     * it redirects to the visit page. If not pending required fields then it redirects to the client profile
     *
     * @param baseEntityId       {@link String}
     * @param personObjectClient {@link CommonPersonObjectClient}
     * @param context            {@link Context}
     * @author martinndegwa
     */
    public static void proceedToContact(String baseEntityId, HashMap<String, String> personObjectClient, Context context) {
        try {

//            personObjectClient = PatientRepository.getClientProfileDetails(baseEntityId);

            String nextContact = personObjectClient.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
            personObjectClient.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, nextContact == null ? "1" : nextContact);


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
            HashMap<String, String> globals = loadGlobalConfig(personObjectClient, baseEntityId, Integer.valueOf(personObjectClient.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
            startVisit.setGlobals(globals);


            //partial contact exists?
            PartialContact partialContactRequest = null;
            if (personObjectClient.containsKey(CURRENT_CONTACT_NO)) {
                partialContactRequest = new PartialContact();
                partialContactRequest.setBaseEntityId(baseEntityId);
                partialContactRequest.setContactNo(Integer.parseInt(personObjectClient.get(CURRENT_CONTACT_NO)));
                partialContactRequest.setType(startVisit.getFormName());
            }

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

            if (ConstantsUtils.FormState.READ_ONLY.equals(personObjectClient.get(ConstantsUtils.FORM_STATE))) {
                JSONObject modifiedForm = new JSONObject(processedForm);
                makeTheFormReadOnly(modifiedForm, 1);
                processedForm = modifiedForm.toString();
            }

            String profileAdolescent = PatientRepository.getClientProfileAdolescent(baseEntityId);
            if (profileAdolescent != null && profileAdolescent.equals("no")) {
                JSONObject modifiedForm = new JSONObject(processedForm);
                JSONArray fields = FPJsonFormUtils.fields(modifiedForm);
                JSONObject adolescentNote = getFieldJSONObject(fields, ConstantsUtils.JsonFormKeyUtils.ADOLESCENT_NOTE);
                if (adolescentNote != null) {
                    adolescentNote.remove(FPJsonFormUtils.TYPE);
                    adolescentNote.put(FPJsonFormUtils.TYPE, FPJsonFormUtils.HIDDEN);
                }
                processedForm = modifiedForm.toString();
            }


            intent.putExtra(ConstantsUtils.JsonFormExtraUtils.JSON, processedForm);
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, startVisit);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, personObjectClient);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME, startVisit.getFormName());
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, startVisit.getContactNumber());
            intent.putExtra(ConstantsUtils.IntentKeyUtils.GLOBAL, globals);
            intent.putExtra(JsonFormConstants.PERFORM_FORM_TRANSLATION, true);
            Activity activity = (Activity) context;
            activity.startActivityForResult(intent, FPJsonFormUtils.REQUEST_CODE_GET_JSON);


        } catch (Exception e) {
            Timber.e(e, " --> proceedToContact");
            Utils.showToast(context,
                    "Error proceeding to contact for client " + personObjectClient.get(DBConstantsUtils.KeyUtils.FIRST_NAME));
        }
    }

    private static void makeTheFormReadOnly(JSONObject form, int stepNo) {

        String step = "step" + stepNo;
        if (form.has(step)) {
            try {
                JSONObject stepObject = form.getJSONObject(step);
                JSONArray fields = stepObject.getJSONArray("fields");

                for (int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    field.put("read_only", true);
                }
            } catch (JSONException ex) {
                Timber.e(ex);
            }

            makeTheFormReadOnly(form, ++stepNo);
        }
    }


    @SuppressWarnings("ConstantConditions")
    public static HashMap<String, String> loadGlobalConfig(HashMap<String, String> personObjectClient, String baseEntityId, int contactNo) {
        HashMap<String, String> globals = new HashMap<>();

        try {
            String methodExit = getMapValue(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT, baseEntityId, contactNo);

            //Inject some form defaults from client details
            globals.put(ConstantsUtils.KeyUtils.CONTACT_NO, String.valueOf(contactNo));
            globals.put(ConstantsUtils.PREVIOUS_CONTACT_NO, contactNo > 1 ? String.valueOf(contactNo - 1) : "0");
            globals.put(DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE, personObjectClient.get(DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE).toLowerCase());
            globals.put(DBConstantsUtils.KeyUtils.GENDER, personObjectClient.get(DBConstantsUtils.KeyUtils.GENDER).toLowerCase());
            globals.put(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT, methodExit == null ? "0" : methodExit);

            String lastContactDate = personObjectClient.get(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE);
            globals.put(ConstantsUtils.KeyUtils.LAST_CONTACT_DATE, !TextUtils.isEmpty(lastContactDate) ? Utils.reverseHyphenSeperatedValues(lastContactDate, "-") : "");

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

    public static Iterable<Object> readYaml(String filename, Yaml yaml, Context context) throws IOException {
        InputStreamReader inputStreamReader =
                new InputStreamReader(context.getApplicationContext().getAssets().open((FilePathUtils.FolderUtils.CONFIG_FOLDER_PATH + filename)));
        return yaml.loadAll(inputStreamReader);
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

    public static ButtonAlertStatus getButtonFollowupStatus(String triggerDate, ScheduleModel scheduleModel, String baseEntityId, String nextContactDate) {

        ButtonAlertStatus buttonAlertStatus = new ButtonAlertStatus();

        boolean isFirst = Utils.isUserFirstVisitForm(baseEntityId);
        FPAlertRule fpAlertRule = new FPAlertRule(scheduleModel, triggerDate, isFirst);

        buttonAlertStatus.buttonAlertStatus = FPLibrary.getInstance().getFPRulesEngineHelper()
                .getFPAlertStatus(fpAlertRule, FP_ALERT_RULES);

        buttonAlertStatus.nextContactDate = formatDateToPattern(nextContactDate, YYYY_MM_DD, FOLLOWUP_VISIT_BUTTON_FORMAT);
        return buttonAlertStatus;
    }

    public static String formatDateToPattern(String date, String inputFormat, String outputFormat) {
        if (StringUtils.isEmpty(date)) return "";
        SimpleDateFormat format = new SimpleDateFormat(inputFormat);
        Date newDate = null;
        try {
            newDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        format = new SimpleDateFormat(outputFormat);
        return format.format(newDate);
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

    public static void processFollowupVisitButton(Context context, Button followUpBtn, ButtonAlertStatus buttonAlertStatus, String baseEntityId, Map<String, String> pc) {
        if (followUpBtn != null) {
            followUpBtn.setVisibility(View.VISIBLE);
            if (buttonAlertStatus.buttonAlertStatus != null) {
                switch (buttonAlertStatus.buttonAlertStatus) {
                    case ConstantsUtils.AlertStatusUtils.NOT_DUE:
                        followUpBtn.setBackgroundResource(R.drawable.not_due_button_bg);
                        followUpBtn.setTextAppearance(context, R.style.followupNotDue);
                        String followupDate = context.getString(R.string.followup_date, buttonAlertStatus.nextContactDate);
                        followUpBtn.setText(followupDate);
                        break;
                    case ConstantsUtils.AlertStatusUtils.DUE:
                        followUpBtn.setBackgroundResource(R.drawable.due_button_bg);
                        followUpBtn.setTextAppearance(context, R.style.followupDue);
                        followUpBtn.setText(R.string.followup_due);
                        addFollowUpClickListener(followUpBtn, context, baseEntityId, pc);
                        break;
                    case ConstantsUtils.AlertStatusUtils.OVERDUE:
                        followUpBtn.setBackgroundResource(R.drawable.overdue_button_bg);
                        followUpBtn.setTextAppearance(context, R.style.followupOverdue);
                        followUpBtn.setText(R.string.followup_overdue);
                        addFollowUpClickListener(followUpBtn, context, baseEntityId, pc);
                        break;
                    case ConstantsUtils.AlertStatusUtils.EXPIRED:
                        followUpBtn.setVisibility(View.GONE);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void addFollowUpClickListener(Button followUpBtn, Context context, String baseEntityId, Map<String, String> pc) {
        followUpBtn.setOnClickListener(v -> proceedToContact(baseEntityId, (HashMap<String, String>) pc, context));
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

    public static boolean checkNonTriggerEvents(String methodName) {
        return !Arrays.asList(ConstantsUtils.SchedulesNonTriggerEventsUtils.ETONOGESTREL_ETG_ONE_RO,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.LEVONORGESTREL_LNG_TWO_RO,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.LACTATIONAL_AMENORRHEA_METHOD_LAM,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.MALE_CONDOM,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.FEMALE_CONDOM,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.EMERGENCY_CONTRACEPTIVE_PILLS_ECPS,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.FERTILITY_AWARENESS_BASED_METHODS_FAB,
                ConstantsUtils.SchedulesNonTriggerEventsUtils.WITHDRAWAL)
                .contains(methodName);
    }

    public static String getMethodScheduleDate(String methodName, boolean isFirst) {
        if (Utils.checkNonTriggerEvents(methodName)) {
            for (SchedulesEnum schedulesEnum : SchedulesEnum.values()) {
                if (schedulesEnum.getScheduleModel().getTriggerEventTag().equals(methodName)) {
                    ScheduleModel scheduleModel = schedulesEnum.getScheduleModel();
                    LocalDate todayDate = new LocalDate();
                    if (scheduleModel.getFrequency().equals(ONCE_OFF)) {
                        return todayDate.plusDays(scheduleModel.getNormalDays().getLeft()).toString();
                    } else if (scheduleModel.getFrequency().equals(RECURRING)) {
                        if (isFirst) {
                            return todayDate.plusDays(scheduleModel.getNormalDays().getLeft()).toString();
                        } else {
                            if (scheduleModel.getRecurringDays() != null)
                                return todayDate.plusDays(scheduleModel.getRecurringDays().getLeft()).toString();
                        }
                    }
                    break;
                }
            }
        }
        return "";
    }

    public static boolean isUserFirstVisitForm(String baseEntityId) {
        boolean isFirst;
        List<HashMap<String, String>> data = FPLibrary.getInstance().getPreviousContactRepository().getVisitHistory(baseEntityId);
        if (data != null && data.size() > 1) {

            String methodExit = data.get(data.size() - 1).get(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT);
            String methodExitPrevious = data.get(data.size() - 2).get(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT);

            if (methodExit != null && !methodExit.isEmpty() && methodExitPrevious != null && !methodExitPrevious.isEmpty()) {
                isFirst = !methodExit.equals(methodExitPrevious);
            } else isFirst = true;
        } else isFirst = true;
        return isFirst;
    }

    public static void updateBtnStartVisit(int compareTwoDatesResult, TextView btnStartFPVisit, String nextContactDate, Context context, HashMap<String, String> detailMap) {
        switch (compareTwoDatesResult) {
            case BOTH_DATE_EQUAL: {
                btnStartFPVisit.setBackgroundResource(R.drawable.btn_start_visit_due_bg);
                btnStartFPVisit.setTextAppearance(context, R.style.btnStartVisitDueStyle);
                String followupDate = context.getString(R.string.start_visit_date, nextContactDate);
                btnStartFPVisit.setText(followupDate);
                btnStartFPVisit.setOnClickListener(v -> continueToContact(detailMap, context));
                break;
            }
            case FIRST_DATE_IS_GREATER: {
                btnStartFPVisit.setBackgroundResource(R.drawable.btn_start_visit_bg);
                btnStartFPVisit.setTextAppearance(context, R.style.btnStartVisitStyle);
                String followupDate = context.getString(R.string.start_visit_date, nextContactDate);
                btnStartFPVisit.setText(followupDate);
                btnStartFPVisit.setOnClickListener(null);
                break;
            }
            case SECOND_DATE_IS_GREATER: {
                btnStartFPVisit.setBackgroundResource(R.drawable.btn_start_visit_original_due_bg);
                btnStartFPVisit.setTextAppearance(context, R.style.btnStartVisitOriginalDueStyle);
                String followupDate = context.getString(R.string.start_visit_date, nextContactDate);
                btnStartFPVisit.setText(followupDate);
                btnStartFPVisit.setOnClickListener(v -> continueToContact(detailMap, context));
                break;
            }
            case ANY_NULL_DATE: {
                btnStartFPVisit.setBackgroundResource(R.drawable.btn_start_visit_bg);
                btnStartFPVisit.setTextAppearance(context, R.style.btnStartVisitStyle);
                btnStartFPVisit.setText(context.getString(R.string.start_visit));
                btnStartFPVisit.setOnClickListener(v -> continueToContact(detailMap, context));
                break;
            }
            default: {
                btnStartFPVisit.setBackgroundResource(R.drawable.btn_start_visit_bg);
                btnStartFPVisit.setTextAppearance(context, R.style.btnStartVisitStyle);
                btnStartFPVisit.setText(context.getString(R.string.start_visit));
                btnStartFPVisit.setOnClickListener(v -> continueToContact(detailMap, context));
                break;
            }

        }
    }

    public static void continueToContact(HashMap<String, String> detailMap, Context context) {
        String baseEntityId = detailMap.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID);

        if (StringUtils.isNotBlank(baseEntityId)) {
            Utils.proceedToContact(baseEntityId, detailMap, context);
        }
    }

    public static long getFlexValue(int value) {
        int minutes = MINIMUM_JOB_FLEX_VALUE;

        if (value > MINIMUM_JOB_FLEX_VALUE) {

            minutes = (int) Math.ceil(value / 3);
        }

        return minutes < MINIMUM_JOB_FLEX_VALUE ? MINIMUM_JOB_FLEX_VALUE : minutes;
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


    public static boolean isCheckboxValueEmpty(JSONObject fieldObject) throws JSONException {
        if (!fieldObject.has(JsonFormConstants.VALUE)) {
            return true;
        }
        String currentValue = fieldObject.getString(JsonFormConstants.VALUE);
        return TextUtils.equals(currentValue, "[]") || (currentValue.length() == 2
                && currentValue.startsWith("[") && currentValue.endsWith("]"));
    }

    public static String getMethodName(String key) {
        return METHODS.containsKey(key) ? METHODS.get(key) : "";
    }

    public static String getFormattedMethodName(String key) {
        return METHODS.containsKey(key) ? METHODS.get(key) : key;
    }

    public static void openMECWheelApp(Context context) {
        String pkgName = "com.who.mecwheel";
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent == null) {
            try {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkgName));
            } catch (Exception ex) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
            }
        }
        context.startActivity(intent);
    }

    /**
     * Given two dates compare if they are equal
     *
     * @param firstDate  the first date entered
     * @param secondDate the second date entered
     * @return returns {-1} when first date occurs before second date, {0} when both dates are equal
     * {1} when second date is greater than first date and {-2} if any of the dates passed is null
     * or is empty
     */
    public static int compareTwoDates(String firstDate, String secondDate) {
        if (!TextUtils.isEmpty(firstDate) && !TextUtils.isEmpty(secondDate)) {
            Calendar dateOne = FormUtils.getDate(firstDate);
            Calendar dateTwo = FormUtils.getDate(secondDate);
            if (dateOne.before(dateTwo)) {
                return -1;
            } else if (dateOne.equals(dateTwo)) {
                return 0;
            } else {
                return 1;
            }
        }
        return -2;
    }
}
