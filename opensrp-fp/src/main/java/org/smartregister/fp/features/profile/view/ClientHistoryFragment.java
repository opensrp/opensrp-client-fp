package org.smartregister.fp.features.profile.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.smartregister.fp.R;
import org.smartregister.view.fragment.BaseProfileFragment;

public class ClientHistoryFragment extends BaseProfileFragment {


    @Override
    protected void onCreation() {

    }

    @Override
    protected void onResumption() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_client_history, container, false);
        return fragmentView;
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
