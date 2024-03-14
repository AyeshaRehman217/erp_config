package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ModuleEntity;
import tuf.webscaf.app.dbContext.master.repositry.ConfigRepository;
import tuf.webscaf.app.dbContext.master.repositry.ModuleRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveConfigRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveModuleRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.helpers.SlugifyHelper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Component
@Tag(name = "moduleHandler")
public class ModuleHandler {

    @Autowired
    SlaveModuleRepository slaveModuleRepository;

    @Autowired
    SlaveConfigRepository slaveConfigRepository;

    @Autowired
    ModuleRepository moduleRepository;

    @Autowired
    ConfigRepository configRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;
    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_modules_index")
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
            Flux<SlaveModuleEntity> slaveModuleEntityFlux = slaveModuleRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveModuleEntityFlux
                    .collectList()
                    .flatMap(moduleEntity -> slaveModuleRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (moduleEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", moduleEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveModuleEntity> slaveModuleEntityFlux = slaveModuleRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveModuleEntityFlux
                    .collectList()
                    .flatMap(moduleEntity -> slaveModuleRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (moduleEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", moduleEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }

    @AuthHasPermission(value = "config_api_v1_id_modules_show")
    public Mono<ServerResponse> showWithId(ServerRequest serverRequest) {
        final long moduleId = Long.parseLong(serverRequest.pathVariable("id"));

        return slaveModuleRepository.findByIdAndDeletedAtIsNull(moduleId)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_modules_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID moduleUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveModuleRepository.findByUuidAndDeletedAtIsNull(moduleUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_modules_store")
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

                    UUID moduleIcon = null;

                    if ((value.getFirst("icon")) != null && (value.getFirst("icon") != "")) {
                        moduleIcon = UUID.fromString(value.getFirst("icon"));
                    }

                    ModuleEntity moduleEntity = ModuleEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(value.getFirst("slug").trim())
                            .description(value.getFirst("description").trim())
                            .baseURL(value.getFirst("baseURL").trim())
                            .infoURL(value.getFirst("infoURL").trim())
                            .hostAddress(value.getFirst("hostAddress").trim())
                            .icon(moduleIcon)
                            .mVersion(value.getFirst("mversion").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .port(Integer.valueOf(value.getFirst("port").trim()))
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
                    UUID finalModuleIcon = moduleIcon;
                    return moduleRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(moduleEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            //check if slug is unique
                            .switchIfEmpty(Mono.defer(() -> moduleRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNull(moduleEntity.getSlug())
                                    .flatMap(key -> responseInfoMsg("Slug Already Exists!"))))
                            .switchIfEmpty(Mono.defer(() -> {
                                Mono<ServerResponse> serverResponse = Mono.empty();
                                if (!slugifyHelper.validateSlug(moduleEntity.getSlug())) {
                                    serverResponse = responseErrorMsg("Invalid Slug!");
                                }
                                return serverResponse;
                            }))
                            .switchIfEmpty(Mono.defer(() -> {
                                        //Check if User Selects Document Image UUID and This document UUID exists in Drive Module
                                        if (finalModuleIcon != null && !finalModuleIcon.equals("")) {
                                            return apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", moduleEntity.getIcon(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                    .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                            .flatMap(documentUUID -> {
                                                                        //Sending Document ids in Form data to check if document UUID's exist
                                                                        MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data

                                                                        sendFormData.add("docId", String.valueOf(documentUUID));//iterating over multiple values and then adding in list

                                                                        //update Document Submitted Status
                                                                        return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                .flatMap(updateDocStatus -> moduleRepository.save(moduleEntity)
                                                                                        .flatMap(moduleSave -> responseSuccessMsg("Record stored successfully!", moduleSave)
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store module. There is something wrong please try again.")))
                                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Store module. Please Contact Developer."))
                                                                                );
                                                                    }
                                                            )
                                                    ).switchIfEmpty(responseInfoMsg("Unable to Upload Image"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Upload Image. Please contact developer"));
                                        } else {
                                            return moduleRepository.save(moduleEntity)
                                                    .flatMap(moduleSave -> responseSuccessMsg("Record stored successfully!", moduleSave)
                                                            .switchIfEmpty(responseInfoMsg("Unable to Store module. There is something wrong please try again."))
                                                    ).onErrorResume(ex -> responseErrorMsg("Unable to Store module. Please Contact Developer."));

                                        }
                                    }
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request"));
    }

    @AuthHasPermission(value = "config_api_v1_modules_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID moduleUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> moduleRepository.findByUuidAndDeletedAtIsNull(moduleUUID)
                        .flatMap(previousModuleEntity -> {

                            UUID moduleIcon = null;

                            if ((value.getFirst("icon")) != null && (value.getFirst("icon") != "")) {
                                moduleIcon = UUID.fromString(value.getFirst("icon"));
                            }

                            ModuleEntity updatedModuleEntity = ModuleEntity.builder()
                                    .uuid(previousModuleEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(value.getFirst("slug").trim())
                                    .description(value.getFirst("description").trim())
                                    .baseURL(value.getFirst("baseURL").trim())
                                    .infoURL(value.getFirst("infoURL").trim())
                                    .hostAddress(value.getFirst("hostAddress").trim())
                                    .icon(moduleIcon)
                                    .mVersion(value.getFirst("mversion").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .port(Integer.valueOf(value.getFirst("port").trim()))
                                    .createdBy(previousModuleEntity.getCreatedBy())
                                    .createdAt(previousModuleEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousModuleEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousModuleEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousModuleEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousModuleEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousModuleEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousModuleEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousModuleEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousModuleEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousModuleEntity.setReqDeletedIP(reqIp);
                            previousModuleEntity.setReqDeletedPort(reqPort);
                            previousModuleEntity.setReqDeletedBrowser(reqBrowser);
                            previousModuleEntity.setReqDeletedOS(reqOs);
                            previousModuleEntity.setReqDeletedDevice(reqDevice);
                            previousModuleEntity.setReqDeletedReferer(reqReferer);

                            UUID finalModuleIcon = moduleIcon;
                            return moduleRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedModuleEntity.getName(), updatedModuleEntity.getUuid())
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    .switchIfEmpty(Mono.defer(() -> moduleRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedModuleEntity.getSlug(), updatedModuleEntity.getUuid())
                                            .flatMap(checkLanguageCode -> responseInfoMsg("Slug already exist"))))
                                    .switchIfEmpty(Mono.defer(() -> {

                                        Mono<ServerResponse> serverResponse = Mono.empty();
                                        if (!slugifyHelper.validateSlug(value.getFirst("slug").trim())) {
                                            serverResponse = responseErrorMsg("Invalid Slug!");
                                        }
                                        return serverResponse;
                                    }))
                                    .switchIfEmpty(Mono.defer(() -> {
                                                //Check if User Selects Document Image UUID and This document UUID exists in Drive Module
                                                if (finalModuleIcon != null && !finalModuleIcon.equals("")) {
                                                    return apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", finalModuleIcon, userUUID, reqCompanyUUID, reqBranchUUID)
                                                            .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                                    .flatMap(documentUUID -> {

                                                                                //Sending Document ids in Form data to check if document Id's exist
                                                                                MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data

                                                                                sendFormData.add("docId", String.valueOf(documentUUID));//iterating over multiple values and then adding in list

                                                                                //update Document Submitted Status
                                                                                return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                        .flatMap(updatedDocument -> moduleRepository.save(previousModuleEntity)
                                                                                                .then(moduleRepository.save(updatedModuleEntity))
                                                                                                .flatMap(moduleUpdate -> responseSuccessMsg("Record Updated Successfully!", moduleUpdate))
                                                                                                .switchIfEmpty(responseInfoMsg("Unable to update Record.There is something wrong please try again."))
                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to update Record.Please Contact Developer."))

                                                                                        );
                                                                            }
                                                                    )).switchIfEmpty(responseInfoMsg("Unable to Upload Image.There is something wrong please try again"))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Upload Image.Please Contact Developer."));
                                                } else {
                                                    //Check if Document Image is Empty the Store Company
                                                    return moduleRepository.save(previousModuleEntity)
                                                            .then(moduleRepository.save(updatedModuleEntity))
                                                            .flatMap(moduleUpdate -> responseSuccessMsg("Record Updated Successfully!", moduleUpdate))
                                                            .switchIfEmpty(responseInfoMsg("Unable to update Record.There is something wrong please try again."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to update Record.Please Contact Developer."));
                                                }
                                            }
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_modules_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID moduleUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        String userUUID = serverRequest.headers().firstHeader("auid");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User!");
            }
        }

        return moduleRepository.findByUuidAndDeletedAtIsNull(moduleUUID)
                .flatMap(moduleEntity -> configRepository.findFirstByModuleUUIDAndDeletedAtIsNull(moduleEntity.getUuid())
                                .flatMap(configEntity -> responseInfoMsg("Unable to Delete Record as reference of record exists."))
//                                         find module in document from drive api call
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/module/show/", moduleEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.getUUID(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to Delete Record as reference of record exists.")))))
//                                         find module in docNature from drive api call
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(driveUri + "api/v1/doc-natures/module/show/", moduleEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.getUUID(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to Delete Record as reference of record exists.")))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    moduleEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    moduleEntity.setDeletedBy(UUID.fromString(userUUID));
                                    moduleEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    moduleEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    moduleEntity.setReqDeletedIP(reqIp);
                                    moduleEntity.setReqDeletedPort(reqPort);
                                    moduleEntity.setReqDeletedBrowser(reqBrowser);
                                    moduleEntity.setReqDeletedOS(reqOs);
                                    moduleEntity.setReqDeletedDevice(reqDevice);
                                    moduleEntity.setReqDeletedReferer(reqReferer);

                                    return moduleRepository.save(moduleEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_modules_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID moduleUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return moduleRepository.findByUuidAndDeletedAtIsNull(moduleUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                ModuleEntity updatedModuleEntity = ModuleEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .slug(previousEntity.getSlug())
                                        .description(previousEntity.getDescription())
                                        .baseURL(previousEntity.getBaseURL())
                                        .infoURL(previousEntity.getInfoURL())
                                        .hostAddress(previousEntity.getHostAddress())
                                        .icon(previousEntity.getIcon())
                                        .mVersion(previousEntity.getMVersion())
                                        .status(status == true ? true : false)
                                        .port(previousEntity.getPort())
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

                                return moduleRepository.save(previousEntity)
                                        .then(moduleRepository.save(updatedModuleEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please Contact Developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }


    @AuthHasPermission(value = "config_api_v1_modules_slug")
    public Mono<ServerResponse> slug(ServerRequest serverRequest) {
        String searchSlug = serverRequest.queryParam("slug").map(String::toString).orElse("");

        return serverRequest.formData()
                .flatMap(value -> moduleRepository.findBySlugAndDeletedAtIsNull(searchSlug.trim())
                        .flatMap(value1 -> responseSlugMsg("Slug Already Exist!"))
                        .switchIfEmpty(Mono.defer(() -> {
                            if (!slugifyHelper.validateSlug(searchSlug.trim())) {
                                return responseErrorMsg("Invalid Slug");
                            } else {
                                return responseSuccessMsg("Valid Slug", value);
                            }
                        })))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

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
