package org.endeavourhealth.endpoints;



import org.apache.http.Header;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.dal.DbConfiguration;
import org.endeavourhealth.dal.DbInstance;
import org.endeavourhealth.dal.DbInstanceEds;
import org.endeavourhealth.sender.EdsSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


public class Test {
    private static final Logger LOG = LoggerFactory.getLogger(Test.class);
    private static DbInstance dbInstanceConfiguration = null;
    private static DbConfiguration dbConfiguration = null;
    private static Configuration configuration;

    public static void sendmsg() {
        try {
            String   instanceName ="TEST";

            ConfigManager.initialize("sftpreader", instanceName);

            configuration = Configuration.getInstance();



            LOG.info("--------------------------------------------------");
            LOG.info("SFTP Reader " + instanceName);
            LOG.info("--------------------------------------------------");

            LOG.info("Instance " + configuration.getInstanceName() + " on host " + configuration.getMachineName());
            LOG.info("Processing configuration(s): " + configuration.getConfigurationIdsForDisplay());

            try {

                dbInstanceConfiguration = configuration.getInstanceConfiguration();
                dbConfiguration = configuration.getConfiguration("EMIS_TEST");

                    DbInstanceEds edsConfiguration = dbInstanceConfiguration.getEdsConfiguration();

                    if (edsConfiguration == null) {
                        throw new Exception("Cannot notify EDS - EDS configuration is not set");
                    }

                    if (edsConfiguration.isUseKeycloak()) {
                        System.out.println("Initialising keycloak at: {}"+ edsConfiguration.getKeycloakTokenUri());

                        try {
                            KeycloakClient.init(edsConfiguration.getKeycloakTokenUri(),
                                    edsConfiguration.getKeycloakRealm(),
                                    edsConfiguration.getKeycloakUsername(),
                                    edsConfiguration.getKeycloakPassword(),
                                    edsConfiguration.getKeycloakClientId());

                            Header response = KeycloakClient.instance().getAuthorizationHeader();
                            System.out.println("Keycloak authorization header is {}: {}"+ response.getName() + response.getValue());

                        } catch (
                                Exception e) {
                            throw new Exception("Error initialising keycloak", e);
                        }

                    }


                    notify("D82027",true,"TESTMESSAGE");


                } catch (Exception e) {
                    e.printStackTrace();
                }


        } catch (ConfigManagerException cme) {

            LOG.error("Fatal exception occurred initializing ConfigManager", cme);

        }
        catch (Exception e) {
            LOG.error("Fatal exception occurred", e);

        }
    }
    private static void notify(String organisationId ,boolean isBulk ,String messagePayload) throws Exception {

        UUID messageId = UUID.randomUUID();

        String softwareContentType = dbConfiguration.getSoftwareContentType();
        String softwareVersion = dbConfiguration.getSoftwareVersion();

        String outboundMessage = null;

        try {
            String edsUrl = dbInstanceConfiguration.getEdsConfiguration().getEdsUrl();
            boolean useKeycloak = dbInstanceConfiguration.getEdsConfiguration().isUseKeycloak();
            EdsSender.notifyEds(edsUrl, useKeycloak, EdsSender.buildEnvelope(messageId, organisationId, softwareContentType, softwareVersion, messagePayload), isBulk);
        } catch (Exception ex) {

            throw new Exception("Error notifying EDS for batch split ", ex);
        }
    }

}

