
package tuf.webscaf.seeder.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "iso3",
    "iso2",
    "numeric_code",
    "phone_code",
    "capital",
    "currency",
    "currency_name",
    "currency_symbol",
    "tld",
    "native",
    "region",
    "subregion",
    "timezones",
    "translations",
    "latitude",
    "longitude",
    "emoji",
    "emojiU",
    "states"
})

public class Country {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("iso3")
    private String iso3;
    @JsonProperty("iso2")
    private String iso2;
    @JsonProperty("numeric_code")
    private String numericCode;
    @JsonProperty("phone_code")
    private String phoneCode;
    @JsonProperty("capital")
    private String capital;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("currency_name")
    private String currencyName;
    @JsonProperty("currency_symbol")
    private String currencySymbol;
    @JsonProperty("tld")
    private String tld;
    @JsonProperty("native")
    private String _native;
    @JsonProperty("region")
    private String region;
    @JsonProperty("subregion")
    private String subregion;
    @JsonProperty("timezones")
    private List<Timezone> timezones = null;
    @JsonProperty("translations")
    private Translations translations;
    @JsonProperty("latitude")
    private String latitude;
    @JsonProperty("longitude")
    private String longitude;
    @JsonProperty("emoji")
    private String emoji;
    @JsonProperty("emojiU")
    private String emojiU;
    @JsonProperty("states")
    private List<State> states = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("iso3")
    public String getIso3() {
        return iso3;
    }

    @JsonProperty("iso3")
    public void setIso3(String iso3) {
        this.iso3 = iso3;
    }

    @JsonProperty("iso2")
    public String getIso2() {
        return iso2;
    }

    @JsonProperty("iso2")
    public void setIso2(String iso2) {
        this.iso2 = iso2;
    }

    @JsonProperty("numeric_code")
    public String getNumericCode() {
        return numericCode;
    }

    @JsonProperty("numeric_code")
    public void setNumericCode(String numericCode) {
        this.numericCode = numericCode;
    }

    @JsonProperty("phone_code")
    public String getPhoneCode() {
        return phoneCode;
    }

    @JsonProperty("phone_code")
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    @JsonProperty("capital")
    public String getCapital() {
        return capital;
    }

    @JsonProperty("capital")
    public void setCapital(String capital) {
        this.capital = capital;
    }

    @JsonProperty("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonProperty("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonProperty("currency_name")
    public String getCurrencyName() {
        return currencyName;
    }

    @JsonProperty("currency_name")
    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    @JsonProperty("currency_symbol")
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    @JsonProperty("currency_symbol")
    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @JsonProperty("tld")
    public String getTld() {
        return tld;
    }

    @JsonProperty("tld")
    public void setTld(String tld) {
        this.tld = tld;
    }

    @JsonProperty("native")
    public String getNative() {
        return _native;
    }

    @JsonProperty("native")
    public void setNative(String _native) {
        this._native = _native;
    }

    @JsonProperty("region")
    public String getRegion() {
        return region;
    }

    @JsonProperty("region")
    public void setRegion(String region) {
        this.region = region;
    }

    @JsonProperty("subregion")
    public String getSubregion() {
        return subregion;
    }

    @JsonProperty("subregion")
    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }

    @JsonProperty("timezones")
    public List<Timezone> getTimezones() {
        return timezones;
    }

    @JsonProperty("timezones")
    public void setTimezones(List<Timezone> timezones) {
        this.timezones = timezones;
    }

    @JsonProperty("translations")
    public Translations getTranslations() {
        return translations;
    }

    @JsonProperty("translations")
    public void setTranslations(Translations translations) {
        this.translations = translations;
    }

    @JsonProperty("latitude")
    public String getLatitude() {
        return latitude;
    }

    @JsonProperty("latitude")
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    @JsonProperty("longitude")
    public String getLongitude() {
        return longitude;
    }

    @JsonProperty("longitude")
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @JsonProperty("emoji")
    public String getEmoji() {
        return emoji;
    }

    @JsonProperty("emoji")
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    @JsonProperty("emojiU")
    public String getEmojiU() {
        return emojiU;
    }

    @JsonProperty("emojiU")
    public void setEmojiU(String emojiU) {
        this.emojiU = emojiU;
    }

    @JsonProperty("states")
    public List<State> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(List<State> states) {
        this.states = states;
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
