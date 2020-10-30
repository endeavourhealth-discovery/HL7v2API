package org.endeavourhealth.msgsender.dal;

import java.util.List;

public class DbInstance {
    private String instanceName;
    private Integer httpManagementPort;
    private List<String> configurationIds;
    private DbInstanceEds edsConfiguration;

    public String getInstanceName() {
        return instanceName;
    }

    public DbInstance setInstanceName(String instanceName) {
        this.instanceName = instanceName;
        return this;
    }
    public Integer getHttpManagementPort() {
        return httpManagementPort;
    }

    public DbInstance setHttpManagementPort(Integer httpManagementPort) {
        this.httpManagementPort = httpManagementPort;
        return this;
    }
    public List<String> getConfigurationIds() {
        return configurationIds;
    }

    public DbInstance setConfigurationIds(List<String> configurationIds) {
        this.configurationIds = configurationIds;
        return this;
    }
    public DbInstanceEds getEdsConfiguration() {
        return this.edsConfiguration;
    }

    public DbInstance setEdsConfiguration(DbInstanceEds edsConfiguration) {
        this.edsConfiguration = edsConfiguration;
        return this;
    }
}
