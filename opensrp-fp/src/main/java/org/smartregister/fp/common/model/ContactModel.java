package org.smartregister.fp.common.model;


import org.smartregister.fp.common.contact.ContactContract;

import java.util.Map;

public class ContactModel extends BaseContactModel implements ContactContract.Model {

    @Override
    public String extractPatientName(Map<String, String> womanDetails) {
        return super.extractPatientName(womanDetails);

    }
}
