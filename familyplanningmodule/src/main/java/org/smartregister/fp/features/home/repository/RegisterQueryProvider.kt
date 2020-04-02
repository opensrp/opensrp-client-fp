package org.smartregister.fp.features.home.repository

import org.apache.commons.lang3.StringUtils
import org.smartregister.commonregistry.CommonFtsObject
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder
import org.smartregister.fp.common.util.DBConstantsUtils

class RegisterQueryProvider {

    fun getObjectIdsQuery(mainCondition: String, filters: String) : String {
        val strMainCondition: String = getMainCondition(mainCondition)

        var strFilters: String = getFilter(filters)

        if (StringUtils.isNotBlank(strFilters) && StringUtils.isBlank(strMainCondition)) {
            strFilters = String.format(" where " + getDemographicTable() + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters)
        }

        return "select " + getDemographicTable() + "." + CommonFtsObject.idColumn + " from " + CommonFtsObject.searchTableName(getDemographicTable()) + " " + getDemographicTable() + "  " +
                "join " + getDetailsTable() + " on " + getDemographicTable() + "." + CommonFtsObject.idColumn + " =  " + getDetailsTable() + "." + "id " + strMainCondition + strFilters
    }

    private fun getMainCondition(mainCondition: String): String {
        return if (StringUtils.isNotBlank(mainCondition)) {
            " where $mainCondition"
        } else ""
    }

    private fun getFilter(filters: String): String {
        return if (StringUtils.isNotBlank(filters)) {
            String.format(" AND " + getDemographicTable() + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters)
        } else ""
    }

    fun getDemographicTable(): String {
        return DBConstantsUtils.RegisterTable.DEMOGRAPHIC
    }

    fun getDetailsTable(): String {
        return DBConstantsUtils.RegisterTable.DETAILS
    }

    fun getCountExecuteQuery(mainCondition: String?, filters: String?): String? {
        var strFilters = getFilter(filters!!)
        if (StringUtils.isNotBlank(filters) && StringUtils.isBlank(mainCondition)) {
            strFilters = String.format(" where " + CommonFtsObject.searchTableName(getDemographicTable()) + "." + CommonFtsObject.phraseColumn + " MATCH '*%s*'", filters)
        }
        val strMainCondition = getMainCondition(mainCondition!!)
        return "select count(" + getDemographicTable() + "." + CommonFtsObject.idColumn + ") from " + CommonFtsObject.searchTableName(getDemographicTable()) + " " + getDemographicTable() + "  " +
                "join " + getDetailsTable() + " on " + getDemographicTable() + "." + CommonFtsObject.idColumn + " =  " + getDetailsTable() + "." + "id " + strMainCondition + strFilters
    }

    fun mainRegisterQuery(): String? {
        val queryBuilder = SmartRegisterQueryBuilder()
        queryBuilder.SelectInitiateMainTable(getDemographicTable(), mainColumns())
        queryBuilder.customJoin(" join " + getDetailsTable()
                + " on " + getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + "= " + getDetailsTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + " ")
        return queryBuilder.selectquery
    }

    fun mainColumns(): Array<String>? {
        return arrayOf(getDemographicTable() + "." + DBConstantsUtils.KeyUtils.RELATIONAL_ID, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.FIRST_NAME,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.LAST_NAME, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.ANC_ID,
                getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DOB, getDetailsTable() + "." + DBConstantsUtils.KeyUtils.PHONE_NUMBER,
                getDetailsTable() + "." + DBConstantsUtils.KeyUtils.ALT_NAME, getDemographicTable() + "." + DBConstantsUtils.KeyUtils.DATE_REMOVED,
                getDetailsTable() + "." + DBConstantsUtils.KeyUtils.EDD, getDetailsTable() + "." + DBConstantsUtils.KeyUtils.RED_FLAG_COUNT,
                getDetailsTable() + "." + DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, getDetailsTable() + "." + DBConstantsUtils.KeyUtils.CONTACT_STATUS,
                getDetailsTable() + "." + DBConstantsUtils.KeyUtils.NEXT_CONTACT, getDetailsTable() + "." + DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE,
                getDetailsTable() + "." + DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE)
    }
}