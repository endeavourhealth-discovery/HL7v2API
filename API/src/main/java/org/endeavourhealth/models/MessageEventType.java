
package org.endeavourhealth.models;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "valueCodeableConcept",
    "url"
})
public class MessageEventType {

    @JsonProperty("valueCodeableConcept")
    private ValueCodeableConcept valueCodeableConcept;
    @JsonProperty("url")
    private String url;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("valueCodeableConcept")
    public ValueCodeableConcept getValueCodeableConcept() {
        return valueCodeableConcept;
    }

    @JsonProperty("valueCodeableConcept")
    public void setValueCodeableConcept(ValueCodeableConcept valueCodeableConcept) {
        this.valueCodeableConcept = valueCodeableConcept;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}