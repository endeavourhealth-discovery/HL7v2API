
package org.endeavourhealth.models;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "profile",
    "tag"
})
public class Meta {

    @JsonProperty("profile")
    private List<String> profile = null;
    @JsonProperty("tag")
    private List<Tag> tag = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("profile")
    public List<String> getProfile() {
        return profile;
    }

    @JsonProperty("profile")
    public void setProfile(List<String> profile) {
        this.profile = profile;
    }

    @JsonProperty("tag")
    public List<Tag> getTag() {
        return tag;
    }

    @JsonProperty("tag")
    public void setTag(List<Tag> tag) {
        this.tag = tag;
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
