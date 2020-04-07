package org.smartregister.fp.common.task;

import android.os.AsyncTask;

import org.jeasy.rules.api.Facts;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.fp.FPLibrary;
import org.smartregister.fp.common.domain.AttentionFlag;
import org.smartregister.fp.common.domain.YamlConfig;
import org.smartregister.fp.common.domain.YamlConfigItem;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.FilePathUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.home.view.HomeRegisterActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import timber.log.Timber;

public class AttentionFlagsTask extends AsyncTask<Void, Void, Void> {
    private HomeRegisterActivity baseHomeRegisterActivity;
    private List<AttentionFlag> attentionFlagList = new ArrayList<>();
    private CommonPersonObjectClient pc;

    public AttentionFlagsTask(HomeRegisterActivity baseHomeRegisterActivity, CommonPersonObjectClient pc) {
        this.baseHomeRegisterActivity = baseHomeRegisterActivity;
        this.pc = pc;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            JSONObject jsonObject = new JSONObject(FPLibrary.getInstance().getDetailsRepository().getAllDetailsForClient(pc.getCaseId()).get(ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS));

            Facts facts = new Facts();
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                facts.put(key, jsonObject.get(key));
            }

            Iterable<Object> ruleObjects = FPLibrary.getInstance().readYaml(FilePathUtils.FileUtils.ATTENTION_FLAGS);

            for (Object ruleObject : ruleObjects) {
                YamlConfig attentionFlagConfig = (YamlConfig) ruleObject;
                for (YamlConfigItem yamlConfigItem : attentionFlagConfig.getFields()) {
                    if (FPLibrary.getInstance().getAncRulesEngineHelper()
                            .getRelevance(facts, yamlConfigItem.getRelevance())) {
                        attentionFlagList
                                .add(new AttentionFlag(Utils.fillTemplate(yamlConfigItem.getTemplate(), facts),
                                        attentionFlagConfig.getGroup().equals(ConstantsUtils.AttentionFlagUtils.RED)));
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e, " --> ");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        baseHomeRegisterActivity.showAttentionFlagsDialog(attentionFlagList);
    }
}
