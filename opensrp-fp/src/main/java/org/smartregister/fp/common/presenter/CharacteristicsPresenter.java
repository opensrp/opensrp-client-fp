package org.smartregister.fp.common.presenter;


import org.smartregister.domain.ServerSetting;
import org.smartregister.fp.R;
import org.smartregister.fp.common.contact.SiteCharacteristicsContract;
import org.smartregister.fp.common.interactor.CharacteristicsInteractor;
import org.smartregister.fp.common.model.SiteCharacteristicModel;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.sync.helper.ServerSettingsHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 27/08/2018.
 */
public class CharacteristicsPresenter implements SiteCharacteristicsContract.Presenter {

    private WeakReference<SiteCharacteristicsContract.View> view;
    private SiteCharacteristicsContract.Interactor interactor;
    private SiteCharacteristicsContract.Model model;

    public CharacteristicsPresenter(SiteCharacteristicsContract.View view) {
        this.view = new WeakReference<>(view);
        interactor = new CharacteristicsInteractor();
        model = new SiteCharacteristicModel();
    }

    @Override
    public void onDestroy(boolean isChangingConfiguration) {

        this.view = null;

    }

    @Override
    public void launchSiteCharacteristicsForm() {
        getSiteCharacteristicsView().launchSiteCharacteristicsSettingsForm();
    }

    @Override
    public void launchSiteCharacteristicsFormForEdit() {
        Map<String, String> settings = getSettingsMapByType(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);
        getSiteCharacteristicsView().launchSiteCharacteristicsSettingsFormForEdit(settings);
    }

    protected Map<String, String> getSettingsMapByType(String characteristicType) {
        List<ServerSetting> characteristicList = ServerSettingsHelper.fetchServerSettingsByTypeKey(characteristicType);

        Map<String, String> settingsMap = new HashMap<>();
        for (ServerSetting characteristic : characteristicList) {
            settingsMap.put(characteristic.getKey(), String.valueOf(characteristic.getValue()));
        }
        return settingsMap;
    }

    @Override
    public SiteCharacteristicsContract.View getSiteCharacteristicsView() {
        if (this.view != null) {
            return this.view.get();
        } else {
            return null;
        }
    }

    @Override
    public void saveSiteCharacteristics(String jsonString) {

        getSiteCharacteristicsView().showProgressDialog(R.string.saving_dialog_title);

        Map<String, String> settings = model.processSiteCharacteristics(jsonString);
        try {

            interactor.saveSiteCharacteristics(settings);

        } catch (Exception e) {
            Timber.e(CharacteristicsPresenter.class.getCanonicalName(), e.getMessage());
        }

        getSiteCharacteristicsView().hideProgressDialog();
        getSiteCharacteristicsView().goToLastPage();
    }
}
