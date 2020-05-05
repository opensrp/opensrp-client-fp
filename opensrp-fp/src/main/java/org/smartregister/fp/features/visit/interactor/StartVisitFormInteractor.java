package org.smartregister.fp.features.visit.interactor;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;

import org.smartregister.fp.common.widgets.FPLabelFactory;

public class StartVisitFormInteractor extends JsonFormInteractor {

    @Override
    protected void registerWidgets() {
        super.registerWidgets();
        map.put(JsonFormConstants.LABEL, new FPLabelFactory());
    }
}
