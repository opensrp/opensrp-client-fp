package org.smartregister.fp.common.model;

import org.smartregister.fp.common.contact.SiteCharacteristicsContract;
import org.smartregister.fp.common.util.ANCJsonFormUtils;

import java.util.Map;

public class SiteCharacteristicModel implements SiteCharacteristicsContract.Model {


    @Override
    public Map<String, String> processSiteCharacteristics(String jsonString) {
        return ANCJsonFormUtils.processSiteCharacteristics(jsonString);
    }

}
