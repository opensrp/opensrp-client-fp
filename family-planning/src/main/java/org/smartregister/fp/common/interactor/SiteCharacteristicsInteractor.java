package org.smartregister.fp.common.interactor;

import org.smartregister.fp.common.contact.BaseCharacteristicsContract;
import org.smartregister.fp.common.contact.PopulationCharacteristicsContract;
import org.smartregister.fp.common.task.FetchSiteCharacteristicsTask;

/**
 * Created by ndegwamartin on 28/08/2018.
 */
public class SiteCharacteristicsInteractor implements BaseCharacteristicsContract.Interactor {
    private PopulationCharacteristicsContract.Presenter presenter;

    public SiteCharacteristicsInteractor(PopulationCharacteristicsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        if (!isChangingConfiguration) {
            presenter = null;
        }
    }

    @Override
    public void fetchCharacteristics() {
        new FetchSiteCharacteristicsTask(presenter).execute();
    }

}
