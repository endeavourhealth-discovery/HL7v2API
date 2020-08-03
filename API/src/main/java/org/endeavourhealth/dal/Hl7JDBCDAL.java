package org.endeavourhealth.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;

public class Hl7JDBCDAL extends BaseJDBCDAL {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7JDBCDAL.class);

    /**
     *
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
}
