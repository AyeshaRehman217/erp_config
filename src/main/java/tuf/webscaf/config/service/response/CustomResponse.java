package tuf.webscaf.config.service.response;

import org.reactivestreams.Publisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.session.ResponseDtoFSession;
import tuf.webscaf.config.service.response.session.ResponseMapperSession;

import java.util.List;

@Component
public class CustomResponse {

//    public Mono<ServerResponse> set(Integer status, String message, String error, String language, String token, List<AppResponseMessage> messages, Object data){
    public Mono<ServerResponse> set(Integer status, String message, String error, String language, String token, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter, List<AppResponseMessage> messages, Object data){

//        Integer totalDataRowsWithFilter = 0;
//        Integer totalDataRowsWithoutFilter = 0;

        ResponseDto responseDto = new ResponseDto(status, message, error, language, token, totalDataRowsWithFilter, totalDataRowsWithoutFilter, messages);

        Mono<ResponseDto> responseDtoMono = Mono.just(responseDto);

        Mono<ResponseDtoF> customResponse = Flux.from((Publisher<Object>)data).collectList()
                .map(ResponseMapper::new)
                .zipWith(responseDtoMono)
                .map(t -> t.getT1().apply(t.getT2()))
         .map(ResponseMapper::build);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(customResponse, ResponseDtoF.class);
    }

    public Mono<ServerResponse> set(Integer status, String message, String error, String language, String token, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter, List<AppResponseMessage> messages, Object data, Object session) {
        ResponseDto responseDto = new ResponseDto(status, message, error, language, token, totalDataRowsWithFilter, totalDataRowsWithoutFilter, messages, session);
        Mono<ResponseDto> responseDtoMono = Mono.just(responseDto);

        Mono<ResponseDtoFSession> customResponse = Flux.from((Publisher<Object>) data).collectList()
                .map(ResponseMapperSession::new)
                .zipWith(responseDtoMono)
                .map(t -> t.getT1().apply(t.getT2()))
                .map(ResponseMapperSession::build);

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(customResponse, ResponseDtoFSession.class);
    }
}
