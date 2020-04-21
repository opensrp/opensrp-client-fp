package org.smartregister.fp.features.profile.interactor;


import org.smartregister.fp.common.contact.PreviousContactsTests;
import org.smartregister.fp.common.task.FetchProfileDataTask;

public class PreviousContactsTestsInteractor implements PreviousContactsTests.Interactor {
    private PreviousContactsTests.Presenter previousContactsTestsPresenter;

    public PreviousContactsTestsInteractor(PreviousContactsTests.Presenter presenter) {
        this.previousContactsTestsPresenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            previousContactsTestsPresenter = null;
        }
    }

    @Override
    public void refreshProfileView(String baseEntityId, boolean isForEdit) {
        new FetchProfileDataTask(isForEdit).execute(baseEntityId);
    }

}
