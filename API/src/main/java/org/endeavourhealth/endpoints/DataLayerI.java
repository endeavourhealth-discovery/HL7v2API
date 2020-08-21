package org.endeavourhealth.endpoints;

import org.endeavourhealth.dal.DbConfiguration;
import org.endeavourhealth.dal.DbInstance;

public interface DataLayerI {

    DbInstance getInstanceConfiguration(String instanceName, String hostname) throws Exception;
    DbConfiguration getConfiguration(String configurationId) throws Exception;



}
