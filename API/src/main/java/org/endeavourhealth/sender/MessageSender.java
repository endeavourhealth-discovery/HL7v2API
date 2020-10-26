package org.endeavourhealth.sender;

import org.endeavourhealth.dal.DbInstance;
import org.endeavourhealth.dal.HL7MessageInstance;
import org.endeavourhealth.dal.Hl7JDBCDAL;
import org.endeavourhealth.endpoints.EdsSenderClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.util.List;
public class MessageSender {


    public static void main(String[] args) {
        try {
           try (Hl7JDBCDAL viewerDAL = new Hl7JDBCDAL()) {
               DbInstance dbInstanceConfiguration = viewerDAL.getInstanceConfiguration();

               while(true) {

                   List<HL7MessageInstance> mlist= viewerDAL.getUnsendMessages(dbInstanceConfiguration.getEdsConfiguration().getChunksize());

                  if(mlist.size()==0)
                   {
                       Thread.sleep(dbInstanceConfiguration.getEdsConfiguration().getSecounds()*1000);
                   }
                   else {
                       for (int i = 0; i < mlist.size(); i++) {
                           HL7MessageInstance hl7message = mlist.get(i);

                           String mwrap = hl7message.getMeta();

                           JSONParser parser = new JSONParser();
                           JSONObject jsonobj = (JSONObject) parser.parse(mwrap);


                           JSONObject jsonobj1 = (JSONObject) parser.parse(jsonobj.get("meta").toString());

                           Object odscode = jsonobj1.get("tag");
                           JSONArray array = (JSONArray) odscode;

                           JSONObject jsonobj2 = (JSONObject) parser.parse((array.get(0)).toString());
                           EdsSenderClient.sendmsg(jsonobj2.get("code").toString(), true, hl7message.getHl7message(), (jsonobj.get("id")).toString(), dbInstanceConfiguration.getEdsConfiguration());
                           viewerDAL.updateSuccess(hl7message.getId());

                       }
                   }

                 // break;
               }//while
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
