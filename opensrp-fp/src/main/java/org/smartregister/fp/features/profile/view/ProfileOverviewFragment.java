package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import org.smartregister.fp.R;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ClientProfileModel;
import org.smartregister.fp.common.model.PartialContact;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.databinding.FragmentProfileOverviewBinding;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.HashMap;

import timber.log.Timber;

public class ProfileOverviewFragment extends BaseProfileFragment {
    public static final String METHOD_CHOSEN = "method_chosen";
    public static final String METHOD_EXIT = "method_exit";
    public static final String METHOD_EXIT_START_DATE = "method_exit_start_date";
    public static final String REFERRAL = "referral";
    public static final String REASON_NO_METHOD_EXIT = "reason_no_method_exit";
    public static final String FORM_TYPE = "fp_start_visit";

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
    }

    @Override
    protected void onResumption() {
        try {
            if (getActivity() != null && getActivity().getIntent() != null) {
                HashMap<String, String> clientDetails =
                        (HashMap<String, String>) getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP);
                if (clientDetails != null) {
                    contactNo = Utils.getTodayContact(clientDetails.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
                }
                baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
            } else {
                Timber.d("getIntent or getActivity might be null");
            }

            PartialContact partialContact = FPLibrary.getInstance().getPartialContactRepository().getPartialContact(
                    new PartialContact(FORM_TYPE, baseEntityId, contactNo));

            if (partialContact != null && partialContact.getFormJsonDraft() != null) {
                ClientProfileModel clientProfileModel = Utils.getClientProfileValuesFromJson(partialContact.getFormJsonDraft());
                showClientProfileOverviewUi();
                clientProfileModel.setMethodAtExit(Utils.getMethodName(clientProfileModel.getMethodAtExit()));
                populateUi(clientProfileModel);
            } else showNoDataRecordedUi();
        } catch (Exception e) {
            Timber.e(e, " --> onResumption");
        }
    }

    private void showNoDataRecordedUi() {
        fragmentProfileBinding.clProfileOverview.setVisibility(View.GONE);
        fragmentProfileBinding.noHealthDataRecordedProfileOverviewLayout.setVisibility(View.VISIBLE);
    }

    private void showClientProfileOverviewUi() {
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
        return fragmentProfileBinding.getRoot();
    }
}
