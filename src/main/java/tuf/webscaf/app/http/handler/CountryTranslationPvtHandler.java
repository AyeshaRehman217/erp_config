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
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryTranslationPvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveTranslationRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "countryTranslationPvtHandler")
public class CountryTranslationPvtHandler {

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    CountryTranslationPvtRepository countryTranslationPvtRepository;

    @Autowired
    SlaveCountryTranslationPvtRepository slaveCountryTranslationPvtRepository;

    @Autowired
    SlaveTranslationRepository slaveTranslationRepository;

    @Autowired
    TranslationRepository translationRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;


    //This function is used to get unmapped translations Against country
    @AuthHasPermission(value = "config_api_v1_country-translation_un-mapped_show")
    public Mono<ServerResponse> showUnMappedTranslationsAgainstCountry(ServerRequest serverRequest) {

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
            Flux<SlaveTranslationEntity> slaveTranslationEntityFlux = slaveTranslationRepository
                    .showUnMappedTranslationListAgainstCountryWithStatus(countryUUID, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveTranslationEntityFlux
                    .collectList()
                    .flatMap(translationEntity -> slaveTranslationRepository
                            .countUnMappedTranslationRecordsWithStatusFilter(countryUUID, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (translationEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", translationEntity, count, 0L);
                                        }
                                    }
                            )).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else {
            Flux<SlaveTranslationEntity> slaveTranslationEntityFlux = slaveTranslationRepository
                    .showUnMappedTranslationListAgainstCountry(countryUUID, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveTranslationEntityFlux
                    .collectList()
                    .flatMap(translationEntity -> slaveTranslationRepository.countUnMappedTranslationRecordsAgainstCountry(countryUUID, searchKeyWord)
                            .flatMap(count -> {
                                        if (translationEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", translationEntity, count, 0L);
                                        }
                                    }
                            )).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

    }

    @AuthHasPermission(value = "config_api_v1_country-translation_mapped_show")
    public Mono<ServerResponse> showMappedTranslationsAgainstCountry(ServerRequest serverRequest) {

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

            Flux<SlaveTranslationEntity> slaveTranslationEntityFlux = slaveTranslationRepository
                    .showMappedTranslationListAgainstCountryWithStatus
                            (countryUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveTranslationEntityFlux
                    .collectList()
                    .flatMap(translationEntity -> slaveTranslationRepository.countMappedTranslationEntityWithStatusFilter
                                    (countryUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (translationEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);
                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", translationEntity, count, 0L);
                                }

                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveTranslationEntity> slaveTranslationEntityFlux = slaveTranslationRepository
                    .showMappedTranslationListAgainstCountry(countryUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveTranslationEntityFlux
                    .collectList()
                    .flatMap(translationEntity -> slaveTranslationRepository.countMappedTranslationEntity(countryUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                if (translationEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", translationEntity, count, 0L);
                                }

                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }


    @AuthHasPermission(value = "config_api_v1_country-translation_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        final UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID").trim());

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
                        .flatMap(translationEntity -> {
                            //getting List of translation uuid From Front
                            List<String> listTranslations = new LinkedList<>(value.get("translationUUID"));

                            //removing any empty String from the Front List
                            listTranslations.removeIf(s -> s.equals(""));

                            //Creating an Empty List to add all the UUID from Front
                            List<UUID> l_list = new ArrayList<>();
                            //Looping Through all the Translation list and add in Empty List
                            for (String groupId : listTranslations) {
                                l_list.add(UUID.fromString(groupId));
                            }

                            //If the List is not empty do all the stuff
                            if (!l_list.isEmpty()) {
                                //Check if Translation Records exist
                                return translationRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                        .collectList()
                                        .flatMap(existingTranslation -> {
                                            // Translation UUID List
                                            List<UUID> translationList = new ArrayList<>();
                                            //If the Translation UUID exists fetch and save it in another list
                                            for (TranslationEntity translation : existingTranslation) {
                                                translationList.add(translation.getUuid());
                                            }

                                            //check if Final Translation list is not empty
                                            if (!translationList.isEmpty()) {

                                                // country uuid list to show in response
                                                List<UUID> returningTranslationRecordList = new ArrayList<>(translationList);

                                                List<CountryTranslationPvtEntity> listPvt = new ArrayList<>();
                                                //All the existing records that exist in Pvt Table
                                                return countryTranslationPvtRepository.findAllByCountryUUIDAndTranslationUUIDInAndDeletedAtIsNull(countryUUID, translationList)
                                                        .collectList()
                                                        .flatMap(removelist -> {
                                                            for (CountryTranslationPvtEntity pvtEntity : removelist) {
                                                                //removing records from the List from front that contain already mapped uuid's
                                                                translationList.remove(pvtEntity.getTranslationUUID());
                                                            }

                                                            for (UUID translationUUIDs : translationList) {
                                                                CountryTranslationPvtEntity countryWithTranslationPvtEntity = CountryTranslationPvtEntity
                                                                        .builder()
                                                                        .translationUUID(translationUUIDs)
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

                                                                listPvt.add(countryWithTranslationPvtEntity);
                                                            }

                                                            return countryTranslationPvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(groupList -> countryTranslationPvtRepository.findByCountryUUIDAndDeletedAtIsNull(countryUUID)
                                                                            .collectList()
                                                                            .flatMap(mappedRecords -> {

                                                                                List<UUID> resultList = new ArrayList<>();

                                                                                for (CountryTranslationPvtEntity entity : mappedRecords) {
                                                                                    resultList.add(entity.getTranslationUUID());
                                                                                }

                                                                                return countryRepository.findAllByUuidInAndDeletedAtIsNull(resultList)
                                                                                        .collectList()
                                                                                        .flatMap(translationRecords -> {
                                                                                            if (!translationList.isEmpty()) {
                                                                                                return responseSuccessMsg("Record Stored Successfully!", returningTranslationRecordList);
                                                                                            } else {
                                                                                                return responseSuccessMsg("Record Already exists", returningTranslationRecordList);
                                                                                            }
                                                                                        });
                                                                            }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                        }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                            } else {
                                                return responseInfoMsg("Translation Record does not exist");
                                            }
                                        }).switchIfEmpty(responseInfoMsg("The Entered Translation Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("The Entered Translation Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Translation First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Translation does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Translation does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //     Delete function for country and translation mapping
    @AuthHasPermission(value = "config_api_v1_country-translation_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID countryUUID = UUID.fromString(serverRequest.pathVariable("countryUUID"));

        UUID translationUUID = UUID.fromString(serverRequest.queryParam("translationUUID").map(String::toString).orElse(""));

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

        return translationRepository.findByUuidAndDeletedAtIsNull(translationUUID)
                .flatMap(translationEntity -> countryTranslationPvtRepository
                        .findFirstByCountryUUIDAndTranslationUUIDAndDeletedAtIsNull(countryUUID, translationUUID)
                        .flatMap(countryTranslationPvtEntity -> {

                            countryTranslationPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            countryTranslationPvtEntity.setDeletedBy(UUID.fromString(userUUID));
                            countryTranslationPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            countryTranslationPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            countryTranslationPvtEntity.setReqDeletedIP(reqIp);
                            countryTranslationPvtEntity.setReqDeletedPort(reqPort);
                            countryTranslationPvtEntity.setReqDeletedBrowser(reqBrowser);
                            countryTranslationPvtEntity.setReqDeletedOS(reqOs);
                            countryTranslationPvtEntity.setReqDeletedDevice(reqDevice);
                            countryTranslationPvtEntity.setReqDeletedReferer(reqReferer);

                            return countryTranslationPvtRepository.save(countryTranslationPvtEntity)
                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", translationEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Translation record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Translation record does not exist.Please Contact Developer."));

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
