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
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryTimezonePvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveTimezoneRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "countryTimezonePvtHandler")
public class

CountryTimezonePvtHandler {

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    CountryTimezonePvtRepository countryTimezonePvtRepository;

    @Autowired
    SlaveCountryTimezonePvtRepository slaveCountryTimezonePvtRepository;

    @Autowired
    TimezoneRepository timezoneRepository;

    @Autowired
    SlaveTimezoneRepository slaveTimezoneRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;


    //This function is used to get unmapped timezones Against country
    @AuthHasPermission(value = "config_api_v1_country-time-zone_un-mapped_show")
    public Mono<ServerResponse> showUnMappedTimezonesAgainstCountry(ServerRequest serverRequest) {

        UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID").trim());

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(d, directionProperty));


        if (!status.isEmpty()) {
            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .showUnMappedTimezoneListAgainstCountryWithStatus
                            (countryUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository.countUnMappedTimezoneRecordsWithStatusFilter(countryUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (timezoneEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", timezoneEntity, count, 0L);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .showUnMappedTimezoneListAgainstCountry(countryUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository.countUnMappedTimezoneRecords(countryUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (timezoneEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", timezoneEntity, count, 0L);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

    }

    //This function is used to get mapped timezones Against country
    @AuthHasPermission(value = "config_api_v1_country-time-zone_mapped_show")
    public Mono<ServerResponse> showMappedTimezonesAgainstCountry(ServerRequest serverRequest) {
        UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID").trim());

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        if (!status.isEmpty()) {
            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .showMappedTimezoneListAgainstCountryWithStatus
                            (countryUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository.countMappedTimezoneAgainstCountryWithStatus(countryUUID, Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (timezoneEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", timezoneEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveTimezoneEntity> slaveTimezoneEntityFlux = slaveTimezoneRepository
                    .showMappedTimezoneListAgainstCountry(countryUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveTimezoneEntityFlux
                    .collectList()
                    .flatMap(timezoneEntity -> slaveTimezoneRepository.countMappedTimezoneAgainstCountry(countryUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (timezoneEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", timezoneEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }

    @AuthHasPermission(value = "config_api_v1_country-time-zone_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID").trim());

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
                .flatMap(value -> countryRepository.findByUuidAndDeletedAtIsNull(countryUUID)
                        .flatMap(timezoneEntity -> {
                            //getting List of timezone uuid From Front
                            List<String> listOfTimezones = new LinkedList<>(value.get("timezoneUUID"));

                            //removing any empty String from the Front List
                            listOfTimezones.removeIf(s -> s.equals(""));

                            //Creating an Empty List to add all the UUID from Front
                            List<UUID> l_list = new ArrayList<>();
                            //Looping Through all the TimeZone list and add in Empty List
                            for (String groupId : listOfTimezones) {
                                l_list.add(UUID.fromString(groupId));
                            }

                            //If the List is not empty do all the stuff
                            if (!l_list.isEmpty()) {
                                //Check if Timezone Records exist
                                return timezoneRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                        .collectList()
                                        .flatMap(existingTimezones -> {
                                            // Timezone UUID List
                                            List<UUID> timezoneList = new ArrayList<>();
                                            //If the TimeZone UUID exists fetch and save it in another list
                                            for (TimezoneEntity timezone : existingTimezones) {
                                                timezoneList.add(timezone.getUuid());
                                            }

                                            //check if Final TimeZone list is not empty
                                            if (!timezoneList.isEmpty()) {

                                                // country uuid list to show in response
                                                List<UUID> returningTimeZoneRecordList = new ArrayList<>(timezoneList);

                                                List<CountryTimezonePvtEntity> listPvt = new ArrayList<>();
                                                //All the existing records that exist in Pvt Table
                                                return countryTimezonePvtRepository.findAllByCountryUUIDAndTimezoneUUIDInAndDeletedAtIsNull(countryUUID, timezoneList)
                                                        .collectList()
                                                        .flatMap(removelist -> {
                                                            for (CountryTimezonePvtEntity pvtEntity : removelist) {
                                                                //removing records from the List from front that contain already mapped uuid's
                                                                timezoneList.remove(pvtEntity.getTimezoneUUID());
                                                            }

                                                            for (UUID timezoneUUIDs : timezoneList) {
                                                                CountryTimezonePvtEntity countryWithTimeZonePvtEntity = CountryTimezonePvtEntity
                                                                        .builder()
                                                                        .timezoneUUID(timezoneUUIDs)
                                                                        .countryUUID(countryUUID)
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

                                                                listPvt.add(countryWithTimeZonePvtEntity);
                                                            }

                                                            return countryTimezonePvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(groupList -> countryTimezonePvtRepository.findByCountryUUIDAndDeletedAtIsNull(countryUUID)
                                                                            .collectList()
                                                                            .flatMap(mappedRecords -> {

                                                                                List<UUID> resultList = new ArrayList<>();

                                                                                for (CountryTimezonePvtEntity entity : mappedRecords) {
                                                                                    resultList.add(entity.getTimezoneUUID());
                                                                                }

                                                                                return countryRepository.findAllByUuidInAndDeletedAtIsNull(resultList)
                                                                                        .collectList()
                                                                                        .flatMap(timezoneRecords -> {
                                                                                            if (!timezoneList.isEmpty()) {
                                                                                                return responseSuccessMsg("Record Stored Successfully!", returningTimeZoneRecordList);
                                                                                            } else {
                                                                                                return responseSuccessMsg("Record Already exists", returningTimeZoneRecordList);
                                                                                            }
                                                                                        });
                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                        }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                            } else {
                                                return responseInfoMsg("Timezone Record does not exist");
                                            }
                                        }).switchIfEmpty(responseInfoMsg("The Entered Timezone Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("The Entered Timezone Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Timezone First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Timezone does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Timezone does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_country-time-zone_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID"));
        UUID timezoneUUID = UUID.fromString(serverRequest.queryParam("timezoneUUID").map(String::toString).orElse(""));
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
            return responseWarningMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return timezoneRepository.findByUuidAndDeletedAtIsNull(timezoneUUID)
                .flatMap(timezoneEntity -> countryTimezonePvtRepository
                        .findFirstByCountryUUIDAndTimezoneUUIDAndDeletedAtIsNull(countryUUID, timezoneUUID)
                        .flatMap(countryTimezonePvtEntity -> {

                            countryTimezonePvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            countryTimezonePvtEntity.setDeletedBy(UUID.fromString(userUUID));
                            countryTimezonePvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            countryTimezonePvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            countryTimezonePvtEntity.setReqDeletedIP(reqIp);
                            countryTimezonePvtEntity.setReqDeletedPort(reqPort);
                            countryTimezonePvtEntity.setReqDeletedBrowser(reqBrowser);
                            countryTimezonePvtEntity.setReqDeletedOS(reqOs);
                            countryTimezonePvtEntity.setReqDeletedDevice(reqDevice);
                            countryTimezonePvtEntity.setReqDeletedReferer(reqReferer);

                            return countryTimezonePvtRepository.save(countryTimezonePvtEntity)
                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", timezoneEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Timezone record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Timezone record does not exist.Please Contact Developer."));

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
