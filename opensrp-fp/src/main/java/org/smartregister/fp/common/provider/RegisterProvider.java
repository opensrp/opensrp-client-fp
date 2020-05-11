package org.smartregister.fp.common.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.text.WordUtils;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewProvider;
import org.smartregister.fp.R;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.schedules.SchedulesEnum;
import org.smartregister.fp.features.home.view.HomeRegisterFragment;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

import java.text.MessageFormat;
import java.util.Set;

import static org.smartregister.fp.common.util.ConstantsUtils.DateFormatPatternUtils.DAY_MONTH_YEAR_FORMAT;
import static org.smartregister.fp.common.util.ConstantsUtils.DateFormatPatternUtils.FP_ALERT_RULE_FORMAT;


public class RegisterProvider implements RecyclerViewProvider<RegisterProvider.RegisterViewHolder> {
    private final LayoutInflater inflater;
    private Set<org.smartregister.configurableviews.model.View> visibleColumns;

    private View.OnClickListener onClickListener;
    private View.OnClickListener paginationClickListener;

    private Context context;
    private CommonRepository commonRepository;

    public RegisterProvider(Context context, CommonRepository commonRepository, Set visibleColumns,
                            View.OnClickListener onClickListener, View.OnClickListener paginationClickListener) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.visibleColumns = visibleColumns;

        this.onClickListener = onClickListener;
        this.paginationClickListener = paginationClickListener;

        this.context = context;
        this.commonRepository = commonRepository;
    }

    @Override
    public void getView(Cursor cursor, SmartRegisterClient client, RegisterViewHolder viewHolder) {
        CommonPersonObjectClient pc = (CommonPersonObjectClient) client;
        if (visibleColumns.isEmpty()) {
            populatePatientColumn(pc, client, viewHolder);
            populateIdentifierColumn(pc, viewHolder);
            populateAlertButtonAndMethodExit(pc, viewHolder);
            populateMethodExitColumn(pc, viewHolder);

        }
    }

    private void populateMethodExitColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, false);
        String nextContact = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.NEXT_CONTACT, false);
        if (baseEntityId != null && !baseEntityId.isEmpty()
                && nextContact != null && !nextContact.isEmpty()) {
            String methodExitKey = Utils.getMapValue(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT, baseEntityId, Integer.parseInt(nextContact));
            String methodName = Utils.getMethodName(methodExitKey);
            viewHolder.methodExitTv.setText(methodName);
        }
    }

    @Override
    public void getFooterView(RecyclerView.ViewHolder viewHolder, int currentPageCount, int totalPageCount, boolean hasNext,
                              boolean hasPrevious) {
        FooterViewHolder footerViewHolder = (FooterViewHolder) viewHolder;
        footerViewHolder.pageInfoView
                .setText(MessageFormat.format(context.getString(R.string.str_page_info), currentPageCount, totalPageCount));

        footerViewHolder.nextPageView.setVisibility(hasNext ? View.VISIBLE : View.INVISIBLE);
        footerViewHolder.previousPageView.setVisibility(hasPrevious ? View.VISIBLE : View.INVISIBLE);

        footerViewHolder.nextPageView.setOnClickListener(paginationClickListener);
        footerViewHolder.previousPageView.setOnClickListener(paginationClickListener);
    }

    @Override
    public SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                              FilterOption searchFilter, SortOption sortOption) {
        return null;
    }

    @Override
    public void onServiceModeSelected(ServiceModeOption serviceModeOption) {//Implement Abstract Method
    }

    @Override
    public OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData) {
        return null;
    }

    @Override
    public LayoutInflater inflater() {
        return inflater;
    }

    @Override
    public RegisterViewHolder createViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.register_home_list_row, parent, false);
        return new RegisterViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder createFooterHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.smart_register_pagination, parent, false);
        return new FooterViewHolder(view);
    }

    @Override
    public boolean isFooterViewHolder(RecyclerView.ViewHolder viewHolder) {
        return viewHolder instanceof FooterViewHolder;
    }

    private void populatePatientColumn(CommonPersonObjectClient pc, SmartRegisterClient client,
                                       RegisterViewHolder viewHolder) {

        String firstName = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.FIRST_NAME, true);
        String lastName = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.LAST_NAME, true);
        String patientName = Utils.getName(firstName, lastName);


        String dobString;

        String DOBUnknown = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.DOB_UNKNOWN, false);
        if (DOBUnknown != null && DOBUnknown.equalsIgnoreCase("true"))
            dobString = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.AGE_ENTERED, false);
        else {
            String DOB = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.DOB, false);
            dobString = DOB.isEmpty() ? "" : String.valueOf(Utils.getAgeFromDate(DOB));
        }

        fillValue(viewHolder.patientName, WordUtils.capitalize(patientName) + ", " + dobString);

        View patient = viewHolder.patientColumn;
        attachPatientOnclickListener(patient, client);

        /*
        String redFlagCountRaw = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, false);
        String yellowFlagCountRaw = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, false);

        int redFlagCount = !TextUtils.isEmpty(redFlagCountRaw) ? Integer.valueOf(redFlagCountRaw) : 0;
        int yellowFlagCount = !TextUtils.isEmpty(yellowFlagCountRaw) ? Integer.valueOf(yellowFlagCountRaw) : 0;
        int totalFlagCount = yellowFlagCount + redFlagCount;

        TextView riskLayout = viewHolder.risk;

        if (totalFlagCount > 0) {
            riskLayout.setCompoundDrawablesWithIntrinsicBounds(
                    redFlagCount > 0 ? R.drawable.ic_red_flag : R.drawable.ic_yellow_flag, 0, 0, 0);
            riskLayout.setText(String.valueOf(totalFlagCount));
            riskLayout.setVisibility(View.VISIBLE);

            attachRiskLayoutOnclickListener(riskLayout, client);
        } else {
            riskLayout.setVisibility(View.GONE);
        }*/
    }

    private void populateIdentifierColumn(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {
        String fpId = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.FP_ID, false);
        fillValue(viewHolder.ancId, String.format(context.getString(R.string.anc_id_text), fpId));
    }

    private void populateAlertButtonAndMethodExit(CommonPersonObjectClient pc, RegisterViewHolder viewHolder) {

        String baseEntityId = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, false);
        String nextContact = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.NEXT_CONTACT, false);
        String nextContactDate = Utils.getValue(pc.getColumnmaps(), DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, false);

        if (!baseEntityId.isEmpty() && nextContact != null && !nextContact.isEmpty()) {
            // user visit exists
            String methodExitKey = Utils.getMapValue(ConstantsUtils.JsonFormFieldUtils.METHOD_EXIT, baseEntityId, Integer.parseInt(nextContact));
            String methodName = Utils.getMethodName(methodExitKey);
            if (methodName != null && !methodName.isEmpty()) {
                // populate method exit
                viewHolder.methodExitTv.setText(methodName);
                // check non trigger events
                if (Utils.checkNonTriggerEvents(methodName)) {
                    //  populate alert status
                    for (SchedulesEnum schedulesEnum : SchedulesEnum.values()) {
                        if (schedulesEnum.getScheduleModel().getTriggerEventTag().equals(methodName)) {
                            String triggerDate = Utils.getMapValue(schedulesEnum.getScheduleModel().getTriggerDateTag(), baseEntityId, Integer.parseInt(nextContact));
                            triggerDate = Utils.formatDateToPattern(triggerDate, DAY_MONTH_YEAR_FORMAT, FP_ALERT_RULE_FORMAT);
                            ButtonAlertStatus buttonAlertStatus = Utils.getButtonFollowupStatus(triggerDate, schedulesEnum.getScheduleModel(), baseEntityId, nextContactDate);
                            Utils.processFollowupVisitButton(context, viewHolder.followupBtn, buttonAlertStatus, baseEntityId, pc.getColumnmaps());
                            break;
                        }
                    }
                }
            }
        }
    }

    //     attachSyncOnclickListener(viewHolder.sync, pc);
    public static void fillValue(TextView v, String value) {
        if (v != null) v.setText(value);

    }

    private void attachPatientOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, HomeRegisterFragment.CLICK_VIEW_NORMAL);
    }

    private void attachRiskLayoutOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, HomeRegisterFragment.CLICK_VIEW_ATTENTION_FLAG);
    }

    private void attachSyncOnclickListener(View view, SmartRegisterClient client) {
        view.setOnClickListener(onClickListener);
        view.setTag(client);
        view.setTag(R.id.VIEW_ID, HomeRegisterFragment.CLICK_VIEW_SYNC);
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////

    public static class RegisterViewHolder extends RecyclerView.ViewHolder {
        private TextView patientName;
        private TextView ancId;
        private TextView risk;
        private Button sync;
        private View patientColumn;
        private Button followupBtn;
        private CustomFontTextView methodExitTv;

        public RegisterViewHolder(View itemView) {
            super(itemView);
            patientName = itemView.findViewById(R.id.patient_name);
            ancId = itemView.findViewById(R.id.fp_id);
            sync = itemView.findViewById(R.id.sync);
            patientColumn = itemView.findViewById(R.id.patient_column);
            followupBtn = itemView.findViewById(R.id.btn_followup);
            methodExitTv = itemView.findViewById(R.id.tv_method_exit);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        private TextView pageInfoView;
        private Button nextPageView;
        private Button previousPageView;

        public FooterViewHolder(View view) {
            super(view);

            nextPageView = view.findViewById(org.smartregister.R.id.btn_next_page);
            previousPageView = view.findViewById(org.smartregister.R.id.btn_previous_page);
            pageInfoView = view.findViewById(org.smartregister.R.id.txt_page_info);
        }
    }
}
