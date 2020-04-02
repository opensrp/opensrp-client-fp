package org.smartregister.fp.common.util

object DBConstantsUtils {

    const val CONTACT_ENTITY_TYPE = "contact"
    const val DEMOGRAPHIC_TABLE_NAME = "ec_client"
    const val WOMAN_DETAILS_TABLE_NAME = "ec_mother_details"

    interface RegisterTable {
        companion object {
            const val DEMOGRAPHIC = "ec_client"
            const val DETAILS = "ec_mother_details"
        }
    }

    object OrderBy {
        const val ASC = "ASC"
        const val DESC = "DESC"
    }

    object KeyUtils {
        const val ID = "_ID"
        const val ID_LOWER_CASE = "_id"
        const val STEPNAME = "stepName"
        const val NUMBER_PICKER = "number_picker"
        const val FIRST_NAME = "first_name"
        const val LAST_NAME = "last_name"
        const val BASE_ENTITY_ID = "base_entity_id"
        const val DOB = "dob" //Date Of Birth

        const val DOB_UNKNOWN = "dob_unknown"
        const val EDD = "edd"
        const val GENDER = "gender"
        const val ANC_ID = "register_id"
        const val LAST_INTERACTED_WITH = "last_interacted_with"
        const val DATE_REMOVED = "date_removed"
        const val PHONE_NUMBER = "phone_number"
        const val ALT_NAME = "alt_name"
        const val ALT_PHONE_NUMBER = "alt_phone_number"
        const val HOME_ADDRESS = "home_address"
        const val AGE = "age"
        const val REMINDERS = "reminders"
        const val RED_FLAG_COUNT = "red_flag_count"
        const val YELLOW_FLAG_COUNT = "yellow_flag_count"
        const val CONTACT_STATUS = "contact_status"
        const val PREVIOUS_CONTACT_STATUS = "previous_contact_status"
        const val NEXT_CONTACT = "next_contact"
        const val NEXT_CONTACT_DATE = "next_contact_date"
        const val LAST_CONTACT_RECORD_DATE = "last_contact_record_date"
        const val RELATIONAL_ID = "relationalid"
        const val VISIT_START_DATE = "visit_start_date"
    }
}