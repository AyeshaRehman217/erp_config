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
import tuf.webscaf.app.dbContext.master.entity.RegionEntity;
import tuf.webscaf.app.dbContext.master.repositry.CountryRepository;
import tuf.webscaf.app.dbContext.master.repositry.RegionRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubRegionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveRegionEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveRegionRepository;
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
@Tag(name = "regionHandler")
public class RegionHandler {

    @Autowired
    SlaveRegionRepository slaveRegionRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_regions_index")
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
            Flux<SlaveRegionEntity> slaveRegionEntityFlux = slaveRegionRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveRegionEntityFlux
                    .collectList()
                    .flatMap(regionEntity -> slaveRegionRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {

                                if (regionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", regionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveRegionEntity> slaveRegionEntityFlux = slaveRegionRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveRegionEntityFlux
                    .collectList()
                    .flatMap(regionEntity -> slaveRegionRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {

                                if (regionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", regionEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }


    }

    @AuthHasPermission(value = "config_api_v1_regions_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID regionUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveRegionRepository.findByUuidAndDeletedAtIsNull(regionUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_regions_store")
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

                    RegionEntity regionEntity = RegionEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
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
                    return regionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(regionEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            .switchIfEmpty(Mono.defer(() -> regionRepository.save(regionEntity)
                                    .flatMap(regionSave -> responseSuccessMsg("Record stored Successfully!", regionEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_regions_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID regionUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> regionRepository.findByUuidAndDeletedAtIsNull(regionUUID)
                        .flatMap(previousRegionEntity -> {


                            RegionEntity updatedRegionEntity = RegionEntity.builder()
                                    .uuid(previousRegionEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousRegionEntity.getCreatedBy())
                                    .createdAt(previousRegionEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousRegionEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousRegionEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousRegionEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousRegionEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousRegionEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousRegionEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();


                            previousRegionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousRegionEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousRegionEntity.setReqDeletedIP(reqIp);
                            previousRegionEntity.setReqDeletedPort(reqPort);
                            previousRegionEntity.setReqDeletedBrowser(reqBrowser);
                            previousRegionEntity.setReqDeletedOS(reqOs);
                            previousRegionEntity.setReqDeletedDevice(reqDevice);
                            previousRegionEntity.setReqDeletedReferer(reqReferer);

                            //check if Name is Unique
                            return regionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedRegionEntity.getName(), updatedRegionEntity.getUuid())
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    .switchIfEmpty(Mono.defer(() ->
                                            //Update previous Region Entity
                                            regionRepository.save(previousRegionEntity)
                                                    //Update the Updated Region Entity
                                                    .then(regionRepository.save(updatedRegionEntity))
                                                    .flatMap(saveLanguageEntity -> responseSuccessMsg("Record Updated successfully!", saveLanguageEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                    ));
                        })
                        .switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer")))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_regions_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID regionUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return regionRepository.findByUuidAndDeletedAtIsNull(regionUUID)
                .flatMap(regionEntity -> subRegionRepository.findFirstByRegionUUIDAndDeletedAtIsNull(regionEntity.getUuid())
//                                If Region Reference exists in Sub Region
                                .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete record as the Reference of record exists!"))
//                                  If Region Reference exists in Country
                                .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByRegionUUIDAndDeletedAtIsNull(regionEntity.getUuid())
                                        .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete record as the Reference of record exists!"))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    regionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    regionEntity.setDeletedBy(UUID.fromString(userUUID));
                                    regionEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    regionEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    regionEntity.setReqDeletedIP(reqIp);
                                    regionEntity.setReqDeletedPort(reqPort);
                                    regionEntity.setReqDeletedBrowser(reqBrowser);
                                    regionEntity.setReqDeletedOS(reqOs);
                                    regionEntity.setReqDeletedDevice(reqDevice);
                                    regionEntity.setReqDeletedReferer(reqReferer);

                                    return regionRepository.save(regionEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not Exist."))
                .onErrorResume(ex -> responseErrorMsg("Record does not Exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_regions_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID languageUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return regionRepository.findByUuidAndDeletedAtIsNull(languageUUID)
                            .flatMap(previousRegionEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousRegionEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                RegionEntity updatedRegionEntity = RegionEntity.builder()
                                        .uuid(previousRegionEntity.getUuid())
                                        .name(previousRegionEntity.getName())
                                        .description(previousRegionEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .createdBy(previousRegionEntity.getCreatedBy())
                                        .createdAt(previousRegionEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousRegionEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousRegionEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousRegionEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousRegionEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousRegionEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousRegionEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();


                                previousRegionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousRegionEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousRegionEntity.setReqDeletedIP(reqIp);
                                previousRegionEntity.setReqDeletedPort(reqPort);
                                previousRegionEntity.setReqDeletedBrowser(reqBrowser);
                                previousRegionEntity.setReqDeletedOS(reqOs);
                                previousRegionEntity.setReqDeletedDevice(reqDevice);
                                previousRegionEntity.setReqDeletedReferer(reqReferer);

                                return regionRepository.save(previousRegionEntity)
                                        .then(regionRepository.save(updatedRegionEntity))
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
