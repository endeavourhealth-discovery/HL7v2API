package org.endeavourhealth.msgsender.sender;

import com.google.common.base.Strings;
import org.endeavourhealth.core.application.ApplicationHeartbeatCallbackI;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;

public class HeartbeatStatus implements ApplicationHeartbeatCallbackI {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void populateIsBusy(ApplicationHeartbeat applicationHeartbeat) {
        if (Strings.isNullOrEmpty(status)) {
            applicationHeartbeat.setBusy(false);
            applicationHeartbeat.setIsBusyDetail(null);

        } else {
            applicationHeartbeat.setBusy(true);
            applicationHeartbeat.setIsBusyDetail(status);
        }
    }

    @Override
    public void populateInstanceNumber(ApplicationHeartbeat applicationHeartbeat) {
        //only one instance, but it can't be null, so just set to empty String
        applicationHeartbeat.setApplicationInstanceName("");
    }
}
