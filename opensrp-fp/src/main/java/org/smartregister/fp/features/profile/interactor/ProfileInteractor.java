package org.smartregister.fp.features.profile.interactor;

import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.task.FetchProfileDataTask;
import org.smartregister.fp.features.profile.contract.ProfileContract;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public class ProfileInteractor implements ProfileContract.Interactor {
    private ProfileContract.Presenter mProfilePresenter;

    public ProfileInteractor(ProfileContract.Presenter loginPresenter) {
        this.mProfilePresenter = loginPresenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {
        if (!isChangingConfiguration) {
            mProfilePresenter = null;
        }
    }

    @Override
    public void refreshProfileView(String baseEntityId, boolean isForEdit) {
        new FetchProfileDataTask(isForEdit).execute(baseEntityId);
    }

    @Override
    public String getTaskCount(String baseEntityId) {
        return FPLibrary.getInstance().getContactTasksRepository().getTasksCount(baseEntityId);
    }

}
