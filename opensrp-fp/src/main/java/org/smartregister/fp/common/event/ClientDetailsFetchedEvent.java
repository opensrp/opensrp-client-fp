package org.smartregister.fp.common.event;

import java.util.Map;

/**
 * Created by ndegwamartin on 17/07/2018.
 */
public class ClientDetailsFetchedEvent extends BaseEvent {
    private Map<String, String> registeredClient;
    private boolean isEditMode = false;

    public ClientDetailsFetchedEvent(Map<String, String> client, boolean isEditMode) {
        this.registeredClient = client;
        this.isEditMode = isEditMode;
    }

    public Map<String, String> getRegisteredClient() {
        return registeredClient;
    }

    public boolean isEditMode() {
        return isEditMode;
    }
}
