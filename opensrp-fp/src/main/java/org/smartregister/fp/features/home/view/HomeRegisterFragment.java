package org.smartregister.fp.features.home.view;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.cursoradapter.RecyclerViewFragment;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.ResponseErrorStatus;
import org.smartregister.fp.R;
import org.smartregister.fp.common.cursor.AdvancedMatrixCursor;
import org.smartregister.fp.common.event.SyncEvent;
import org.smartregister.fp.common.fragment.NoMatchDialogFragment;
import org.smartregister.fp.common.helper.DBQueryHelper;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.provider.RegisterProvider;
import org.smartregister.fp.common.task.AttentionFlagsTask;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.contract.RegisterFragmentContract;
import org.smartregister.fp.features.home.presenter.RegisterFragmentPresenter;
import org.smartregister.job.SyncServiceJob;
import org.smartregister.job.SyncSettingsServiceJob;
import org.smartregister.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.fragment.BaseRegisterFragment;
import org.smartregister.view.fragment.SecuredNativeSmartRegisterFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import timber.log.Timber;

public class HomeRegisterFragment extends BaseRegisterFragment implements RegisterFragmentContract.View, SyncStatusBroadcastReceiver.SyncStatusListener {
    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String CLICK_VIEW_ALERT_STATUS = "click_view_alert_status";
    public static final String CLICK_VIEW_SYNC = "click_view_sync";
    public static final String CLICK_VIEW_ATTENTION_FLAG = "click_view_attention_flag";

    private PopupMenu popupMenu;

    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }

        String viewConfigurationIdentifier = ((BaseRegisterActivity) getActivity()).getViewIdentifiers().get(0);
        presenter = new RegisterFragmentPresenter(this, viewConfigurationIdentifier);
    }

    @Override
    public void setUniqueID(String qrCode) {
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> formData) {
    }

    @Override
    public void setupViews(View view) {
        super.setupViews(view);
        //Do not show filter button at the moment until all filters are implemented
        RelativeLayout filterSortRelativeLayout = view.findViewById(R.id.filter_sort_layout);
        if (filterSortRelativeLayout != null) {
            filterSortRelativeLayout.setVisibility(View.GONE);
        }

        View filterText = view.findViewById(R.id.filter_text_view);
        if (filterText != null) {
            filterText.setOnClickListener(registerActionHandler);
        }

        // Due Button
        View contactButton = view.findViewById(R.id.btn_followup);
        if (contactButton != null) {
            contactButton.setOnClickListener(registerActionHandler);
        }

        //Risk view
        /*View attentionFlag = view.findViewById(R.id.risk);
        if (attentionFlag != null) {
            attentionFlag.setOnClickListener(registerActionHandler);
        }*/

        view.findViewById(R.id.due_only_text_view).setOnClickListener(registerActionHandler);
        view.findViewById(R.id.popup_menu).setOnClickListener(registerActionHandler);
    }

    @Override
    protected String getMainCondition() {
        return DBQueryHelper.getHomePatientRegisterCondition();
    }

    @Override
    protected String getDefaultSortQuery() {
        return DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH + " DESC";
    }

    @Override
    protected void startRegistration() {
        ((HomeRegisterActivity) getActivity()).startFormActivity(ConstantsUtils.JsonFormUtils.FP_REGISTER, null, null);
    }

    @Override
    protected void onViewClicked(View view) {
        if (getActivity() == null) {
            return;
        }

        final HomeRegisterActivity baseHomeRegisterActivity = (HomeRegisterActivity) getActivity();
        final CommonPersonObjectClient pc = (CommonPersonObjectClient) view.getTag();

        if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            Utils.navigateToProfile(getActivity(), (HashMap<String, String>) pc.getColumnmaps());
        } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_ALERT_STATUS) {
            if (Integer.valueOf(view.getTag(R.id.GESTATION_AGE).toString()) >= ConstantsUtils.DELIVERY_DATE_WEEKS) {
                baseHomeRegisterActivity.showRecordBirthPopUp((CommonPersonObjectClient) view.getTag());
            } else {
                String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, false);

                if (StringUtils.isNotBlank(baseEntityId)) {
                    Utils.proceedToContact(baseEntityId, (HashMap<String, String>) pc.getColumnmaps(), getActivity());
                }
            }
        } else if (view.getTag() != null && view.getTag(R.id.VIEW_ID) == CLICK_VIEW_ATTENTION_FLAG) {
            new AttentionFlagsTask(baseHomeRegisterActivity, pc).execute();
        } else if (view.getId() == R.id.filter_text_view) {
            baseHomeRegisterActivity.switchToFragment(BaseRegisterActivity.SORT_FILTER_POSITION);
        } else if (view.getId() == R.id.due_only_text_view) {

        } else if (view.getId() == R.id.popup_menu) {

            if (popupMenu == null) {
                popupMenu = new PopupMenu(getActivity(), view);
                popupMenu.getMenuInflater().inflate(R.menu.home_main_menu, popupMenu.getMenu());
                popupMenu.getMenu().findItem(R.id.btn_logout).setTitle("Logout as " + Utils.getPrefferedName());

                popupMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.btn_sync) {
                        SyncServiceJob.scheduleJobImmediately(SyncServiceJob.TAG);
                        SyncSettingsServiceJob.scheduleJobImmediately(SyncSettingsServiceJob.TAG);
                    } else if (item.getItemId() == R.id.btn_logout) {
                        DrishtiApplication.getInstance().logoutCurrentUser();
                    }

                    return true;
                });
            }

            popupMenu.show();
        }
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        Utils.postEvent(new SyncEvent(fetchStatus));
    }

    @SuppressLint("NewApi")
    @Override
    public void showNotFoundPopup(String whoAncId) {
        NoMatchDialogFragment
                .launchDialog((BaseRegisterActivity) Objects.requireNonNull(getActivity()), SecuredNativeSmartRegisterFragment.DIALOG_TAG, whoAncId);
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> visibleColumns) {
        RegisterProvider registerProvider =
                new RegisterProvider(getActivity(), commonRepository(), visibleColumns, registerActionHandler,
                        paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, registerProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public void recalculatePagination(AdvancedMatrixCursor matrixCursor) {
        clientAdapter.setTotalcount(matrixCursor.getCount());
        Timber.tag("total count here").v("%d", clientAdapter.getTotalcount());
        clientAdapter.setCurrentlimit(20);
        if (clientAdapter.getTotalcount() > 0) {
            clientAdapter.setCurrentlimit(clientAdapter.getTotalcount());
        }
        clientAdapter.setCurrentoffset(0);
    }

    public void updateSortAndFilter(List<Field> filterList, Field sortField) {
        ((RegisterFragmentPresenter) presenter).updateSortAndFilter(filterList, sortField);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final AdvancedMatrixCursor matrixCursor = ((RegisterFragmentPresenter) presenter).getMatrixCursor();
        if (!globalQrSearch || matrixCursor == null) {
            return super.onCreateLoader(id, args);
        } else {
            globalQrSearch = false;
            if (id == RecyclerViewFragment.LOADER_ID) {// Returns a new CursorLoader
                return new CursorLoader(getActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        return matrixCursor;
                    }
                };
            }// An invalid id was passed in
            return null;
        }
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
        if (popupMenu != null) {
            popupMenu.getMenu().findItem(R.id.btn_sync).setTitle(String.format(getString(R.string.last_synced), new SimpleDateFormat("hh:mm a", Utils.getDefaultLocale()).format(new Date()), new SimpleDateFormat("MMM dd", Utils.getDefaultLocale()).format(new Date())));
        }
    }

    private void refreshSyncStatusViews(FetchStatus fetchStatus) {

        if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
            org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.syncing));
            Timber.i( getString(org.smartregister.R.string.syncing));
        } else {
            if (fetchStatus != null) {

                if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
                    if(fetchStatus.displayValue().equals(ResponseErrorStatus.malformed_url.name())) {
                        org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.sync_failed_malformed_url));
                        Timber.i( getString(org.smartregister.R.string.sync_failed_malformed_url));
                    }
                    else if (fetchStatus.displayValue().equals(ResponseErrorStatus.timeout.name())) {
                        org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.sync_failed_timeout_error));
                        Timber.i(getString(org.smartregister.R.string.sync_failed_timeout_error));
                    }
                    else {
                        org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.sync_failed));
                        Timber.i(getString(org.smartregister.R.string.sync_failed));
                    }

                } else if (fetchStatus.equals(FetchStatus.fetched)
                        || fetchStatus.equals(FetchStatus.nothingFetched)) {

                    setRefreshList(true);
                    renderView();

                    org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.sync_complete));
                    Timber.i( getString(org.smartregister.R.string.sync_complete));

                } else if (fetchStatus.equals(FetchStatus.noConnection)) {

                    org.smartregister.util.Utils.showShortToast(getActivity(), getString(org.smartregister.R.string.sync_failed_no_internet));
                    Timber.i( getString(org.smartregister.R.string.sync_failed_no_internet));
                }
            }
            else{
                Timber.i("Fetch Status NULL");
            }

        }

        refreshSyncProgressSpinner();
    }

    private void renderView() {
        getDefaultOptionsProvider();
        if (isPausedOrRefreshList()) {
            presenter.initializeQueries(getMainCondition());
        }
        updateSearchView();
        presenter.processViewConfigurations();
        // updateLocationText();
        refreshSyncProgressSpinner();
        setTotalPatients();
    }
}

