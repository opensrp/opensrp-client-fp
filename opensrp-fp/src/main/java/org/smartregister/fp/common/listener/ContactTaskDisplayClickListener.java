package org.smartregister.fp.common.listener;

import android.content.Context;
import android.view.View;

import com.vijay.jsonwizard.domain.ExpansionPanelValuesModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.fp.R;
import org.smartregister.fp.common.model.Task;
import org.smartregister.fp.common.util.FPFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.fp.features.profile.view.ProfileTasksFragment;

import java.util.Map;

public class ContactTaskDisplayClickListener implements View.OnClickListener {
    private ProfileTasksFragment profileTasksFragment;
    private FPFormUtils ANCFormUtils = new FPFormUtils();

    public ContactTaskDisplayClickListener(ProfileTasksFragment profileTasksFragment) {
        this.profileTasksFragment = profileTasksFragment;
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            if (view.getId() == R.id.accordion_info_icon) {
                Utils.infoAlertDialog(view);
            } else if (view.getId() == R.id.undo_button) {
                undoTasksEntries(view);
            } else {
                prepFormForLaunch(view);
            }
        }
    }



    /**
     * Intitiates the undo tasks functionality
     *
     * @param view {@link View}
     */
    private void undoTasksEntries(View view) {
        Context context = ((Context) view.getTag(R.id.accordion_context));
        Task task = ((Task) view.getTag(R.id.task_object));
        JSONObject taskValue = ((JSONObject) view.getTag(R.id.accordion_jsonObject));

        if (context != null && task != null && taskValue != null) {
            Task newTask = Utils.createTask(Utils.removeTestResults(taskValue), task);
            profileTasksFragment.updateTask(newTask);
        }
    }

    /**
     * This performs all the necessary calculations to get the form ready for launch. This updates the title,
     * Adds the new form fields
     *
     * @param view {@link View}
     */
    private void prepFormForLaunch(View view) {
        Context context = ((Context) view.getTag(R.id.accordion_context));
        Task task = ((Task) view.getTag(R.id.task_object));
        JSONObject taskValue = ((JSONObject) view.getTag(R.id.accordion_jsonObject));

        if (context != null && task != null && taskValue != null) {
            JSONArray taskValues = Utils.getExpansionPanelValues(taskValue, task.getKey());
            Map<String, ExpansionPanelValuesModel> secondaryValuesMap = Utils.getSecondaryValues(taskValues);
            JSONArray subFormFields = ANCFormUtils.addExpansionPanelFormValues(Utils.loadSubFormFields(taskValue, context), secondaryValuesMap);
            String formTitle = Utils.getFormTitle(taskValue);
            JSONObject form = ANCFormUtils.loadTasksForm(context);
            Utils.updateFormTitle(form, formTitle);
            ANCFormUtils.updateFormFields(form, subFormFields);

            profileTasksFragment.startTaskForm(form, task);
        }
    }

}
