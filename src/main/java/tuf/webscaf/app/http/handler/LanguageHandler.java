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
import tuf.webscaf.app.dbContext.master.entity.LanguageEntity;
import tuf.webscaf.app.dbContext.master.repositry.BranchProfileRepository;
import tuf.webscaf.app.dbContext.master.repositry.CompanyProfileRepository;
import tuf.webscaf.app.dbContext.master.repositry.LanguageRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLanguageRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "languageHandler")
public class LanguageHandler {

    @Autowired
    LanguageRepository languageRepository;

    @Autowired
    SlaveLanguageRepository slaveLanguageRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    CustomResponse appresponse;
    @Autowired
    ApiCallService apiCallService;
    @Value("${server.zone}")
    private String zone;
    @Value("${server.erp_academic_module.uri}")
    private String academicUri;
    @Value("${server.erp_hr_module.uri}")
    private String hrUri;

    @AuthHasPermission(value = "config_api_v1_languages_index")
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
            Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveLanguageEntityFlux
                    .collectList()
                    .flatMap(languageEntity -> slaveLanguageRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (languageEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", languageEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveLanguageEntityFlux
                    .collectList()
                    .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (languageEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", languageEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "config_api_v1_languages_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID languageUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveLanguageRepository.findByUuidAndDeletedAtIsNull(languageUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //This Function Is used By Academic Module to Check if Language UUID List exists
    @AuthHasPermission(value = "config_api_v1_languages_list_show")
    public Mono<ServerResponse> showLanguageListInAcademic(ServerRequest serverRequest) {

        List<String> uuids = serverRequest.queryParams().get("uuid");

        //This is Language List to paas in the query
        List<UUID> languageList = new ArrayList<>();
        if (uuids != null) {
            for (String language : uuids) {
                languageList.add(UUID.fromString(language));
            }
        }

        return slaveLanguageRepository.getUUIDsOfExitingRecords(languageList)
                .collectList()
                .flatMap(languageUUIDs -> responseSuccessMsg("Records Fetched Successfully", languageUUIDs))
                .switchIfEmpty(responseInfoMsg("Unable to read request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read request. Please Contact Developer."));
    }

    /**
     * These functions return the mapped and un mapped languages for a student
     **/

    //    Show Mapped Languages for Student UUID
    @AuthHasPermission(value = "config_api_v1_languages_student_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudent(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentUUID = UUID.fromString(serverRequest.pathVariable("studentUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-languages/list/show/", studentUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student UUID
    @AuthHasPermission(value = "config_api_v1_languages_student_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudent(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentUUID = UUID.fromString(serverRequest.pathVariable("studentUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-languages/list/show/", studentUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student mother
     **/

    //    Show Mapped Languages for Student Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-mother_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentMotherUUID = UUID.fromString(serverRequest.pathVariable("studentMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-languages/list/show/", studentMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-mother_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentMotherUUID = UUID.fromString(serverRequest.pathVariable("studentMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-languages/list/show/", studentMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student father
     **/

    //    Show Mapped Languages for Student Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-father_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentFatherUUID = UUID.fromString(serverRequest.pathVariable("studentFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-languages/list/show/", studentFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-father_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentFatherUUID = UUID.fromString(serverRequest.pathVariable("studentFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-languages/list/show/", studentFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student sibling
     **/

    //    Show Mapped Languages for Student Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-sibling_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentSiblingUUID = UUID.fromString(serverRequest.pathVariable("studentSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-languages/list/show/", studentSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-sibling_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentSiblingUUID = UUID.fromString(serverRequest.pathVariable("studentSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-languages/list/show/", studentSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student child
     **/

    //    Show Mapped Languages for Student Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-child_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentChildUUID = UUID.fromString(serverRequest.pathVariable("studentChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-child-languages/list/show/", studentChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-child_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentChildUUID = UUID.fromString(serverRequest.pathVariable("studentChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-child-languages/list/show/", studentChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student spouse
     **/

    //    Show Mapped Languages for Student Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-spouse_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentSpouseUUID = UUID.fromString(serverRequest.pathVariable("studentSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-spouse-languages/list/show/", studentSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-spouse_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentSpouseUUID = UUID.fromString(serverRequest.pathVariable("studentSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-spouse-languages/list/show/", studentSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a student guardian
     **/

    //    Show Mapped Languages for Student Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-guardian_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstStudentGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentGuardianUUID = UUID.fromString(serverRequest.pathVariable("studentGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-languages/list/show/", studentGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Student Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_student-guardian_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstStudentGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID studentGuardianUUID = UUID.fromString(serverRequest.pathVariable("studentGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-languages/list/show/", studentGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher
     **/

    //    Show Mapped Languages for Teacher UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherUUID = UUID.fromString(serverRequest.pathVariable("teacherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-languages/list/show/", teacherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherUUID = UUID.fromString(serverRequest.pathVariable("teacherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-languages/list/show/", teacherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher mother
     **/

    //    Show Mapped Languages for Teacher Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-mother_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherMotherUUID = UUID.fromString(serverRequest.pathVariable("teacherMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-languages/list/show/", teacherMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-mother_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherMotherUUID = UUID.fromString(serverRequest.pathVariable("teacherMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-languages/list/show/", teacherMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher father
     **/

    //    Show Mapped Languages for Teacher Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-father_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherFatherUUID = UUID.fromString(serverRequest.pathVariable("teacherFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-languages/list/show/", teacherFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-father_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherFatherUUID = UUID.fromString(serverRequest.pathVariable("teacherFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-languages/list/show/", teacherFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher sibling
     **/

    //    Show Mapped Languages for Teacher Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-sibling_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherSiblingUUID = UUID.fromString(serverRequest.pathVariable("teacherSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-languages/list/show/", teacherSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-sibling_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherSiblingUUID = UUID.fromString(serverRequest.pathVariable("teacherSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-languages/list/show/", teacherSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher child
     **/

    //    Show Mapped Languages for Teacher Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-child_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherChildUUID = UUID.fromString(serverRequest.pathVariable("teacherChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-languages/list/show/", teacherChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-child_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherChildUUID = UUID.fromString(serverRequest.pathVariable("teacherChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-languages/list/show/", teacherChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher spouse
     **/

    //    Show Mapped Languages for Teacher Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-spouse_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherSpouseUUID = UUID.fromString(serverRequest.pathVariable("teacherSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-spouse-languages/list/show/", teacherSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-spouse_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherSpouseUUID = UUID.fromString(serverRequest.pathVariable("teacherSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-spouse-languages/list/show/", teacherSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for a teacher guardian
     **/

    //    Show Mapped Languages for Teacher Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-guardian_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstTeacherGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherGuardianUUID = UUID.fromString(serverRequest.pathVariable("teacherGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-guardian-languages/list/show/", teacherGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Teacher Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_teacher-guardian_existing_show")
    public Mono<ServerResponse> listOfExistingLanguagesAgainstTeacherGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID teacherGuardianUUID = UUID.fromString(serverRequest.pathVariable("teacherGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-guardian-languages/list/show/", teacherGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }


    /**
     * These functions return the mapped and un mapped languages for an employee
     **/

    //    Show Mapped Languages for Employee UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployee(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeUUID = UUID.fromString(serverRequest.pathVariable("employeeUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-languages/list/show/", employeeUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployee(ServerRequest serverRequest) {

        UUID employeeUUID = UUID.fromString(serverRequest.pathVariable("employeeUUID"));

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-languages/list/show/", employeeUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee mother
     **/

    //    Show Mapped Languages for Employee Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-mother_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeMotherUUID = UUID.fromString(serverRequest.pathVariable("employeeMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-mother-languages/list/show/", employeeMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Mother UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-mother_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeMother(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeMotherUUID = UUID.fromString(serverRequest.pathVariable("employeeMotherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-mother-languages/list/show/", employeeMotherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee father
     **/

    //    Show Mapped Languages for Employee Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-father_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeFatherUUID = UUID.fromString(serverRequest.pathVariable("employeeFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-father-languages/list/show/", employeeFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Father UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-father_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeFather(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeFatherUUID = UUID.fromString(serverRequest.pathVariable("employeeFatherUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-father-languages/list/show/", employeeFatherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee sibling
     **/

    //    Show Mapped Languages for Employee Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-sibling_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeSiblingUUID = UUID.fromString(serverRequest.pathVariable("employeeSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-sibling-languages/list/show/", employeeSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Sibling UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-sibling_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeSibling(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeSiblingUUID = UUID.fromString(serverRequest.pathVariable("employeeSiblingUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-sibling-languages/list/show/", employeeSiblingUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee child
     **/

    //    Show Mapped Languages for Employee Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-child_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeChildUUID = UUID.fromString(serverRequest.pathVariable("employeeChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-child-languages/list/show/", employeeChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Child UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-child_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeChild(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeChildUUID = UUID.fromString(serverRequest.pathVariable("employeeChildUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-child-languages/list/show/", employeeChildUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee spouse
     **/

    //    Show Mapped Languages for Employee Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-spouse_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeSpouseUUID = UUID.fromString(serverRequest.pathVariable("employeeSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-spouse-languages/list/show/", employeeSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Spouse UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-spouse_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeSpouse(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeSpouseUUID = UUID.fromString(serverRequest.pathVariable("employeeSpouseUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-spouse-languages/list/show/", employeeSpouseUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    /**
     * These functions return the mapped and un mapped languages for an employee guardian
     **/

    //    Show Mapped Languages for Employee Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-guardian_mapped_show")
    public Mono<ServerResponse> listOfMappedLanguagesAgainstEmployeeGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeGuardianUUID = UUID.fromString(serverRequest.pathVariable("employeeGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-guardian-languages/list/show/", employeeGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    //    Show Unmapped Languages for Employee Guardian UUID
    @AuthHasPermission(value = "config_api_v1_languages_employee-guardian_un-mapped_show")
    public Mono<ServerResponse> listOfUnMappedLanguagesAgainstEmployeeGuardian(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID employeeGuardianUUID = UUID.fromString(serverRequest.pathVariable("employeeGuardianUUID"));

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

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

        //Optional Query Parameter
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(hrUri + "api/v1/employee-guardian-languages/list/show/", employeeGuardianUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );

                    } else {
                        Flux<SlaveLanguageEntity> slaveLanguageEntityFlux = slaveLanguageRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);
                        return slaveLanguageEntityFlux
                                .collectList()
                                .flatMap(languageEntity -> slaveLanguageRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count -> {
                                            if (languageEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {

                                                return responseIndexSuccessMsg("Records Fetched Successfully", languageEntity, count, 0L);
                                            }
                                        })
                                );
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
    }

    @AuthHasPermission(value = "config_api_v1_languages_store")
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
                    LanguageEntity languageEntity = LanguageEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .languageCode(value.getFirst("languageCode").trim())
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

                    //Check if Name is Unique or not
                    return languageRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(languageEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            //Check if Language Code is Unique or not
                            .switchIfEmpty(Mono.defer(() -> languageRepository.findFirstByLanguageCodeIgnoreCaseAndDeletedAtIsNull(languageEntity.getLanguageCode())
                                    .flatMap(checkLanguageCode -> responseInfoMsg("Language code already exist"))))
                            .switchIfEmpty(Mono.defer(() -> languageRepository.save(languageEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }


    @AuthHasPermission(value = "config_api_v1_languages_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID languageUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> languageRepository.findByUuidAndDeletedAtIsNull(languageUUID)
                        .flatMap(previousEntity -> {

                            LanguageEntity updatedLanguageEntity = LanguageEntity.builder()
                                    .uuid(previousEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .languageCode(value.getFirst("languageCode").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .createdAt(previousEntity.getCreatedAt())
                                    .createdBy(previousEntity.getCreatedBy())
                                    .reqCreatedIP(previousEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
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

                            //check if Name Already Exists
                            return languageRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedLanguageEntity.getName(), updatedLanguageEntity.getUuid())
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    //check if Language Code Already Exists
                                    .switchIfEmpty(Mono.defer(() -> languageRepository.findFirstByLanguageCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedLanguageEntity.getLanguageCode(), updatedLanguageEntity.getUuid())
                                            .flatMap(checkLanguageCode -> responseInfoMsg("Language code already exist"))))
                                    .switchIfEmpty(Mono.defer(() ->
                                            //Update previous Language Entity
                                            languageRepository.save(previousEntity)
                                                    //Update the Updated Language Entity
                                                    .then(languageRepository.save(updatedLanguageEntity))
                                                    .flatMap(saveLanguageEntity -> responseSuccessMsg("Record Updated successfully!", saveLanguageEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_languages_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        final UUID languageUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return languageRepository.findByUuidAndDeletedAtIsNull(languageUUID)
                .flatMap(languageEntity -> companyProfileRepository.findFirstByLanguageUUIDAndDeletedAtIsNull(languageEntity.getUuid())
                                .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete! Reference of record exists!"))
//                        check language exist in branch profile
                                .switchIfEmpty(Mono.defer(() -> branchProfileRepository.findFirstByLanguageUUIDAndDeletedAtIsNull(languageEntity.getUuid())
                                        .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete record as the Reference of record exists!"))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    languageEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    languageEntity.setDeletedBy(UUID.fromString(userUUID));
                                    languageEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    languageEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    languageEntity.setReqDeletedIP(reqIp);
                                    languageEntity.setReqDeletedPort(reqPort);
                                    languageEntity.setReqDeletedBrowser(reqBrowser);
                                    languageEntity.setReqDeletedOS(reqOs);
                                    languageEntity.setReqDeletedDevice(reqDevice);
                                    languageEntity.setReqDeletedReferer(reqReferer);

                                    return languageRepository.save(languageEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_languages_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID languageUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return languageRepository.findByUuidAndDeletedAtIsNull(languageUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                LanguageEntity updatedLanguageEntity = LanguageEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
                                        .languageCode(previousEntity.getLanguageCode())
                                        .status(status == true ? true : false)
                                        .createdAt(previousEntity.getCreatedAt())
                                        .createdBy(previousEntity.getCreatedBy())
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

                                return languageRepository.save(previousEntity)
                                        .then(languageRepository.save(updatedLanguageEntity))
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
