package org.endeavourhealth.msgsender.sender;

import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.common.config.ConfigManagerException;
import org.endeavourhealth.common.utility.SlackHelper;
import org.endeavourhealth.msgsender.dal.DbInstance;
import org.endeavourhealth.msgsender.dal.HL7MessageInstance;
import org.endeavourhealth.msgsender.dal.Hl7JDBCDAL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static org.endeavourhealth.common.config.ConfigManager.*;

public class MessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);

    public static void main(String[] args) throws InterruptedException, ConfigManagerException {
        int maxtries = 10;
        int trycount = 0;
        Initialize("db_hl7v2_inbound");
        while (true) {
            try {
                try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {


                    DbInstance dbInstanceConfiguration = viewerDAL.getInstanceConfiguration();

                    while (true) {
                        HL7MessageInstance hl7message = new HL7MessageInstance();
                        try {
                            trycount = 0;
                            List<HL7MessageInstance> mlist = viewerDAL.getUnsendMessages(dbInstanceConfiguration.getEdsConfiguration().getChunksize());
                            try {

                                trycount = 0;
                                if (mlist.size() == 0) {
                                    Thread.sleep(dbInstanceConfiguration.getEdsConfiguration().getSecounds() * 1000);
                                } else {
                                    for (int i = 0; i < mlist.size(); i++) {
                                        hl7message = mlist.get(i);

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
                                        EdsSenderClient.sendmsg(odsCode, true, hl7message.getHl7message(), UUID.randomUUID(), dbInstanceConfiguration.getEdsConfiguration());
                                        viewerDAL.updateSuccess(hl7message.getId());

                                    }
                                }

                            } catch (Exception e) {

                                LOG.error("JSON Parsing/ message send failed for the message id : " + hl7message.getId(), e);
                                LOG.info(" Waiting for " + dbInstanceConfiguration.getEdsConfiguration().getSecounds() + " Seconds");
                                SlackHelper.sendSlackMessage(SlackHelper.Channel.EnterpriseAgeUpdaterAlerts, "JSON Parsing/ message send failed for the message id : " + hl7message.getId(), e);
                                Thread.sleep(dbInstanceConfiguration.getEdsConfiguration().getSecounds() * 1000);
                            }
                        } catch (Exception e) {
                            throw (e);

                        }
                    }//while
                }
            } catch (Exception e) {

                trycount++;
                if (trycount < maxtries) {
                    SlackHelper.sendSlackMessage(SlackHelper.Channel.EnterpriseAgeUpdaterAlerts, trycount + "/" + maxtries + " try failed , Error fetching HL7 message from hl7v2_inbound.imperial table , retry after 2 min", e);
                    LOG.error(trycount + "/" + maxtries + " try failed , Error fetching HL7 message from hl7v2_inbound.imperial table , retry after 2 min", e);
                    Thread.sleep(60 * 2000);
                }
            }
            if (trycount == maxtries) {

                break;
            }

        }


        SlackHelper.sendSlackMessage(SlackHelper.Channel.EnterpriseAgeUpdaterAlerts, "MessageSender is exited please restart process manually");
    }

}



