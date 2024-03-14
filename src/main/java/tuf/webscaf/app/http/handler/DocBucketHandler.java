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
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.master.repositry.DocBucketRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveDocBucketRepository;
import tuf.webscaf.app.service.ApiCallService;
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
@Tag(name = "docBucketHandler")
public class DocBucketHandler {

    @Autowired
    SlaveDocBucketRepository slaveDocBucketRepository;

    @Autowired
    DocBucketRepository docBucketRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_doc-buckets_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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
            Flux<SlaveDocBucketEntity> slaveDocBucketEntityFlux = slaveDocBucketRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveDocBucketEntityFlux
                    .collectList()
                    .flatMap(docBucketEntity -> slaveDocBucketRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (docBucketEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", docBucketEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveDocBucketEntity> slaveDocBucketEntityFlux = slaveDocBucketRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord, pageable);
            return slaveDocBucketEntityFlux
                    .collectList()
                    .flatMap(docBucketEntity -> slaveDocBucketRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (docBucketEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", docBucketEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }


    @AuthHasPermission(value = "config_api_v1_doc-buckets_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID docBucketUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveDocBucketRepository.findByUuidAndDeletedAtIsNull(docBucketUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_store")
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

                    DocBucketEntity docBucketEntity = DocBucketEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(value.getFirst("slug").trim())
                            .description(value.getFirst("description").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .url(value.getFirst("url").trim())
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
                    return docBucketRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(docBucketEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            //check if slug is unique
                            .switchIfEmpty(Mono.defer(() -> docBucketRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNull(docBucketEntity.getSlug())
                                    .flatMap(key -> responseInfoMsg("Slug Already Exists!"))))
                            .switchIfEmpty(Mono.defer(() -> {
                                Mono<ServerResponse> serverResponse = Mono.empty();
                                if (!slugifyHelper.validateSlug(docBucketEntity.getSlug())) {
                                    serverResponse = responseErrorMsg("Invalid Slug!");
                                }
                                return serverResponse;
                            }))
                            .switchIfEmpty(Mono.defer(() -> docBucketRepository.save(docBucketEntity)
                                    .flatMap(docBucketSave -> responseSuccessMsg("Record Stored Successfully!", docBucketSave))
                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to store record.Please Contact Developer."))
                            ));
                })
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    @AuthHasPermission(value = "config_api_v1_doc-buckets_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID docBucketUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> docBucketRepository.findByUuidAndDeletedAtIsNull(docBucketUUID)
                        .flatMap(previousDocBucketEntity -> {

                            DocBucketEntity updatedDocBucketEntity = DocBucketEntity.builder()
                                    .uuid(previousDocBucketEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(value.getFirst("slug").trim())
                                    .description(value.getFirst("description").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .url(value.getFirst("url").trim())
                                    .port(Integer.valueOf(value.getFirst("port").trim()))
                                    .createdBy(previousDocBucketEntity.getCreatedBy())
                                    .createdAt(previousDocBucketEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousDocBucketEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousDocBucketEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousDocBucketEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousDocBucketEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousDocBucketEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousDocBucketEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousDocBucketEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousDocBucketEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousDocBucketEntity.setReqDeletedIP(reqIp);
                            previousDocBucketEntity.setReqDeletedPort(reqPort);
                            previousDocBucketEntity.setReqDeletedBrowser(reqBrowser);
                            previousDocBucketEntity.setReqDeletedOS(reqOs);
                            previousDocBucketEntity.setReqDeletedDevice(reqDevice);
                            previousDocBucketEntity.setReqDeletedReferer(reqReferer);

                            //check if name is unique
                            return docBucketRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedDocBucketEntity.getName(), updatedDocBucketEntity.getUuid())
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    //check if Slug is unique
                                    .switchIfEmpty(Mono.defer(() -> docBucketRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedDocBucketEntity.getSlug(), updatedDocBucketEntity.getUuid())
                                            .flatMap(checkLanguageCode -> responseInfoMsg("Slug already exist"))))
                                    .switchIfEmpty(Mono.defer(() -> {
                                        Mono<ServerResponse> serverResponse = Mono.empty();
                                        if (!slugifyHelper.validateSlug(value.getFirst("slug").trim())) {
                                            serverResponse = responseErrorMsg("Invalid Slug!");
                                        }
                                        return serverResponse;
                                    }))
                                    .switchIfEmpty(Mono.defer(() -> docBucketRepository.save(previousDocBucketEntity)
                                            .then(docBucketRepository.save(updatedDocBucketEntity))
                                            .flatMap(saveDocEntity -> responseSuccessMsg("Record Stored Successfully!", saveDocEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer.")))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID docBucketUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

//        find docBucket id
        return docBucketRepository.findByUuidAndDeletedAtIsNull(docBucketUUID)
//                    find docBucket in documents from drive api
                .flatMap(docBucketEntity -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/bucket/show/", docBucketEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                .flatMap(checkDocBucketApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists")))
                        .switchIfEmpty(Mono.defer(() -> {

                                    docBucketEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    docBucketEntity.setDeletedBy(UUID.fromString(userUUID));
                                    docBucketEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    docBucketEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    docBucketEntity.setReqDeletedIP(reqIp);
                                    docBucketEntity.setReqDeletedPort(reqPort);
                                    docBucketEntity.setReqDeletedBrowser(reqBrowser);
                                    docBucketEntity.setReqDeletedOS(reqOs);
                                    docBucketEntity.setReqDeletedDevice(reqDevice);
                                    docBucketEntity.setReqDeletedReferer(reqReferer);

                                    return docBucketRepository.save(docBucketEntity)
                                            .flatMap(value1 -> responseSuccessMsg("Record Deleted Successfully", value1))
                                            .switchIfEmpty(responseInfoMsg("Unable to Delete record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete record. Please Contact Developer."));
                                })
                        )).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_active_update")
    public Mono<ServerResponse> activeBucket(ServerRequest serverRequest) {
        final UUID docBucketUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> docBucketRepository.findByUuidAndDeletedAtIsNull(docBucketUUID)
                        .flatMap(previousBucket -> docBucketRepository.findAllByIsActiveAndDeletedAtIsNull(true)
                                .collectList()
                                .flatMap(bucketActive -> {

                                    boolean isActiveBucket = Boolean.parseBoolean(value.getFirst("isActive"));

                                    // If same value already exist in database
                                    if (((previousBucket.getIsActive()) == isActiveBucket)) {

                                        return responseWarningMsg("Record already exist with same value!");
                                    }

                                    DocBucketEntity updatedDocBucketEntity = DocBucketEntity.builder()
                                            .uuid(previousBucket.getUuid())
                                            .name(previousBucket.getName())
                                            .slug(previousBucket.getSlug())
                                            .description(previousBucket.getDescription())
                                            .status(previousBucket.getStatus())
                                            .url(previousBucket.getUrl())
                                            .port(previousBucket.getPort())
                                            .createdBy(previousBucket.getCreatedBy())
                                            .createdAt(previousBucket.getCreatedAt())
                                            .updatedBy(UUID.fromString(userUUID))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                            .reqCreatedIP(previousBucket.getReqCreatedIP())
                                            .reqCreatedPort(previousBucket.getReqCreatedPort())
                                            .reqCreatedBrowser(previousBucket.getReqCreatedBrowser())
                                            .reqCreatedOS(previousBucket.getReqCreatedOS())
                                            .reqCreatedDevice(previousBucket.getReqCreatedDevice())
                                            .reqCreatedReferer(previousBucket.getReqCreatedReferer())
                                            .reqUpdatedIP(reqIp)
                                            .reqUpdatedPort(reqPort)
                                            .reqUpdatedBrowser(reqBrowser)
                                            .reqUpdatedOS(reqOs)
                                            .reqUpdatedDevice(reqDevice)
                                            .reqUpdatedReferer(reqReferer)
                                            .build();


                                    // If no bucket is already active, activate the bucket
                                    if (isActiveBucket && bucketActive.isEmpty()) {

                                        previousBucket.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                        previousBucket.setDeletedBy(UUID.fromString(userUUID));
                                        previousBucket.setReqDeletedIP(reqIp);
                                        previousBucket.setReqDeletedPort(reqPort);
                                        previousBucket.setReqDeletedBrowser(reqBrowser);
                                        previousBucket.setReqDeletedOS(reqOs);
                                        previousBucket.setReqDeletedDevice(reqDevice);
                                        previousBucket.setReqDeletedReferer(reqReferer);
                                        updatedDocBucketEntity.setIsActive(isActiveBucket);

                                        return docBucketRepository.save(previousBucket)
                                                .then(docBucketRepository.save(updatedDocBucketEntity))
                                                .flatMap(docBucketRecord -> responseSuccessMsg("Bucket Activated Successfully!", docBucketRecord))
                                                .switchIfEmpty(responseInfoMsg("Unable to active Bucket.There is something wrong please try again."))
                                                .onErrorResume(err -> responseErrorMsg("Unable to active Bucket.Please Contact Developer."));
                                    }
                                    // If another bucket is already active
                                    else if (isActiveBucket && !bucketActive.isEmpty()) {
                                        return responseInfoMsg("Unable to active Bucket As Another Bucket is Already Active and in use.")
                                                .onErrorResume(err -> responseErrorMsg("Unable to activate Bucket  As Another Bucket is Already Active.Please Contact Developer."));
                                    }    // Inactivate a bucket
                                    else {
                                        updatedDocBucketEntity.setIsActive(isActiveBucket);

                                        previousBucket.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                        previousBucket.setDeletedBy(UUID.fromString(userUUID));
                                        previousBucket.setReqDeletedIP(reqIp);
                                        previousBucket.setReqDeletedPort(reqPort);
                                        previousBucket.setReqDeletedBrowser(reqBrowser);
                                        previousBucket.setReqDeletedOS(reqOs);
                                        previousBucket.setReqDeletedDevice(reqDevice);
                                        previousBucket.setReqDeletedReferer(reqReferer);

                                        return docBucketRepository.save(previousBucket)
                                                .then(docBucketRepository.save(updatedDocBucketEntity))
                                                .flatMap(docBucketRecord -> responseSuccessMsg("Bucket Activated Successfully!", docBucketRecord))
                                                .switchIfEmpty(responseInfoMsg("Unable to active Bucket.There is something wrong please try again."))
                                                .onErrorResume(err -> responseErrorMsg("Unable to active Bucket.Please Contact Developer."));
                                    }

                                })

                        ).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_active_show")
    public Mono<ServerResponse> showActiveBucket(ServerRequest serverRequest) {
        return serverRequest.formData()
                .flatMap(value -> docBucketRepository.findByIsActiveAndDeletedAtIsNull(true)
                        .flatMap(docBucketEntity -> responseSuccessMsg("Record Fetched successfully!", docBucketEntity))
                        .switchIfEmpty(responseInfoMsg("Currently no bucket is active"))
                        .onErrorResume(ex -> responseErrorMsg("No Bucket is Active Currently.Please Contact Developer.")))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID docBucketUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    boolean status = Boolean.parseBoolean(value.getFirst("status"));

                    return docBucketRepository.findByUuidAndDeletedAtIsNull(docBucketUUID)
                            .flatMap(previousBucket -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousBucket.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                DocBucketEntity updatedDocBucketEntity = DocBucketEntity.builder()
                                        .uuid(previousBucket.getUuid())
                                        .name(previousBucket.getName())
                                        .slug(previousBucket.getSlug())
                                        .description(previousBucket.getDescription())
                                        .status(status == true ? true : false)
                                        .url(previousBucket.getUrl())
                                        .port(previousBucket.getPort())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .createdBy(previousBucket.getCreatedBy())
                                        .createdAt(previousBucket.getCreatedAt())
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousBucket.getReqCreatedIP())
                                        .reqCreatedPort(previousBucket.getReqCreatedPort())
                                        .reqCreatedBrowser(previousBucket.getReqCreatedBrowser())
                                        .reqCreatedOS(previousBucket.getReqCreatedOS())
                                        .reqCreatedDevice(previousBucket.getReqCreatedDevice())
                                        .reqCreatedReferer(previousBucket.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousBucket.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousBucket.setDeletedBy(UUID.fromString(userUUID));
                                previousBucket.setReqDeletedIP(reqIp);
                                previousBucket.setReqDeletedPort(reqPort);
                                previousBucket.setReqDeletedBrowser(reqBrowser);
                                previousBucket.setReqDeletedOS(reqOs);
                                previousBucket.setReqDeletedDevice(reqDevice);
                                previousBucket.setReqDeletedReferer(reqReferer);

                                return docBucketRepository.save(previousBucket)
                                        .then(docBucketRepository.save(updatedDocBucketEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.Please Contact Developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }

    @AuthHasPermission(value = "config_api_v1_doc-buckets_slug")
    public Mono<ServerResponse> slug(ServerRequest serverRequest) {

        String searchSlug = serverRequest.queryParam("slug").map(String::toString).orElse("");
        return serverRequest.formData()
                .flatMap(value -> docBucketRepository.findBySlugAndDeletedAtIsNull(searchSlug.trim())
                        .flatMap(value1 -> responseSlugMsg("Slug Already Exist!"))
                        .switchIfEmpty(Mono.defer(() -> {
                            if (!slugifyHelper.validateSlug(searchSlug.trim())) {
                                return responseErrorMsg("Invalid Slug");
                            } else {
                                return responseSuccessMsg("Valid Slug", value);
                            }
                        }))).switchIfEmpty(responseInfoMsg("Unable to read request")).onErrorResume(ex -> responseErrorMsg("Unable to read request"));

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
