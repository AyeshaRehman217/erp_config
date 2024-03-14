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
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;
import tuf.webscaf.app.dbContext.master.repositry.CalendarCategoryRepository;
import tuf.webscaf.app.dbContext.master.repositry.CalendarDateRepository;
import tuf.webscaf.app.dbContext.master.repositry.CalendarRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCalendarRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "calendarHandler")
@Component
public class CalendarHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    SlaveCalendarRepository slaveCalendarRepository;

    @Autowired
    CalendarCategoryRepository calendarCategoryRepository;

    @Autowired
    CalendarDateRepository calendarDateRepository;

    @Value("${server.zone}")
    private String zone;


    @AuthHasPermission(value = "config_api_v1_calendars_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        Flux<SlaveCalendarEntity> slaveCastEntityFlux = slaveCalendarRepository
                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord);
        return slaveCastEntityFlux
                .collectList()
                .flatMap(calendarEntityDB -> slaveCalendarRepository
                        .countAllByNameContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord)
                        .flatMap(count -> {
                            if (calendarEntityDB.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));

    }


    @AuthHasPermission(value = "config_api_v1_calendars_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                .flatMap(calendarEntityDB -> responseSuccessMsg("Record Fetched Successfully.", calendarEntityDB))
                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_calendars_store")
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

                    CalendarEntity calendarEntity = CalendarEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .date(LocalDate.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                            .calendarCategoryUUID(UUID.fromString(value.getFirst("calendarCategoryUUID").trim()))
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

                    //Check if Calendar Date Already Exist
                    return calendarRepository.findFirstByDateAndDeletedAtIsNull(calendarEntity.getDate())
                            .flatMap(checkSDate -> responseInfoMsg(calendarEntity.getDate().getYear() + " Calendar already exist"))
                            //check if Calendar Category exists
                            .switchIfEmpty(Mono.defer(() -> calendarCategoryRepository.findByUuidAndDeletedAtIsNull(calendarEntity.getCalendarCategoryUUID())
                                    .flatMap(calendarCategoryEntity -> {

                                        // Set Calendar Name like Calendar-Year
                                        calendarEntity.setName("Calendar " + calendarEntity.getDate().getYear());

                                        return calendarRepository.save(calendarEntity)
                                                .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully.", saveEntity))
                                                .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again"))
                                                .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer"));
                                    }).switchIfEmpty(responseInfoMsg("Calendar Category record does not exist."))
                                    .onErrorResume(err -> responseErrorMsg("Calendar Category record does not exist. Please contact developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer"));
    }

    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                        .flatMap(previousCalendarEntity -> {

                            CalendarEntity updatedCalendarEntity = CalendarEntity.builder()
                                    .uuid(previousCalendarEntity.getUuid())
                                    .date(LocalDate.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                                    .calendarCategoryUUID(UUID.fromString(value.getFirst("calendarCategoryUUID")))
                                    .createdAt(previousCalendarEntity.getCreatedAt())
                                    .createdBy(previousCalendarEntity.getCreatedBy())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCalendarEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousCalendarEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCalendarEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCalendarEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousCalendarEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCalendarEntity.getReqCreatedReferer())
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
                            previousCalendarEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousCalendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCalendarEntity.setReqDeletedIP(reqIp);
                            previousCalendarEntity.setReqDeletedPort(reqPort);
                            previousCalendarEntity.setReqDeletedBrowser(reqBrowser);
                            previousCalendarEntity.setReqDeletedOS(reqOs);
                            previousCalendarEntity.setReqDeletedDevice(reqDevice);
                            previousCalendarEntity.setReqDeletedReferer(reqReferer);

                            //Check if Calendar Date Already Exist
                            return calendarRepository.findFirstByDateAndDeletedAtIsNullAndUuidIsNot(updatedCalendarEntity.getDate(), calendarUUID)
                                    .flatMap(checkDate -> responseInfoMsg(updatedCalendarEntity.getDate().getYear() + " Calendar already exist"))
                                    //check if Calendar Category Exists
                                    .switchIfEmpty(Mono.defer(() -> calendarCategoryRepository.findByUuidAndDeletedAtIsNull(updatedCalendarEntity.getCalendarCategoryUUID())
                                            .flatMap(calendarCategoryEntity -> {

                                                // Set Calendar Name like Calendar-Year
                                                updatedCalendarEntity.setName("Calendar " + updatedCalendarEntity.getDate().getYear());

                                                //Delete Previous Calendar
                                                return calendarRepository.save(previousCalendarEntity)
                                                        .then(calendarRepository.save(updatedCalendarEntity))
                                                        .flatMap(saveEntity -> responseSuccessMsg("Record Updated Successfully.", saveEntity))
                                                        .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again"))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer"));
                                            }).switchIfEmpty(responseInfoMsg("Calendar Category record does not exist."))
                                            .onErrorResume(err -> responseErrorMsg("Calendar Category record does not exist. Please contact developer."))
                                    ));
                        }).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer"))
                ).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request.")))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer"));
    }

    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID calendarUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                //check if Calendar Exists in Calendar Dates
                .flatMap(calendarEntity -> calendarDateRepository.findFirstByCalendarUUIDAndDeletedAtIsNull(calendarEntity.getUuid())
                        .flatMap(date -> responseInfoMsg("Unable to Delete Record as the Reference exists."))
                        .switchIfEmpty(Mono.defer(() -> {

                            calendarEntity.setDeletedBy(UUID.fromString(userUUID));
                            calendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            calendarEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            calendarEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            calendarEntity.setReqDeletedIP(reqIp);
                            calendarEntity.setReqDeletedPort(reqPort);
                            calendarEntity.setReqDeletedBrowser(reqBrowser);
                            calendarEntity.setReqDeletedOS(reqOs);
                            calendarEntity.setReqDeletedDevice(reqDevice);
                            calendarEntity.setReqDeletedReferer(reqReferer);
                            
                            return calendarRepository.save(calendarEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully.", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
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
