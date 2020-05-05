package org.smartregister.fp.common.widgets;

import android.content.Context;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.views.CustomTextView;
import com.vijay.jsonwizard.widgets.LabelFactory;

import org.json.JSONObject;

import java.util.List;

public class FPLabelFactory extends LabelFactory {

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        List<View> views = super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener);
        if (!views.isEmpty() && views.get(0) != null && views.get(0) instanceof ConstraintLayout) {
            ConstraintLayout layout = (ConstraintLayout) views.get(0);
            CustomTextView tvLabel = layout.findViewById(com.vijay.jsonwizard.R.id.label_text);
            tvLabel.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            ((JsonApi) context).addFormDataView(tvLabel);
        }
        return views;
    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        List<View> views =  super.getViewsFromJson(stepName, context, formFragment, jsonObject, listener, popup);
        if (!views.isEmpty() && views.get(0) != null && views.get(0) instanceof ConstraintLayout) {
            ConstraintLayout layout = (ConstraintLayout) views.get(0);
            CustomTextView tvLabel = layout.findViewById(com.vijay.jsonwizard.R.id.label_text);
            tvLabel.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.getString(JsonFormConstants.KEY));
            ((JsonApi) context).addFormDataView(tvLabel);
        }
        return views;
    }
}
