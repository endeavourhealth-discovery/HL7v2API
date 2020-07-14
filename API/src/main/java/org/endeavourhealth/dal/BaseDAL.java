package org.endeavourhealth.dal;

public interface BaseDAL extends AutoCloseable {
    void beginTransaction() throws Exception;
    void rollback() throws Exception;
    void commit() throws Exception;
}
