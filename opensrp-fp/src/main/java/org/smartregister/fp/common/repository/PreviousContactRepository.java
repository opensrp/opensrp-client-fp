package org.smartregister.fp.common.repository;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.model.PreviousContactsSummaryModel;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.repository.BaseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class PreviousContactRepository extends BaseRepository {
    public static final String TABLE_NAME = "previous_contact";
    public static final String ID = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String CONTACT_NO = "contact_no";
    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String CREATED_AT = "created_at";
    public static final String GEST_AGE = "gest_age_openmrs";
    private static final String TAG = PreviousContactRepository.class.getCanonicalName();
    private static final String CREATE_TABLE_SQL = "CREATE TABLE " + TABLE_NAME + "("
            + ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + CONTACT_NO + "  VARCHAR NOT NULL, "
            + BASE_ENTITY_ID + "  VARCHAR NOT NULL, "
            + KEY + "  VARCHAR, "
            + VALUE + "  VARCHAR NOT NULL, "
            + CREATED_AT + " INTEGER NOT NULL, " +
            "UNIQUE(" + BASE_ENTITY_ID + ", " + CONTACT_NO + ", " + KEY + ", " + VALUE + ") ON CONFLICT REPLACE)";

    private static final String INDEX_ID =
            "CREATE INDEX " + TABLE_NAME + "_" + ID + "_index ON " + TABLE_NAME + "(" + ID + " COLLATE NOCASE);";

    private static final String INDEX_BASE_ENTITY_ID = "CREATE INDEX " + TABLE_NAME + "_" + BASE_ENTITY_ID +
            "_index ON " + TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";

    private static final String INDEX_KEY = "CREATE INDEX " + TABLE_NAME + "_" + KEY +
            "_index ON " + TABLE_NAME + "(" + KEY + " COLLATE NOCASE);";

    private static final String INDEX_CONTACT_NO = "CREATE INDEX " + TABLE_NAME + "_" + CONTACT_NO +
            "_index ON " + TABLE_NAME + "(" + CONTACT_NO + " COLLATE NOCASE);";

    private String[] projectionArgs = new String[]{ID, CONTACT_NO, KEY, VALUE, BASE_ENTITY_ID, CREATED_AT};

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_SQL);
        database.execSQL(INDEX_ID);
        database.execSQL(INDEX_BASE_ENTITY_ID);
        database.execSQL(INDEX_KEY);
        database.execSQL(INDEX_CONTACT_NO);
    }

    public void savePreviousContact(PreviousContact previousContact) {
        if (previousContact == null) return;
        previousContact.setVisitDate(Utils.getDBDateToday());
        getWritableDatabase().insert(TABLE_NAME, null, createValuesFor(previousContact));
    }

    private ContentValues createValuesFor(PreviousContact PreviousContact) {
        ContentValues values = new ContentValues();
        values.put(ID, PreviousContact.getId());
        values.put(CONTACT_NO, PreviousContact.getContactNo());
        values.put(BASE_ENTITY_ID, PreviousContact.getBaseEntityId());
        values.put(VALUE, PreviousContact.getValue());
        values.put(KEY, PreviousContact.getKey());
        values.put(CREATED_AT, PreviousContact.getVisitDate());
        return values;
    }

    /**
     * @param previousContactRequest object holding contact request params it MUST contain NON NULL values for key
     *                               baseEntityId contactNo
     */
    public PreviousContact getPreviousContact(PreviousContact previousContactRequest) {
        String selection = null;
        String orderBy = ID + " DESC";
        String[] selectionArgs = null;
        PreviousContact dbPreviousContact = null;
        Cursor mCursor = null;
        try {
            if (StringUtils.isNotBlank(previousContactRequest.getBaseEntityId()) &&
                    StringUtils.isNotBlank(previousContactRequest.getKey())) {
                selection = BASE_ENTITY_ID + " = ? " + BaseRepository.COLLATE_NOCASE + " AND " + KEY + " = ? " + BaseRepository.COLLATE_NOCASE;
                selectionArgs = new String[]{previousContactRequest.getBaseEntityId(), previousContactRequest.getKey()};
            }

            mCursor = getReadableDatabase()
                    .query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, orderBy, null);
            if (mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                dbPreviousContact = getContactResult(mCursor);
            }
        } catch (Exception e) {
            Timber.e(e, " --> getPreviousContact");

        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return dbPreviousContact;
    }

    private PreviousContact getContactResult(Cursor cursor) {
        PreviousContact previousContact = new PreviousContact();
        previousContact.setId(cursor.getLong(cursor.getColumnIndex(ID)));
        previousContact.setKey(cursor.getString(cursor.getColumnIndex(KEY)));
        previousContact.setValue(cursor.getString(cursor.getColumnIndex(VALUE)));
        previousContact.setBaseEntityId(cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)));
        previousContact.setVisitDate(cursor.getString(cursor.getColumnIndex(CREATED_AT)));

        return previousContact;
    }

    /**
     * @param baseEntityId is the Base entity Id No to filter by
     * @param keysList     an optional list of keys to query null otherwise to get all keys for that base entity id
     */
    public List<PreviousContact> getPreviousContacts(String baseEntityId, List<String> keysList) {
        String orderBy = ID + " DESC ";
        Cursor mCursor = null;
        String selection = "";
        String[] selectionArgs = null;
        List<PreviousContact> previousContacts = new ArrayList<>();
        try {
            SQLiteDatabase db = getWritableDatabase();

            if (StringUtils.isNotBlank(baseEntityId)) {
                if (keysList != null) {
                    selection = BASE_ENTITY_ID + " = ? " + BaseRepository.COLLATE_NOCASE + " AND " + KEY + " IN (?) " + BaseRepository.COLLATE_NOCASE;
                    selectionArgs = new String[]{baseEntityId, FPFormUtils.getListValuesAsString(keysList)};
                } else {
                    selection = BASE_ENTITY_ID + " = ? " + BaseRepository.COLLATE_NOCASE;
                    selectionArgs = new String[]{baseEntityId};
                }
            }

            mCursor = db.query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, orderBy, null);
            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    previousContacts.add(getContactResult(mCursor));
                }
                return previousContacts;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        return previousContacts;
    }

    public List<PreviousContactsSummaryModel> getPreviousContactsFacts(String baseEntityId) {
        List<PreviousContactsSummaryModel> previousContactFacts = new ArrayList<>();
        Cursor factsCursor = null;
        try {
            SQLiteDatabase database = getWritableDatabase();

            String selection = "";
            String orderBy = "order by abs_contact_no, contact_no,_id DESC";
            String[] selectionArgs = null;

            if (StringUtils.isNotBlank(baseEntityId)) {
                selection = "select *,  abs(" + CONTACT_NO + ") as abs_contact_no from " + TABLE_NAME + " where "
                        + BASE_ENTITY_ID + " = ? and ( " + KEY + " = ? or " + KEY + " = ? or " + KEY + " = ? or " + KEY +
                        " = ? or " + KEY + " = ? ) " + orderBy;
                selectionArgs = new String[]{baseEntityId, ConstantsUtils.ATTENTION_FLAG_FACTS, ConstantsUtils.WEIGHT_GAIN,
                        ConstantsUtils.PHYS_SYMPTOMS, ConstantsUtils.CONTACT_DATE, ConstantsUtils.GEST_AGE_OPENMRS};
            }

            factsCursor = database.rawQuery(selection, selectionArgs);
            if (factsCursor != null) {
                while (factsCursor.moveToNext()) {
                    Facts contactFacts = new Facts();
                    contactFacts.put(factsCursor.getString(factsCursor.getColumnIndex(KEY)),
                            factsCursor.getString(factsCursor.getColumnIndex(VALUE)));

                    PreviousContactsSummaryModel previousContactsSummary = new PreviousContactsSummaryModel();
                    previousContactsSummary.setContactNumber(factsCursor.getString(factsCursor.getColumnIndex(CONTACT_NO)));
                    previousContactsSummary.setCreatedAt(factsCursor.getString(factsCursor.getColumnIndex(CREATED_AT)));
                    previousContactsSummary.setVisitFacts(contactFacts);

                    previousContactFacts.add(previousContactsSummary);
                }
            }

            return previousContactFacts;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (factsCursor != null) {
                factsCursor.close();
            }
        }

        return previousContactFacts;
    }

    public Facts getPreviousContactTestsFacts(String baseEntityId) {
        Cursor mCursor = null;
        Facts previousContactsTestsFacts = new Facts();
        try {
            SQLiteDatabase db = getWritableDatabase();
            mCursor = getAllTests(baseEntityId, db);

            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    previousContactsTestsFacts.put(mCursor.getString(mCursor.getColumnIndex(KEY)),
                            mCursor.getString(mCursor.getColumnIndex(VALUE)));

                }
                return previousContactsTestsFacts;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        return previousContactsTestsFacts;
    }

    /**
     * Gets all the tests recorded in all contacts for the specific patient
     *
     * @param baseEntityId
     * @param database
     * @return
     */
    private Cursor getAllTests(String baseEntityId, SQLiteDatabase database) {
        String selection = "";
        String orderBy = ID + " DESC";
        String[] selectionArgs = null;

        if (StringUtils.isNotBlank(baseEntityId)) {
            selection = BASE_ENTITY_ID + " = ?";
            selectionArgs = new String[]{baseEntityId};
        }

        return database.query(TABLE_NAME, projectionArgs, selection, selectionArgs, KEY, null, orderBy, null);
    }

    public Facts getAllTestResultsForIndividualTest(String baseEntityId, String indicator, String dateKey) {
        String orderBy = ID + " DESC ";
        String[] selectionArgs = null;
        String selection = "";

        Cursor mCursor = null;
        Facts allTestResults = new Facts();
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (StringUtils.isNoneEmpty(baseEntityId) && StringUtils.isNoneEmpty(indicator)) {
                selection = BASE_ENTITY_ID + " = ? And ( " + KEY + " = ? OR " + KEY + " = '" + GEST_AGE + "' OR " + KEY +
                        " = ? )";
                selectionArgs = new String[]{baseEntityId, indicator, dateKey};
            }
            mCursor = db.query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, orderBy, null);

            if (mCursor != null) {
                while (mCursor.moveToNext()) {
                    String factKey =
                            mCursor.getString(mCursor.getColumnIndex(KEY)) + ":" + mCursor
                                    .getString(mCursor.getColumnIndex(CONTACT_NO));

                    allTestResults.put(factKey, mCursor.getString(mCursor.getColumnIndex(VALUE)));

                }
                return allTestResults;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        return allTestResults;
    }

    /**
     * Gets the Immediate previous contact's facts. It checks for both referral and normal contacts hence the recursion.
     *
     * @param baseEntityId  {@link String}
     * @param contactNo     {@link String}
     * @param checkNegative {@link Boolean}
     * @return previousContactsFacts {@link Facts}
     */
    public Facts getPreviousContactFacts(String baseEntityId, String contactNo, boolean checkNegative) {
        Cursor mCursor = null;
        String selection = "";
        String orderBy = "created_at DESC";
        String[] selectionArgs = null;
        Facts previousContactFacts = new Facts();
        try {
            SQLiteDatabase db = getWritableDatabase();

            if (StringUtils.isNotBlank(baseEntityId) && StringUtils.isNotBlank(contactNo)) {
                selection = BASE_ENTITY_ID + " = ? AND " + CONTACT_NO + " = ?";
                selectionArgs = new String[]{baseEntityId, getContactNo(contactNo, checkNegative)};
            }

            mCursor = db.query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, orderBy, null);

            if (mCursor != null && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    previousContactFacts.put(mCursor.getString(mCursor.getColumnIndex(KEY)),
                            mCursor.getString(mCursor.getColumnIndex(VALUE)));

                }
                previousContactFacts.put(CONTACT_NO, selectionArgs[1]);
                return previousContactFacts;
            } else if (Integer.parseInt(contactNo) > 0) {
                return getPreviousContactFacts(baseEntityId, contactNo, false);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        return previousContactFacts;
    }

    private String[] profileOverviewProjectionArgs = new String[]{ID, CONTACT_NO, KEY, VALUE, BASE_ENTITY_ID, CREATED_AT};

    public Facts getProfileOverviewDetails(String baseEntityId, String contactNo, List<String> keys) {
        Cursor mCursor = null;
        String selection = "";
        String[] selectionArgs = null;
        Facts previousContactFacts = new Facts();
        try {
            SQLiteDatabase db = getWritableDatabase();

            if (StringUtils.isNotBlank(baseEntityId) && StringUtils.isNotBlank(contactNo)) {
                selection = BASE_ENTITY_ID + " = ? AND " + CONTACT_NO + " = ? AND " + KEY + " IN(";
                selectionArgs = new String[keys.size() + 2];
                selectionArgs[0] = baseEntityId;
                selectionArgs[1] = contactNo;
                for (int i = 0; i < keys.size(); i++) {
                    selectionArgs[i + 2] = keys.get(i);
                    if (i != keys.size() - 1) selection = selection.concat("?,");
                    else selection = selection.concat("?)");
                }
            }

            mCursor = db.query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                while (mCursor.moveToNext()) {
                    previousContactFacts.put(mCursor.getString(mCursor.getColumnIndex(KEY)),
                            mCursor.getString(mCursor.getColumnIndex(VALUE)));

                }
                return previousContactFacts;
            } else if (Integer.parseInt(contactNo) > 0) {
                return getPreviousContactFacts(baseEntityId, contactNo, false);
            }
        } catch (Exception e) {
            Timber.e(e);
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }

        return previousContactFacts;
    }

    /**
     * Returns contact numbers according to the @param checkNegative. If true then it just uses the initial contact. If
     * true the it would return a previous|referral contact
     *
     * @param previousContact {@link String}
     * @param checkNegative   {@link Boolean}
     * @return contactNo {@link String}
     */
    private String getContactNo(String previousContact, boolean checkNegative) {
        String contactNo = previousContact;
        if (!TextUtils.isEmpty(previousContact) && checkNegative) {
            contactNo = "-" + (Integer.parseInt(previousContact) + 1);
        }

        return contactNo;
    }

    /**
     * Gets the last contacts Schedule
     *
     * @param baseEntityId {@link String}
     * @param contactNo    {@link String}
     * @return schedule {@link Facts}
     */
    public Facts getImmediatePreviousSchedule(String baseEntityId, String contactNo) {
        Cursor scheduleCursor = null;
        String selection = "";
        String orderBy = "created_at DESC";
        String[] selectionArgs = null;
        Facts schedule = new Facts();
        try {
            SQLiteDatabase db = getWritableDatabase();

            if (StringUtils.isNotBlank(baseEntityId) && StringUtils.isNotBlank(contactNo)) {
                selection =
                        BASE_ENTITY_ID + " = ? AND " + CONTACT_NO + " = ? AND " + KEY + " = " + "'" + ConstantsUtils.CONTACT_SCHEDULE + "'";
                selectionArgs = new String[]{baseEntityId, contactNo};
            }

            scheduleCursor = db.query(TABLE_NAME, projectionArgs, selection, selectionArgs, null, null, orderBy, null);

            if (scheduleCursor != null) {
                while (scheduleCursor.moveToNext()) {
                    schedule.put(scheduleCursor.getString(scheduleCursor.getColumnIndex(KEY)),
                            scheduleCursor.getString(scheduleCursor.getColumnIndex(VALUE)));
                }
                return schedule;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        } finally {
            if (scheduleCursor != null) {
                scheduleCursor.close();
            }
        }

        return schedule;
    }

    public List<HashMap<String, String>> getVisitHistory(@NonNull  String baseEntityId) {
        List<HashMap<String, String>> data = new ArrayList<>();

        try {

            String query = "SELECT DISTINCT pc._id, pc.contact_no, pc.base_entity_id, pc.`value` as visit_date, (" +
                    "SELECT spc.value FROM previous_contact AS spc WHERE spc.contact_no = pc.contact_no AND spc.key = 'method_exit' AND spc.base_entity_id = '" + baseEntityId + "'" +
                    ") as method_exit " +
                    "FROM previous_contact AS pc " +
                    "WHERE pc.base_entity_id='" + baseEntityId + "' AND pc.key = 'visit_date' " +
                    "ORDER BY pc.`value` DESC";
            Cursor cursor = getReadableDatabase().rawQuery(query, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> historyMap = new HashMap<>();
                    historyMap.put("_id", cursor.getString(cursor.getColumnIndex("_id")));
                    historyMap.put("contact_no", cursor.getString(cursor.getColumnIndex("contact_no")));
                    historyMap.put("base_entity_id", cursor.getString(cursor.getColumnIndex("base_entity_id")));
                    historyMap.put("visit_date", cursor.getString(cursor.getColumnIndex("visit_date")));
                    historyMap.put("method_exit", cursor.getString(cursor.getColumnIndex("method_exit")));
                    data.add(historyMap);
                }
            }
        }
        catch (Exception ex) {
            Timber.e(ex);
        }

        return data;
    }

    public List<HashMap<String, String>> getLatestSterilizeContacts() {
        List<HashMap<String, String>> data = new ArrayList<>();

        try {

            String query = "SELECT pc." + DBConstantsUtils.KeyUtils.ID_LOWER_CASE + ", MAX(pc." + ConstantsUtils.CONTACT_NO + ") as " + ConstantsUtils.CONTACT_NO + ", pc." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + ", " +
                    "(SELECT ipc." + ConstantsUtils.KeyUtils.VALUE + " FROM " + TABLE_NAME + " AS ipc WHERE ipc." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = pc." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " AND ipc." + ConstantsUtils.KeyUtils.KEY + " = '" + ConstantsUtils.JsonFormFieldUtils.STERILIZATION_DATE + "') AS " + ConstantsUtils.JsonFormFieldUtils.STERILIZATION_DATE + " " +
                    "FROM " + TABLE_NAME + " AS pc " +
                    "LEFT JOIN " + FPLibrary.getInstance().getRegisterQueryProvider().getDemographicTable() + " AS ec ON ec." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " = pc." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " " +
                    "WHERE pc." + ConstantsUtils.KeyUtils.KEY + " = '" + ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT + "' AND (pc." + ConstantsUtils.KeyUtils.VALUE + " = '" + ConstantsUtils.JsonFormFieldUtils.MALE_STERILIZATION + "' OR pc." + ConstantsUtils.KeyUtils.VALUE + " = '" + ConstantsUtils.JsonFormFieldUtils.FEMALE_STERILIZATION + "') AND ec." + DBConstantsUtils.KeyUtils.ARCHIVED + " IS NULL " +
                    "GROUP BY pc." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID;
            Timber.d(query);
            Cursor cursor = getReadableDatabase().rawQuery(query, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    HashMap<String, String> historyMap = new HashMap<>();
                    historyMap.put(DBConstantsUtils.KeyUtils.ID_LOWER_CASE, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.ID_LOWER_CASE)));
                    historyMap.put(ConstantsUtils.CONTACT_NO, cursor.getString(cursor.getColumnIndex(ConstantsUtils.CONTACT_NO)));
                    historyMap.put(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, cursor.getString(cursor.getColumnIndex(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID)));
                    historyMap.put(ConstantsUtils.JsonFormFieldUtils.STERILIZATION_DATE, cursor.getString(cursor.getColumnIndex(ConstantsUtils.JsonFormFieldUtils.STERILIZATION_DATE)));
                    data.add(historyMap);
                }
            }
        }
        catch (Exception ex) {
            Timber.e(ex);
        }

        return data;
    }

    public void execRawQuery(@NonNull String query) {
        getWritableDatabase().execSQL(query);
    }
}
