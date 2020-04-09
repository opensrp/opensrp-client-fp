package org.smartregister.fp.features.home.view

import android.annotation.SuppressLint
import android.database.Cursor
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import org.apache.commons.lang3.StringUtils
import org.smartregister.commonregistry.CommonPersonObjectClient
import org.smartregister.configurableviews.model.Field
import org.smartregister.cursoradapter.RecyclerViewFragment
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter
import org.smartregister.cursoradapter.RecyclerViewProvider
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder
import org.smartregister.domain.FetchStatus
import org.smartregister.fp.FPLibrary
import org.smartregister.fp.R
import org.smartregister.fp.common.cursor.AdvancedMatrixCursor
import org.smartregister.fp.common.event.SyncEvent
import org.smartregister.fp.common.fragment.NoMatchDialogFragment
import org.smartregister.fp.common.helper.DBQueryHelper
import org.smartregister.fp.common.provider.RegisterProvider
import org.smartregister.fp.common.task.AttentionFlagsTask
import org.smartregister.fp.common.util.ConstantsUtils
import org.smartregister.fp.common.util.DBConstantsUtils
import org.smartregister.fp.common.util.Utils
import org.smartregister.fp.features.home.contract.RegisterFragmentContract
import org.smartregister.fp.features.home.presenter.RegisterFragmentPresenter
import org.smartregister.receiver.SyncStatusBroadcastReceiver
import org.smartregister.view.activity.BaseRegisterActivity
import org.smartregister.view.fragment.BaseRegisterFragment
import org.smartregister.view.fragment.SecuredNativeSmartRegisterFragment
import timber.log.Timber
import java.util.*

open class HomeRegisterFragment : BaseRegisterFragment(), RegisterFragmentContract.View, SyncStatusBroadcastReceiver.SyncStatusListener {

    companion object {
        const val CLICK_VIEW_NORMAL = "click_view_normal"
        const val CLICK_VIEW_ALERT_STATUS = "click_view_alert_status"
        const val CLICK_VIEW_SYNC = "click_view_sync"
        const val CLICK_VIEW_ATTENTION_FLAG = "click_view_attention_flag"
    }

    override fun initializePresenter() {
        if (activity == null) {
            return
        }
        val viewConfigurationIdentifier = (activity as BaseRegisterActivity?)!!.viewIdentifiers[0]
        presenter = RegisterFragmentPresenter(this, viewConfigurationIdentifier)
    }

    override fun setUniqueID(qrCode: String?) {
        val baseRegisterActivity = activity as BaseRegisterActivity?
        if (baseRegisterActivity != null) {
           /* val currentFragment: android.support.v4.app.Fragment = baseRegisterActivity.findFragmentByPosition(BaseRegisterActivity.ADVANCED_SEARCH_POSITION)
            if (currentFragment is AdvancedSearchFragment) {
                (currentFragment as AdvancedSearchFragment).getAncId().setText(qrCode)
            }*/
        }
    }

    override fun setAdvancedSearchFormData(formData: HashMap<String, String>?) {
        val baseRegisterActivity = activity as BaseRegisterActivity?
        if (baseRegisterActivity != null) {
            /*val currentFragment: android.support.v4.app.Fragment = baseRegisterActivity.findFragmentByPosition(BaseRegisterActivity.ADVANCED_SEARCH_POSITION)
            (currentFragment as AdvancedSearchFragment).setSearchFormData(formData)*/
        }
    }

    override fun setupViews(view: View) {
        super.setupViews(view)

        //Do not show filter button at the moment until all filters are implemented
        val filterSortRelativeLayout = view.findViewById<RelativeLayout>(R.id.filter_sort_layout)
        if (filterSortRelativeLayout != null) {
            filterSortRelativeLayout.visibility = View.GONE
        }

        val filterText = view.findViewById<View>(R.id.filter_text_view)
        filterText?.setOnClickListener(registerActionHandler)

        // Due Button
        val contactButton = view.findViewById<View>(R.id.due_button)
        contactButton?.setOnClickListener(registerActionHandler)

        //Risk view
        //val attentionFlag = view.findViewById<View>(R.id.risk)
        //attentionFlag?.setOnClickListener(registerActionHandler)

        view.findViewById<TextView>(R.id.due_only_text_view).setOnClickListener(registerActionHandler)
        view.findViewById<ImageView>(R.id.popup_menu).setOnClickListener(registerActionHandler)
    }

    override fun getMainCondition(): String {
        return DBQueryHelper.getHomePatientRegisterCondition()
    }

    override fun getDefaultSortQuery(): String {
        return DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH + " DESC"
    }

    override fun startRegistration() {
        (activity as HomeRegisterActivity?)?.startFormActivity(ConstantsUtils.JsonFormUtils.FP_REGISTER, null, null)
    }

    override fun onViewClicked(view: View) {
        if (activity == null) {
            return
        }

        val baseHomeRegisterActivity: HomeRegisterActivity? = activity as HomeRegisterActivity?
        val pc = view.tag as CommonPersonObjectClient?

        if (view.tag != null && view.getTag(R.id.VIEW_ID) === CLICK_VIEW_NORMAL) {
            Utils.navigateToProfile(activity, pc?.columnmaps as HashMap<String?, String?>)
        } else if (view.tag != null && view.getTag(R.id.VIEW_ID) === CLICK_VIEW_ALERT_STATUS) {
            if (Integer.valueOf(view.getTag(R.id.GESTATION_AGE).toString()) >= ConstantsUtils.DELIVERY_DATE_WEEKS) {
                baseHomeRegisterActivity?.showRecordBirthPopUp(view.tag as CommonPersonObjectClient)
            } else {
                val baseEntityId: String = Utils.getValue(pc?.columnmaps, DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, false)
                if (StringUtils.isNotBlank(baseEntityId)) {
                    Utils.proceedToContact(baseEntityId, pc?.columnmaps as HashMap<String?, String?>, activity)
                }
            }
        } else if (view.tag != null && view.getTag(R.id.VIEW_ID) === CLICK_VIEW_ATTENTION_FLAG) {
            AttentionFlagsTask(baseHomeRegisterActivity, pc).execute()
        } else if (view.id == R.id.filter_text_view) {
            baseHomeRegisterActivity?.switchToFragment(BaseRegisterActivity.SORT_FILTER_POSITION)
        }
        else if (view.id == R.id.due_only_text_view) {

        }
        else if (view.id == R.id.popup_menu) {
            val popupMenu = PopupMenu(activity, view)
            popupMenu.menuInflater.inflate(R.menu.home_main_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                true
            }

            popupMenu.show()
        }
    }

    override fun onSyncInProgress(fetchStatus: FetchStatus?) {
        Utils.postEvent(SyncEvent(fetchStatus))
    }

    @SuppressLint("NewApi")
    override fun showNotFoundPopup(whoAncId: String?) {
        NoMatchDialogFragment
                .launchDialog(Objects.requireNonNull(activity) as BaseRegisterActivity?, SecuredNativeSmartRegisterFragment.DIALOG_TAG, whoAncId)
    }

    override fun initializeAdapter(visibleColumns: Set<org.smartregister.configurableviews.model.View?>?) {
        val registerProvider = RegisterProvider(activity, commonRepository(), visibleColumns, registerActionHandler,
                paginationViewHandler) as RecyclerViewProvider<RecyclerView.ViewHolder>
        clientAdapter = RecyclerViewPaginatedAdapter<RegisterProvider.RegisterViewHolder>(null, registerProvider, context().commonrepository(tablename))
        clientAdapter.setCurrentlimit(20)
        clientsView.adapter = clientAdapter
    }

    override fun recalculatePagination(matrixCursor: AdvancedMatrixCursor?) {
        clientAdapter.setTotalcount(matrixCursor!!.count)
        Timber.tag("total count here").v("%d", clientAdapter.getTotalcount())
        clientAdapter.setCurrentlimit(20)
        if (clientAdapter.getTotalcount() > 0) {
            clientAdapter.setCurrentlimit(clientAdapter.getTotalcount())
        }
        clientAdapter.setCurrentoffset(0)
    }

    open fun updateSortAndFilter(filterList: List<Field>?, sortField: Field?) {
        (presenter as RegisterFragmentPresenter).updateSortAndFilter(filterList, sortField)
    }

    override fun countExecute() {
        try {
            val sql: String? = FPLibrary.getInstance().getRegisterQueryProvider().getCountExecuteQuery(mainCondition, filters)
            Timber.i(sql)
            val totalCount = commonRepository().countSearchIds(sql)
            clientAdapter.setTotalcount(totalCount)
            Timber.i("Total Register Count %d", clientAdapter.getTotalcount())
            clientAdapter.setCurrentlimit(20)
            clientAdapter.setCurrentoffset(0)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }


    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val matrixCursor = (presenter as RegisterFragmentPresenter).matrixCursor
        return (if (!globalQrSearch || matrixCursor == null) {
            if (id == RecyclerViewFragment.LOADER_ID) {
                object : CursorLoader(activity!!) {
                    override fun loadInBackground(): Cursor? {
                        val query: String? = filterAndSortQuery()
                        return commonRepository().rawCustomQueryForAdapter(query)
                    }
                }
            } else {
                null
            }
        } else {
            globalQrSearch = false
            if (id == RecyclerViewFragment.LOADER_ID) { // Returns a new CursorLoader
                object : CursorLoader(activity!!) {
                    override fun loadInBackground(): Cursor? {
                        return matrixCursor
                    }
                }
            } else null // An invalid id was passed in
        })!!
    }

    private fun filterAndSortQuery(): String? {
        val registerQueryBuilder = SmartRegisterQueryBuilder(mainSelect)
        var query = ""
        try {
            return if (isValidFilterForFts(commonRepository())) {
                var sql: String? = FPLibrary.getInstance().getRegisterQueryProvider().getObjectIdsQuery(mainCondition, filters) + if (StringUtils.isBlank(defaultSortQuery)) "" else " order by $defaultSortQuery"
                sql = registerQueryBuilder.addlimitandOffset(sql, clientAdapter.getCurrentlimit(), clientAdapter.getCurrentoffset())
                //val ids = commonRepository().findSearchIds(sql)
                query = FPLibrary.getInstance().getRegisterQueryProvider().mainRegisterQuery().toString() + "  order by $defaultSortQuery"
                //val joinedIds = "'" + StringUtils.join(ids, "','") + "'"
                //query.replace("%s", joinedIds)
                query
            } else {
                if (!TextUtils.isEmpty(filters) && TextUtils.isEmpty(Sortqueries)) {
                    registerQueryBuilder.addCondition(filters)
                    query = registerQueryBuilder.orderbyCondition(Sortqueries)
                    query = registerQueryBuilder.Endquery(registerQueryBuilder.addlimitandOffset(query
                            , clientAdapter.getCurrentlimit()
                            , clientAdapter.getCurrentoffset()))
                }
                query
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e)
        }
        return query
    }
}