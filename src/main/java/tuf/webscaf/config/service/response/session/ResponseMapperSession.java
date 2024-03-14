package tuf.webscaf.config.service.response.session;

import tuf.webscaf.config.service.response.AppResponseDto;
import tuf.webscaf.config.service.response.ResponseDto;

import java.time.LocalDateTime;

public class ResponseMapperSession {

    public LocalDateTime timestamp;
    public Integer status;
    public String message;
    public String error;
    public String language;
    public String token;
    public String requestId;
    public AppResponseDto appResponse;
    public Object data;
    public Object session;

    public ResponseMapperSession(Object data) {
        this.data = data;
    }

    public ResponseMapperSession apply(ResponseDto responseDto) {
        this.timestamp = responseDto.timestamp;
        this.status = responseDto.status;
        this.message = responseDto.message;
        this.error = responseDto.error;
        this.token = responseDto.token;
        this.requestId = responseDto.requestId;
        this.appResponse = responseDto.appResponse;
        this.session = responseDto.session;
        return this;
    }

    public ResponseDtoFSession build() {
        return new ResponseDtoFSession(timestamp, status, message, error, language, token, requestId, appResponse, data, session);
    }
}
