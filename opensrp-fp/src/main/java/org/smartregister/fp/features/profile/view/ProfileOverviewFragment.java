package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;

import org.jeasy.rules.api.Facts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ClientProfileModel;
import org.smartregister.fp.common.model.PartialContact;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.databinding.FragmentProfileOverviewBinding;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 12/07/2018.
 */
public class ProfileOverviewFragment extends BaseProfileFragment {
    public static final String METHOD_CHOSEN = "method_chosen";
    public static final String METHOD_EXIT = "method_exit";
    public static final String METHOD_EXIT_START_DATE = "method_exit_start_date";
    public static final String REFERRAL = "referral";
    public static final String REASON_NO_METHOD_EXIT = "reason_no_method_exit";
    public static final String FORM_TYPE = "fp_start_visit";

    private Button dueButton;
    private ButtonAlertStatus buttonAlertStatus;
    private String baseEntityId;
    private int contactNo;

    static ProfileOverviewFragment newInstance(Bundle bundle) {
        Bundle bundles = bundle;
        ProfileOverviewFragment fragment = new ProfileOverviewFragment();
        if (bundles == null) {
            bundles = new Bundle();
        }
        fragment.setArguments(bundles);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onCreation() {
        if (getActivity() != null && getActivity().getIntent() != null) {
            HashMap<String, String> clientDetails =
                    (HashMap<String, String>) getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP);
            if (clientDetails != null) {
                buttonAlertStatus = Utils.getButtonAlertStatus(clientDetails, getActivity().getApplicationContext(), true);
                contactNo = Utils.getTodayContact(clientDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
            }
            baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
        } else {
            Timber.d("getIntent or getActivity might be null");
        }
    }

    @Override
    protected void onResumption() {
        try {
            PartialContact partialContact = FPLibrary.getInstance().getPartialContactRepository().getPartialContact(
                    new PartialContact(FORM_TYPE, baseEntityId, contactNo));

            if (partialContact != null && partialContact.getFormJsonDraft() != null) {
                ClientProfileModel clientProfileModel = Utils.getClientProfileValuesFromJson(partialContact.getFormJsonDraft());
                showClientProfileOverview();
                clientProfileModel.setMethodAtExit(Utils.getMethodName(clientProfileModel.getMethodAtExit()));
                populateUi(clientProfileModel);
            } else showNoDataRecorded();
        } catch (Exception e) {
            Timber.e(e, " --> onResumption");
        }
    }

    private void showNoDataRecorded() {
        fragmentProfileBinding.clProfileOverview.setVisibility(View.GONE);
        fragmentProfileBinding.noHealthDataRecordedProfileOverviewLayout.setVisibility(View.VISIBLE);
    }

    private void showClientProfileOverview() {
        fragmentProfileBinding.clProfileOverview.setVisibility(View.VISIBLE);
        fragmentProfileBinding.noHealthDataRecordedProfileOverviewLayout.setVisibility(View.GONE);
    }

    private void populateUi(ClientProfileModel clientProfileModel) {
        fragmentProfileBinding.setClientProfileModel(clientProfileModel);
    }

    private FragmentProfileOverviewBinding fragmentProfileBinding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentProfileBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_profile_overview, container, false);

        dueButton = ((ProfileActivity) getActivity()).getDueButton();
        if (!ConstantsUtils.AlertStatusUtils.TODAY.equals(buttonAlertStatus.buttonAlertStatus)) {
            dueButton.setOnClickListener((ProfileActivity) getActivity());
        } else {
            dueButton.setEnabled(false);
        }

        return fragmentProfileBinding.getRoot();
    }
}
