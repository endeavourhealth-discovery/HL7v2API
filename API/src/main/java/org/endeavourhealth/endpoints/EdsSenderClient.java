package org.endeavourhealth.endpoints;


import org.apache.http.Header;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.dal.DbInstanceEds;
import org.endeavourhealth.sender.EdsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class EdsSenderClient {
    private static final Logger LOG = LoggerFactory.getLogger(EdsSenderClient.class);

    public static void sendmsg(String odscode, boolean isbulk, String message, String payloadId, DbInstanceEds edsConfiguration) throws Exception {
        try {
            LOG.info("I AM IN SENDER");

            if (edsConfiguration == null) {
                throw new Exception("Cannot notify EDS - EDS configuration is not set");
            }

            if (edsConfiguration.isUseKeycloak()) {
                System.out.println("Initialising keycloak at: {}" + edsConfiguration.getKeycloakTokenUri());

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
            LOG.info("creating envolop");
            String envolop = EdsSender.buildEnvelope(UUID.fromString(payloadId), odscode, edsConfiguration.getSoftwareContentType(), edsConfiguration.getSoftwareVersion(), message);

            LOG.info("envelop"+ envolop);
            EdsSender.notifyEds(edsConfiguration.getEdsUrl(), edsConfiguration.isUseKeycloak(), envolop, isbulk);

            LOG.info("suuccess");

        } catch (Exception ex) {
           ex.printStackTrace();
            throw new Exception("Error notifying EDS for batch split ", ex);
        }
    }
}

