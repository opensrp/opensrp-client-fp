package org.smartregister.fp.features.profile.interactor;

import org.smartregister.fp.common.task.FetchProfileDataTask;
import org.smartregister.fp.features.profile.contract.PreviousContactsDetails;

public class PreviousContactsDetailsInteractor implements PreviousContactsDetails.Interactor {
    private PreviousContactsDetails.Presenter previousContactsPresenter;

    public PreviousContactsDetailsInteractor(PreviousContactsDetails.Presenter presenter) {
        this.previousContactsPresenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            previousContactsPresenter = null;
        }
    }

    @Override
    public void refreshProfileView(String baseEntityId, boolean isForEdit) {
        new FetchProfileDataTask(isForEdit).execute(baseEntityId);
    }


    public PreviousContactsDetails.View getProfileView() {
        return previousContactsPresenter.getProfileView();
    }
}
