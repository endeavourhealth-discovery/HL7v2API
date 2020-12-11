package org.endeavourhealth.msgsender.sender;


import org.apache.http.Header;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.msgsender.dal.DbInstanceEds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class EdsSenderClient {
    private static final Logger LOG = LoggerFactory.getLogger(EdsSenderClient.class);

    public static void sendmsg(String odscode, boolean isbulk, String message, UUID payloadId, DbInstanceEds edsConfiguration) throws Exception {
        try {

            if (edsConfiguration == null) {
                throw new Exception("Cannot notify EDS - EDS configuration is not set");
            }

            if (edsConfiguration.isUseKeycloak()) {
                LOG.debug("Initialising keycloak at: {}" + edsConfiguration.getKeycloakTokenUri());

                try {
                    KeycloakClient.init(edsConfiguration.getKeycloakTokenUri(),
                            edsConfiguration.getKeycloakRealm(),
                            edsConfiguration.getKeycloakUsername(),
                            edsConfiguration.getKeycloakPassword(),
                            edsConfiguration.getKeycloakClientId());

                    Header response = KeycloakClient.instance().getAuthorizationHeader();
                    LOG.info("Keycloak authorization header is {}: {}" + response.getName() + response.getValue());

                } catch (
                        Exception e) {
                    throw new Exception("Error initialising keycloak", e);
                }

            }

            String envelop = EdsSender.buildEnvelope(payloadId, odscode, edsConfiguration.getSoftwareContentType(), edsConfiguration.getSoftwareVersion(), message);

            LOG.debug("envelop"+ envelop);
            EdsSender.notifyEds(edsConfiguration.getEdsUrl(), edsConfiguration.isUseKeycloak(), envelop, isbulk);

            LOG.debug("success");

        } catch (Exception ex) {
                throw new Exception("Error notifying EDS for batch split ", ex);
        }
    }
}

