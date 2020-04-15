package org.smartregister.fp.features.visit.view

import android.app.ProgressDialog
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vijay.jsonwizard.R
import com.vijay.jsonwizard.activities.JsonFormActivity
import com.vijay.jsonwizard.constants.JsonFormConstants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.smartregister.fp.common.helper.FPRulesEngineFactory
import org.smartregister.fp.common.util.ConstantsUtils
import timber.log.Timber
import java.util.*

class StartVisitJsonFormActivity : JsonFormActivity() {

    private var formName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent != null) {
            formName = intent.getStringExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME)
        }
        super.onCreate(savedInstanceState)
    }

    override fun init(json: String?) {
        try {
            setmJSONObject(JSONObject(json))
            if (!getmJSONObject().has(ConstantsUtils.JsonFormKeyUtils.ENCOUNTER_TYPE)) {
                setmJSONObject(JSONObject())
                throw JSONException("Form encounter_type not set")
            }

            //populate them global values
            globalValues = if (getmJSONObject().has(JsonFormConstants.JSON_FORM_KEY.GLOBAL)) {
                Gson()
                        .fromJson<Map<String, String>>(getmJSONObject().getJSONObject(JsonFormConstants.JSON_FORM_KEY.GLOBAL).toString(),
                                object : TypeToken<HashMap<String?, String?>?>() {}.type)
            } else {
                HashMap()
            }
            rulesEngineFactory = FPRulesEngineFactory(this, globalValues, getmJSONObject())
            setRulesEngineFactory(rulesEngineFactory)
            confirmCloseTitle = getString(R.string.confirm_form_close)
            confirmCloseMessage = getString(R.string.confirm_form_close_explanation)
            localBroadcastManager = LocalBroadcastManager.getInstance(this)
        } catch (e: JSONException) {
            Timber.e(e, "Initialization error. Json passed is invalid : ")
        }
    }

    override fun writeValue(stepName: String?, key: String?, value: String?, openMrsEntityParent: String?, openMrsEntity: String?, openMrsEntityId: String?, popup: Boolean) {
        callSuperWriteValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup)
    }

    fun showProgressDialog(titleIdentifier: String?) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog.setCancelable(false)
            progressDialog.setTitle(titleIdentifier)
            progressDialog.setMessage(getString(org.smartregister.R.string.please_wait_message))
        }
        if (!isFinishing) progressDialog.show()
    }

    fun hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss()
        }
    }

    @Throws(JSONException::class)
    protected fun callSuperWriteValue(stepName: String?, key: String?, value: String?, openMrsEntityParent: String?, openMrsEntity: String?, openMrsEntityId: String?, popup: Boolean?) {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup!!)
    }

    override fun onFormFinish() {
        callSuperFinish()
    }

    protected fun callSuperFinish() {
        super.onFormFinish()
    }

    /*override fun checkBoxWriteValue(stepName: String?, parentKey: String?, childObjectKey: String?, childKey: String?, value: String?, popup: Boolean) {
        synchronized(getmJSONObject()) {
            val jsonObject = getmJSONObject().getJSONObject(stepName)
            val fields = fetchFields(jsonObject, popup)
            for (i in 0 until fields.length()) {
                val item = fields.getJSONObject(i)
                val keyAtIndex = item.getString(JsonFormConstants.KEY)
                if (parentKey == keyAtIndex) {
                    val jsonArray = item.getJSONArray(childObjectKey)
                    for (j in 0 until jsonArray.length()) {
                        val innerItem = jsonArray.getJSONObject(j)
                        val anotherKeyAtIndex = innerItem.getString(JsonFormConstants.KEY)
                        if (childKey == anotherKeyAtIndex) {
                            innerItem.put(JsonFormConstants.VALUE, value)
                            if (!TextUtils.isEmpty(formName) && formName == ConstantsUtils.JsonFormUtils.ANC_QUICK_CHECK) {
                                quickCheckDangerSignsSelectionHandler(fields)
                            }
                            invokeRefreshLogic(value, popup, parentKey, childKey)
                            return
                        }
                    }
                }
            }
        }
    }

    *//**
     * Finds gets the currently selected dangers signs on the quick change page and sets the none [Boolean] and other
     * [Boolean] so as  to identify times to show the refer and proceed buttons on quick check
     *
     *
     * This fix is a bit hacky but feel free to use it
     *
     * @param fields [JSONArray]
     * @throws JSONException
     * @author dubdabasoduba
     *//*
    @Throws(JSONException::class)
    fun quickCheckDangerSignsSelectionHandler(fields: JSONArray) {
        var none = false
        var other = false
        val fragment: Fragment = getVisibleFragment()
        if (fragment is ContactWizardJsonFormFragment) {
            for (i in 0 until fields.length()) {
                val jsonObject = fields.getJSONObject(i)
                if (jsonObject != null && jsonObject.getString(JsonFormConstants.KEY) == ConstantsUtils.DANGER_SIGNS) {
                    val jsonArray = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME)
                    for (k in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(k)
                        if (item != null && item.getBoolean(JsonFormConstants.VALUE)) {
                            if (item.getString(JsonFormConstants.KEY) == ConstantsUtils.DANGER_NONE) {
                                none = true
                            }
                            if (item.getString(JsonFormConstants.KEY) != ConstantsUtils.DANGER_NONE) {
                                other = true
                            }
                        }
                    }
                }
            }
            (fragment as ContactWizardJsonFormFragment).displayQuickCheckBottomReferralButtons(none, other)
        }
    }*/
}