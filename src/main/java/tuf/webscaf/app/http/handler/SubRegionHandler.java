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
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.master.entity.SubRegionEntity;
import tuf.webscaf.app.dbContext.master.repositry.CountryRepository;
import tuf.webscaf.app.dbContext.master.repositry.RegionRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubRegionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubRegionEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveSubRegionRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helpers.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "subRegionHandler")
public class SubRegionHandler {

    @Autowired
    SlaveSubRegionRepository slaveSubRegionRepository;

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_sub-regions_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Regions
        String regionUUID = serverRequest.queryParam("regionUUID").map(String::toString).orElse("").trim();

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

        if (!status.isEmpty() && !regionUUID.isEmpty()) {
            Flux<SlaveSubRegionEntity> slaveSubRegionEntityFlux = slaveSubRegionRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUID
                            (pageable, searchKeyWord, Boolean.valueOf(status), UUID.fromString(regionUUID), searchKeyWord, Boolean.valueOf(status), UUID.fromString(regionUUID));

            return slaveSubRegionEntityFlux
                    .collectList()
                    .flatMap(subSubRegionEntity -> slaveSubRegionRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUID
                                    (searchKeyWord, Boolean.valueOf(status), UUID.fromString(regionUUID), searchKeyWord, Boolean.valueOf(status), UUID.fromString(regionUUID))
                            .flatMap(count -> {
                                if (subSubRegionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subSubRegionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else if (!regionUUID.isEmpty()) {
            Flux<SlaveSubRegionEntity> slaveSubRegionEntityFlux = slaveSubRegionRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUID
                            (pageable, searchKeyWord, UUID.fromString(regionUUID), searchKeyWord, UUID.fromString(regionUUID));

            return slaveSubRegionEntityFlux
                    .collectList()
                    .flatMap(subSubRegionEntity -> slaveSubRegionRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUID
                                    (searchKeyWord, UUID.fromString(regionUUID), searchKeyWord, UUID.fromString(regionUUID))
                            .flatMap(count -> {
                                if (subSubRegionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subSubRegionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else if (!status.isEmpty()) {
            Flux<SlaveSubRegionEntity> slaveSubRegionEntityFlux = slaveSubRegionRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveSubRegionEntityFlux
                    .collectList()
                    .flatMap(subSubRegionEntity -> slaveSubRegionRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (subSubRegionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subSubRegionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveSubRegionEntity> slaveSubRegionEntityFlux = slaveSubRegionRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveSubRegionEntityFlux
                    .collectList()
                    .flatMap(subSubRegionEntity -> slaveSubRegionRepository.
                            countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (subSubRegionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", subSubRegionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "config_api_v1_sub-regions_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID subSubRegionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveSubRegionRepository.findByUuidAndDeletedAtIsNull(subSubRegionUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-regions_store")
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

                    SubRegionEntity subRegionEntity = SubRegionEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .regionUUID(UUID.fromString(value.getFirst("regionUUID").trim()))
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
                    return subRegionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(subRegionEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            .switchIfEmpty(Mono.defer(() -> regionRepository.findByUuidAndDeletedAtIsNull(subRegionEntity.getRegionUUID())
                                    .flatMap(region -> subRegionRepository.save(subRegionEntity)
                                            .flatMap(currencySave -> responseSuccessMsg("Record stored successfully!", currencySave))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please contact developer."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Region does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Region does not exist.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-regions_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID subRegionUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> subRegionRepository.findByUuidAndDeletedAtIsNull(subRegionUUID)
                        .flatMap(previousEntity -> {

                            SubRegionEntity updatedSubRegionEntity = SubRegionEntity.builder()
                                    .uuid(previousEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .regionUUID(UUID.fromString(value.getFirst("regionUUID").trim()))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousEntity.getCreatedBy())
                                    .createdAt(previousEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
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

                            //check if Name is Unique
                            return subRegionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedSubRegionEntity.getName(), updatedSubRegionEntity.getUuid())
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    .switchIfEmpty(Mono.defer(() ->
                                            regionRepository.findByUuidAndDeletedAtIsNull(updatedSubRegionEntity.getRegionUUID())
                                                    .flatMap(regionEntity -> subRegionRepository.save(previousEntity)
                                                            .then(subRegionRepository.save(updatedSubRegionEntity))
                                                            .flatMap(subRegionSave -> responseSuccessMsg("Record updated successfully!", subRegionSave))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("Region does not exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("Region does not exist.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_sub-regions_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID subRegionUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return subRegionRepository.findByUuidAndDeletedAtIsNull(subRegionUUID)
                .flatMap(subRegionEntity -> countryRepository.findFirstByRegionUUIDAndDeletedAtIsNull(subRegionEntity.getUuid())
//                                If Sub-Region Reference exists in country
                                .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete as reference of record exists."))
                                .switchIfEmpty(Mono.defer(() -> {

                                    subRegionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    subRegionEntity.setDeletedBy(UUID.fromString(userUUID));
                                    subRegionEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    subRegionEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    subRegionEntity.setReqDeletedIP(reqIp);
                                    subRegionEntity.setReqDeletedPort(reqPort);
                                    subRegionEntity.setReqDeletedBrowser(reqBrowser);
                                    subRegionEntity.setReqDeletedOS(reqOs);
                                    subRegionEntity.setReqDeletedDevice(reqDevice);
                                    subRegionEntity.setReqDeletedReferer(reqReferer);

                                    return subRegionRepository.save(subRegionEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-regions_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID subRegionUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return subRegionRepository.findByUuidAndDeletedAtIsNull(subRegionUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                SubRegionEntity updatedSubRegionEntity = SubRegionEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
                                        .regionUUID(previousEntity.getRegionUUID())
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

                                return subRegionRepository.save(previousEntity)
                                        .then(subRegionRepository.save(updatedSubRegionEntity))
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
