package org.endeavourhealth.hl7v2msgreceiver.dal;

import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class Hl7JDBCDAL implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7JDBCDAL.class);


    Connection conn = null;

    public void closeAcuteConnection() throws Exception {
        this.conn.close();
    }

    public void setAcuteConnection() throws Exception {
        conn = ConnectionManager.getHL7v2InboundConnection();
        if (conn == null) {
            LOG.info("Connection has not been found");
            throw new Exception("Connection has not been found");
        }

    }

    @Override
    public void close() throws Exception {
        LOG.info("closing the JDBCDal");
        if (conn != null) {
            this.conn.close();
        }
    }

    /**
     * Service Method to save HL7v2 Message to DB for Imperial Trust
     *
     * @param wrapper
     * @param message
     * @param payloadId
     * @throws Exception
     */
    public void saveHL7Message(String wrapper, String message, String payloadId, String source, String messageId) throws Exception {

        String sql = "INSERT INTO acute (date_received, message_wrapper, hl7_message, payload_id, source_system, message_id)"
                + " VALUES (?, ?, ?, ?,?, ?)";

        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, now);
            stmt.setString(2, wrapper);
            stmt.setString(3, message);
            stmt.setString(4, payloadId);
            stmt.setString(5, source);
            stmt.setString(6, messageId);

            stmt.executeUpdate();
        }
        conn.commit();
    }



}
