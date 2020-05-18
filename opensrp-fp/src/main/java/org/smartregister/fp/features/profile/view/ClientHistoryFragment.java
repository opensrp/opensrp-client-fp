package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.fp.R;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.repository.PatientRepository;
import org.smartregister.fp.features.profile.adapter.ClientHistoryAdapter;
import org.smartregister.view.fragment.BaseProfileFragment;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class ClientHistoryFragment extends BaseProfileFragment implements ClientHistoryAdapter.HistoryItemClickListener {

    private String baseEntityId;
    private View noHealthRecord;
    private RecyclerView recyclerView;

    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {
        Bundle args = getArguments();
        if (args != null) {
            baseEntityId = args.getString(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, "");
            List<HashMap<String, String>> data = FPLibrary.getInstance().getPreviousContactRepository().getVisitHistory(baseEntityId);
            populateTheData(data);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_client_history, container, false);
        recyclerView = fragmentView.findViewById(R.id.profile_history_recycler);
        noHealthRecord = fragmentView.findViewById(R.id.no_health_data_recorded_profile_overview_layout);
        return fragmentView;
    }

    private void populateTheData(List<HashMap<String, String>> data) {

        if (! data.isEmpty()) {
            noHealthRecord.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new ClientHistoryAdapter(data, this));
        }
    }

    boolean hasRecords() {
        return (recyclerView.getAdapter() != null) && recyclerView.getAdapter().getItemCount() > 0;
    }

    @Override
    public void onItemClicked(HashMap<String, String> item, int position) {
        HashMap<String, String> details = PatientRepository.getClientProfileDetails(baseEntityId);
        String rawContactNo = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        int contactNo = rawContactNo == null ? 1 : Integer.parseInt(rawContactNo) - 1;
        details.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, String.valueOf(contactNo));
        details.put(ConstantsUtils.FORM_STATE, ConstantsUtils.FormState.READ_ONLY);
        Utils.proceedToContact(baseEntityId, details, getActivity());
    }

    public static ClientHistoryFragment newInstance(Bundle bundle) {
        Bundle bundles = bundle;
        ClientHistoryFragment fragment = new ClientHistoryFragment();
        if (bundles == null) {
            bundles = new Bundle();
        }
        fragment.setArguments(bundles);
        return fragment;
    }

}
