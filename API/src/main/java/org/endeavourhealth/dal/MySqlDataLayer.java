package org.endeavourhealth.dal;


import org.endeavourhealth.core.database.rdbms.ConnectionManager;
import org.endeavourhealth.endpoints.DataLayerI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


public class MySqlDataLayer implements DataLayerI {

    public MySqlDataLayer() {}

    @Override
    public DbInstance getInstanceConfiguration(String instanceName, String hostname) throws Exception {
        Connection connection = getConnection();
        PreparedStatement psSelectInstance = null;
        PreparedStatement psUpdateInstance = null;
        PreparedStatement psSelectInstanceConfiguration = null;
        PreparedStatement psSelectEdsConfiguration = null;
        try {
            String sql = "SELECT hostname, http_management_port FROM instance WHERE instance_name = ?;";

            psSelectInstance = connection.prepareStatement(sql);
            psSelectInstance.setString(1, instanceName);

            ResultSet rs = psSelectInstance.executeQuery();
            if (!rs.next()) {
                throw new Exception("Instance record not found for instance name " + instanceName);
            }
            String dbHostName = rs.getString(1);
            Integer httpPort = null;
            int portNum = rs.getInt(2);
            if (!rs.wasNull()) {
                httpPort = new Integer(portNum);
            }

            //if the host name isn't set on the DB, update the record
            if (dbHostName == null) {

                sql = "UPDATE instance SET hostname = ? WHERE instance_name = ?;";

                psUpdateInstance = connection.prepareStatement(sql);
                psUpdateInstance.setString(1, hostname);
                psUpdateInstance.setString(2, instanceName);
                psUpdateInstance.executeUpdate();

            } else {
                if (!hostname.equalsIgnoreCase(dbHostName)) {
                    throw new Exception("Hostname on instance table (" + dbHostName + ") doesn't match this server's name (" + hostname + ") - is this being run on the wrong server?");
                }
            }

            DbInstance ret = new DbInstance();
            ret.setInstanceName(instanceName);
            ret.setHttpManagementPort(httpPort);

            //select the configuration IDs that this instance checks
            sql = "SELECT configuration_id FROM instance_configuration WHERE instance_name = ?;";

            psSelectInstanceConfiguration = connection.prepareStatement(sql);
            psSelectInstanceConfiguration.setString(1, instanceName);

            List<String> configurationIds = new ArrayList<>();
            ret.setConfigurationIds(configurationIds);

            rs = psSelectInstanceConfiguration.executeQuery();
            while (rs.next()) {
                String configurationId = rs.getString(1);
                configurationIds.add(configurationId);
            }

            if (configurationIds.isEmpty()) {
                throw new Exception("No configuration IDs associated with instance name " + instanceName + " in instance_configuration table");
            }

            //select the eds record
            sql = "SELECT eds_url, use_keycloak, keycloak_token_uri, keycloak_realm, keycloak_username, keycloak_password, keycloak_clientid, temp_directory, shared_storage_path"
                    + " FROM configuration_eds;";

            psSelectEdsConfiguration = connection.prepareStatement(sql);

            rs = psSelectEdsConfiguration.executeQuery();
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
            eds.setTempDirectory(rs.getString(col++));
            eds.setSharedStoragePath(rs.getString(col++));

            ret.setEdsConfiguration(eds);

            return ret;

        } finally {
            if (psSelectInstance != null) {
                psSelectInstance.close();
            }
            if (psUpdateInstance != null) {
                psUpdateInstance.close();
            }
            if (psSelectInstanceConfiguration != null) {
                psSelectInstanceConfiguration.close();
            }
            if (psSelectEdsConfiguration != null) {
                psSelectEdsConfiguration.close();
            }

            connection.close();
        }
    }

    @Override
    public DbConfiguration getConfiguration(String configurationId) throws Exception {
        Connection connection = getConnection();
        PreparedStatement psConfiguration = null;

        try {
            String sql = "SELECT c.configuration_id, c.configuration_friendly_name, it.interface_type_name, c.poll_frequency_seconds, c.local_root_path, c.software_content_type, c.software_version"
                    + " FROM configuration c"
                    + " INNER JOIN interface_type it"
                    + " ON c.interface_type_id = it.interface_type_id"
                    + " WHERE c.configuration_id = ?;";

            psConfiguration = connection.prepareStatement(sql);
            psConfiguration.setString(1, configurationId);

            DbConfiguration ret = null;

            ResultSet rs = psConfiguration.executeQuery();
            if (rs.next()) {
                int col = 1;

                ret = new DbConfiguration();
                ret.setConfigurationId(rs.getString(col++));
                ret.setConfigurationFriendlyName(rs.getString(col++));
                ret.setInterfaceTypeName(rs.getString(col++));
                ret.setPollFrequencySeconds(rs.getInt(col++));
                ret.setLocalRootPath(rs.getString(col++));
                ret.setSoftwareContentType(rs.getString(col++));
                ret.setSoftwareVersion(rs.getString(col++));

            } else {
                throw new Exception("No configuration found with configuration id " + configurationId);
            }






            return ret;

        } finally {
            if (psConfiguration != null) {
                psConfiguration.close();
            }

            connection.close();
        }
    }



    private Connection getConnection() throws Exception {
        Connection conn = ConnectionManager.getSftpReaderConnection();
        conn.setAutoCommit(true);
        return conn;
    }
}
