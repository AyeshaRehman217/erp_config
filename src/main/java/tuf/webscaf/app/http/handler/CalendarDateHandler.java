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
import tuf.webscaf.app.dbContext.master.entity.CalendarDateEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarDateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCalendarDateRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "calendarDateHandler")
@Component
public class CalendarDateHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    CalendarDateRepository calendarDateRepository;

    @Autowired
    SlaveCalendarDateRepository slaveCalendarDateRepository;

    @Autowired
    CalendarRepository calendarRepository;

    @Autowired
    CalendarCategoryRepository calendarCategoryRepository;

    @Value("${server.zone}")
    private String zone;


    @AuthHasPermission(value = "config_api_v1_calendar-dates_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        //   Search Keyword
        String searchKeyWord = serverRequest.queryParam("skw").orElse("");

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        Flux<SlaveCalendarDateEntity> slaveCalendarEntityFlux = slaveCalendarDateRepository
                .showAllRecordsWithSearchFilter(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

        return slaveCalendarEntityFlux
                .collectList()
                .flatMap(calendarEntityDB -> slaveCalendarDateRepository
                        .countAllByDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord, searchKeyWord)
                        .flatMap(count ->
                        {
                            if (calendarEntityDB.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", calendarEntityDB, count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));

    }

    @AuthHasPermission(value = "config_api_v1_calendar-dates_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID calendarUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCalendarDateRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
                .flatMap(calendarEntityDB -> responseSuccessMsg("Record Fetched Successfully.", calendarEntityDB))
                .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

//    This route returns list of calendar dates between given start and end date
    @AuthHasPermission(value = "config_api_v1_calendar-dates_list_show")
    public Mono<ServerResponse> showList(ServerRequest serverRequest) {
        LocalDate startDate = LocalDate.parse(serverRequest.queryParam("startDate").orElse(""), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate endDate = LocalDate.parse(serverRequest.queryParam("endDate").orElse(""), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return slaveCalendarDateRepository.getAllDatesBetween(startDate, endDate)
                .flatMap(uuids -> {
                    List<String> listOfUuids = Arrays.asList(uuids.split("\\s*,\\s*"));

                    return responseSuccessMsg("Record Fetched Successfully.", listOfUuids);
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_calendar-dates_store")
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
//                    check date exists
                    return calendarRepository.findByUuidAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID")))
                            .flatMap(calendarEntity -> {
                                String startDate = calendarEntity.getDate().getYear() + "-01-01";
                                LocalDate parsedStartDate = LocalDate.parse(startDate);
                                LocalDate endDate = parsedStartDate.plusYears(1);
                                List<LocalDate> listOfDates = parsedStartDate.datesUntil(endDate).collect(Collectors.toList());
                                List<CalendarDateEntity> calendarDateEntityList = new ArrayList<>();
                                List<UUID> calendarEntityUUID = new ArrayList<>();

                                for (LocalDate list : listOfDates) {
                                    CalendarDateEntity calendarDateEntity = CalendarDateEntity.builder()
                                            .uuid(UUID.randomUUID())
                                            .calendarUUID(calendarEntity.getUuid())
                                            .date(list)
                                            .monthName(String.valueOf(list.getMonth()))
                                            .dayName(String.valueOf(list.getDayOfWeek()))
                                            .year(list.getYear())
                                            .month(list.getMonthValue())
                                            .day(list.getDayOfMonth())
                                            .quarter(list.get(IsoFields.QUARTER_OF_YEAR))
                                            .week(list.get(ChronoField.ALIGNED_WEEK_OF_MONTH))
                                            .dayOfWeek(list.getDayOfWeek().getValue())
                                            .dayOfYear(list.getDayOfYear())
                                            .weekOfYear(list.get(ChronoField.ALIGNED_WEEK_OF_YEAR))
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
                                    calendarDateEntityList.add(calendarDateEntity);
                                    calendarEntityUUID.add(calendarDateEntity.getUuid());
                                }

                                return calendarDateRepository.findFirstByCalendarUUIDAndDeletedAtIsNull(UUID.fromString(value.getFirst("calendarUUID")))
                                        .flatMap(checkDate -> responseInfoMsg(checkDate.getYear() + " Calendar already exist"))
                                        .switchIfEmpty(Mono.defer(() -> calendarDateRepository.saveAll(calendarDateEntityList)
                                                .collectList()
                                                .flatMap(calendarLists ->
                                                {
                                                    if (calendarLists.isEmpty()) {
                                                        return responseInfoMsg("Unable to store record. There is something wrong please try again")
                                                                .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer"));
                                                    } else {
                                                        return responseSuccessMsg("Record stored successfully", calendarEntityUUID);
                                                    }
                                                })));

                            }).switchIfEmpty(responseInfoMsg("Date record does not exist."))
                            .onErrorResume(err -> responseErrorMsg("Date record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }


//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        UUID calendarUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User!");
//            }
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> calendarRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
//                                .flatMap(previousCalendarEntity -> {
//
//                                    CalendarDateEntity updatedCalendarEntity = CalendarDateEntity.builder()
//                                            .uuid(previousCalendarEntity.getUuid())
//                                            .name(value.getFirst("name"))
//                                            .description(value.getFirst("description"))
//                                            .createdAt(previousCalendarEntity.getCreatedAt())
//                                            .createdBy(previousCalendarEntity.getCreatedBy())
//                                            .updatedBy(UUID.fromString(userUUID))
//                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                            .build();
//
//                                    //Deleting Previous Record and Creating a New One Based on UUID
//                                    previousCalendarEntity.setDeletedBy(UUID.fromString(userUUID));
//                                    previousCalendarEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
////                                  check calendar name is unique
//                                    return calendarRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCalendarEntity.getName(), calendarUUID)
//                                            .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exists"))
//                                            .switchIfEmpty(Mono.defer(() -> calendarRepository.save(previousCalendarEntity)
//                                                    .then(calendarRepository.save(updatedCalendarEntity))
//                                                    .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully.", saveEntity))
//                                                    .switchIfEmpty(Mono.defer(() -> responseErrorMsg("Unable to store record.")))
//                                                    .onErrorResume(err -> responseErrorMsg("Unable to store record."))
//                                            ));
//                                }).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
//                                .onErrorResume(err -> responseErrorMsg("Record does not exist."))
//                ).switchIfEmpty(Mono.defer(() -> responseInfoMsg("Unable to read the request.")))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request."));
//    }


//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID calendarUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User!");
//            }
//        }
//
//        return calendarDateRepository.findByUuidAndDeletedAtIsNull(calendarUUID)
//                .flatMap(calendarDateEntity -> {
//                    calendarDateEntity.setDeletedBy(UUID.fromString(userUUID));
//                    calendarDateEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    return calendarDateRepository.save(calendarDateEntity)
//                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully.", entity))
//                            .switchIfEmpty(responseErrorMsg("Unable to delete record. There is something wrong please try again."))
//                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }


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

//    SELECT classrooms.name as classroom_name, start_time, end_time, campuses.name as campus_name, subjects.name as subject_name from timetables
//        join classrooms on classrooms.uuid = timetables.classroom_uuid
//        join campus_course on campus_course.uuid= timetables.campus_course_uuid
//        join campuses on campuses.uuid = campus_course.campus_uuid
//        join sections on sections.uuid = timetables.section_uuid
//        join enrollments on enrollments.uuid=sections.enrollment_uuid
//        join subject_offered on subject_offered.uuid = enrollments.subject_offered_uuid
//        join course_subject on course_subject.uuid = subject_offered.course_subject_uuid
//        join subjects on subjects.uuid = course_subject.subject_uuid
