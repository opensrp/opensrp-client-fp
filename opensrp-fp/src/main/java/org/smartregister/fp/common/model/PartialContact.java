package org.smartregister.fp.common.model;

import com.google.gson.annotations.SerializedName;

public class PartialContact extends Contact {

    @SerializedName("form_json_draft")
    private String formJsonDraft;
    @SerializedName("is_finalized")
    private Boolean isFinalized;
    private int sortOrder;

    public PartialContact(String baseEntityId, String formType, int contactNo) {
        super(baseEntityId,formType, contactNo);
    }

    public PartialContact() {
        super();
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getFormJsonDraft() {
        return formJsonDraft;
    }

    public void setFormJsonDraft(String formJson) {
        this.formJsonDraft = formJson;
    }

    public Boolean getFinalized() {
        return isFinalized;
    }

    public void setFinalized(Boolean finalized) {
        isFinalized = finalized;
    }

}
