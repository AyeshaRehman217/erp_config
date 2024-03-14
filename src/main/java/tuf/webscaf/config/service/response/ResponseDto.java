package tuf.webscaf.config.service.response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

//@Builder
//@ToString
//@NoArgsConstructor
//@AllArgsConstructor
public class ResponseDto {

    public LocalDateTime timestamp;
    public Integer status;
    public String message;
    public String error;
    public String language;
    public String token;
    public Long totalDataRowsWithFilter;
    public Long totalDataRowsWithoutFilter;
    public String requestId;
    public AppResponseDto appResponse;
    public Object session;

    public ResponseDto(Integer status, String message, String error, String language, String token, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter, List<AppResponseMessage> messages){
        Timestamp timestampMilli = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestampMilli.toLocalDateTime();
        this.status = status;
        this.message = message;
        this.error = error ;
        this.language = language;
        this.token = token;
        this.totalDataRowsWithFilter = totalDataRowsWithFilter;
        this.totalDataRowsWithoutFilter = totalDataRowsWithoutFilter;

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String appMessages = objectMapper.writeValueAsString(messages);
            this.appResponse = new AppResponseDto("language", "token", totalDataRowsWithFilter, totalDataRowsWithoutFilter, appMessages);
        } catch(Exception e) {
            e.printStackTrace();
        }


    }

    public ResponseDto(Integer status, String message, String error, String language, String token, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter, List<AppResponseMessage> messages, Object session) {
        Timestamp timestampMilli = new Timestamp(System.currentTimeMillis());
        this.timestamp = timestampMilli.toLocalDateTime();
        this.status = status;
        this.message = message;
        this.error = error;
        this.language = language;
        this.token = token;
        this.totalDataRowsWithFilter = totalDataRowsWithFilter;
        this.totalDataRowsWithoutFilter = totalDataRowsWithoutFilter;
        this.session = session;


        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String appMessages = objectMapper.writeValueAsString(messages);
            this.appResponse = new AppResponseDto("language", "token", totalDataRowsWithFilter, totalDataRowsWithoutFilter, appMessages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
