package org.endeavourhealth.msgsender.sender;

import org.apache.http.Header;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.security.keycloak.client.KeycloakClient;
import org.endeavourhealth.common.utility.MetricsHelper;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.core.application.ApplicationHeartbeatCallbackI;
import org.endeavourhealth.core.application.ApplicationHeartbeatHelper;
import org.endeavourhealth.core.database.dal.audit.models.ApplicationHeartbeat;
import org.endeavourhealth.msgsender.dal.DbInstanceEds;
import org.endeavourhealth.msgsender.dal.HL7MessageInstance;
import org.endeavourhealth.msgsender.dal.Hl7JDBCDAL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);

    public static void main(String[] args) throws InterruptedException, ConfigManagerException {

        //initialise our connection to config DB
        ConfigManager.Initialize("hl7v2_msg_sender");

        try {
            //start both these off, so we know this app is running
            MetricsHelper.startHeartbeat();
            HeartbeatStatus heartbeatStatus = new HeartbeatStatus();
            ApplicationHeartbeatHelper.start(heartbeatStatus);


            int maxTries = 10;
            int tryCount = 0;

            while (true) {
                try {
                    try(Hl7JDBCDAL edsViewerDAL = new Hl7JDBCDAL()){
                        DbInstanceEds dbInstanceEds = edsViewerDAL.getInstanceConfiguration();

                        if (dbInstanceEds == null) {
                            throw new Exception("Cannot notify EDS - EDS configuration is not set");
                        }

                        if (dbInstanceEds.isUseKeycloak()) {
                            LOG.debug("Initialising keycloak at: {}" + dbInstanceEds.getKeycloakTokenUri());

                            try {
                                initializeKeyCloak(dbInstanceEds);

                                Header response = KeycloakClient.instance().getAuthorizationHeader();
                                LOG.info("Keycloak authorization header is {}: {}" + response.getName() + response.getValue());
                            } catch (
                                    Exception e) {
                                throw new Exception("Error initialising keycloak", e);
                            }
                        }}
                    while (true) {
                        try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
                            viewerDAL.setAcuteConnection();

                            DbInstanceEds dbInstanceEds = viewerDAL.getInstanceConfiguration();

                            HL7MessageInstance hl7message = new HL7MessageInstance();
                            try {
                                tryCount = 0;
                                heartbeatStatus.setStatus(null);
                                List<HL7MessageInstance> mList = viewerDAL.getUnsendMessages(dbInstanceEds.getChunksize());
                                try {

                                    tryCount = 0;
                                    if (mList.size() == 0) {
                                        viewerDAL.close();
                                        Thread.sleep(dbInstanceEds.getSecounds() * 1000);
                                    } else {

                                        heartbeatStatus.setStatus("Processing messages");
                                        for (int i = 0; i < mList.size(); i++) {
                                            hl7message = mList.get(i);

                                            String mwrap = hl7message.getMeta();

                                            JSONParser parser = new JSONParser();
                                            JSONObject megWrapperElement = (JSONObject) parser.parse(mwrap);


                                            JSONObject metaElement = (JSONObject) parser.parse(megWrapperElement.get("meta").toString());

                                            String odsCode = null;
                                            Object tagElement = metaElement.get("tag");

                                            if (tagElement instanceof JSONArray) {
                                                JSONArray array = (JSONArray) tagElement;
                                                JSONObject odsElement = (JSONObject) parser.parse((array.get(0)).toString());
                                                odsCode = odsElement.get("code").toString();
                                            } else {
                                                odsCode = ((JSONObject) tagElement).get("code").toString();
                                            }

                                            Date messageTimestamp = TimestampHelper.findMessageTimestamp(hl7message);

                                            EdsSenderClient.sendmsg(odsCode, messageTimestamp, hl7message.getHl7message(), UUID.randomUUID(), dbInstanceEds);
                                            viewerDAL.updateSuccess(hl7message.getId());

                                        }
                                    }

                                } catch (Exception e) {

                                    LOG.error("JSON Parsing/ message send failed for the message id : " + hl7message.getId(), e);
                                    LOG.info(" Waiting for " + dbInstanceEds.getSecounds() + " Seconds");
                                    SlackHelper.sendSlackMessage(SlackHelper.Channel.HL7v2MessageSender, "JSON Parsing/message send failed for the message id : " + hl7message.getId(), e);
                                    Thread.sleep(dbInstanceEds.getSecounds() * 1000);
                                }
                            } catch (Exception e) {
                                throw (e);

                            }
                        }

                    }//while
                } catch (Exception e) {

                    tryCount++;
                    if (tryCount < maxTries) {
                        SlackHelper.sendSlackMessage(SlackHelper.Channel.HL7v2MessageSender, tryCount + "/" + maxTries + " try failed, Error fetching HL7 message from hl7v2_inbound.acute table , retry after 2 min", e);
                        LOG.error(tryCount + "/" + maxTries + " try failed, Error fetching HL7 message from hl7v2_inbound.acute table, retry after 2 min", e);
                        Thread.sleep(60 * 2000);
                    }
                }
                if (tryCount == maxTries) {

                    break;
                }
            }

            SlackHelper.sendSlackMessage(SlackHelper.Channel.HL7v2MessageSender, "HL7v2 Message Sender has exited please restart process manually");

        } catch (Throwable t) {
            LOG.error("Uncaught error in HL7v2 Message Sender", t);
        }
    }

    private static void initializeKeyCloak (DbInstanceEds edsConfiguration){
        KeycloakClient.init(edsConfiguration.getKeycloakTokenUri(),
                edsConfiguration.getKeycloakRealm(),
                edsConfiguration.getKeycloakUsername(),
                edsConfiguration.getKeycloakPassword(),
                edsConfiguration.getKeycloakClientId());
    }

}



