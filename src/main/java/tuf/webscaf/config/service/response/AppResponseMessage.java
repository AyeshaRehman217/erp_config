package tuf.webscaf.config.service.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.EnumMap;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AppResponseMessage {

    EnumMap<AppResponse.Response, Integer> responseMessageType = new EnumMap<AppResponse.Response, Integer>(AppResponse.Response.class);

    public AppResponse.Response messageType;
    public Integer messageCode;
    public String message;

    public AppResponseMessage(AppResponse.Response messageType, String message) {

        this.responseMessageType.put(AppResponse.Response.SUCCESS,99200);
        this.responseMessageType.put(AppResponse.Response.INFO,99203);
        this.responseMessageType.put(AppResponse.Response.WARNING,99409);
        this.responseMessageType.put(AppResponse.Response.ERROR,99500);
        this.messageType = messageType;
        this.messageCode = responseMessageType.get(this.messageType);
        this.message = message;


    }
}
