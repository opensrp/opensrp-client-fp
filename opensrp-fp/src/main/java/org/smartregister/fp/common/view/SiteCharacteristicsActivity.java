package org.smartregister.fp.common.view;

import android.os.Bundle;
import android.view.View;

import org.smartregister.fp.R;
import org.smartregister.fp.common.contact.BaseCharacteristicsContract;
import org.smartregister.fp.common.presenter.CharacteristicsPresenter;
import org.smartregister.fp.common.presenter.SiteCharacteristicsPresenter;

/**
 * Created by ndegwamartin on 30/08/2018.
 */
public class SiteCharacteristicsActivity extends BaseCharacteristicsActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToolbar.findViewById(R.id.characteristics_toolbar_edit).setOnClickListener(this);
        presenter = new CharacteristicsPresenter(this);
    }

    @Override
    protected BaseCharacteristicsContract.BasePresenter getPresenter() {
        return new SiteCharacteristicsPresenter(this);
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.site_characteristics);
    }

    @Override
    public void onClick(View view) {
        presenter.launchSiteCharacteristicsFormForEdit();
    }
}
