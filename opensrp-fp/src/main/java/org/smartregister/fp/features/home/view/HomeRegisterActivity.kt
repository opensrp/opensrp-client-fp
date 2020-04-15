package org.smartregister.fp.features.home.view

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.barcode.Barcode
import com.vijay.jsonwizard.activities.JsonFormActivity
import org.apache.commons.lang3.StringUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.smartregister.AllConstants
import org.smartregister.commonregistry.CommonPersonObjectClient
import org.smartregister.configurableviews.ConfigurableViewsLibrary
import org.smartregister.configurableviews.model.Field
import org.smartregister.domain.FetchStatus
import org.smartregister.fp.R
import org.smartregister.fp.common.domain.AttentionFlag
import org.smartregister.fp.common.domain.Contact
import org.smartregister.fp.common.event.PatientRemovedEvent
import org.smartregister.fp.common.event.ShowProgressDialogEvent
import org.smartregister.fp.common.library.FPLibrary
import org.smartregister.fp.common.util.*
import org.smartregister.fp.features.home.contract.RegisterContract
import org.smartregister.fp.features.home.presenter.RegisterPresenter
import org.smartregister.fp.features.home.repository.PatientRepository
import org.smartregister.helper.BottomNavigationHelper
import org.smartregister.listener.BottomNavigationListener
import org.smartregister.view.activity.BaseRegisterActivity
import org.smartregister.view.fragment.BaseRegisterFragment
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

open class HomeRegisterActivity : BaseRegisterActivity(), RegisterContract.View {
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
    private var recordBirthAlertDialog: AlertDialog? = null
    private var attentionFlagAlertDialog: AlertDialog? = null
    private var attentionFlagDialogView: View? = null
    private var isAdvancedSearch = false
    private var advancedSearchQrText = ""
    private var advancedSearchFormData = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recordBirthAlertDialog = createAlertDialog()
        createAttentionFlagsAlertDialog()
    }

    override fun registerBottomNavigation() {
        bottomNavigationHelper = BottomNavigationHelper()
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation)

        if (bottomNavigationView != null) {

            bottomNavigationView.menu.findItem(R.id.action_clients).setIcon(R.drawable.ic_icon_nav_clients)

            bottomNavigationView.menu.add(R.string.action_mec_wheel).setIcon(R.drawable.ic_icon_nav_mec_wheel)

            // remove unused menu
            bottomNavigationView.menu.removeItem(R.id.action_search)
            bottomNavigationView.menu.removeItem(R.id.action_library)

            val bottomNavigationListener = BottomNavigationListener(this)
            bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationListener)
        }
    }

    override fun onBackPressed() {
        val fragment = findFragmentByPosition(currentPage)
        if (fragment is AdvancedSearchFragment) {
            fragment.onBackPressed()
            return
        } else if (fragment is BaseRegisterFragment) {
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients)
            if (fragment.onBackPressed()) {
                return
            }
        }
        if (currentPage == 0) {
            super.onBackPressed()
        } else {
            switchToBaseFragment()
            setSelectedBottomBarMenuItem(org.smartregister.R.id.action_clients)
        }
    }

    public override fun initializePresenter() {
        presenter = RegisterPresenter(this)
    }

    public override fun getRegisterFragment() = HomeRegisterFragment()

    public override fun getOtherFragments(): Array<Fragment> {
        val fragments = arrayOf<Fragment>()
        return fragments
    }

    override fun startFormActivity(formName: String?, entityId: String?, metaData: String?) {
        try {
            if (mBaseFragment is HomeRegisterFragment) {
                val locationId: String = FPLibrary.getInstance().getContext().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID)
                (presenter as RegisterPresenter).startForm(formName, entityId, metaData, locationId)
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e, "%s --> startFormActivity()", this.javaClass.canonicalName)
            displayToast(getString(R.string.error_unable_to_start_form))
        }
    }

    override fun startFormActivity(form: JSONObject?) {
        val intent = Intent(this, JsonFormActivity::class.java)
        intent.putExtra(ConstantsUtils.JsonFormExtraUtils.JSON, form.toString())
        startActivityForResult(intent, FPJsonFormUtils.REQUEST_CODE_GET_JSON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AllConstants.BARCODE.BARCODE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                val barcode: Barcode? = data.getParcelableExtra(AllConstants.BARCODE.BARCODE_KEY)
                if (barcode != null) {
                    Timber.d(barcode.displayValue)
                    val fragment = findFragmentByPosition(currentPage)
                    if (fragment is AdvancedSearchFragment) {
                        advancedSearchQrText = barcode.displayValue
                    } else {
                        mBaseFragment.onQRCodeSucessfullyScanned(barcode.displayValue)
                        mBaseFragment.setSearchTerm(barcode.displayValue)
                    }
                }
            } else {
                Timber.i("NO RESULT FOR QR CODE")
            }
        } else {
            onActivityResultExtended(requestCode, resultCode, data)
        }
    }

    override fun onActivityResultExtended(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FPJsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            try {
                val jsonString = data!!.getStringExtra(ConstantsUtils.JsonFormExtraUtils.JSON)
                Timber.d(jsonString)
                if (StringUtils.isNotBlank(jsonString)) {
                    val form = JSONObject(jsonString)
                    when (form.getString(FPJsonFormUtils.ENCOUNTER_TYPE)) {
                        ConstantsUtils.EventTypeUtils.REGISTRATION -> (presenter as RegisterContract.Presenter).saveRegistrationForm(jsonString, false)
                        ConstantsUtils.EventTypeUtils.CLOSE -> (presenter as RegisterContract.Presenter).closeFPRecord(jsonString)
                        ConstantsUtils.EventTypeUtils.QUICK_CHECK -> {
                            val contact = Contact()
                            contact.setContactNumber(intent.getIntExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, 0))
                            FPFormUtils
                                    .persistPartial(intent.getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID), contact)
                            PatientRepository
                                    .updateContactVisitStartDate(intent.getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID),
                                            Utils.getDBDateToday())
                        }
                        else -> {
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Timber.e(e, "%s --> onActivityResultExtended()", this.javaClass.canonicalName)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun getViewIdentifiers(): MutableList<String> {
        return Arrays.asList(ConstantsUtils.ConfigurationUtils.HOME_REGISTER)
    }

    override fun updateInitialsText(initials: String?) {
        userInitials = initials
    }

    override fun switchToBaseFragment() {
        switchToFragment(BASE_REG_POSITION)
    }

    override fun setSelectedBottomBarMenuItem(itemId: Int) {
        bottomNavigationView.selectedItemId = itemId
    }

    fun showRecordBirthPopUp(client: CommonPersonObjectClient) {
        //This is required
        intent
                .putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, client.columnmaps[DBConstantsUtils.KeyUtils.BASE_ENTITY_ID])
        recordBirthAlertDialog?.setMessage(java.lang.String.format(this.getString(R.string.record_birth_popup_message),
                Utils.getGestationAgeFromEDDate(client.columnmaps[DBConstantsUtils.KeyUtils.EDD]),
                Utils.convertDateFormat(Utils.dobStringToDate(client.columnmaps[DBConstantsUtils.KeyUtils.EDD]),
                        dateFormatter), Utils.getDuration(client.columnmaps[DBConstantsUtils.KeyUtils.EDD]),
                client.columnmaps[DBConstantsUtils.KeyUtils.FIRST_NAME]))
        recordBirthAlertDialog?.show()
    }

    protected fun createAlertDialog(): AlertDialog? {
        val alertDialog: AlertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(getString(R.string.record_birth) + "?")
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel).toUpperCase()
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.record_birth).toUpperCase()
        ) { dialog, which -> FPJsonFormUtils.launchANCCloseForm(this@HomeRegisterActivity) }
        return alertDialog
    }

    protected fun createAttentionFlagsAlertDialog() {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        attentionFlagDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_attention_flag, null)
        dialogBuilder.setView(attentionFlagDialogView)
        attentionFlagDialogView?.findViewById<View>(R.id.closeButton)?.setOnClickListener { view: View? -> attentionFlagAlertDialog?.dismiss() }
        attentionFlagAlertDialog = dialogBuilder.create()
        setAttentionFlagAlertDialog(attentionFlagAlertDialog)
    }

    fun setAttentionFlagAlertDialog(attentionFlagAlertDialog: AlertDialog?) {
        this.attentionFlagAlertDialog = attentionFlagAlertDialog
    }

    fun getAttentionFlagAlertDialog(): AlertDialog? {
        return attentionFlagAlertDialog
    }

    override fun showLanguageDialog(displayValues: MutableList<String>?) {
        val adapter: ArrayAdapter<String> = object : ArrayAdapter<String>(applicationContext, android.R.layout.simple_list_item_1,
                displayValues!!.toTypedArray()) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                ConfigurableViewsLibrary.getInstance()
                view.setTextColor(ConfigurableViewsLibrary.getContext().getColorResource(R.color.customAppThemeBlue))
                return view
            }
        }

        val languageDialog: AlertDialog = createLanguageDialog(adapter, displayValues)
        languageDialog.show()
    }

    fun createLanguageDialog(adapter: ArrayAdapter<String>, displayValues: List<String>): AlertDialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(this.getString(R.string.select_language))
        builder.setSingleChoiceItems(adapter, 0) { dialog, which ->
            val selectedItem = displayValues[which]
            (presenter as RegisterContract.Presenter).saveLanguage(selectedItem)
            dialog.dismiss()
        }
        return builder.create()
    }

    override fun showAttentionFlagsDialog(attentionFlags: MutableList<AttentionFlag>?) {
        val redFlagsContainer: ViewGroup? = attentionFlagDialogView?.findViewById(R.id.red_flags_container)
        val yellowFlagsContainer: ViewGroup? = attentionFlagDialogView?.findViewById(R.id.yellow_flags_container)

        redFlagsContainer?.removeAllViews()
        yellowFlagsContainer?.removeAllViews()

        var yellowFlagCount = 0
        var redFlagCount = 0

        for (flag in attentionFlags!!) {
            if (flag.isRedFlag) {
                val redRow = LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_red, redFlagsContainer, false) as LinearLayout
                (redRow.getChildAt(1) as TextView).text = flag.title
                redFlagsContainer?.addView(redRow)
                redFlagCount += 1
            } else {
                val yellowRow = LayoutInflater.from(this)
                        .inflate(R.layout.alert_dialog_attention_flag_row_yellow, yellowFlagsContainer, false) as LinearLayout
                (yellowRow.getChildAt(1) as TextView).text = flag.title
                yellowFlagsContainer?.addView(yellowRow)
                yellowFlagCount += 1
            }
        }

        (redFlagsContainer?.parent as View).visibility = if (redFlagCount > 0) View.VISIBLE else View.GONE
        (yellowFlagsContainer?.parent as View).visibility = if (yellowFlagCount > 0) View.VISIBLE else View.GONE

        getAttentionFlagAlertDialog()?.show()
    }

    fun startAdvancedSearch() {
        if (isAdvancedSearchEnabled()) {
            try {
                mPager.setCurrentItem(ADVANCED_SEARCH_POSITION, false)
            } catch (e: Exception) {
                Timber.e(e, "%s --> startAdvancedSearch()", this.javaClass.canonicalName)
            }
        }
    }

    fun isAdvancedSearchEnabled(): Boolean {
        return false
    }

    fun setAdvancedSearch(advancedSearch: Boolean) {
        isAdvancedSearch = advancedSearch
    }

    fun setAdvancedSearchFormData(advancedSearchFormData: HashMap<String, String>) {
        this.advancedSearchFormData = advancedSearchFormData
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        super.onPause()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun showProgressDialogHandler(showProgressDialogEvent: ShowProgressDialogEvent?) {
        if (showProgressDialogEvent != null) {
            showProgressDialog(R.string.saving_dialog_title)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun removePatientHandler(event: PatientRemovedEvent?) {
        if (event != null) {
            Utils.removeStickyEvent(event)
            refreshList(FetchStatus.fetched)
            hideProgressDialog()
        }
    }

    override fun startRegistration() {
        startFormActivity(ConstantsUtils.JsonFormUtils.FP_REGISTER, null, null)
    }

    fun updateSortAndFilter(filterList: List<Field>?, sortField: Field?) {
        (mBaseFragment as HomeRegisterFragment).updateSortAndFilter(filterList, sortField)
        switchToBaseFragment()
    }
}