package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.databinding.DataBindingUtil;

import org.jeasy.rules.api.Facts;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ClientProfileModel;
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
    private static final String METHOD_CHOSEN = "method_chosen";
    private static final String METHOD_EXIT = "method_exit";
    private static final String METHOD_EXIT_START_DATE = "method_exit_start_date";
    private static final String REFERRAL = "referral";
    private static final String REASON_NO_METHOD_EXIT = "reason_no_method_exit";
    private Button dueButton;
    private ButtonAlertStatus buttonAlertStatus;
    private String baseEntityId;
    private String contactNo;

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
                contactNo = String.valueOf(Utils.getTodayContact(clientDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)));
            }
            baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
        } else {
            Timber.d("getIntent or getActivity might be null");
        }
    }

    @Override
    protected void onResumption() {
        try {
            List<String> keys = new ArrayList<>(Arrays.asList(METHOD_CHOSEN, METHOD_EXIT, METHOD_EXIT_START_DATE, REFERRAL, REASON_NO_METHOD_EXIT));
            Facts result = FPLibrary.getInstance().getPreviousContactRepository().getProfileOverviewDetails(baseEntityId, contactNo, keys);
            if (result.asMap().size() > 0) {
                ClientProfileModel clientProfileModel = new ClientProfileModel();
                clientProfileModel.setChosenMethod(result.get(METHOD_CHOSEN));
                clientProfileModel.setMethodAtExit(result.get(METHOD_EXIT));
                clientProfileModel.setMethodStartDate(result.get(METHOD_EXIT_START_DATE));
                clientProfileModel.setReferred(result.get(REFERRAL));
                clientProfileModel.setReasonForNoMethodAtExit(result.get(REASON_NO_METHOD_EXIT));
                showClientProfileOverview();
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
