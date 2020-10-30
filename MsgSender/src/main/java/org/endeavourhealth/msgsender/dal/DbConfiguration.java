package org.endeavourhealth.msgsender.dal;


public class DbConfiguration {
    private String configurationId;
    private String configurationFriendlyName;
    private String interfaceTypeName;
    private int pollFrequencySeconds;
    private String localRootPath;
    private String softwareContentType;
    private String softwareVersion;

    public String getConfigurationId() {
        return configurationId;
    }

    public DbConfiguration setConfigurationId(String configurationId) {
        this.configurationId = configurationId;
        return this;
    }

    public String getConfigurationFriendlyName() {
        return configurationFriendlyName;
    }

    public DbConfiguration setConfigurationFriendlyName(String configurationFriendlyName) {
        this.configurationFriendlyName = configurationFriendlyName;
        return this;
    }

    public String getInterfaceTypeName() {
        return interfaceTypeName;
    }

    public DbConfiguration setInterfaceTypeName(String interfaceTypeName) {
        this.interfaceTypeName = interfaceTypeName;
        return this;
    }

    public int getPollFrequencySeconds() {
        return pollFrequencySeconds;
    }

    public DbConfiguration setPollFrequencySeconds(int pollFrequencySeconds) {
        this.pollFrequencySeconds = pollFrequencySeconds;
        return this;
    }



    public String getLocalRootPath() {
        return localRootPath;
    }

    public DbConfiguration setLocalRootPath(String localRootPath) {
        this.localRootPath = localRootPath;
        return this;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public DbConfiguration setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
        return this;
    }

    public String getSoftwareContentType() {
        return softwareContentType;
    }

    public DbConfiguration setSoftwareContentType(String softwareContentType) {
        this.softwareContentType = softwareContentType;
        return this;
    }


}
