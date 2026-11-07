package org.endeavourhealth.msgsender.dal;

import com.fasterxml.jackson.databind.JsonNode;
import org.endeavourhealth.common.config.ConfigManager;
import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class Hl7JDBCDAL implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(Hl7JDBCDAL.class);

    private Connection conn = null;

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

    public DbInstanceEds getInstanceConfiguration() throws Exception {

        JsonNode json = ConfigManager.getConfigurationAsJson("keycloak");
        if (null == json) {
            throw new Exception("Failed to find keycloak configuration JSON");
        }

        DbInstanceEds eds = new DbInstanceEds();
        if (json.get("use_keycloak").asText().equalsIgnoreCase("1"))
            eds.setUseKeycloak(true);
        else
            eds.setUseKeycloak(false);

        eds.setKeycloakTokenUri(json.get("keycloak_token_uri").asText());
        eds.setKeycloakRealm(json.get("keycloak_realm").asText());
        eds.setKeycloakUsername(json.get("keycloak_username").asText());
        eds.setKeycloakPassword(json.get("keycloak_password").asText());
        eds.setKeycloakClientId(json.get("keycloak_clientid").asText());

        json = ConfigManager.getConfigurationAsJson("sender-config");
        if (null == json) {
            throw new Exception("Failed to find sender-config configuration JSON");
        }

        eds.setEdsUrl(json.get("eds_url").asText());
        eds.setSoftwareContentType(json.get("software_content_type").asText());
        eds.setSoftwareVersion(json.get("software_version").asText());
        eds.setChunksize(Integer.parseInt(json.get("chunk_size").asText()));
        eds.setSecounds(Integer.parseInt(json.get("sleep_seconds").asText()));

        return eds;
    }

    public List<HL7MessageInstance> getUnsendMessages(int chunksize) throws Exception {

        ArrayList<HL7MessageInstance> mlist = new ArrayList<>();

        //select the eds record
        String sql = "SELECT id, message_wrapper, hl7_message FROM acute WHERE send_to_mq='N' ORDER BY id LIMIT ?";

        try (PreparedStatement psSelectEdsConfiguration = conn.prepareStatement(sql)) {
            psSelectEdsConfiguration.setInt(1, chunksize);

            ResultSet rs = psSelectEdsConfiguration.executeQuery();
            while (rs.next()) {
                int col = 1;
                mlist.add(new HL7MessageInstance().setId(rs.getInt(col++)).setMeta(rs.getString(col++)).setHl7message(rs.getString(col++)));
            }
        }

        return mlist;


    }

    public void updateSuccess(int id) throws Exception {
        String sql = "UPDATE acute SET send_to_mq='Y' WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            conn.commit();
        }
    }

    @Override
    public void close() throws Exception {

        if (conn != null) {
            this.conn.close();
        }
    }
}
