package tuf.webscaf.seeder.model;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "native",
        "rtl"
})

public class Language {

    @JsonProperty("name")
    private String name;
    @JsonProperty("native")
    private String _native;
    @JsonProperty("rtl")
    private Integer rtl;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("native")
    public String getNative() {
        return _native;
    }

    @JsonProperty("native")
    public void setNative(String _native) {
        this._native = _native;
    }

    @JsonProperty("rtl")
    public Integer getRtl() {
        return rtl;
    }

    @JsonProperty("rtl")
    public void setRtl(Integer rtl) {
        this.rtl = rtl;
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