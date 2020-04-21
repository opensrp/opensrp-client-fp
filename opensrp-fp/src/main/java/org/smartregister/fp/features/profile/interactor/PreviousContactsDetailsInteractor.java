package org.smartregister.fp.features.profile.interactor;

import org.smartregister.fp.common.task.FetchProfileDataTask;
import org.smartregister.fp.features.profile.contract.PreviousContactsDetailsContract;

public class PreviousContactsDetailsInteractor implements PreviousContactsDetailsContract.Interactor {
    private PreviousContactsDetailsContract.Presenter previousContactsPresenter;

    public PreviousContactsDetailsInteractor(PreviousContactsDetailsContract.Presenter presenter) {
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


    public PreviousContactsDetailsContract.View getProfileView() {
        return previousContactsPresenter.getProfileView();
    }
}
