package tuf.webscaf.config.service.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDtoF {

    public LocalDateTime timestamp;
    public Integer status;
    public String message;
    public String error;
    public String language;
    public String token;
    public String requestId;
    public AppResponseDto appResponse;
    public Object data;
}
