package tuf.webscaf.config.service.response.session;

import lombok.*;
import tuf.webscaf.config.service.response.AppResponseDto;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDtoFSession {

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

}
