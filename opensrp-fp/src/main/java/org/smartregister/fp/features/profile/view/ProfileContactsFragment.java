package org.smartregister.fp.features.profile.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jeasy.rules.api.Facts;
import org.smartregister.fp.R;
import org.smartregister.fp.common.adapter.LastContactAdapter;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.domain.LastContactDetailsWrapper;
import org.smartregister.fp.common.domain.YamlConfig;
import org.smartregister.fp.common.domain.YamlConfigItem;
import org.smartregister.fp.common.domain.YamlConfigWrapper;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.Task;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;
import org.smartregister.fp.common.util.FilePathUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.profile.contract.ProfileFragmentContract;
import org.smartregister.fp.features.profile.presenter.ProfileFragmentPresenter;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class ProfileContactsFragment extends BaseProfileFragment implements ProfileFragmentContract.View {
    private List<YamlConfigWrapper> lastContactDetails;
    private List<YamlConfigWrapper> lastContactTests;
    private TextView testsHeader;
    private LinearLayout lastContactLayout;
    private LinearLayout testLayout;
    private LinearLayout testsDisplayLayout;
    private ProfileContactsActionHandler profileContactsActionHandler = new ProfileContactsActionHandler();
    private FPJsonFormUtils formUtils = new FPJsonFormUtils();
    private ProfileFragmentContract.Presenter presenter;
    private String baseEntityId;
    private String contactNo;
    private Button dueButton;
    private ButtonAlertStatus buttonAlertStatus;
    private HashMap<String, String> clientDetails;
    private View noHealthRecordLayout;
    private ScrollView profileContactsLayout;
    private Utils utils = new Utils();

    public static ProfileContactsFragment newInstance(Bundle bundle) {
        Bundle args = bundle;
        ProfileContactsFragment fragment = new ProfileContactsFragment();
        if (args == null) {
            args = new Bundle();
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
    }

    protected void initializePresenter() {
        if (getActivity() == null || getActivity().getIntent() == null) {
            return;
        }
        presenter = new ProfileFragmentPresenter(this);
    }

    @Override
    protected void onCreation() {
        lastContactDetails = new ArrayList<>();
        lastContactTests = new ArrayList<>();
        if (testsDisplayLayout != null) {
            testsDisplayLayout.removeAllViews();
        }
        if (getActivity() != null) {
            if (getActivity().getIntent() != null) {
                clientDetails =
                        (HashMap<String, String>) getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP);
            }
            buttonAlertStatus = Utils.getButtonAlertStatus(clientDetails, getActivity().getApplicationContext(), true);
        }
    }

    @Override
    protected void onResumption() {
        lastContactDetails = new ArrayList<>();
        lastContactTests = new ArrayList<>();
        if (testsDisplayLayout != null) {
            testsDisplayLayout.removeAllViews();
        }
        if (getActivity() != null && getActivity().getIntent() != null) {
            baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
        }
        setUpAlertStatusButton();
        contactNo = String.valueOf(Utils.getTodayContact(clientDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
        initializeLastContactDetails(clientDetails);

        if (lastContactDetails.isEmpty() && lastContactTests.isEmpty()) {
            noHealthRecordLayout.setVisibility(View.VISIBLE);
            profileContactsLayout.setVisibility(View.GONE);
        } else {
            noHealthRecordLayout.setVisibility(View.GONE);
            profileContactsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setUpAlertStatusButton() {
        Utils.processButtonAlertStatus(getActivity(), dueButton, buttonAlertStatus);
    }

    private void initializeLastContactDetails(HashMap<String, String> clientDetails) {
        if (clientDetails != null) {
            try {
                List<LastContactDetailsWrapper> lastContactDetailsWrapperList = new ArrayList<>();
                List<LastContactDetailsWrapper> lastContactDetailsTestsWrapperList = new ArrayList<>();

                Facts facts = presenter.getImmediatePreviousContact(clientDetails, baseEntityId, contactNo);
                addOtherRuleObjects(facts);
                addAttentionFlagsRuleObjects(facts);
                contactNo = (String) facts.asMap().get(ConstantsUtils.CONTACT_NO);

                addTestsRuleObjects(facts);

                String contactDate = (String) facts.asMap().get(ConstantsUtils.CONTACT_DATE);
                String displayContactDate = "";
                if (!TextUtils.isEmpty(contactDate)) {
                    Date lastContactDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(contactDate);
                    displayContactDate = new SimpleDateFormat("dd MMM " + "yyyy", Locale.getDefault())
                            .format(lastContactDate);
                }


                if (lastContactDetails.isEmpty()) {
                    lastContactLayout.setVisibility(View.GONE);
                } else {
                    lastContactDetailsWrapperList
                            .add(new LastContactDetailsWrapper(contactNo, displayContactDate, lastContactDetails, facts));
                    setUpContactDetailsRecycler(lastContactDetailsWrapperList);
                }

                if (lastContactTests.isEmpty()) {
                    testLayout.setVisibility(View.GONE);
                } else {
                    lastContactDetailsTestsWrapperList
                            .add(new LastContactDetailsWrapper(contactNo, displayContactDate, lastContactTests, facts));
                    testsHeader.setText(
                            String.format(getActivity().getResources().getString(R.string.recent_test), displayContactDate));
                    setUpContactTestsDetails(lastContactDetailsTestsWrapperList);
                }

            } catch (Exception e) {
                Timber.e(e, " --> initializeLastContactDetails");
            }
        }
    }

    private void addOtherRuleObjects(Facts facts) throws IOException {
        Iterable<Object> ruleObjects = utils.loadRulesFiles(FilePathUtils.FileUtils.PROFILE_LAST_CONTACT);

        for (Object ruleObject : ruleObjects) {
            List<YamlConfigWrapper> yamlConfigList = new ArrayList<>();
            int valueCount = 0;
            YamlConfig yamlConfig = (YamlConfig) ruleObject;

            List<YamlConfigItem> configItems = yamlConfig.getFields();

            for (YamlConfigItem configItem : configItems) {
                if (FPLibrary.getInstance().getAncRulesEngineHelper().getRelevance(facts, configItem.getRelevance())) {
                    yamlConfigList.add(new YamlConfigWrapper(null, null, configItem));
                    valueCount += 1;
                }
            }

            if (valueCount > 0) {
                lastContactDetails.addAll(yamlConfigList);
            }
        }
    }

    private void addAttentionFlagsRuleObjects(Facts facts) throws IOException {
        Iterable<Object> attentionFlagsRuleObjects = FPLibrary.getInstance().readYaml(FilePathUtils.FileUtils.ATTENTION_FLAGS);

        for (Object ruleObject : attentionFlagsRuleObjects) {
            YamlConfig attentionFlagConfig = (YamlConfig) ruleObject;
            for (YamlConfigItem yamlConfigItem : attentionFlagConfig.getFields()) {

                if (FPLibrary.getInstance().getAncRulesEngineHelper()
                        .getRelevance(facts, yamlConfigItem.getRelevance())) {
                    lastContactDetails.add(new YamlConfigWrapper(null, null, yamlConfigItem));

                }

            }
        }
    }

    private void addTestsRuleObjects(Facts facts) throws IOException {
        Iterable<Object> testsRuleObjects = FPLibrary.getInstance()
                .readYaml(FilePathUtils.FileUtils.PROFILE_TAB_PREVIOUS_CONTACT_TEST);

        for (Object ruleObject : testsRuleObjects) {
            YamlConfig testsConfig = (YamlConfig) ruleObject;
            for (YamlConfigItem yamlConfigItem : testsConfig.getFields()) {

                if (FPLibrary.getInstance().getAncRulesEngineHelper()
                        .getRelevance(facts, yamlConfigItem.getRelevance())) {
                    lastContactTests.add(new YamlConfigWrapper(null, null, yamlConfigItem));

                }

            }
        }
    }

    private void setUpContactDetailsRecycler(List<LastContactDetailsWrapper> lastContactDetailsWrappers) {
        LastContactAdapter adapter = new LastContactAdapter(lastContactDetailsWrappers, getActivity());
        adapter.notifyDataSetChanged();
        RecyclerView recyclerView = lastContactLayout.findViewById(R.id.last_contact_information);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    private void setUpContactTestsDetails(List<LastContactDetailsWrapper> lastContactDetailsTestsWrapperList) {
        List<YamlConfigWrapper> data = new ArrayList<>();
        Facts facts = new Facts();
        if (lastContactDetailsTestsWrapperList.size() > 0) {
            for (int i = 0; i < lastContactDetailsTestsWrapperList.size(); i++) {
                LastContactDetailsWrapper lastContactDetailsTest = lastContactDetailsTestsWrapperList.get(i);
                data = lastContactDetailsTest.getExtraInformation();
                facts = lastContactDetailsTest.getFacts();
            }
        }

        populateTestDetails(data, facts);
    }

    private void populateTestDetails(List<YamlConfigWrapper> data, Facts facts) {
        if (data != null && data.size() > 0) {
            for (int position = 0; position < data.size(); position++) {
                if (data.get(position).getYamlConfigItem() != null) {
                    ConstraintLayout constraintLayout = formUtils.createListViewItems(data, facts, position, getActivity());
                    testsDisplayLayout.addView(constraintLayout);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_profile_contacts, container, false);
        lastContactLayout = fragmentView.findViewById(R.id.last_contact_layout);
        TextView lastContactBottom = lastContactLayout.findViewById(R.id.last_contact_bottom);
        lastContactBottom.setOnClickListener(profileContactsActionHandler);

        testLayout = fragmentView.findViewById(R.id.test_layout);
        testsHeader = testLayout.findViewById(R.id.tests_header);
        TextView testsBottom = testLayout.findViewById(R.id.tests_bottom);
        testsBottom.setOnClickListener(profileContactsActionHandler);

        testsDisplayLayout = testLayout.findViewById(R.id.test_display_layout);

        noHealthRecordLayout = fragmentView.findViewById(R.id.no_health_data_recorded_layout);
        profileContactsLayout = fragmentView.findViewById(R.id.profile_contacts_layout);

        dueButton = ((ProfileActivity) getActivity()).getDueButton();
        if (!ConstantsUtils.AlertStatusUtils.TODAY.equals(buttonAlertStatus.buttonAlertStatus)) {
            dueButton.setOnClickListener((ProfileActivity) getActivity());
        } else {
            dueButton.setEnabled(false);
        }

        return fragmentView;
    }

    private void goToPreviousContacts() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), PreviousContactsDetailsActivity.class);
            String baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP,
                    getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP));

            this.startActivity(intent);
        }
    }

    private void goToPreviousContactsTests() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), PreviousContactsTestsActivity.class);
            String baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, baseEntityId);
            intent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP,
                    getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP));

            this.startActivity(intent);
        }
    }

    @Override
    public void setContactTasks(List<Task> contactTasks) {
        // Implement here
    }

    @Override
    public void updateTask(Task task) {
        // Implement here
    }

    @Override
    public void refreshTasksList(boolean refresh) {
        // Implement here
    }

    /**
     * Handles the Click actions on any of the section in the page.
     */
    private class ProfileContactsActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.last_contact_bottom && !lastContactDetails.isEmpty()) {
                goToPreviousContacts();
            } else if (view.getId() == R.id.tests_bottom && !lastContactTests.isEmpty()) {
                goToPreviousContactsTests();
            }
        }

    }
}