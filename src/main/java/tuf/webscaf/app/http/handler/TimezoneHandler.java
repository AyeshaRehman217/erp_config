package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;
import tuf.webscaf.app.dbContext.master.entity.TimezoneEntity;
import tuf.webscaf.app.dbContext.master.repositry.CountryTimezonePvtRepository;
import tuf.webscaf.app.dbContext.master.repositry.TimezoneRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveTimezoneRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helpers.SlugifyHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "timezoneHandler")
public class TimezoneHandler {

    @Autowired
    SlaveTimezoneRepository slaveTimezoneRepository;

    @Autowired
    TimezoneRepository timezoneRepository;

    @Autowired
    CountryTimezonePvtRepository countryTimezonePvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_time-zones_index")
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

            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .findAllByZoneNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository
                            .countByZoneNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (timezoneEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", timezoneEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .findAllByZoneNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository.countByZoneNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (timezoneEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully", timezoneEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "config_api_v1_time-zones_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID timezoneUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveTimezoneRepository.findByUuidAndDeletedAtIsNull(timezoneUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_time-zones_store")
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


                    // get encoded value of Encode Gmt Offset Name
                    String encodedGmtOffsetName = value.getFirst("gmtOffsetName");

                    String decodedGmtOffsetName = "";

                    // decode the value of  Gmt Offset Name
                    try {
                        decodedGmtOffsetName = URLDecoder.decode(encodedGmtOffsetName, "UTF-8");
                    } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }


                    TimezoneEntity timezoneEntity = TimezoneEntity.builder()
                            .uuid(UUID.randomUUID())
                            .zoneName(value.getFirst("zoneName").trim())
                            .gmtOffset(value.getFirst("gmtOffset").trim())
                            .gmtOffsetName(decodedGmtOffsetName)
                            .abbreviation(value.getFirst("abbreviation").trim())
                            .tzName(value.getFirst("tzName").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
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

                    //check if zone name is unique
                    return timezoneRepository.findFirstByZoneNameIgnoreCaseAndDeletedAtIsNull(timezoneEntity.getZoneName())
                            .flatMap(checkKey -> responseInfoMsg("Zone already exist"))
                            .switchIfEmpty(Mono.defer(() -> timezoneRepository.save(timezoneEntity)
                                    .flatMap(timezoneEntity1 -> responseSuccessMsg("Record stored successfully!", timezoneEntity1))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please contact developer."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record. Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_time-zones_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID timezoneUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> timezoneRepository.findByUuidAndDeletedAtIsNull(timezoneUUID)
                        .flatMap(previousTimeZone -> {

                            // get encoded value of Encode Gmt Offset Name
                            String encodedGmtOffsetName = value.getFirst("gmtOffsetName");

                            String decodedGmtOffsetName = "";

                            // decode the value of  Gmt Offset Name
                            try {
                                decodedGmtOffsetName = URLDecoder.decode(encodedGmtOffsetName, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            TimezoneEntity updatedTimeZoneEntity = TimezoneEntity.builder()
                                    .uuid(previousTimeZone.getUuid())
                                    .zoneName(value.getFirst("zoneName").trim())
                                    .gmtOffset(value.getFirst("gmtOffset").trim())
                                    .gmtOffsetName(decodedGmtOffsetName)
                                    .abbreviation(value.getFirst("abbreviation").trim())
                                    .tzName(value.getFirst("tzName").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousTimeZone.getCreatedBy())
                                    .createdAt(previousTimeZone.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousTimeZone.getReqCreatedIP())
                                    .reqCreatedPort(previousTimeZone.getReqCreatedPort())
                                    .reqCreatedBrowser(previousTimeZone.getReqCreatedBrowser())
                                    .reqCreatedOS(previousTimeZone.getReqCreatedOS())
                                    .reqCreatedDevice(previousTimeZone.getReqCreatedDevice())
                                    .reqCreatedReferer(previousTimeZone.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousTimeZone.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousTimeZone.setDeletedBy(UUID.fromString(userUUID));
                            previousTimeZone.setReqDeletedIP(reqIp);
                            previousTimeZone.setReqDeletedPort(reqPort);
                            previousTimeZone.setReqDeletedBrowser(reqBrowser);
                            previousTimeZone.setReqDeletedOS(reqOs);
                            previousTimeZone.setReqDeletedDevice(reqDevice);
                            previousTimeZone.setReqDeletedReferer(reqReferer);

                            return timezoneRepository.findFirstByZoneNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedTimeZoneEntity.getZoneName(), updatedTimeZoneEntity.getUuid())
                                    .flatMap(checkKey -> responseInfoMsg("Zone already exist"))
                                    .switchIfEmpty(Mono.defer(() -> timezoneRepository.save(previousTimeZone)
                                            .then(timezoneRepository.save(updatedTimeZoneEntity))
                                            .flatMap(subRegionSave -> responseSuccessMsg("Record updated successfully!", subRegionSave))
                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record. Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to Read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_time-zones_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID timezoneUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return timezoneRepository.findByUuidAndDeletedAtIsNull(timezoneUUID)
                .flatMap(timezoneEntity -> countryTimezonePvtRepository.findFirstByTimezoneUUIDAndDeletedAtIsNull(timezoneEntity.getUuid())
//                                If timezone Reference exists in Country-Timezone-Pvt-Entity
                                .flatMap(countryTimezonePvtEntity -> responseInfoMsg("Unable to delete! Reference of record exists!"))
                                .switchIfEmpty(Mono.defer(() -> {

                                    timezoneEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    timezoneEntity.setDeletedBy(UUID.fromString(userUUID));
                                    timezoneEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    timezoneEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    timezoneEntity.setReqDeletedIP(reqIp);
                                    timezoneEntity.setReqDeletedPort(reqPort);
                                    timezoneEntity.setReqDeletedBrowser(reqBrowser);
                                    timezoneEntity.setReqDeletedOS(reqOs);
                                    timezoneEntity.setReqDeletedDevice(reqDevice);
                                    timezoneEntity.setReqDeletedReferer(reqReferer);

                                    return timezoneRepository.save(timezoneEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_time-zones_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID timezoneUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return timezoneRepository.findByUuidAndDeletedAtIsNull(timezoneUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                TimezoneEntity updatedTimeZoneEntity = TimezoneEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .zoneName(previousEntity.getZoneName())
                                        .gmtOffset(previousEntity.getGmtOffset())
                                        .gmtOffsetName(previousEntity.getGmtOffsetName())
                                        .abbreviation(previousEntity.getAbbreviation())
                                        .tzName(previousEntity.getTzName())
                                        .description(previousEntity.getDescription())
                                        .status(status == true ? true : false)
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

                                return timezoneRepository.save(previousEntity)
                                        .then(timezoneRepository.save(updatedTimeZoneEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please Contact Developer."));
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

    public Mono<ServerResponse> responseSlugMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
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
