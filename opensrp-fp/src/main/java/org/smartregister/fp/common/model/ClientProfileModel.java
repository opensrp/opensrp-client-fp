package org.smartregister.fp.common.model;

public class ClientProfileModel {
    private String chosenMethod;
    private String methodAtExit;
    private String methodStartDate;
    private String referred;
    private String reasonForNoMethodAtExit;

    public String getChosenMethod() {
        return chosenMethod;
    }

    public void setChosenMethod(String chosenMethod) {
        this.chosenMethod = chosenMethod;
    }

    public String getMethodAtExit() {
        return methodAtExit;
    }

    public void setMethodAtExit(String methodAtExit) {
        this.methodAtExit = methodAtExit;
    }

    public String getMethodStartDate() {
        return methodStartDate;
    }

    public void setMethodStartDate(String methodStartDate) {
        this.methodStartDate = methodStartDate;
    }

    public String getReferred() {
        return referred;
    }

    public void setReferred(String referred) {
        this.referred = referred;
    }

    public String getReasonForNoMethodAtExit() {
        return reasonForNoMethodAtExit;
    }

    public void setReasonForNoMethodAtExit(String reasonForNoMethodAtExit) {
        this.reasonForNoMethodAtExit = reasonForNoMethodAtExit;
    }
}
