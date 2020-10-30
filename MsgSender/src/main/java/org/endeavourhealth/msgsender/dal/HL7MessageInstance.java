package org.endeavourhealth.msgsender.dal;


public class HL7MessageInstance {
    private String meta;
    private String hl7message;
    private int id;

    public String getMeta() {
        return meta;
    }

    public int getId() {
        return id;
    }

    public HL7MessageInstance setId(int id) {
        this.id = id;
        return this;
    }

    public HL7MessageInstance setMeta(String meta) {
        this.meta = meta;
        return this;
    }

    public String getHl7message() {
        return hl7message;
    }

    public HL7MessageInstance setHl7message(String hl7message) {
        this.hl7message = hl7message;
        return this;
    }
}
