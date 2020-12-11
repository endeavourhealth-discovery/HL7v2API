package org.endeavourhealth.msgsender.sender;

import org.endeavourhealth.msgsender.dal.DbInstance;
import org.endeavourhealth.msgsender.dal.HL7MessageInstance;
import org.endeavourhealth.msgsender.dal.Hl7JDBCDAL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(MessageSender.class);
    public static void main(String[] args) {
        try {
           try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
               DbInstance dbInstanceConfiguration = viewerDAL.getInstanceConfiguration();

               while(true) {
                   HL7MessageInstance hl7message=new HL7MessageInstance();
                   try {
                       List<HL7MessageInstance> mlist = viewerDAL.getUnsendMessages(dbInstanceConfiguration.getEdsConfiguration().getChunksize());

                       if (mlist.size() == 0) {
                           Thread.sleep(dbInstanceConfiguration.getEdsConfiguration().getSecounds() * 1000);
                       } else {
                           for (int i = 0; i < mlist.size(); i++) {
                                hl7message = mlist.get(i);

                               String mwrap = hl7message.getMeta();

                               JSONParser parser = new JSONParser();
                               JSONObject megWrapperElement = (JSONObject) parser.parse(mwrap);


                               JSONObject metaElement = (JSONObject) parser.parse(megWrapperElement.get("meta").toString());

                               String odsCode=null;
                               Object tagElement = metaElement.get("tag");

                               if(tagElement instanceof JSONArray ) {
                                   JSONArray array = (JSONArray) tagElement;
                                   JSONObject odsElement=(JSONObject) parser.parse((array.get(0)).toString());
                                   odsCode=odsElement.get("code").toString();
                               }
                               else
                               {
                                   odsCode=((JSONObject)tagElement).get("code").toString();
                               }
                               EdsSenderClient.sendmsg(odsCode, true, hl7message.getHl7message(), (megWrapperElement.get("id")).toString(), dbInstanceConfiguration.getEdsConfiguration());
                               viewerDAL.updateSuccess(hl7message.getId());

                           }
                       }
                   }
                   catch(Exception e)
                   {

                       LOG.error("JSON Parsing/ message send failed for the message id : " +hl7message.getId(),e);
                       LOG.info(" Waiting for "+dbInstanceConfiguration.getEdsConfiguration().getSecounds() +" Seconds");
                       Thread.sleep(dbInstanceConfiguration.getEdsConfiguration().getSecounds() * 1000);
                   }

                 // break;
               }//while
           }
        } catch (Exception e) {
            LOG.error("Error fetching HL7 message from hl7v2_inbound.imperial table ",e);
        }
    }
}
