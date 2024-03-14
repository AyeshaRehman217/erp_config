package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CityEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveConfigModuleDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCityRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "infoHandler")
public class ConfigModuleHandler {

    @Autowired
    CustomResponse appresponse;
    
    @Autowired
    Environment environment;


    @AuthHasPermission(value = "config_api_v1_info_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        String baseUrl = getBaseUrl(String.valueOf(serverRequest.uri()));
        String hostAddress = "";

        try {
            hostAddress = new URL(baseUrl).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        SlaveConfigModuleDto slaveStudentFinancialModuleDto = SlaveConfigModuleDto.builder()
                .moduleId(Long.valueOf(environment.getProperty("server.module.id")))
                .moduleUUID(UUID.fromString(environment.getProperty("server.module.uuid")))
                .baseUrl(getBaseUrl(String.valueOf(serverRequest.uri())))
                .infoUrl(String.valueOf(serverRequest.uri()))
                .hostAddress(hostAddress)
                .build();

        return responseSuccessMsg("Record Fetched Successfully", slaveStudentFinancialModuleDto)
                .switchIfEmpty(responseInfoMsg("Record does not exist. There is something wrong please try again."))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    public String getBaseUrl(String uri){
        return uri.substring(0, uri.indexOf("api"));
    }

    public static String baseUrl(ServerRequest request){
        URI uri = request.uri();
        String port = "";
        if(uri.getPort()!=80){
            port = String.format(":%s", uri.getPort());
        }
        return String.format("%s://%s%s", uri.getScheme(), uri.getHost(), port);
    }

    //    Custom Functions

    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );
        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }
}


