package org.smartregister.fp.common.presenter;

import org.smartregister.fp.common.contact.BaseCharacteristicsContract;
import org.smartregister.fp.common.interactor.SiteCharacteristicsInteractor;

/**
 * Created by ndegwamartin on 28/08/2018.
 */
public class SiteCharacteristicsPresenter extends BaseCharacteristicsPresenter {

    public SiteCharacteristicsPresenter(BaseCharacteristicsContract.View view) {
        super(view);
    }

    @Override
    public BaseCharacteristicsContract.Interactor getInteractor() {
        return new SiteCharacteristicsInteractor(this);
    }
}
