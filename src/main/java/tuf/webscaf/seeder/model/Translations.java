
package tuf.webscaf.seeder.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "kr",
    "br",
    "pt",
    "nl",
    "hr",
    "fa",
    "de",
    "es",
    "fr",
    "ja",
    "it",
    "cn",
    "tr"
})

public class Translations {

    @JsonProperty("kr")
    private String kr;
    @JsonProperty("br")
    private String br;
    @JsonProperty("pt")
    private String pt;
    @JsonProperty("nl")
    private String nl;
    @JsonProperty("hr")
    private String hr;
    @JsonProperty("fa")
    private String fa;
    @JsonProperty("de")
    private String de;
    @JsonProperty("es")
    private String es;
    @JsonProperty("fr")
    private String fr;
    @JsonProperty("ja")
    private String ja;
    @JsonProperty("it")
    private String it;
    @JsonProperty("cn")
    private String cn;
    @JsonProperty("tr")
    private String tr;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("kr")
    public String getKr() {
        return kr;
    }

    @JsonProperty("kr")
    public void setKr(String kr) {
        this.kr = kr;
    }

    @JsonProperty("br")
    public String getBr() {
        return br;
    }

    @JsonProperty("br")
    public void setBr(String br) {
        this.br = br;
    }

    @JsonProperty("pt")
    public String getPt() {
        return pt;
    }

    @JsonProperty("pt")
    public void setPt(String pt) {
        this.pt = pt;
    }

    @JsonProperty("nl")
    public String getNl() {
        return nl;
    }

    @JsonProperty("nl")
    public void setNl(String nl) {
        this.nl = nl;
    }

    @JsonProperty("hr")
    public String getHr() {
        return hr;
    }

    @JsonProperty("hr")
    public void setHr(String hr) {
        this.hr = hr;
    }

    @JsonProperty("fa")
    public String getFa() {
        return fa;
    }

    @JsonProperty("fa")
    public void setFa(String fa) {
        this.fa = fa;
    }

    @JsonProperty("de")
    public String getDe() {
        return de;
    }

    @JsonProperty("de")
    public void setDe(String de) {
        this.de = de;
    }

    @JsonProperty("es")
    public String getEs() {
        return es;
    }

    @JsonProperty("es")
    public void setEs(String es) {
        this.es = es;
    }

    @JsonProperty("fr")
    public String getFr() {
        return fr;
    }

    @JsonProperty("fr")
    public void setFr(String fr) {
        this.fr = fr;
    }

    @JsonProperty("ja")
    public String getJa() {
        return ja;
    }

    @JsonProperty("ja")
    public void setJa(String ja) {
        this.ja = ja;
    }

    @JsonProperty("it")
    public String getIt() {
        return it;
    }

    @JsonProperty("it")
    public void setIt(String it) {
        this.it = it;
    }

    @JsonProperty("cn")
    public String getCn() {
        return cn;
    }

    @JsonProperty("cn")
    public void setCn(String cn) {
        this.cn = cn;
    }

    @JsonProperty("tr")
    public String getTr() {
        return tr;
    }

    @JsonProperty("tr")
    public void setTr(String tr) {
        this.tr = tr;
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
