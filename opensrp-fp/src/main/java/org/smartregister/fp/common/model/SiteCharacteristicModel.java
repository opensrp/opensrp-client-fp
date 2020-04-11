package org.smartregister.fp.common.model;

import org.smartregister.fp.common.contact.SiteCharacteristicsContract;
import org.smartregister.fp.common.util.FPJsonFormUtils;

import java.util.Map;

public class SiteCharacteristicModel implements SiteCharacteristicsContract.Model {


    @Override
    public Map<String, String> processSiteCharacteristics(String jsonString) {
        return FPJsonFormUtils.processSiteCharacteristics(jsonString);
    }

}
