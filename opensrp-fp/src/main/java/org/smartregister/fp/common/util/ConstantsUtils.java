package org.smartregister.fp.common.util;

public abstract class ConstantsUtils {
    public static final String SQLITE_DATE_TIME_FORMAT = "yyyy-MM-dd";
    public static final String CONTACT_DATE_FORMAT = "dd/MM/yyyy";
    public static final String VISIT_DATE_FORMAT = "dd-MM-yyyy";
    public static final String CONTACT_SUMMARY_DATE_FORMAT = "dd MMMM yyyy";
    public static final String VIEW_CONFIGURATION_PREFIX = "ViewConfiguration_";
    public static final String FORM = "form";
    public static final String ACCORDION_INFO_TEXT = "accordion_info_text";
    public static final String ACCORDION_INFO_TITLE = "accordion_info_title";
    public static final String DISPLAY_BOTTOM_SECTION = "display_bottom_section";
    public static final String NEXT = "next";
    public static final String YES = "yes";

    public static final String GLOBAL_IDENTIFIER = "identifier";
    public static final int DELIVERY_DATE_WEEKS = 40;
    public static final String EXPANSION_PANEL = "expansion_panel";
    public static final String EXTENDED_RADIO_BUTTON = "extended_radio_button";
    public static final String DEFAULT_VALUES = "default_values";
    public static final String PREVIOUS_CONTACT_NO = "previous_contact_no";
    public static final String GLOBAL_PREVIOUS = "global_previous";
    public static final String GLOBAL = "global";
    public static final String REQUIRED_FIELDS = "required_fields";
    public static final String EDITABLE_FIELDS = "editable_fields";
    public static final String FALSE = "false";
    public static final String DANGER_SIGNS = "danger_signs";
    public static final String DANGER_NONE = "danger_none";
    public static final String CALL = "Call";
    public static final String START_CONTACT = "Start Contact";
    public static final String CONTINUE_CONTACT = "Continue Contact";
    public static final String ORIGIN = "origin";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String EDD = "edd";
    public static final String DOB = "dob";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String ALT_CONTACT_NAME = "altContactName";
    public static final String CONTACT = "Contact";
    public static final String CURRENT_OPENSRP_ID = "current_opensrp_id";
    public static final String FORM_SUBMISSION_IDS = "form_submission_ids";
    public static final String REFERRAL = "referral";
    public static final String GEST_AGE = "gest_age";
    public static final String GEST_AGE_OPENMRS = "gest_age_openmrs";
    public static final String WOM_IMAGE = "wom_image";
    public static final String INDEX = "index";
    public static final String BOTTOM_SECTION = "bottom_section";
    public static final String DISPLAY_RECORD_BUTTON = "display_record_button";
    public static final String FILTER_OPTIONS_SOURCE = "filter_options_source";
    public static final String FILTER_OPTIONS = "filter_options";
    public static final String FILTERED_ITEMS = "filtered_items";
    public static final String PREVIOUS = "previous";
    public static final String IS_FILTERED = "is_filtered";
    public static final String CONTACT_DATE = "contact_date";
    public static final String CONTACT_SCHEDULE = "contact_schedule";
    public static final String ATTENTION_FLAG_FACTS = "attention_flag_facts";
    public static final String WEIGHT_GAIN = "weight_gain";
    public static final String PHYS_SYMPTOMS = "phys_symptoms";
    public static final String DATE_TODAY_HIDDEN = "date_today_hidden";
    public static final String AGE = "age";
    public static final String CONTACT_NO = "contact_no";
    public static final String OTHER_FOR = "other_for";
    public static final String OTHER = "other";
    public static final String CONTINUE = "Continue";
    public static final String DUE = "Due";
    public static final String ANDROID_SWITCHER = "android:switcher:";
    public static final String FORM_STATE = "form_state";
    public static final String CURRENT_CONTACT_NO = "current_contact_no";

    public interface Properties {
        String CAN_SAVE_SITE_INITIAL_SETTING = "CAN_SAVE_INITIAL_SITE_SETTING";
    }

    public interface TemplateUtils {
        interface SiteCharacteristics {
            String TEAM_ID = "teamId";
            String TEAM = "team";
            String LOCATION_ID = "locationId";
            String PROVIDER_ID = "providerId";
        }
    }

    public static class AncRadioButtonOptionTypesUtils {
        public static final String DONE_TODAY = "done_today";
        public static final String DONE_EARLIER = "done_earlier";
        public static final String ORDERED = "ordered";
        public static final String NOT_DONE = "not_done";
        public static final String DONE = "done";
    }

    public static class ConfigurationUtils {
        public static final String LOGIN = "login";
        public static final String HOME_REGISTER = "home_register";
    }

    public static class IdentifierUtils {
        public static final String ANC_ID = "ANC_ID";
    }

    public static final class EventTypeUtils {
        public static final String REGISTRATION = "Client Registration";
        public static final String UPDATE_REGISTRATION = "Edit Client Registration";
        public static final String QUICK_CHECK = "Quick Check";
        public static final String CLOSE = "Close FP Record";
        public static final String SITE_CHARACTERISTICS = "Site Characteristics";
        public static final String CONTACT_VISIT = "Contact Visit";
        public static final String VISIT_FORM_JSON = "Visit Form Json";
    }

    public static class JsonFormUtils {
        public static final String FP_REGISTER = "fp_register";
        public static final String FP_CLOSE = "fp_close";
        public static final String ANC_PROFILE = "fp_profile";
        public static final String ANC_PROFILE_ENCOUNTER_TYPE = "Profile";
        public static final String ANC_SYMPTOMS_FOLLOW_UP = "fp_symptoms_follow_up";
        public static final String ANC_PHYSICAL_EXAM = "fp_physical_exam";
        public static final String ANC_TEST = "fp_test";
        public static final String ANC_COUNSELLING_TREATMENT = "fp_counselling_treatment";
        public static final String ANC_TEST_ENCOUNTER_TYPE = "Tests";
        public static final String ANC_COUNSELLING_TREATMENT_ENCOUNTER_TYPE = "Counselling and Treatment";
        public static final String ANC_SITE_CHARACTERISTICS = "fp_site_characteristics";
        public static final String FP_START_VISIT = "fp_start_visit";
        public static final String ANC_TEST_TASKS = "fp_test_tasks";
    }

    public static class JsonFormKeyUtils {
        public static final String ENTITY_ID = "entity_id";
        public static final String OPTIONS = "options";
        public static final String ENCOUNTER_LOCATION = "encounter_location";
        public static final String ENCOUNTER_TYPE = "encounter_type";
        public static final String ATTRIBUTES = "attributes";
        public static final String DEATH_DATE = "deathdate";
        public static final String DEATH_DATE_APPROX = "deathdateApprox";
        public static final String RECORD_CLOSE_REASON = "record_close_reason";
        public static final String DOB_ENTERED = "dob_entered";
        public static final String AGE_ENTERED = "age_entered";
        //
        public static final String CLIENT_ID = "client_id";
        public static final String ADOLESCENT_NOTE = "adolescent_note";
        public static final String STEP1 = "step1";
        public static final String FIELDS = "fields";
        public static final String STILL_ON_METHOD = "still_on_method";
        public static final String FP_ID = "client_id";
        public static final String STEP7 = "step7";
        public static final String METHOD_EXIT = "method_exit";
        public static final String IUD_INSERTION_DATE = "iud_insertion_date";
        public static final String LAST_INJECTION_DATE = "last_injection_date";
        public static final String VISIT_DATE = "visit_date";
        public static final String STERILIZATION_DATE = "sterilization_date";
    }

    public static class JsonFormFieldUtils {
        public static final String CURRENT_METHOD = "current_method";
        public static final String METHOD_EXIT = "method_exit";
        public static final String STERILIZATION_DATE = "sterilization_date";
        public static final String MALE_STERILIZATION = "male_sterilization";
        public static final String FEMALE_STERILIZATION = "female_sterilization";
    }

    public static class JsonFormExtraUtils {
        public static final String CONTACT = "contact";
        public static final String JSON = "json";
        public static final String FORM = "form";
    }

    public static class SchedulesTriggerEventsUtils {
        public static final String COPPER_BEARING_INTRAUTERINE_DEVICES = "Copper-bearing intrauterine devices (Cu-IUDs)";
        public static final String LEVONORGESTREL_IUD = "Levonorgestrel IUD (LNG-IUD)";
        public static final String DMPA_IM = "DMPA-IM (DMPA, administered intramuscularly)";
        public static final String DMPA_SC = "DMPA-SC (DMPA, administered subcutaneously)";
        public static final String NET_EN_NORETHISTERONE_ENANTHATE = "NET-EN norethisterone enanthate";
        public static final String PROGESTOGEN_ONLY_PILLS = "Progestogen-only pills (POP)";
        public static final String COMBINED_ORAL_CONTRACEPTIVES = "Combined oral contraceptives (COCs)";
        public static final String COMBINED_CONTRACEPTIVE_PATCH = "Combined contraceptive patch";
        public static final String COMBINED_CONTRACEPTIVE_VAGINAL_RING = "Combined contraceptive vaginal ring (CVR)";
        public static final String PROGESTERONE_RELEASING_VAGINAL_RING = "Progesterone-releasing vaginal ring (PVR)";
        public static final String MALE_STERILIZATION = "Male sterilization";
        public static final String FEMALE_STERILIZATION = "Female sterilization";
    }

    public static class SchedulesNonTriggerEventsUtils {
        public static final String ETONOGESTREL_ETG_ONE_RO = "Etonogestrel (ETG) one-rod";
        public static final String LEVONORGESTREL_LNG_TWO_RO = "Levonorgestrel (LNG) two-rod";
        public static final String LACTATIONAL_AMENORRHEA_METHOD_LAM = "Lactational amenorrhea method (LAM)";
        public static final String MALE_CONDOM = "Male Condoms";
        public static final String FEMALE_CONDOM = "Female Condoms";
        public static final String EMERGENCY_CONTRACEPTIVE_PILLS_ECPS = "Emergency contraceptive pills (ECPs)";
        public static final String FERTILITY_AWARENESS_BASED_METHODS_FAB = "Fertility awareness-based methods (FAB)";
        public static final String WITHDRAWAL = "Withdrawal";
    }

    public static class SchedulesTriggerDatesUtils {
        public static final String STERILIZATION_DATE = "sterilization_date";
        public static final String VISIT_DATE = "visit_date";
        public static final String LAST_INJECTION_DATE = "last_injection_date";
        public static final String IUD_INSERTION_DATE = "iud_insertion_date";
    }

    public static class ScheduleUtils {
        public static final String ONCE_OFF = "once_off";
        public static final String RECURRING = "recurring";
    }

    public static class ProfileDateStatusUtils {
        public static final int BOTH_DATE_EQUAL = 0;
        public static final int FIRST_DATE_IS_GREATER = -1;
        public static final int SECOND_DATE_IS_GREATER = 1;
        public static final int ANY_NULL_DATE = -2;
    }


    public static class PrefKeyUtils {
        public static final String SITE_CHARACTERISTICS = "site_characteristics";
        public static final String POPULATION_CHARACTERISTICS = "population_characteristics";
        public static String FORM_INVISIBLE_REQUIRED_FIELDS = "anc.invisible.req.fields";
    }

    public static final class KeyUtils {
        public static final String KEY = "key";
        public static final String VALUE = "value";
        public static final String TREE = "tree";
        public static final String DEFAULT = "default";
        public static final String PHOTO = "photo";
        public static final String AGE_ENTERED = "age_entered";
        public static final String STEP = "step";
        public static final String TYPE = "type";
        public static final String FORM = "form";
        public static final String CONTACT_NO = "contact_no";
        public static final String LAST_CONTACT_DATE = "last_contact_date";
        public static final String SECONDARY_VALUES = "secondary_values";
        public static final String PARENT_SECONDARY_KEY = "parent_secondary_key";
    }

    public static final class IntentKeyUtils {
        public static final String BASE_ENTITY_ID = "base_entity_id";
        public static final String JSON = "json";
        public static final String TO_RESCHEDULE = "to_reschedule";
        public static final String IS_REMOTE_LOGIN = "is_remote_login";
        public static final String CONTACT_NO = "contact_number";
        public static final String FORM_NAME = "form_name";
        public static final String CLIENT_MAP = "client_map";
        public static final String UNDONE_VALUES = "undone_values";
        public static final String GLOBAL = "form_global";
    }

    public static class DetailsKeyUtils {
        public static final String CONTACT_SCHEDULE = "contact_schedule";
        public static final String ATTENTION_FLAG_FACTS = "attention_flag_facts";
        public static final String PREVIOUS_CONTACTS = "previous_contacts";
    }

    public static final class AlertStatusUtils {
        public static final String DUE = "due";
        public static final String OVERDUE = "overdue";
        public static final String NOT_DUE = "not_due";
        public static final String DELIVERY_DUE = "delivery_due";
        public static final String IN_PROGRESS = "in_progress";
        public static final String EXPIRED = "expired";
        public static final String TODAY = "today";
        public static final String ACTIVE = "active";
    }

    public static class FileCategoryUtils {
        public static final String PROFILE_PIC = "profilepic";

    }

    public static class DateFormatPatternUtils {
        public static final String DD_MM_YYYY = "dd-MM-yyyy";
        public static final String FOLLOWUP_VISIT_BUTTON_FORMAT = "dd MMM yyyy";
        public static final String YYYY_MM_DD = "yyyy-MM-dd";
    }

    public static class RulesFileUtils {
        public static final String VISIT_SCHEDULE_RULES = "visit-schedule-rules.yml";
        public static final String ALERT_RULES = "alert-rules.yml";
        public static final String FP_ALERT_RULES = "fp-alert-rules.yml";

    }

    public static class EcFileUtils {
        public static final String CLIENT_CLASSIFICATION = "ec_client_classification.json";
        public static final String CLIENT_FIELDS = "ec_client_fields.json";

    }

    public static class PrefixUtils {
        public static final String PREVIOUS = "previous_";
    }

    public static class SuffixUtils {
        public static final String VALUE = "_value";
        public static final String OTHER = "_other";
        public static final String ABNORMAL = "_abnormal";
        public static final String ABNORMAL_OTHER = ABNORMAL + OTHER;
    }

    public static class BooleanUtils {
        public static final String TRUE = "true";
    }

    public static class ClientUtils {
        public static final String FP_ID = "client_id";
    }

    public static final class FormState {
        public static final String READ_ONLY = "read_only";
        public static final String EDITABLE = "editable";
    }

    public static final class MethodKeyUtil {
        public static final String CU_IUD = "cu_iud";
        public static final String LNG_IUD = "lng_iud";
        public static final String DMPA_IM = "dmpa_im";
        public static final String DMPA_SC = "dmpa_sc";
        public static final String NET_EN = "net_en";
        public static final String POP = "pop";
        public static final String COC = "coc";
        public static final String PATCH = "patch";
        public static final String CVR = "cvr";
        public static final String PVR = "pvr";
        public static final String MALE_STERILIZATION = "male_sterilization";
        public static final String FEMALE_STERILIZATION = "female_sterilization";
    }
}
