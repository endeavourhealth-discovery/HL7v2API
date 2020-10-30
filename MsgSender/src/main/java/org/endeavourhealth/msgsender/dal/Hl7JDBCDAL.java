package org.endeavourhealth.msgsender.dal;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class Hl7JDBCDAL extends BaseJDBCDAL {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7JDBCDAL.class);

    /**
     * Service Method to save HL7v2 Message to DB for Imperial Trust
     * @param wrapper
     * @param message
     * @param payloadId
     * @throws Exception
     */
    public void saveHL7Message(String wrapper, String message, String payloadId) throws Exception {
        String sql = "INSERT INTO hl7v2_inbound.imperial (date_received, message_wrapper, hl7_message, payload_id) " +
                "VALUES (?, ?, ?, ?)";

        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, now);
            stmt.setString(2, wrapper);
            stmt.setString(3, message);
            stmt.setString(4, payloadId);

            stmt.executeUpdate();
        }
    }


    public DbInstance getInstanceConfiguration() throws Exception {
     DbInstance ret = new DbInstance();
        ConfigManager.Initialize("message_sender");
        JsonNode json = ConfigManager.getConfigurationAsJson("rabbit");
            if (null==json) {
                    throw new Exception("please add configurations in config table with app_id ='message_sender' ,config_id='rabbit'");
                }

            else{
                DbInstanceEds eds = new DbInstanceEds();
                eds.setEdsUrl(json.get("eds_url").asText());
               if(json.get("use_keycloak").asText().equalsIgnoreCase("1"))
                eds.setUseKeycloak(true);
               else
                   eds.setUseKeycloak(false);
                eds.setKeycloakTokenUri(json.get("keycloak_token_uri").asText());
                eds.setKeycloakRealm(json.get("keycloak_realm").asText());
                eds.setKeycloakUsername(json.get("keycloak_username").asText());
                eds.setKeycloakPassword(json.get("keycloak_password").asText());
                eds.setKeycloakClientId(json.get("keycloak_clientid").asText());
                eds.setSoftwareContentType(json.get("software_content_type").asText());
                eds.setSoftwareVersion(json.get("software_version").asText());
                eds.setChunksize(Integer.parseInt(json.get("chunk_size").asText()));
                eds.setSecounds(Integer.parseInt(json.get("sleep_seconds").asText()));

                ret.setEdsConfiguration(eds);
            }
            return ret;


    }

    public java.util.List<HL7MessageInstance> getUnsendMessages(int chunksize) throws Exception {

        ArrayList<HL7MessageInstance> mlist=new ArrayList<HL7MessageInstance>();
        HL7MessageInstance ret=null;

        //select the eds record
        String  sql = "SELECT id,message_wrapper,hl7_message  FROM hl7v2_inbound.imperial where send_to_mq='N' ORDER BY ID";

        try (PreparedStatement psSelectEdsConfiguration = conn.prepareStatement(sql)) {
            ResultSet rs = psSelectEdsConfiguration.executeQuery();
              int rec_count=0;
                while(rs.next())
                {
                    rec_count=rec_count+1;
                     int col=1;
                    mlist.add(new HL7MessageInstance().setId(rs.getInt(col++)).setMeta(rs.getString(col++)).setHl7message(rs.getString(col++)));
                    if(rec_count==chunksize)
                    {
                        break;
                    }
                }

        }
        return mlist;


    }

    public void updateSuccess(int id)throws Exception
    {
    String sql = "update  hl7v2_inbound.imperial set send_to_mq='Y' where id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
}


}
