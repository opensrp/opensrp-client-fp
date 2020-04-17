package org.smartregister.fp.common.helper;

import org.smartregister.fp.common.util.DBConstantsUtils;

/**
 * Created by ndegwamartin on 28/01/2018.
 */

public class DBQueryHelper {
    public static final String getHomePatientRegisterCondition() {
        return DBConstantsUtils.KeyUtils.DATE_REMOVED + " IS NULL";
    }
}
