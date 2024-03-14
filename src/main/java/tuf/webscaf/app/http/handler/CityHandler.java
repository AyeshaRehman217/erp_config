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
import tuf.webscaf.app.dbContext.master.entity.CityEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCityRepository;
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
@Tag(name = "cityHandler")
public class

CityHandler {

    @Autowired
    SlaveCityRepository slaveCityRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_cities_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of State
        String stateUUID = serverRequest.queryParam("stateUUID").map(String::toString).orElse("").trim();

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

        if (!stateUUID.isEmpty() && !countryUUID.isEmpty() && !status.isEmpty()) {

            return stateRepository.findFirstByCountryUUIDAndDeletedAtIsNull(UUID.fromString(countryUUID))
                    .flatMap(checkCountry -> {

                        Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                                .findAllByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), Boolean.valueOf(status));

                        return slaveCityWithCompanyEntityFlux
                                .collectList()
                                .flatMap(cityEntity ->
                                        slaveCityRepository.countByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNull(searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), Boolean.valueOf(status))
                                                .flatMap(count -> {
                                                    if (cityEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count, 0L);

                                                    } else {
                                                        return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                                    }
                                                })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

                    }).switchIfEmpty(responseInfoMsg("State Does not exist Against this Country"));
        } else if (!stateUUID.isEmpty() && !countryUUID.isEmpty()) {

            return stateRepository.findFirstByCountryUUIDAndDeletedAtIsNull(UUID.fromString(countryUUID))
                    .flatMap(checkCountry -> {

                        Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                                .findAllByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID));

                        return slaveCityWithCompanyEntityFlux
                                .collectList()
                                .flatMap(cityEntity ->
                                        slaveCityRepository.countByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID), searchKeyWord, UUID.fromString(countryUUID), UUID.fromString(stateUUID))
                                                .flatMap(count -> {
                                                    if (cityEntity.isEmpty()) {
                                                        return responseIndexInfoMsg("Record does not exist", count, 0L);

                                                    } else {
                                                        return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                                    }
                                                })
                                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

                    }).switchIfEmpty(responseInfoMsg("State Does not exist Against this Country"));
        } else if (!stateUUID.isEmpty() && !status.isEmpty()) {

            Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(stateUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(stateUUID), Boolean.valueOf(status));

            return slaveCityWithCompanyEntityFlux
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository.countByNameContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNull(searchKeyWord, UUID.fromString(stateUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(stateUUID), Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        } else if (!countryUUID.isEmpty() && !status.isEmpty()) {

            Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(countryUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(countryUUID), Boolean.valueOf(status));

            return slaveCityWithCompanyEntityFlux
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository.countByNameContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNull(searchKeyWord, UUID.fromString(countryUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(countryUUID), Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        } else if (!stateUUID.isEmpty()) {
            Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(stateUUID), searchKeyWord, UUID.fromString(stateUUID));

            return slaveCityWithCompanyEntityFlux
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository.countByNameContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNull(searchKeyWord, UUID.fromString(stateUUID), searchKeyWord, UUID.fromString(stateUUID))
                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        } else if (!countryUUID.isEmpty()) {
            Flux<SlaveCityEntity> slaveCityWithCompanyEntityFlux = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(pageable, searchKeyWord, UUID.fromString(countryUUID), searchKeyWord, UUID.fromString(countryUUID));

            return slaveCityWithCompanyEntityFlux
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository.countByNameContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(searchKeyWord, UUID.fromString(countryUUID), searchKeyWord, UUID.fromString(countryUUID))
                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", cityEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        }
//        This method returns all cities based on status filter
        else if (!status.isEmpty()) {

            Flux<SlaveCityEntity> slaveCityEntityFluxWithStatus = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveCityEntityFluxWithStatus
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository
                                    .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                            (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))

                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", cityEntity, count, 0L);
                                        }

                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

//        Return All cities
        else {

            Flux<SlaveCityEntity> slaveCityEntityFlux = slaveCityRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveCityEntityFlux
                    .collectList()
                    .flatMap(cityEntity ->
                            slaveCityRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (cityEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", cityEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
    }

    @AuthHasPermission(value = "config_api_v1_cities_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID cityUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveCityRepository.findByUuidAndDeletedAtIsNull(cityUUID)
                .flatMap(cityEntity -> responseSuccessMsg("Record Fetched Successfully", cityEntity))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_cities_store")
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

                    Double longitude = null;
                    Double latitude = null;

                    if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                        longitude = Double.valueOf(String.valueOf(value.getFirst("longitude")));
                    }

                    if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                        latitude = Double.valueOf(String.valueOf(value.getFirst("latitude")));
                    }

                    CityEntity cityEntity = CityEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .postalCode(value.getFirst("postalCode").trim())
                            .longitude(longitude)
                            .latitude(latitude)
                            .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                            .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
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

                    //check if Country exists or not
                    return countryRepository.findByUuidAndDeletedAtIsNull(cityEntity.getCountryUUID())
                            //check if State exists or not
                            .flatMap(country -> stateRepository.findByUuidAndDeletedAtIsNull(cityEntity.getStateUUID())
                                    //check if Name exists for a given Country and State
                                    .flatMap(stateEntity -> {

                                                //check if the given state exists against this country
                                                if (!(stateEntity.getCountryUUID().equals(country.getUuid()))) {
                                                    return responseInfoMsg("Entered State Does not Exist against this Country!");
                                                }

                                                return cityRepository.findFirstByNameIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(cityEntity.getName(), cityEntity.getCountryUUID(), cityEntity.getStateUUID())
                                                        .flatMap(checkNameAndCountry -> responseInfoMsg("Name already exists against this country and State"))
                                                        .switchIfEmpty(Mono.defer(() -> cityRepository.save(cityEntity)
                                                                .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                                                .switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                                                .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."))
                                                        ));
                                            }
                                    ).switchIfEmpty(responseInfoMsg("State does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("State does not exist.Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("Country does not exist"))
                            .onErrorResume(ex -> responseErrorMsg("Country does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_cities_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        final UUID cityUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> cityRepository.findByUuidAndDeletedAtIsNull(cityUUID)
                        .flatMap(previousCityEntity -> {

                            Double longitude = null;
                            Double latitude = null;

                            if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                                longitude = Double.valueOf(String.valueOf(value.getFirst("longitude")));
                            }

                            if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                                latitude = Double.valueOf(String.valueOf(value.getFirst("latitude")));
                            }

                            CityEntity updatedCityEntity = CityEntity.builder()
                                    .uuid(previousCityEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .postalCode(value.getFirst("postalCode").trim())
                                    .longitude(longitude)
                                    .latitude(latitude)
                                    .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                                    .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
                                    .createdBy(previousCityEntity.getCreatedBy())
                                    .createdAt(previousCityEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCityEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousCityEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCityEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCityEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousCityEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCityEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCityEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCityEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousCityEntity.setReqDeletedIP(reqIp);
                            previousCityEntity.setReqDeletedPort(reqPort);
                            previousCityEntity.setReqDeletedBrowser(reqBrowser);
                            previousCityEntity.setReqDeletedOS(reqOs);
                            previousCityEntity.setReqDeletedDevice(reqDevice);
                            previousCityEntity.setReqDeletedReferer(reqReferer);

                            //Check if Country Exists
                            return countryRepository.findByUuidAndDeletedAtIsNull(updatedCityEntity.getCountryUUID())
                                    //Check if State Exists
                                    .flatMap(country -> stateRepository.findByUuidAndDeletedAtIsNull(updatedCityEntity.getStateUUID())
                                            //Check if City Name is Unique for the given Country and State
                                            .flatMap(state -> cityRepository.findFirstByNameIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullAndUuidIsNot(updatedCityEntity.getName(), updatedCityEntity.getCountryUUID(), updatedCityEntity.getStateUUID(), updatedCityEntity.getUuid())
                                                    .flatMap(checkNameForCountry -> responseInfoMsg("Name Already Exists for this Country and State."))
                                                    .switchIfEmpty(Mono.defer(() -> {
                                                                //check if the given state exists against this country
                                                                if (!(state.getCountryUUID().equals(country.getUuid()))) {
                                                                    return responseInfoMsg("Entered State Does not Exist against this Country!");
                                                                }

                                                                //Update previous City Entity
                                                                return cityRepository.save(previousCityEntity)
                                                                        //Update the Updated City Entity
                                                                        .then(cityRepository.save(updatedCityEntity))
                                                                        .flatMap(saveLanguageEntity -> responseSuccessMsg("Record Updated successfully!", saveLanguageEntity))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."));
                                                            }
                                                    ))
                                            ).switchIfEmpty(responseInfoMsg("State does not exist"))
                                            .onErrorResume(ex -> responseErrorMsg("State does not exist.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Country does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Country does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_cities_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        final UUID cityUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
//        find city uuid
        return cityRepository.findByUuidAndDeletedAtIsNull(cityUUID)
//                        Check branch profile uuid
                .flatMap(cityEntity -> companyProfileRepository.findFirstByCityUUIDAndDeletedAtIsNull(cityEntity.getUuid())
//                                Check City Reference exists in Company Profile
                                .flatMap(branchMsg -> responseInfoMsg("Unable to delete. Reference of record exists."))
                                // find location uuid
                                .switchIfEmpty(Mono.defer(() -> locationRepository.findFirstByCityUUIDAndDeletedAtIsNull(cityEntity.getUuid())
                                        // If City Reference exists in location
                                        .flatMap(officeMsg -> responseInfoMsg("Unable to delete. Reference of record exists."))))
                                // If City uuid exists in student profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-academic-records/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student guardian profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student guardian academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher father academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher child profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-profiles/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher child academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in teacher academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-academic-records/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student father academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If City uuid exists in student sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-academic-histories/city/show/", cityEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    cityEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    cityEntity.setDeletedBy(UUID.fromString(userUUID));
                                    cityEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    cityEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    cityEntity.setReqDeletedIP(reqIp);
                                    cityEntity.setReqDeletedPort(reqPort);
                                    cityEntity.setReqDeletedBrowser(reqBrowser);
                                    cityEntity.setReqDeletedOS(reqOs);
                                    cityEntity.setReqDeletedDevice(reqDevice);
                                    cityEntity.setReqDeletedReferer(reqReferer);

                                    return cityRepository.save(cityEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                                }))
                )
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_cities_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID cityUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return cityRepository.findByUuidAndDeletedAtIsNull(cityUUID)
                            .flatMap(previousCityEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCityEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CityEntity updatedCityEntity = CityEntity.builder()
                                        .uuid(previousCityEntity.getUuid())
                                        .name(previousCityEntity.getName())
                                        .description(previousCityEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .postalCode(previousCityEntity.getPostalCode())
                                        .longitude(previousCityEntity.getLongitude())
                                        .latitude(previousCityEntity.getLatitude())
                                        .countryUUID(previousCityEntity.getCountryUUID())
                                        .stateUUID(previousCityEntity.getStateUUID())
                                        .createdBy(previousCityEntity.getCreatedBy())
                                        .createdAt(previousCityEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCityEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCityEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCityEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCityEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCityEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCityEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCityEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCityEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousCityEntity.setReqDeletedIP(reqIp);
                                previousCityEntity.setReqDeletedPort(reqPort);
                                previousCityEntity.setReqDeletedBrowser(reqBrowser);
                                previousCityEntity.setReqDeletedOS(reqOs);
                                previousCityEntity.setReqDeletedDevice(reqDevice);
                                previousCityEntity.setReqDeletedReferer(reqReferer);


                                return cityRepository.save(previousCityEntity)
                                        .then(cityRepository.save(updatedCityEntity))
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


