package org.smartregister.fp.features.profile.presenter;

import android.text.TextUtils;

import org.jeasy.rules.api.Facts;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.ContactSummaryModel;
import org.smartregister.fp.common.model.PreviousContactsSummaryModel;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.profile.contract.PreviousContactsDetailsContract;
import org.smartregister.fp.features.profile.interactor.PreviousContactsDetailsInteractor;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public class PreviousContactDetailsPresenter implements PreviousContactsDetailsContract.Presenter {
    private FPJsonFormUtils formUtils = new FPJsonFormUtils();

    private WeakReference<PreviousContactsDetailsContract.View> mProfileView;
    private PreviousContactsDetailsContract.Interactor mProfileInteractor;

    public PreviousContactDetailsPresenter(PreviousContactsDetailsContract.View profileView) {
        mProfileView = new WeakReference<>(profileView);
        mProfileInteractor = new PreviousContactsDetailsInteractor(this);
    }

    public void onDestroy(boolean isChangingConfiguration) {
        mProfileView = null;//set to null on destroy

        // Inform interactor
        if (mProfileInteractor != null) {
            mProfileInteractor.onDestroy(isChangingConfiguration);
        }

        // Activity destroyed set interactor to null
        if (!isChangingConfiguration) {
            mProfileInteractor = null;
        }
    }

    @Override
    public PreviousContactsDetailsContract.View getProfileView() {
        if (mProfileView != null) {
            return mProfileView.get();
        } else {
            return null;
        }
    }

    @Override
    public void loadPreviousContactSchedule(String baseEntityId, String contactNo, String edd) {
        try {
            Facts immediatePreviousSchedule = getPreviousContactRepository()
                    .getImmediatePreviousSchedule(baseEntityId, contactNo);
            String contactScheduleString = "";
            if (immediatePreviousSchedule != null) {
                Map<String, Object> scheduleMap = immediatePreviousSchedule.asMap();
                for (Map.Entry<String, Object> entry : scheduleMap.entrySet()) {
                    if (ConstantsUtils.CONTACT_SCHEDULE.equals(entry.getKey())) {
                        contactScheduleString = entry.getValue().toString();
                    }
                }
            }

            List<String> scheduleList = new ArrayList<>();
            if (!TextUtils.isEmpty(contactScheduleString)) {
                scheduleList = Utils.getListFromString(contactScheduleString);
            }
            List<ContactSummaryModel> schedule = new ArrayList<>();
            if (!TextUtils.isEmpty(edd)) {
                Date lastContactEdd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(edd);
                String formattedEdd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(lastContactEdd);
                schedule = formUtils.generateNextContactSchedule(formattedEdd, scheduleList, Integer.valueOf(contactNo));
            }

            getProfileView().displayPreviousContactSchedule(schedule);
        } catch (ParseException e) {
            Timber.e(e);
        }
    }

    @Override
    public void loadPreviousContacts(String baseEntityId, String contactNo) {
        List<PreviousContactsSummaryModel> allContactsFacts = getPreviousContactRepository()
                .getPreviousContactsFacts(baseEntityId);
        LinkedHashMap<String, List<Facts>> filteredContacts = new LinkedHashMap<>();

        if (allContactsFacts != null && allContactsFacts.size() > 0 && !TextUtils.isEmpty(contactNo)) {
            Collections.reverse(allContactsFacts);
            for (PreviousContactsSummaryModel previousContactsSummaryModel : allContactsFacts) {
                String currentContactInLoop = previousContactsSummaryModel.getContactNumber();
                List<Facts> factsList = filteredContacts.get(currentContactInLoop);
                if (factsList != null && factsList.size() > 0) {
                    factsList.add(previousContactsSummaryModel.getVisitFacts());
                    filteredContacts.put(currentContactInLoop, factsList);
                } else {
                    List<Facts> newList = new ArrayList<>();
                    newList.add(previousContactsSummaryModel.getVisitFacts());
                    filteredContacts.put(currentContactInLoop, newList);
                }
            }
        }

        try {
            getProfileView().loadPreviousContactsDetails(filteredContacts);
        } catch (IOException e) {
            Timber.e(e);
        } catch (ParseException e) {
            Timber.e(e);
        }

    }

    private PreviousContactRepository getPreviousContactRepository() {
        return FPLibrary.getInstance().getPreviousContactRepository();
    }
}
