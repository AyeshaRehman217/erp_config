
package tuf.webscaf.seeder.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "zoneName",
    "gmtOffset",
    "gmtOffsetName",
    "abbreviation",
    "tzName"
})

public class Timezone {

    @JsonProperty("zoneName")
    private String zoneName;
    @JsonProperty("gmtOffset")
    private Integer gmtOffset;
    @JsonProperty("gmtOffsetName")
    private String gmtOffsetName;
    @JsonProperty("abbreviation")
    private String abbreviation;
    @JsonProperty("tzName")
    private String tzName;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("zoneName")
    public String getZoneName() {
        return zoneName;
    }

    @JsonProperty("zoneName")
    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    @JsonProperty("gmtOffset")
    public Integer getGmtOffset() {
        return gmtOffset;
    }

    @JsonProperty("gmtOffset")
    public void setGmtOffset(Integer gmtOffset) {
        this.gmtOffset = gmtOffset;
    }

    @JsonProperty("gmtOffsetName")
    public String getGmtOffsetName() {
        return gmtOffsetName;
    }

    @JsonProperty("gmtOffsetName")
    public void setGmtOffsetName(String gmtOffsetName) {
        this.gmtOffsetName = gmtOffsetName;
    }

    @JsonProperty("abbreviation")
    public String getAbbreviation() {
        return abbreviation;
    }

    @JsonProperty("abbreviation")
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    @JsonProperty("tzName")
    public String getTzName() {
        return tzName;
    }

    @JsonProperty("tzName")
    public void setTzName(String tzName) {
        this.tzName = tzName;
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
