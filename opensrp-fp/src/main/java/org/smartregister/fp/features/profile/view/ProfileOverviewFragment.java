package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;
import org.jeasy.rules.api.Facts;
import org.smartregister.fp.R;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ClientProfileModel;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.databinding.FragmentProfileOverviewBinding;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

import static org.smartregister.fp.common.repository.PreviousContactRepository.CONTACT_NO;

public class ProfileOverviewFragment extends BaseProfileFragment {
    public static final String METHOD_CHOSEN = "method_chosen";
    public static final String METHOD_EXIT = "method_exit";
    public static final String METHOD_EXIT_START_DATE = "method_exit_start_date";
    public static final String REFERRAL = "referral";
    public static final String REASON_NO_METHOD_EXIT = "reason_no_method_exit";
    public static final String FORM_TYPE = "fp_start_visit";

    private String baseEntityId;
    private int contactNo;

    public static ProfileOverviewFragment newInstance(Bundle bundle) {
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
        // this method is useful for objection creation
    }

    @Override
    protected void onResumption() {
        try {
            if (getActivity() != null && getActivity().getIntent() != null) {
                HashMap<String, String> clientDetails =
                        (HashMap<String, String>) getActivity().getIntent().getSerializableExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP);
                baseEntityId = getActivity().getIntent().getStringExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID);
                if (clientDetails != null) {
                    List<HashMap<String, String>> data = FPLibrary.getInstance().getPreviousContactRepository().getVisitHistory(baseEntityId);
                    if (data.size() > 0 && StringUtils.isNotEmpty(data.get(data.size() - 1).get(CONTACT_NO)))
                        contactNo = Integer.parseInt(data.get(data.size() - 1).get(CONTACT_NO));
                }
            } else {
                Timber.d("getIntent or getActivity might be null");
            }

            Facts facts = FPLibrary.getInstance().getPreviousContactRepository().getProfileOverviewDetails(baseEntityId, String.valueOf(contactNo), Lists.newArrayList(METHOD_CHOSEN, METHOD_EXIT, METHOD_EXIT_START_DATE, REFERRAL, REASON_NO_METHOD_EXIT));

            if (facts != null) {
                ClientProfileModel clientProfileModel = new ClientProfileModel();
                clientProfileModel.setMethodAtExit(Utils.getFormattedMethodName(facts.get(METHOD_EXIT)));
                clientProfileModel.setReasonForNoMethodAtExit(facts.get(REASON_NO_METHOD_EXIT));
                clientProfileModel.setMethodStartDate(facts.get(METHOD_EXIT_START_DATE));
                clientProfileModel.setReferred(facts.get(REFERRAL));
                clientProfileModel.setChosenMethod(Utils.getFormattedMethodName(facts.get(METHOD_CHOSEN)));
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
        showClientProfileOverviewUi();
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
