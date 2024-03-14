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
import tuf.webscaf.app.dbContext.master.entity.StateEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveStateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveStateRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.service.ApiCallService;
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
@Tag(name = "stateHandler")
public class StateHandler {

    @Autowired
    SlaveStateRepository slaveStateRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.zone}")
    private String zone;


    @AuthHasPermission(value = "config_api_v1_states_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Country
        String countryUUID = serverRequest.queryParam("countryUUID").map(String::toString).orElse("").trim();

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

        if (!countryUUID.isEmpty() && !status.isEmpty()) {
            String directionProperty1 = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable1 = PageRequest.of(page, size, Sort.by(direction, directionProperty1));

            Flux<SlaveStateEntity> slaveStateWithCountryEntityFlux = slaveStateRepository
                    .indexStateAgainstCountryWithStatus(UUID.fromString(countryUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, searchKeyWord, pageable1.getPageSize(), pageable1.getOffset(), directionProperty1, d);

            return slaveStateWithCountryEntityFlux
                    .collectList()
                    .flatMap(stateEntity ->
                            slaveStateRepository.countStateAgainstCountryEntityWithStatus(UUID.fromString(countryUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (stateEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All States fetched successfully!", stateEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

//         Return All states of given country
        else if (!countryUUID.isEmpty()) {

            String directionProperty1 = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable1 = PageRequest.of(page, size, Sort.by(direction, directionProperty1));

            Flux<SlaveStateEntity> slaveStateWithCountryEntityFlux = slaveStateRepository
                    .indexStateAgainstCountry(UUID.fromString(countryUUID), searchKeyWord, searchKeyWord, searchKeyWord, pageable1.getPageSize(), pageable1.getOffset(), directionProperty1, d);

            return slaveStateWithCountryEntityFlux
                    .collectList()
                    .flatMap(stateEntity ->
                            slaveStateRepository.countStateAgainstCountryEntity(UUID.fromString(countryUUID), searchKeyWord, searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (stateEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All States fetched successfully!", stateEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
//        Return states with status filter
        else if (!status.isEmpty()) {

            Flux<SlaveStateEntity> slaveStateEntityFlux = slaveStateRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveStateEntityFlux
                    .collectList()
                    .flatMap(stateEntity ->
                            slaveStateRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                            (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (stateEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", stateEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
//        Return All states
        else {

            Flux<SlaveStateEntity> slaveStateEntityFlux = slaveStateRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord, searchKeyWord);
            return slaveStateEntityFlux
                    .collectList()
                    .flatMap(stateEntity ->
                            slaveStateRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (stateEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", stateEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }


    @AuthHasPermission(value = "config_api_v1_states_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID stateUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        return slaveStateRepository.findByUuidAndDeletedAtIsNull(stateUUID)
                .flatMap(stateEntity -> responseSuccessMsg("Record Fetched Successfully", stateEntity))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_states_store")
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

                    Double longitude = null;
                    Double latitude = null;

                    if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                        longitude = Double.valueOf(String.valueOf(value.getFirst("longitude")));
                    }

                    if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                        latitude = Double.valueOf(String.valueOf(value.getFirst("latitude")));
                    }
                    StateEntity stateEntity = StateEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .type(value.getFirst("type").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .stateCode(value.getFirst("stateCode").trim())
                            .latitude(latitude)
                            .longitude(longitude)
                            .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
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

                    //check if Country UUID exists
                    return countryRepository.findByUuidAndDeletedAtIsNull(stateEntity.getCountryUUID())
                            //check if State Name is Unique Against this Country
                            .flatMap(country -> stateRepository.findFirstByNameIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(stateEntity.getName(), stateEntity.getCountryUUID())
                                    .flatMap(checkNameMsg -> responseInfoMsg("State Against This Country Already exists."))
                                    .switchIfEmpty(Mono.defer(() ->
                                            //if everything condition is okay Save State
                                            stateRepository.save(stateEntity)
                                                    .flatMap(state -> responseSuccessMsg("Record stored successfully!", stateEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                    ))
                            )
                            .switchIfEmpty(responseInfoMsg("Country does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Country does not exist.Please Contact Developer."));
                })
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_states_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID stateUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> stateRepository.findByUuidAndDeletedAtIsNull(stateUUID)
                        .flatMap(previousStateEntity -> {

                            Double longitude = null;
                            Double latitude = null;

                            if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                                longitude = Double.valueOf(String.valueOf(value.getFirst("longitude")));
                            }

                            if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                                latitude = Double.valueOf(String.valueOf(value.getFirst("latitude")));
                            }

                            //Build New State Entity
                            StateEntity updatedStateEntity = StateEntity.builder()
                                    .uuid(previousStateEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .type(value.getFirst("type").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .stateCode(value.getFirst("stateCode").trim())
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                                    .createdBy(previousStateEntity.getCreatedBy())
                                    .createdAt(previousStateEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousStateEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousStateEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousStateEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousStateEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousStateEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousStateEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            //Delete the Previous Entity
                            previousStateEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousStateEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousStateEntity.setReqDeletedIP(reqIp);
                            previousStateEntity.setReqDeletedPort(reqPort);
                            previousStateEntity.setReqDeletedBrowser(reqBrowser);
                            previousStateEntity.setReqDeletedOS(reqOs);
                            previousStateEntity.setReqDeletedDevice(reqDevice);
                            previousStateEntity.setReqDeletedReferer(reqReferer);

                            //check if Country Exists in Countries
                            return countryRepository.findByUuidAndDeletedAtIsNull(updatedStateEntity.getCountryUUID())
                                    //check if State Name Already Exists for this Country
                                    .flatMap(country -> stateRepository.findFirstByNameIgnoreCaseAndCountryUUIDAndDeletedAtIsNullAndUuidIsNot(updatedStateEntity.getName(), updatedStateEntity.getCountryUUID(), updatedStateEntity.getUuid())
                                            .flatMap(checkNameMsg -> responseInfoMsg("State Already exists against this Country"))
                                            .switchIfEmpty(Mono.defer(() ->
                                                    //Update previous State Entity
                                                    stateRepository.save(previousStateEntity)
                                                            //Update the Updated State Entity
                                                            .then(stateRepository.save(updatedStateEntity))
                                                            .flatMap(saveLanguageEntity -> responseSuccessMsg("Record Updated successfully!", saveLanguageEntity))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("Country does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Country does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_states_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        final UUID stateUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return stateRepository.findByUuidAndDeletedAtIsNull(stateUUID)
                .flatMap(stateEntity -> companyProfileRepository.findFirstByStateUUIDAndDeletedAtIsNull(stateEntity.getUuid())
//                                If State Reference exists in Company Profile
                                .flatMap(companyProfile -> responseInfoMsg("Unable to delete. Reference of record exists."))
//                                .switchIfEmpty(Mono.defer(() -> companyProfileRepository.findFirstByStateUUIDAndDeletedAtIsNull(stateEntity.getUuid())
////                                        If State Reference exists in Company
//                                        .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete. Reference of record exists."))))
                                .switchIfEmpty(Mono.defer(() -> cityRepository.findFirstByStateUUIDAndDeletedAtIsNull(stateEntity.getUuid())
//                                        If State Reference exists in City Table
                                        .flatMap(cityEntity -> responseInfoMsg("Unable to delete. Reference of record exists."))))
                                .switchIfEmpty(Mono.defer(() -> locationRepository.findFirstByStateUUIDAndDeletedAtIsNull(stateEntity.getUuid())
//                                        If State Reference exists in location
                                        .flatMap(officeEntity -> responseInfoMsg("Unable to delete. Reference of record exists."))))
                                // If State uuid exists in student profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-academic-records/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student guardian profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student guardian academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher father academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher child profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-profiles/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher child academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in teacher academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-academic-records/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student father academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If State uuid exists in student sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-academic-histories/state/show/", stateEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    stateEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    stateEntity.setDeletedBy(UUID.fromString(userUUID));
                                    stateEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    stateEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    stateEntity.setReqDeletedIP(reqIp);
                                    stateEntity.setReqDeletedPort(reqPort);
                                    stateEntity.setReqDeletedBrowser(reqBrowser);
                                    stateEntity.setReqDeletedOS(reqOs);
                                    stateEntity.setReqDeletedDevice(reqDevice);
                                    stateEntity.setReqDeletedReferer(reqReferer);

                                    return stateRepository.save(stateEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_states_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID stateUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return stateRepository.findByUuidAndDeletedAtIsNull(stateUUID)
                            .flatMap(previousStateEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousStateEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                //Build New State Entity
                                StateEntity updatedStateEntity = StateEntity.builder()
                                        .uuid(previousStateEntity.getUuid())
                                        .name(previousStateEntity.getName())
                                        .description(previousStateEntity.getDescription())
                                        .type(previousStateEntity.getType())
                                        .status(status == true ? true : false)
                                        .stateCode(previousStateEntity.getStateCode())
                                        .latitude(previousStateEntity.getLatitude())
                                        .longitude(previousStateEntity.getLongitude())
                                        .countryUUID(previousStateEntity.getCountryUUID())
                                        .createdBy(previousStateEntity.getCreatedBy())
                                        .createdAt(previousStateEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousStateEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousStateEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousStateEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousStateEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousStateEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousStateEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                //Delete the Previous Entity
                                previousStateEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousStateEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousStateEntity.setReqDeletedIP(reqIp);
                                previousStateEntity.setReqDeletedPort(reqPort);
                                previousStateEntity.setReqDeletedBrowser(reqBrowser);
                                previousStateEntity.setReqDeletedOS(reqOs);
                                previousStateEntity.setReqDeletedDevice(reqDevice);
                                previousStateEntity.setReqDeletedReferer(reqReferer);


                                return stateRepository.save(previousStateEntity)
                                        .then(stateRepository.save(updatedStateEntity))
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


