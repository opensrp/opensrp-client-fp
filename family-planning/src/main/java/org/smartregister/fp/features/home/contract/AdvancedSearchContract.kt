package org.smartregister.fp.features.home.contract

import android.database.Cursor
import org.smartregister.domain.Response

interface AdvancedSearchContract {

    interface Presenter : RegisterFragmentContract.Presenter {
        fun search(firstName: String?, lastName: String?, ancId: String?, edd: String?, dob: String?, phoneNumber: String?,
                   alternateContact: String?, isLocal: Boolean)
    }

    interface View : RegisterFragmentContract.View {
        fun switchViews(showList: Boolean)
        fun updateSearchCriteria(searchCriteriaString: String?)
        fun filterAndSortQuery(): String?
        fun getRawCustomQueryForAdapter(query: String?): Cursor?
    }

    interface Model : RegisterFragmentContract.Model {
        fun createEditMap(firstName: String?, lastName: String?, ancId: String?, edd: String?, dob: String?,
                          phoneNumber: String?, alternateContact: String?, isLocal: Boolean): Map<String?, String?>?

        fun createSearchString(firstName: String?, lastName: String?, ancId: String?, edd: String?, dob: String?,
                               phoneNumber: String?, alternateContact: String?): String?

        fun getMainConditionString(editMap: Map<String?, String?>?): String?
    }

    interface Interactor {
        fun search(editMap: Map<String, String>, callBack: InteractorCallBack?, fpId: String)
    }

    interface InteractorCallBack {
        fun onResultsFound(response: Response<String>, fpId: String)
    }
}