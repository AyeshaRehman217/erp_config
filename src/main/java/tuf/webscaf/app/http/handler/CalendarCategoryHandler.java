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
import tuf.webscaf.app.dbContext.master.entity.CalendarCategoryEntity;
import tuf.webscaf.app.dbContext.master.repositry.CalendarCategoryRepository;
import tuf.webscaf.app.dbContext.master.repositry.CalendarRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarCategoryEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCalendarCategoryRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "calendarCategoryHandler")
@Component
public class CalendarCategoryHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    CalendarCategoryRepository calendarCategoryRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    SlaveCalendarCategoryRepository slaveCalendarCategoryRepository;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_calendar-categories_index")
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
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

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
            Flux<SlaveCalendarCategoryEntity> slaveCastEntityFlux = slaveCalendarCategoryRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
                            Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), pageable);
            return slaveCastEntityFlux
                    .collectList()
                    .flatMap(calendarCategoryEntityDB -> slaveCalendarCategoryRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count ->
                            {
                                if (calendarCategoryEntityDB.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarCategoryEntityDB, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveCalendarCategoryEntity> slaveCastEntityFlux = slaveCalendarCategoryRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, pageable);
            return slaveCastEntityFlux
                    .collectList()
                    .flatMap(calendarCategoryEntityDB -> slaveCalendarCategoryRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count ->
                            {
                                if (calendarCategoryEntityDB.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", calendarCategoryEntityDB, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }

    }

    @AuthHasPermission(value = "config_api_v1_calendar-categories_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarCategoryUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarCategoryRepository.findByUuidAndDeletedAtIsNull(calendarCategoryUUID)
                .flatMap(calendarCategoryEntityDB -> responseSuccessMsg("Record Fetched Successfully.", calendarCategoryEntityDB))
                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_calendar-categories_store")
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
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User!");
            }
        }

        return serverRequest.formData()
                .flatMap(value -> {

                    CalendarCategoryEntity calendarCategoryEntity = CalendarCategoryEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name"))
                            .description(value.getFirst("description"))
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .createdBy(UUID.fromString(userUUID))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

//                    check calendar-category name is unique
                    return calendarCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(calendarCategoryEntity.getName())
                            .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exists"))
                            .switchIfEmpty(Mono.defer(() -> calendarCategoryRepository.save(calendarCategoryEntity)
                                    .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully.", saveEntity))
                                    .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record. There is some thing wrong please try again")))
                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer"))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.  Please contact developer"));
    }

    @AuthHasPermission(value = "config_api_v1_calendar-categories_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID calendarCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> calendarCategoryRepository.findByUuidAndDeletedAtIsNull(calendarCategoryUUID)
                                .flatMap(previousCalendarCategoryEntity -> {

                                    CalendarCategoryEntity updatedCalendarCategoryEntity = CalendarCategoryEntity.builder()
                                            .uuid(previousCalendarCategoryEntity.getUuid())
                                            .name(value.getFirst("name"))
                                            .description(value.getFirst("description"))
                                            .status(Boolean.valueOf(value.getFirst("status")))
                                            .createdAt(previousCalendarCategoryEntity.getCreatedAt())
                                            .createdBy(previousCalendarCategoryEntity.getCreatedBy())
                                            .updatedBy(UUID.fromString(userUUID))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(previousCalendarCategoryEntity.getReqCreatedIP())
                                            .reqCreatedPort(previousCalendarCategoryEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(previousCalendarCategoryEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(previousCalendarCategoryEntity.getReqCreatedOS())
                                            .reqCreatedDevice(previousCalendarCategoryEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(previousCalendarCategoryEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                            .reqUpdatedIP(reqIp)
                                            .reqUpdatedPort(reqPort)
                                            .reqUpdatedBrowser(reqBrowser)
                                            .reqUpdatedOS(reqOs)
                                            .reqUpdatedDevice(reqDevice)
                                            .reqUpdatedReferer(reqReferer)
                                            .build();

                                    //Deleting Previous Record and Creating a New One Based on UUID
                                    previousCalendarCategoryEntity.setDeletedBy(UUID.fromString(userUUID));
                                    previousCalendarCategoryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousCalendarCategoryEntity.setReqDeletedIP(reqIp);
                                    previousCalendarCategoryEntity.setReqDeletedPort(reqPort);
                                    previousCalendarCategoryEntity.setReqDeletedBrowser(reqBrowser);
                                    previousCalendarCategoryEntity.setReqDeletedOS(reqOs);
                                    previousCalendarCategoryEntity.setReqDeletedDevice(reqDevice);
                                    previousCalendarCategoryEntity.setReqDeletedReferer(reqReferer);

//                                  check calendarCategory name is unique
                                    return calendarCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCalendarCategoryEntity.getName(), calendarCategoryUUID)
                                            .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exists"))
                                            .switchIfEmpty(Mono.defer(() -> calendarCategoryRepository.save(previousCalendarCategoryEntity)
                                                    .then(calendarCategoryRepository.save(updatedCalendarCategoryEntity))
                                                    .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully.", saveEntity))
                                                    .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record. There is some thing wrong please try again")))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to store record."))
                                            ));
                                }).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer"))
                ).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request.")))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer"));
    }

    @AuthHasPermission(value = "config_api_v1_calendar-categories_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID calendarCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return calendarCategoryRepository.findByUuidAndDeletedAtIsNull(calendarCategoryUUID)
                            .flatMap(previousCalendarCategoryEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCalendarCategoryEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                CalendarCategoryEntity updatedCalendarCategoryEntity = CalendarCategoryEntity.builder()
                                        .name(previousCalendarCategoryEntity.getName())
                                        .status(status == true ? true : false)
                                        .description(previousCalendarCategoryEntity.getDescription())
                                        .uuid(previousCalendarCategoryEntity.getUuid())
                                        .createdAt(previousCalendarCategoryEntity.getCreatedAt())
                                        .createdBy(previousCalendarCategoryEntity.getCreatedBy())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCalendarCategoryEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCalendarCategoryEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCalendarCategoryEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCalendarCategoryEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCalendarCategoryEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCalendarCategoryEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                // update status
                                previousCalendarCategoryEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousCalendarCategoryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCalendarCategoryEntity.setReqDeletedIP(reqIp);
                                previousCalendarCategoryEntity.setReqDeletedPort(reqPort);
                                previousCalendarCategoryEntity.setReqDeletedBrowser(reqBrowser);
                                previousCalendarCategoryEntity.setReqDeletedOS(reqOs);
                                previousCalendarCategoryEntity.setReqDeletedDevice(reqDevice);
                                previousCalendarCategoryEntity.setReqDeletedReferer(reqReferer);

                                return calendarCategoryRepository.save(previousCalendarCategoryEntity)
                                        .then(calendarCategoryRepository.save(updatedCalendarCategoryEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseErrorMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please Contact Developer."));
                }).switchIfEmpty(responseErrorMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_calendar-categories_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID calendarCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return calendarCategoryRepository.findByUuidAndDeletedAtIsNull(calendarCategoryUUID)
                .flatMap(calendarCategoryEntity -> calendarRepository.findFirstByCalendarCategoryUUIDAndDeletedAtIsNull(calendarCategoryEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference of Record Exists"))
                        .switchIfEmpty(Mono.defer(() -> {

                            calendarCategoryEntity.setDeletedBy(UUID.fromString(userUUID));
                            calendarCategoryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarCategoryEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarCategoryEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarCategoryEntity.setReqDeletedIP(reqIp);
                            calendarCategoryEntity.setReqDeletedPort(reqPort);
                            calendarCategoryEntity.setReqDeletedBrowser(reqBrowser);
                            calendarCategoryEntity.setReqDeletedOS(reqOs);
                            calendarCategoryEntity.setReqDeletedDevice(reqDevice);
                            calendarCategoryEntity.setReqDeletedReferer(reqReferer);

                            return calendarCategoryRepository.save(calendarCategoryEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully.", entity))
                                    .switchIfEmpty(responseErrorMsg("Unable to delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record.Please contact developer."));

                        }))
                ).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
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

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
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
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
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
                0L,
                messages,
                Mono.just(entity)
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
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
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
}
