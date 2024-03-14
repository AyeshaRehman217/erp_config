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
import tuf.webscaf.app.dbContext.master.entity.LocationEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLocationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCompanyRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLocationRepository;
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
@Tag(name = "locationHandler")
public class LocationHandler {

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    SlaveLocationRepository slaveLocationRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    SlaveCompanyRepository slaveCompanyRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_locations_index")
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
            Flux<SlaveLocationEntity> slaveLocationEntityFlux = slaveLocationRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveLocationEntityFlux
                    .collectList()
                    .flatMap(locationEntity -> slaveLocationRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (locationEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", locationEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else {
            Flux<SlaveLocationEntity> slaveLocationEntityFlux = slaveLocationRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord, searchKeyWord);
            return slaveLocationEntityFlux
                    .collectList()
                    .flatMap(locationEntity -> slaveLocationRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull
                                    (searchKeyWord, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (locationEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", locationEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
    }

    @AuthHasPermission(value = "config_api_v1_locations_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID locationUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveLocationRepository.findByUuidAndDeletedAtIsNull(locationUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_locations_store")
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

                    LocationEntity locationEntity = LocationEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .description(value.getFirst("description").trim())
                            .name(value.getFirst("name").trim())
                            .address(value.getFirst("address").trim())
                            .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                            .cityUUID(UUID.fromString(value.getFirst("cityUUID").trim()))
                            .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
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

                    //check if name is unique
                    return locationRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(locationEntity.getName())
                            .flatMap(locationRecord -> responseInfoMsg("Name Already Exist!"))
                            //check if Country UUID exists in countries
                            .switchIfEmpty(Mono.defer(() -> countryRepository.findByUuidAndDeletedAtIsNull(locationEntity.getCountryUUID())
                                    // check if State UUID exists in state
                                    .flatMap(country -> stateRepository.findByUuidAndDeletedAtIsNull(locationEntity.getStateUUID())
                                            //check if State UUID exists in city
                                            .flatMap(state -> cityRepository.findByUuidAndDeletedAtIsNull(locationEntity.getCityUUID())
                                                    .flatMap(cityEntity -> {
                                                                //check if City Lie Against this Country and State
                                                                if (!cityEntity.getCountryUUID().equals(country.getUuid())) {
                                                                    return responseInfoMsg("City Does not Exist Against This Country!");
                                                                }
                                                                if (!cityEntity.getStateUUID().equals(state.getUuid())) {
                                                                    return responseInfoMsg("City Does not exist Against this State!");
                                                                }
                                                                // If everything is okay then save record
                                                                return locationRepository.save(locationEntity)
                                                                        .flatMap(citySave -> responseSuccessMsg("Record stored successfully!", locationEntity))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is Something wrong please try again."))
                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."));
                                                            }
                                                    ).switchIfEmpty(responseInfoMsg("City record does not exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("City record does not exist.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("State record does not exist"))
                                            .onErrorResume(ex -> responseErrorMsg("State record does not exist.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Country record does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Country record does not exist.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_locations_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        final UUID locationUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> locationRepository.findByUuidAndDeletedAtIsNull(locationUUID)
                        .flatMap(previousLocationEntity -> {

                            LocationEntity updatedLocationEntity = LocationEntity.builder()
                                    .uuid(previousLocationEntity.getUuid())
                                    .description(value.getFirst("description").trim())
                                    .name(value.getFirst("name").trim())
                                    .address(value.getFirst("address").trim())
                                    .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                                    .cityUUID(UUID.fromString(value.getFirst("cityUUID").trim()))
                                    .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousLocationEntity.getCreatedBy())
                                    .createdAt(previousLocationEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousLocationEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousLocationEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousLocationEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousLocationEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousLocationEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousLocationEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousLocationEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousLocationEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousLocationEntity.setReqDeletedIP(reqIp);
                            previousLocationEntity.setReqDeletedPort(reqPort);
                            previousLocationEntity.setReqDeletedBrowser(reqBrowser);
                            previousLocationEntity.setReqDeletedOS(reqOs);
                            previousLocationEntity.setReqDeletedDevice(reqDevice);
                            previousLocationEntity.setReqDeletedReferer(reqReferer);

                            //check if Name is Unique
                            return locationRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedLocationEntity.getName(), updatedLocationEntity.getUuid())
                                    .flatMap(locationRecord -> responseInfoMsg("Name Already Exists!"))
                                    //check if given Country exist in Countries
                                    .switchIfEmpty(Mono.defer(() -> countryRepository.findByUuidAndDeletedAtIsNull(updatedLocationEntity.getCountryUUID())
                                            //check if given State exist in states
                                            .flatMap(country -> stateRepository.findByUuidAndDeletedAtIsNull(updatedLocationEntity.getStateUUID())
                                                    //check if given city exists in Cities
                                                    .flatMap(state -> cityRepository.findByUuidAndDeletedAtIsNull(updatedLocationEntity.getCityUUID())
                                                            .flatMap(city -> {
                                                                        //check if City Lie Against this Country and State
                                                                        if (!city.getCountryUUID().equals(country.getUuid())) {
                                                                            return responseInfoMsg("City Does not Exist Against This Country!");
                                                                        }
                                                                        if (!city.getStateUUID().equals(state.getUuid())) {
                                                                            return responseInfoMsg("City Does not exist Against this State!");
                                                                        }
                                                                        // If everything is okay then Delete previous Entity
                                                                        return locationRepository.save(previousLocationEntity)
                                                                                .then(locationRepository.save(updatedLocationEntity))
                                                                                .flatMap(saveLocationEntity -> responseSuccessMsg("Record Updated successfully!", saveLocationEntity))
                                                                                .switchIfEmpty(responseInfoMsg("Unable to update Record.There is Something wrong please try again."))
                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to update Record.Please Contact Developer."));
                                                                    }
                                                            ).switchIfEmpty(responseInfoMsg("City record does not exist"))
                                                            .onErrorResume(ex -> responseErrorMsg("City record does not exist.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("State record does not exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("State record does not exist.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("Country record does not exist"))
                                            .onErrorResume(ex -> responseErrorMsg("Country record does not exist.Please Contact Developer."))));
                        }).switchIfEmpty(responseInfoMsg("Record record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record record does not exist.Please Contact Developer.")))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_locations_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID locationUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return locationRepository.findByUuidAndDeletedAtIsNull(locationUUID)
                // check location exist in company profile
                .flatMap(locationEntity -> companyProfileRepository.findFirstByLocationUUIDAndDeletedAtIsNull(locationEntity.getUuid())
                        .flatMap(configEntity -> responseInfoMsg("Unable to delete Record as the Reference of record exists!"))
                        //  check location exist in branch profile
                        .switchIfEmpty(Mono.defer(() -> branchProfileRepository.findFirstByLocationUUIDAndDeletedAtIsNull(locationEntity.getUuid())
                                .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete record as the Reference of record exists!"))))
                        .switchIfEmpty(Mono.defer(() -> {

                            locationEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            locationEntity.setDeletedBy(UUID.fromString(userUUID));
                            locationEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            locationEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            locationEntity.setReqDeletedIP(reqIp);
                            locationEntity.setReqDeletedPort(reqPort);
                            locationEntity.setReqDeletedBrowser(reqBrowser);
                            locationEntity.setReqDeletedOS(reqOs);
                            locationEntity.setReqDeletedDevice(reqDevice);
                            locationEntity.setReqDeletedReferer(reqReferer);

                            return locationRepository.save(locationEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_locations_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID locationUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return locationRepository.findByUuidAndDeletedAtIsNull(locationUUID)
                            .flatMap(previousLocationEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousLocationEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                LocationEntity updatedLocationEntity = LocationEntity.builder()
                                        .uuid(previousLocationEntity.getUuid())
                                        .description(previousLocationEntity.getDescription())
                                        .name(previousLocationEntity.getName())
                                        .address(previousLocationEntity.getAddress())
                                        .countryUUID(previousLocationEntity.getCountryUUID())
                                        .cityUUID(previousLocationEntity.getCityUUID())
                                        .stateUUID(previousLocationEntity.getStateUUID())
                                        .status(status == true ? true : false)
                                        .createdBy(previousLocationEntity.getCreatedBy())
                                        .createdAt(previousLocationEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousLocationEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousLocationEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousLocationEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousLocationEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousLocationEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousLocationEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousLocationEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousLocationEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousLocationEntity.setReqDeletedIP(reqIp);
                                previousLocationEntity.setReqDeletedPort(reqPort);
                                previousLocationEntity.setReqDeletedBrowser(reqBrowser);
                                previousLocationEntity.setReqDeletedOS(reqOs);
                                previousLocationEntity.setReqDeletedDevice(reqDevice);
                                previousLocationEntity.setReqDeletedReferer(reqReferer);

                                return locationRepository.save(previousLocationEntity)
                                        .then(locationRepository.save(updatedLocationEntity))
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


//    Custom Functions

}

