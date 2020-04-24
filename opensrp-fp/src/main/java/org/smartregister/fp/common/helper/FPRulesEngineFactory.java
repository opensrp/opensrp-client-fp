package org.smartregister.fp.common.helper;

import android.content.Context;

import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.rules.RulesEngineFactory;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.json.JSONObject;

import java.util.Map;

public class FPRulesEngineFactory extends RulesEngineFactory {
    private Map<String, String> globalValues;
    private FPRulesEngineHelper FPRulesEngineHelper;
    private String selectedRuleName;


    public FPRulesEngineFactory(Context context, Map<String, String> globalValues, JSONObject mJSONObject) {
        super(context, globalValues);
        this.FPRulesEngineHelper = new FPRulesEngineHelper(context);
        this.FPRulesEngineHelper.setJsonObject(mJSONObject);
        this.globalValues = globalValues;

    }

    @Override
    protected Facts initializeFacts(Facts facts) {
        if (globalValues != null) {
            for (Map.Entry<String, String> entry : globalValues.entrySet()) {
                facts.put(RuleConstant.PREFIX.GLOBAL + entry.getKey(), getValue(entry.getValue()));
            }

            facts.asMap().putAll(globalValues);
        }

        selectedRuleName = facts.get(RuleConstant.SELECTED_RULE);

        facts.put("helper", FPRulesEngineHelper);
        return facts;
    }

    @Override
    public boolean beforeEvaluate(Rule rule, Facts facts) {
        return selectedRuleName != null && selectedRuleName.equals(rule.getName());
    }
}
