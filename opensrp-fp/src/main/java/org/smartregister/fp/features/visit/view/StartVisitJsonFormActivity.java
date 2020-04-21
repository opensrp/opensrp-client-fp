package org.smartregister.fp.features.visit.view;

import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.helper.FPRulesEngineFactory;
import org.smartregister.fp.common.util.ConstantsUtils;

import java.util.HashMap;

import timber.log.Timber;

public class StartVisitJsonFormActivity extends JsonFormActivity {

    private String formName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent() != null) {
            formName = getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void init(String json) {
        try {
            setmJSONObject(new JSONObject(json));
            if (!getmJSONObject().has(ConstantsUtils.JsonFormKeyUtils.ENCOUNTER_TYPE)) {
                setmJSONObject(new JSONObject());
                throw new JSONException("Form encounter_type not set");
            }

            //populate them global values
            if (getmJSONObject().has(JsonFormConstants.JSON_FORM_KEY.GLOBAL)) {
                globalValues = new Gson().fromJson(getmJSONObject().getJSONObject(JsonFormConstants.JSON_FORM_KEY.GLOBAL).toString(),
                        new TypeToken<HashMap<String, String>>(){}.getType());
            }
            else {
                globalValues = new HashMap<>();
            }

            rulesEngineFactory = new FPRulesEngineFactory(this, globalValues, getmJSONObject());
            setRulesEngineFactory(rulesEngineFactory);
            confirmCloseTitle = getString(R.string.confirm_form_close);
            confirmCloseMessage = getString(R.string.confirm_form_close_explanation);
            localBroadcastManager = LocalBroadcastManager.getInstance(this);
        } catch (JSONException e) {
            Timber.e(e, "Initialization error. Json passed is invalid : ");
        }
    }
}
