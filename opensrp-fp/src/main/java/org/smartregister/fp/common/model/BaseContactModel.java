package org.smartregister.fp.common.model;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.util.FormUtils;

import java.util.Map;

public abstract class BaseContactModel {
    private FormUtils formUtils;

    protected String extractPatientName(Map<String, String> womanDetails) {
        String firstName = extractValue(womanDetails, DBConstantsUtils.KeyUtils.FIRST_NAME);
        String lastName = extractValue(womanDetails, DBConstantsUtils.KeyUtils.LAST_NAME);

        if (StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName)) {
            return "";
        } else if (StringUtils.isBlank(firstName)) {
            return lastName.trim();
        } else if (StringUtils.isBlank(lastName)) {
            return firstName.trim();
        } else {
            return firstName.trim() + " " + lastName.trim();
        }

    }

    private String extractValue(Map<String, String> details, String key) {
        if (details == null || details.isEmpty() || StringUtils.isBlank(key)) {
            return "";
        }

        return details.get(key);
    }
}
