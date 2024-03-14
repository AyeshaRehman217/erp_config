package tuf.webscaf.config.service.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AppResponseDto {

    public String language;
    public String token;
    public Long totalDataRowsWithFilter;
    public Long totalDataRowsWithoutFilter;

    @JsonRawValue
    public String message;


    public AppResponseDto(String language, String token,  Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter ,String message){
        this.language = language;
        this.token = token;
        this.message = message;
        this.totalDataRowsWithFilter = totalDataRowsWithFilter;
        this.totalDataRowsWithoutFilter = totalDataRowsWithoutFilter;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
