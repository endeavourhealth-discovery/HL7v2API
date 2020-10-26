package org.endeavourhealth.dal;

public class DbInstanceEds {
    private String edsUrl;
    private boolean useKeycloak;
    private String keycloakTokenUri;
    private String keycloakRealm;
    private String keycloakUsername;
    private String keycloakPassword;
    private String keycloakClientId;
    private String softwareContentType;
    private String softwareVersion;
    private int chunksize;
    private int secounds;

    public int getSecounds() {
        return secounds;
    }

    public DbInstanceEds setSecounds(int secounds) {
        this.secounds = secounds;
        return this;
    }

    public int getChunksize() {
        return chunksize;
    }

    public DbInstanceEds setChunksize(int chunksize) {
        this.chunksize = chunksize;
        return this;
    }

    public String getSoftwareContentType() {
        return softwareContentType;
    }

    public void setSoftwareContentType(String softwareContentType) {
        this.softwareContentType = softwareContentType;
    }


    public String getEdsUrl() {
        return edsUrl;
    }

    public DbInstanceEds setEdsUrl(String edsUrl) {
        this.edsUrl = edsUrl;
        return this;
    }

    public boolean isUseKeycloak() {
        return useKeycloak;
    }

    public DbInstanceEds setUseKeycloak(boolean useKeycloak) {
        this.useKeycloak = useKeycloak;
        return this;
    }

    public String getKeycloakTokenUri() {
        return keycloakTokenUri;
    }

    public DbInstanceEds setKeycloakTokenUri(String keycloakTokenUri) {
        this.keycloakTokenUri = keycloakTokenUri;
        return this;
    }

    public String getKeycloakRealm() {
        return keycloakRealm;
    }

    public DbInstanceEds setKeycloakRealm(String keycloakRealm) {
        this.keycloakRealm = keycloakRealm;
        return this;
    }

    public String getKeycloakUsername() {
        return keycloakUsername;
    }

    public DbInstanceEds setKeycloakUsername(String keycloakUsername) {
        this.keycloakUsername = keycloakUsername;
        return this;
    }

    public String getKeycloakPassword() {
        return keycloakPassword;
    }

    public DbInstanceEds setKeycloakPassword(String keycloakPassword) {
        this.keycloakPassword = keycloakPassword;
        return this;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getKeycloakClientId() {
        return keycloakClientId;
    }

    public DbInstanceEds setKeycloakClientId(String keycloakClientId) {
        this.keycloakClientId = keycloakClientId;
        return this;
    }

}
