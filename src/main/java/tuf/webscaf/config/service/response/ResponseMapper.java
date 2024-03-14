package tuf.webscaf.config.service.response;

import java.time.LocalDateTime;

public class ResponseMapper {

    public LocalDateTime timestamp;
    public Integer status;
    public String message;
    public String error;
    public String language;
    public String token;
    public String requestId;
    public AppResponseDto appResponse;
    public Object data;


    public ResponseMapper(Object data) {
        this.data = data;
    }

    public ResponseMapper apply(ResponseDto responseDto) {

        this.timestamp = responseDto.timestamp;
        this.status = responseDto.status;
        this.message = responseDto.message;
        this.error = responseDto.error;
        this.token = responseDto.token;
        this.requestId = responseDto.requestId;
        this.appResponse = responseDto.appResponse;
        return this;
    }

    public ResponseDtoF build() {
        return new ResponseDtoF(timestamp, status, message, error, language, token, requestId, appResponse, data);
    }
}
