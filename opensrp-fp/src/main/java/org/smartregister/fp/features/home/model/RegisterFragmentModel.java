package org.smartregister.fp.features.home.model;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.configurableviews.ConfigurableViewsLibrary;
import org.smartregister.configurableviews.model.Field;
import org.smartregister.configurableviews.model.RegisterConfiguration;
import org.smartregister.configurableviews.model.View;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.domain.Response;
import org.smartregister.domain.ResponseStatus;
import org.smartregister.fp.common.cursor.AdvancedMatrixCursor;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.util.ConfigHelperUtils;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.features.home.contract.RegisterFragmentContract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.smartregister.fp.common.util.ConstantsUtils.GLOBAL_IDENTIFIER;


public class RegisterFragmentModel implements RegisterFragmentContract.Model {

    @Override
    public RegisterConfiguration defaultRegisterConfiguration() {
        return ConfigHelperUtils.defaultRegisterConfiguration(FPLibrary.getInstance().getApplicationContext());
    }

    @Override
    public ViewConfiguration getViewConfiguration(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper()
                .getViewConfiguration(viewConfigurationIdentifier);
    }

    @Override
    public Set<View> getRegisterActiveColumns(String viewConfigurationIdentifier) {
        return ConfigurableViewsLibrary.getInstance().getConfigurableViewsHelper()
                .getRegisterActiveColumns(viewConfigurationIdentifier);
    }

    @Override
    public String countSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder countQueryBuilder = new SmartRegisterQueryBuilder();
        countQueryBuilder.SelectInitiateMainTableCounts(tableName);
        return countQueryBuilder.mainCondition(mainCondition);
    }

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        String[] columns =
                new String[]{tableName + ".relationalid", tableName + "." + DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH,
                        tableName + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID, tableName + "." + DBConstantsUtils.KeyUtils.CLIENT_ID,
                        tableName + "." + DBConstantsUtils.KeyUtils.CLIENT_ID_NOTE, tableName + "." + DBConstantsUtils.KeyUtils.FIRST_NAME,
                        tableName + "." + DBConstantsUtils.KeyUtils.LAST_NAME, tableName + "." + DBConstantsUtils.KeyUtils.DOB,
                        tableName + "." + DBConstantsUtils.KeyUtils.AGE_ENTERED, tableName + "." + DBConstantsUtils.KeyUtils.DOB_UNKNOWN,
                        tableName + "." + DBConstantsUtils.KeyUtils.REGISTRATION_DATE, tableName + "." + DBConstantsUtils.KeyUtils.REFERRAL,
                        tableName + "." + DBConstantsUtils.KeyUtils.REFERRED_BY, tableName + "." + DBConstantsUtils.KeyUtils.UNIVERSAL_ID,
                        tableName + "." + DBConstantsUtils.KeyUtils.AGE_FROM_DOB, tableName + "." + DBConstantsUtils.KeyUtils.DOB_FROM_AGE,
                        tableName + "." + DBConstantsUtils.KeyUtils.AGE, tableName + "." + DBConstantsUtils.KeyUtils.GENDER,
                        tableName + "." + DBConstantsUtils.KeyUtils.BIOLOGICAL_SEX, tableName + "." + DBConstantsUtils.KeyUtils.METHOD_GENDER_TYPE,
                        tableName + "." + DBConstantsUtils.KeyUtils.MARITAL_STATUS, tableName + "." + DBConstantsUtils.KeyUtils.ADMIN_AREA,
                        tableName + "." + DBConstantsUtils.KeyUtils.CLIENT_ADDRESS, tableName + "." + DBConstantsUtils.KeyUtils.TEL_NUMBER,
                        tableName + "." + DBConstantsUtils.KeyUtils.COMM_CONSENT, tableName + "." + DBConstantsUtils.KeyUtils.REMINDER_MESSAGE,
                        tableName + "." + DBConstantsUtils.KeyUtils.LAST_INTERACTED_WITH, tableName + "." + DBConstantsUtils.KeyUtils.DATE_REMOVED};
        queryBuilder.SelectInitiateMainTable(tableName, columns);
        /*queryBuilder.customJoin(" join " + getRegisterQueryProvider().getDetailsTable()
                + " on " + getRegisterQueryProvider().getDemographicTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID + "= " + getRegisterQueryProvider().getDetailsTable() + "." + DBConstantsUtils.KeyUtils.BASE_ENTITY_ID); */
        return queryBuilder.mainCondition(mainCondition);
    }

    @Override
    public String getFilterText(List<Field> list, String filterTitle) {
        List<? extends Field> filterList = list;
        if (filterList == null) {
            filterList = new ArrayList<>();
        }

        String filter = filterTitle;
        if (filter == null) {
            filter = "";
        }
        return "<font color=#727272>" + filter + "</font> <font color=#f0ab41>(" + filterList.size() + ")</font>";
    }

    @Override
    public String getSortText(Field sortField) {
        String sortText = "";
        if (sortField != null) {
            if (StringUtils.isNotBlank(sortField.getDisplayName())) {
                sortText = "(Sort: " + sortField.getDisplayName() + ")";
            } else if (StringUtils.isNotBlank(sortField.getDbAlias())) {
                sortText = "(Sort: " + sortField.getDbAlias() + ")";
            }
        }
        return sortText;
    }

    @Override
    public Map<String, String> createEditMap(String ancId) {
        Map<String, String> editMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(ancId)) {

            editMap.put(GLOBAL_IDENTIFIER, ConstantsUtils.IdentifierUtils.ANC_ID + ":" + ancId);
        }
        return editMap;
    }

    @Override
    public AdvancedMatrixCursor createMatrixCursor(Response<String> response) {
        String[] columns = new String[]{"_id", "relationalid", DBConstantsUtils.KeyUtils.FIRST_NAME, DBConstantsUtils.KeyUtils.LAST_NAME,
                DBConstantsUtils.KeyUtils.CLIENT_ID};
        AdvancedMatrixCursor matrixCursor = new AdvancedMatrixCursor(columns);

        if (response == null || response.isFailure() || StringUtils.isBlank(response.payload())) {
            return matrixCursor;
        }

        JSONArray jsonArray = getJsonArray(response);
        if (jsonArray != null) {

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject client = getJsonObject(jsonArray, i);
                String entityId;
                String firstName;
                String lastName;
                String clientId;
                if (client == null) {
                    continue;
                }

                // Skip deceased children
                if (StringUtils.isNotBlank(getJsonString(client, "deathdate"))) {
                    continue;
                }

                entityId = getJsonString(client, "baseEntityId");
                firstName = getJsonString(client, "firstName");
                lastName = getJsonString(client, "lastName");
                clientId = getJsonString(getJsonObject(client, "identifiers"), DBConstantsUtils.KeyUtils.CLIENT_ID);
                if (StringUtils.isNotBlank(clientId)) {
                    clientId = clientId.replace("-", "");
                }


                matrixCursor
                        .addRow(new Object[]{entityId, null, firstName, lastName, clientId});
            }
        }
        return matrixCursor;
    }

    private JSONObject getJsonObject(JSONArray jsonArray, int position) {
        try {
            if (jsonArray != null && jsonArray.length() > 0) {
                return jsonArray.getJSONObject(position);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;
    }

    private String getJsonString(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                String string = jsonObject.getString(field);
                if (StringUtils.isBlank(string)) {
                    return "";
                } else {
                    return string;
                }
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return "";
    }

    private JSONObject getJsonObject(JSONObject jsonObject, String field) {
        try {
            if (jsonObject != null && jsonObject.has(field)) {
                return jsonObject.getJSONObject(field);
            }
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;

    }

    @Override
    public JSONArray getJsonArray(Response<String> response) {
        try {
            if (response.status().equals(ResponseStatus.success)) {
                return new JSONArray(response.payload());
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

}
