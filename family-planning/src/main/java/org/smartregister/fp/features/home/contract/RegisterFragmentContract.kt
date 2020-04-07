package org.smartregister.fp.features.home.contract

import org.json.JSONArray
import org.smartregister.configurableviews.model.Field
import org.smartregister.configurableviews.model.RegisterConfiguration
import org.smartregister.configurableviews.model.ViewConfiguration
import org.smartregister.domain.Response
import org.smartregister.fp.common.cursor.AdvancedMatrixCursor
import org.smartregister.view.contract.BaseRegisterFragmentContract


interface RegisterFragmentContract {

    interface View : BaseRegisterFragmentContract.View {
        fun initializeAdapter(visibleColumns: Set<org.smartregister.configurableviews.model.View?>?)
        fun recalculatePagination(matrixCursor: AdvancedMatrixCursor?)
    }

    interface Presenter : BaseRegisterFragmentContract.Presenter {
        fun updateSortAndFilter(filterList: List<Field>?, sortField: Field?)
    }

    interface Model {
        fun defaultRegisterConfiguration(): RegisterConfiguration?
        fun getViewConfiguration(viewConfigurationIdentifier: String): ViewConfiguration?
        fun getRegisterActiveColumns(viewConfigurationIdentifier: String): Set<org.smartregister.configurableviews.model.View>
        fun countSelect(tableName: String, mainCondition: String): String
        fun mainSelect(tableName: String, mainCondition: String): String
        fun getFilterText(filterList: List<Field>?, filter: String?): String
        fun getSortText(sortField: Field?): String
        fun createEditMap(ancId: String): Map<String, String>
        fun createMatrixCursor(response: Response<String>?): AdvancedMatrixCursor
        fun getJsonArray(response: Response<String>): JSONArray?
    }
}