package org.smartregister.fp.common.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ndegwamartin on 09/01/2019.
 */
public abstract class Contact {

    @SerializedName("_id")
    private Long id;
    @SerializedName("base_entity_id")
    private String baseEntityId;
    @SerializedName("type")
    private String type;
    private String formJson;
    @SerializedName("contact_no")
    private Integer contactNo;
    @SerializedName("created_at")
    private Long createdAt;
    @SerializedName("updated_at")
    private Long updatedAt;

    public Contact(String baseEntityId, String type, Integer contactNo) {
        this.baseEntityId = baseEntityId;
        this.type = type;
        this.contactNo = contactNo;
    }

    public Contact() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFormJson() {
        return formJson;
    }

    public void setFormJson(String formJson) {
        this.formJson = formJson;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getContactNo() {
        return contactNo;
    }

    public void setContactNo(Integer contactNo) {
        this.contactNo = contactNo;
    }
}
