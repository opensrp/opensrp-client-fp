package org.smartregister.fp.common.event;

import java.util.Map;

/**
 * Created by ndegwamartin on 17/07/2018.
 */
public class ClientDetailsFetchedEvent extends BaseEvent {
    private Map<String, String> registrationClient;
    private boolean isEditMode = false;

    public ClientDetailsFetchedEvent(Map<String, String> client, boolean isEditMode) {
        this.registrationClient = client;
        this.isEditMode = isEditMode;
    }

    public Map<String, String> getRegistrationClient() {
        return registrationClient;
    }

    public boolean isEditMode() {
        return isEditMode;
    }
}
