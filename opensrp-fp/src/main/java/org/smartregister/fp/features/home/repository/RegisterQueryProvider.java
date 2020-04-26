package org.smartregister.fp.features.home.repository;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.fp.common.util.DBConstantsUtils;


public class RegisterQueryProvider {

    public String getObjectIdsQuery(String mainCondition, String filters) {

        String strMainCondition = getMainCondition(mainCondition);

        String strFilters = getFilter(filters);

        if (StringUtils.isNotBlank(strFilters) && StringUtils.isBlank(strMainCondition)) {
            strFilters = String.format(" where " + getDemographicTable() + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters);
        }

        return "select " + getDemographicTable() + "." + CommonFtsObject.idColumn + " from " + getDemographicTable() + "  " +
                strMainCondition + strFilters;
    }

    private String getMainCondition(String mainCondition) {
        if (StringUtils.isNotBlank(mainCondition)) {
            return " where " + mainCondition;
        }
        return "";
    }

    private String getFilter(String filters) {
        if (StringUtils.isNotBlank(filters)) {
            return String.format(" AND " + getDemographicTable() + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters);
        }
        return "";
    }

    public String getDemographicTable() {
        return DBConstantsUtils.RegisterTable.DEMOGRAPHIC;
    }

    /*public String getDetailsTable() {
        return DBConstantsUtils.RegisterTable.DETAILS;
    }*/

    public String getCountExecuteQuery(String mainCondition, String filters) {

        String strFilters = getFilter(filters);

        if (StringUtils.isNotBlank(filters) && StringUtils.isBlank(mainCondition)) {
            strFilters = String.format(" where " + CommonFtsObject.searchTableName(getDemographicTable()) + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters);
        }

        String strMainCondition = getMainCondition(mainCondition);

        return "select count(" + getDemographicTable() + "." + CommonFtsObject.idColumn + ") from " + getDemographicTable() + "  " +
                strMainCondition + strFilters;
    }

    public String mainRegisterQuery() {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.SelectInitiateMainTable(getDemographicTable(), mainColumns());
        /*queryBuilder.customJoin(" join " + getDetailsTable()
                + " on " + getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + "= " + getDetailsTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " ");*/
        return queryBuilder.getSelectquery();
    }

    public String[] mainColumns() {
        return new String[]{getDemographicTable() + "." + DBConstantsUtils.KeyUtils.RELATIONAL_ID, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.FIRST_NAME,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.LAST_NAME, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.FP_ID,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DOB, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DATE_REMOVED,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.REGISTRATION_DATE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.REFERRAL,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.REFERRED_BY, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.UNIVERSAL_ID,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.AGE_ENTERED, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.EDD,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.AGE_FROM_DOB, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DOB_ENTERED,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DOB_FROM_AGE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.AGE,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.GENDER, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BIOLOGICAL_SEX,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.MARITAL_STATUS,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.ADMIN_AREA, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.CLIENT_ADDRESS,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.TEL_NUMBER, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.COMM_CONSENT,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.REMINDER_MESSAGE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.CLIENT_ID_NOTE,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.CONTACT_STATUS, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.NEXT_CONTACT,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.VISIT_START_DATE, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS
        };
    }
}

