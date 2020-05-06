package org.smartregister.fp.features.home.repository;

import android.content.ContentValues;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.fp.common.domain.WomanDetail;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.schedules.SchedulesEnum;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.Calendar;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 14/07/2018.
 */
public class PatientRepository extends BaseRepository {
    private static final String[] projection =
            new String[]{DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, DBConstantsUtils.KeyUtils.CLIENT_ID, DBConstantsUtils.KeyUtils.CLIENT_ID_NOTE,
                    DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME, DBConstantsUtils.KeyUtils.DOB,
                    DBConstantsUtils.KeyUtils.AGE_ENTERED, DBConstantsUtils.KeyUtils.DOB_UNKNOWN, DBConstantsUtils.KeyUtils.REGISTRATION_DATE,
                    DBConstantsUtils.KeyUtils.REFERRAL, DBConstantsUtils.KeyUtils.REFERRED_BY, DBConstantsUtils.KeyUtils.UNIVERSAL_ID,
                    DBConstantsUtils.KeyUtils.AGE_FROM_DOB, /*DBConstantsUtils.KeyUtils.DOB_ENTERED,*/ DBConstantsUtils.KeyUtils.DOB_FROM_AGE,
                    DBConstantsUtils.KeyUtils.GENDER, DBConstantsUtils.KeyUtils.BIOLOGICAL_SEX,
                    DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE, DBConstantsUtils.KeyUtils.MARITAL_STATUS, DBConstantsUtils.KeyUtils.ADMIN_AREA,
                    DBConstantsUtils.KeyUtils.CLIENT_ADDRESS, DBConstantsUtils.KeyUtils.TEL_NUMBER, DBConstantsUtils.KeyUtils.COMM_CONSENT,
                    DBConstantsUtils.KeyUtils.REMINDER_MESSAGE, DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, DBConstantsUtils.KeyUtils.DATE_REMOVED,
                    DBConstantsUtils.KeyUtils.CONTACT_STATUS, DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS,
                    DBConstantsUtils.KeyUtils.NEXT_CONTACT, DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE,
                    DBConstantsUtils.KeyUtils.VISIT_START_DATE, DBConstantsUtils.KeyUtils.RED_FLAG_COUNT,
                    DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE,
                    DBConstantsUtils.KeyUtils.EDD};

    public static HashMap<String, String> getClientProfileDetails(String baseEntityId) {
        Cursor cursor = null;

        HashMap<String, String> detailsMap = null;
        try {
            SQLiteDatabase db = getMasterRepository().getReadableDatabase();
            String query =
                    "SELECT " + StringUtils.join(projection, ",") + " FROM "
                            + FPLibrary.getInstance().getRegisterQueryProvider().getDemographicTable()
                            + " WHERE " +
                            DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{baseEntityId});
            if (cursor != null && cursor.moveToFirst()) {
                String nextContact = cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
                detailsMap = new HashMap<>();
                detailsMap.put(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID)));
                detailsMap.put(DBConstantsUtils.KeyUtils.CLIENT_ID, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.CLIENT_ID)));
                detailsMap.put(DBConstantsUtils.KeyUtils.CLIENT_ID_NOTE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.CLIENT_ID_NOTE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.FIRST_NAME, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.FIRST_NAME)));
                detailsMap.put(DBConstantsUtils.KeyUtils.LAST_NAME, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.LAST_NAME)));
                detailsMap.put(DBConstantsUtils.KeyUtils.DOB, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DOB)));
                detailsMap.put(DBConstantsUtils.KeyUtils.AGE_ENTERED, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.AGE_ENTERED)));
                detailsMap.put(DBConstantsUtils.KeyUtils.DOB_UNKNOWN, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DOB_UNKNOWN)));
                detailsMap.put(DBConstantsUtils.KeyUtils.REGISTRATION_DATE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.REGISTRATION_DATE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.REFERRAL, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.REFERRAL)));
                detailsMap.put(DBConstantsUtils.KeyUtils.REFERRED_BY, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.REFERRED_BY)));
                detailsMap.put(DBConstantsUtils.KeyUtils.UNIVERSAL_ID, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.UNIVERSAL_ID)));
                detailsMap.put(DBConstantsUtils.KeyUtils.AGE_FROM_DOB, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.AGE_FROM_DOB)));
                //detailsMap.put(DBConstantsUtils.KeyUtils.DOB_ENTERED, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DOB_ENTERED)));
                detailsMap.put(DBConstantsUtils.KeyUtils.DOB_FROM_AGE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DOB_FROM_AGE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.GENDER, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.GENDER)));
                detailsMap.put(DBConstantsUtils.KeyUtils.BIOLOGICAL_SEX, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.BIOLOGICAL_SEX)));
                detailsMap.put(DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.MARITAL_STATUS, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.MARITAL_STATUS)));
                detailsMap.put(DBConstantsUtils.KeyUtils.ADMIN_AREA, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.ADMIN_AREA)));
                detailsMap.put(DBConstantsUtils.KeyUtils.CLIENT_ADDRESS, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.CLIENT_ADDRESS)));
                detailsMap.put(DBConstantsUtils.KeyUtils.TEL_NUMBER, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.TEL_NUMBER)));
                detailsMap.put(DBConstantsUtils.KeyUtils.COMM_CONSENT, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.COMM_CONSENT)));
                detailsMap.put(DBConstantsUtils.KeyUtils.REMINDER_MESSAGE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.REMINDER_MESSAGE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH)));
                detailsMap.put(DBConstantsUtils.KeyUtils.DATE_REMOVED, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.DATE_REMOVED)));
                detailsMap.put(DBConstantsUtils.KeyUtils.EDD, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.EDD)));
                detailsMap.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.CONTACT_STATUS)));
                detailsMap.put(DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS)));
                detailsMap.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, nextContact == null ? "1" : nextContact);
                detailsMap.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.VISIT_START_DATE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.VISIT_START_DATE)));
                detailsMap.put(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT)));
                detailsMap.put(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT)));
                detailsMap.put(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE)));
            }
            return detailsMap;
        } catch (Exception e) {
            Timber.e(e, "%s ==> getClientProfileDetails()", PatientRepository.class.getCanonicalName());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    protected static Repository getMasterRepository() {
        return DrishtiApplication.getInstance().getRepository();
    }

    private static RegisterQueryProvider getRegisterQueryProvider() {
        return FPLibrary.getInstance().getRegisterQueryProvider();
    }

    public static void updateWomanAlertStatus(String baseEntityId, String alertStatus) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, alertStatus);

        getMasterRepository().getWritableDatabase()
                .update(getRegisterQueryProvider().getDemographicTable(), contentValues, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                        new String[]{baseEntityId});

        updateLastInteractedWith(baseEntityId);
    }

    private static void updateLastInteractedWith(String baseEntityId) {
        ContentValues lastInteractedWithContentValue = new ContentValues();

        lastInteractedWithContentValue.put(DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, Calendar.getInstance().getTimeInMillis());

        getMasterRepository().getWritableDatabase().update(getRegisterQueryProvider().getDemographicTable(), lastInteractedWithContentValue, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                new String[]{baseEntityId});
    }

    public static void updateContactVisitDetails(WomanDetail patientDetail, boolean isFinalize) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, patientDetail.getNextContact());
        contentValues.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, patientDetail.getNextContactDate());
        contentValues.put(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, patientDetail.getYellowFlagCount());
        contentValues.put(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, patientDetail.getRedFlagCount());
        contentValues.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, patientDetail.getContactStatus());
        if (isFinalize) {
            contentValues.put(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE, Utils.DB_DF.format(Calendar.getInstance().getTime()));
            contentValues.put(DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS, patientDetail.getContactStatus());
        }

        getMasterRepository().getWritableDatabase().update(getRegisterQueryProvider().getDemographicTable(), contentValues, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                new String[]{patientDetail.getBaseEntityId()});

        updateLastInteractedWith(patientDetail.getBaseEntityId());
    }

    public static void updateEDDDate(String baseEntityId, String edd) {

        ContentValues contentValues = new ContentValues();
        if (edd != null) {
            contentValues.put(DBConstantsUtils.KeyUtils.EDD, edd);
        } else {
            contentValues.putNull(DBConstantsUtils.KeyUtils.EDD);
        }
        getMasterRepository().getWritableDatabase()
                .update(getRegisterQueryProvider().getDemographicTable(), contentValues, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                        new String[]{baseEntityId});
    }

    public static void updateContactVisitStartDate(String baseEntityId, String contactVisitStartDate) {

        ContentValues contentValues = new ContentValues();
        if (contactVisitStartDate != null) {
            contentValues.put(DBConstantsUtils.KeyUtils.VISIT_START_DATE, contactVisitStartDate);
        } else {
            contentValues.putNull(DBConstantsUtils.KeyUtils.VISIT_START_DATE);
        }
        getMasterRepository().getWritableDatabase()
                .update(getRegisterQueryProvider().getDemographicTable(), contentValues, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                        new String[]{baseEntityId});
    }

    public static void updateNextContactDate(String nextContactDate, String baseEntityId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, nextContactDate);
        getMasterRepository().getWritableDatabase().update(
                CommonFtsObject.searchTableName(getRegisterQueryProvider().getDemographicTable()),
                contentValues, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = ?",
                new String[]{baseEntityId});
    }

}
