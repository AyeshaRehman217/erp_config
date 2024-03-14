package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;
import tuf.webscaf.app.dbContext.master.repositry.ConfigRepository;
import tuf.webscaf.app.dbContext.master.repositry.ModuleRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveConfigRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveModuleRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "configHandler")
public class ConfigHandler {

    @Autowired
    SlaveConfigRepository slaveConfigRepository;

    @Autowired
    ConfigRepository configRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    SlaveModuleRepository slaveModuleRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_configs_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveConfigEntity> slaveConfigEntityFlux = slaveConfigRepository
                    .findAllByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveConfigEntityFlux
                    .collectList()
                    .flatMap(configEntity -> slaveConfigRepository
                            .countByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (configEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", configEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        } else {
            Flux<SlaveConfigEntity> slaveConfigEntityFlux = slaveConfigRepository
                    .findAllByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveConfigEntityFlux
                    .collectList()
                    .flatMap(configEntity -> slaveConfigRepository.countByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (configEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", configEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
    }

    @AuthHasPermission(value = "config_api_v1_configs_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID configUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveConfigRepository.findByUuidAndDeletedAtIsNull(configUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //    Show Configs for module id
    @AuthHasPermission(value = "config_api_v1_configs_module_show")
    public Mono<ServerResponse> showListsOfConfig(ServerRequest serverRequest) {

        UUID moduleUUID = UUID.fromString(serverRequest.pathVariable("moduleUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);

        if (size > 100) {
            size = 100;
        }
        int page = pageRequest - 1;

        Pageable pageable = PageRequest.of(page, size);

        if (!status.isEmpty()) {

            Flux<SlaveConfigEntity> slaveConfigEntityFlux = slaveConfigRepository.
                    listOfConfigsWithStatusFilterAgainstModule(moduleUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveConfigEntityFlux
                    .collectList()
                    .flatMap(configEntity -> slaveConfigRepository.countConfigWithStatusFilterAgainstModule
                                    (moduleUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (configEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", configEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveConfigEntity> slaveConfigEntityFlux = slaveConfigRepository.
                    listOfConfigsAgainstModule(moduleUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveConfigEntityFlux
                    .collectList()
                    .flatMap(configEntity -> slaveConfigRepository.countConfigAgainstModule(moduleUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (configEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", configEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }

    @AuthHasPermission(value = "config_api_v1_configs_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> {

                    ConfigEntity configEntity = ConfigEntity.builder()
                            .uuid(UUID.randomUUID())
                            .key(value.getFirst("key").trim())
                            .value(value.getFirst("value").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .moduleUUID(UUID.fromString(value.getFirst("moduleUUID").trim()))
                            .createdBy(UUID.fromString(userUUID))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    //check if Module exist in modules table
                    return moduleRepository.findByUuidAndDeletedAtIsNull(configEntity.getModuleUUID())
                            //check if key is unique
                            .flatMap(checkKey -> configRepository.findFirstByKeyIgnoreCaseAndDeletedAtIsNull(configEntity.getKey())
                                    .flatMap(key -> responseInfoMsg("Key already exist"))
                                    //save Configs
                                    .switchIfEmpty(Mono.defer(() -> configRepository.save(configEntity))
                                            .flatMap(configSave -> responseSuccessMsg("Record stored successfully!", configEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer.")))
                            ).switchIfEmpty(responseInfoMsg("Module does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Module does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_configs_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        UUID configUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> configRepository.findByUuidAndDeletedAtIsNull(configUUID)
                        .flatMap(previousConfigEntity -> {

                            ConfigEntity updatedConfigEntity = ConfigEntity.builder()
                                    .uuid(previousConfigEntity.getUuid())
                                    .key(value.getFirst("key").trim())
                                    .value(value.getFirst("value").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .moduleUUID(UUID.fromString(value.getFirst("moduleUUID").trim()))
                                    .createdBy(previousConfigEntity.getCreatedBy())
                                    .createdAt(previousConfigEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousConfigEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousConfigEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousConfigEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousConfigEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousConfigEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousConfigEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousConfigEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousConfigEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousConfigEntity.setReqDeletedIP(reqIp);
                            previousConfigEntity.setReqDeletedPort(reqPort);
                            previousConfigEntity.setReqDeletedBrowser(reqBrowser);
                            previousConfigEntity.setReqDeletedOS(reqOs);
                            previousConfigEntity.setReqDeletedDevice(reqDevice);
                            previousConfigEntity.setReqDeletedReferer(reqReferer);

                            return moduleRepository.findByUuidAndDeletedAtIsNull(updatedConfigEntity.getModuleUUID())
                                    .flatMap(checkKey -> configRepository.findFirstByKeyIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedConfigEntity.getKey(), updatedConfigEntity.getUuid())
                                            .flatMap(key -> responseInfoMsg("Key already exist"))
                                            .switchIfEmpty(Mono.defer(() -> configRepository.save(previousConfigEntity)
                                                    .then(configRepository.save(updatedConfigEntity))
                                                    .flatMap(configSave -> responseSuccessMsg("Record updated successfully!", configSave))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("Module does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Module does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_configs_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID configUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseErrorMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return configRepository.findByUuidAndDeletedAtIsNull(configUUID)
                .flatMap(configEntity -> {
                    configEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    configEntity.setDeletedBy(UUID.fromString(userUUID));
                    configEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    configEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    configEntity.setReqDeletedIP(reqIp);
                    configEntity.setReqDeletedPort(reqPort);
                    configEntity.setReqDeletedBrowser(reqBrowser);
                    configEntity.setReqDeletedOS(reqOs);
                    configEntity.setReqDeletedDevice(reqDevice);
                    configEntity.setReqDeletedReferer(reqReferer);

                    return configRepository.save(configEntity)
                            .flatMap(value1 -> responseSuccessMsg("Record deleted successfully", value1))
                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_configs_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {

        UUID configUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User!");
            }
        }
        return serverRequest.formData()
                .flatMap(value -> {

                    boolean status = Boolean.parseBoolean(value.getFirst("status"));

                    return configRepository.findByUuidAndDeletedAtIsNull(configUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                ConfigEntity updatedConfigEntity = ConfigEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .key(previousEntity.getKey())
                                        .value(previousEntity.getValue())
                                        .description(previousEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .moduleUUID(previousEntity.getModuleUUID())
                                        .createdBy(previousEntity.getCreatedBy())
                                        .createdAt(previousEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);


                                return configRepository.save(previousEntity)
                                        .then(configRepository.save(updatedConfigEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    //    Custom Functions


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

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter) {
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
                totalDataRowsWithFilter,
                totalDataRowsWithoutFilter,
                messages,
                Mono.empty()

        );
    }


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

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter, Long totalDataRowsWithoutFilter) {
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
                totalDataRowsWithFilter,
                totalDataRowsWithoutFilter,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }
//    Custom Functions
}


