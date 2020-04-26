package org.smartregister.fp.features.visit.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.fragments.JsonWizardFormFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.Contact;
import org.smartregister.fp.common.helper.FPRulesEngineFactory;
import org.smartregister.fp.common.task.FinalizeVisitFormTask;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.FilePathUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class StartVisitJsonFormActivity extends JsonFormActivity {

    protected FPRulesEngineFactory rulesEngineFactory = null;
    private ProgressDialog progressDialog;
    private String formName;
    private FPFormUtils FPFormUtils = new FPFormUtils();
    private Yaml yaml = new Yaml();

    private Map<String, List<String>> formGlobalKeys = new HashMap<>();
    private Map<String, String> formGlobalValues = new HashMap<>();
    private Set<String> globalKeys = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //loadGlobals();
        if (getIntent() != null) {
            formName = getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME);
        }
        super.onCreate(savedInstanceState);

        ///new Handler().postDelayed(this::updateViewsProperties, 200);
    }



    private void loadGlobals() {
        try {
            loadContactGlobalsConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateViewsProperties() {
        RelativeLayout rv = (RelativeLayout) findViewById(R.id.duration).getParent();
        ((LinearLayout.LayoutParams) rv.getLayoutParams()).setMargins(0, -80, 0, 0);
        rv.requestLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getIntent() != null) {
            formName = getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME);
        }
        try {
            FPFormUtils.processCheckboxFilteredItems(mJSONObject);
        } catch (JSONException e) {
            Timber.e(e, "An error occurred while trying to filter checkbox items");
        }
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

    public Contact getContact() {
        Form form = getForm();
        if (form instanceof Contact) {
            return (Contact) form;
        }
        return null;
    }

    private void loadContactGlobalsConfig() throws IOException {
        Iterable<Object> contactGlobals = readYaml(FilePathUtils.FileUtils.VISIT_GLOBALS);

        for (Object ruleObject : contactGlobals) {
            Map<String, Object> map = ((Map<String, Object>) ruleObject);
            formGlobalKeys.put(map.get(ConstantsUtils.FORM).toString(), (List<String>) map.get(JsonFormConstants.FIELDS));
            globalKeys.addAll((List<String>) map.get(JsonFormConstants.FIELDS));
        }
    }

    public Iterable<Object> readYaml(String filename) throws IOException {
        InputStreamReader inputStreamReader =
                new InputStreamReader(this.getAssets().open((FilePathUtils.FolderUtils.CONFIG_FOLDER_PATH + filename)));
        return yaml.loadAll(inputStreamReader);
    }

    @Override
    public synchronized void initializeFormFragment() {
        initializeFormFragmentCore();
    }

    protected void initializeFormFragmentCore() {
        JsonWizardFormFragment contactJsonFormFragment = StartVisitJsonWizardFormFragment.getFormFragment(JsonFormConstants.FIRST_STEP_NAME);
        getSupportFragmentManager().beginTransaction().add(com.vijay.jsonwizard.R.id.container, contactJsonFormFragment).commit();
    }

    /**
     * Finds gets the currently selected dangers signs on the quick change page and sets the none {@link Boolean} and other
     * {@link Boolean} so as  to identify times to show the refer and proceed buttons on quick check
     * <p>
     * This fix is a bit hacky but feel free to use it
     *
     * @param fields {@link JSONArray}
     * @throws JSONException
     * @author dubdabasoduba
     */
    public void quickCheckDangerSignsSelectionHandler(JSONArray fields) throws JSONException {
        boolean none = false;
        boolean other = false;

        Fragment fragment = getVisibleFragment();
        if (fragment instanceof StartVisitJsonWizardFormFragment) {
            for (int i = 0; i < fields.length(); i++) {
                JSONObject jsonObject = fields.getJSONObject(i);
                if (jsonObject != null && jsonObject.getString(JsonFormConstants.KEY).equals(ConstantsUtils.DANGER_SIGNS)) {

                    JSONArray jsonArray = jsonObject.getJSONArray(JsonFormConstants.OPTIONS_FIELD_NAME);
                    for (int k = 0; k < jsonArray.length(); k++) {
                        JSONObject item = jsonArray.getJSONObject(k);
                        if (item != null && item.getBoolean(JsonFormConstants.VALUE)) {
                            if (item.getString(JsonFormConstants.KEY).equals(ConstantsUtils.DANGER_NONE)) {
                                none = true;
                            }

                            if (!item.getString(JsonFormConstants.KEY).equals(ConstantsUtils.DANGER_NONE)) {
                                other = true;
                            }
                        }
                    }
                }
            }

            ((StartVisitJsonWizardFormFragment) fragment).displayQuickCheckBottomReferralButtons(none, other);
        }
    }

    /**
     * Returns the current visible fragment on the device
     *
     * @return fragment {@link Fragment}
     * @author dubdabasoduba
     */
    public Fragment getVisibleFragment() {
        List<Fragment> fragments = this.getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible()) return fragment;
        }
        return null;
    }

    protected void callSuperWriteValue(String stepName, String key, String value, String openMrsEntityParent,
                                       String openMrsEntity, String openMrsEntityId, Boolean popup) throws JSONException {
        super.writeValue(stepName, key, value, openMrsEntityParent, openMrsEntity, openMrsEntityId, popup);


    }

    public void showProgressDialog(String titleIdentifier) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(titleIdentifier);
            progressDialog.setMessage(getString(R.string.please_wait_message));
        }

        if (!isFinishing()) progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /*z
     * Partially saves the Quick Check forms details then proceeds to the main contact page
     *
     * @author dubdabasoduba
     */
    public void proceedToMainContactPage() {

        int contactNo = getIntent().getIntExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, 0);
        String baseEntityId = getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);

        /*Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP));
        intent.putExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME, getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.FORM_NAME));
        intent.putExtra(ConstantsUtils.IntentKeyUtils.CONTACT_NO, contactNo);*/

        new FinalizeVisitFormTask(this, baseEntityId, contactNo, getContact(), currentJsonState()).execute();
    }

    /**
     * Stops the ContactJsonForm activity and move to the main register page
     *
     * @author dubdabasoduba
     */
    public void finishInitialQuickCheck() {
        StartVisitJsonFormActivity.this.finish();
    }


}