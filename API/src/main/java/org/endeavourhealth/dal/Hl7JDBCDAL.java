package org.endeavourhealth.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
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

        PreparedStatement psSelectEdsConfiguration = null;
        try {
            DbInstance ret = new DbInstance();
            //select the eds record
            String  sql = "SELECT eds_url, use_keycloak, keycloak_token_uri, keycloak_realm, keycloak_username, keycloak_password, keycloak_clientid, software_content_type, software_version"
                    + " FROM configuration_eds;";



            psSelectEdsConfiguration = conn.prepareStatement(sql);
            ResultSet rs = psSelectEdsConfiguration.executeQuery();
            if (!rs.next()) {
                throw new Exception("No configuration_eds record found");
            }
            int col = 1;

            DbInstanceEds eds = new DbInstanceEds();
            eds.setEdsUrl(rs.getString(col++));
            eds.setUseKeycloak(rs.getBoolean(col++));
            eds.setKeycloakTokenUri(rs.getString(col++));
            eds.setKeycloakRealm(rs.getString(col++));
            eds.setKeycloakUsername(rs.getString(col++));
            eds.setKeycloakPassword(rs.getString(col++));
            eds.setKeycloakClientId(rs.getString(col++));
            eds.setSoftwareContentType(rs.getString(col++));
            eds.setSoftwareVersion(rs.getString(col++));

            ret.setEdsConfiguration(eds);

            return ret;

        } finally {

            if (psSelectEdsConfiguration != null) {
                psSelectEdsConfiguration.close();
            }


        }
    }


}
