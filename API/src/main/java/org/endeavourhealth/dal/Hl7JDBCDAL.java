package org.endeavourhealth.dal;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

          String sql = "INSERT INTO imperial (date_received, message_wrapper, hl7_message, payload_id) " +
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



    public java.util.List<HL7MessageInstance> getUnsendMessages(int chunksize) throws Exception {

        ArrayList<HL7MessageInstance> mlist=new ArrayList<HL7MessageInstance>();
        HL7MessageInstance ret=null;

        //select the eds record
        String  sql = "SELECT id,message_wrapper,hl7_message  FROM imperial where send_to_mq='N' ORDER BY ID";

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
    String sql = "update  imperial set send_to_mq='Y' where id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }
}


}
