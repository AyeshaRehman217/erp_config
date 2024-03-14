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
import tuf.webscaf.app.dbContext.master.entity.AdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.master.repositry.AdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.master.repositry.BranchAdministrationDepartmentPvtRepository;
import tuf.webscaf.app.dbContext.master.repositry.CompanyRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCompanyRepository;
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
@Tag(name = "administrationDepartmentHandler")
public class AdministrationDepartmentHandler {

    @Autowired
    SlaveAdministrationDepartmentRepository slaveAdministrationDepartmentRepository;

    @Autowired
    AdministrationDepartmentRepository administrationDepartmentRepository;

    @Autowired
    SubAdministrationDepartmentRepository subAdministrationDepartmentRepository;

    @Autowired
    BranchAdministrationDepartmentPvtRepository branchAdministrationDepartmentPvtRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    SlaveCompanyRepository slaveCompanyRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    SlugifyHelper slugifyHelper;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_administration-departments_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company uuid
        String companyUUID = serverRequest.queryParam("companyUUID").map(String::toString).orElse("").trim();

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


        //   This function returns all administration departments for a  given company
        if (!companyUUID.isEmpty() && !status.isEmpty()) {

            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentWithCompanyEntityFlux = slaveAdministrationDepartmentRepository
                    .showAdministrationDepartmentsAgainstCompanyAndStatus(UUID.fromString(companyUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveAdministrationDepartmentWithCompanyEntityFlux
                    .collectList()
                    .flatMap(administrationDepartmentEntity ->
                            slaveAdministrationDepartmentRepository.countAdministrationDepartmentAgainstCompanyEntityAndStatus(UUID.fromString(companyUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records Fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else if (!companyUUID.isEmpty()) {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentWithCompanyEntityFlux = slaveAdministrationDepartmentRepository
                    .showAdministrationDepartmentsAgainstCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveAdministrationDepartmentWithCompanyEntityFlux
                    .collectList()
                    .flatMap(administrationDepartmentEntity ->
                            slaveAdministrationDepartmentRepository.countAdministrationDepartmentAgainstCompanyEntity(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records Fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
        // This method returns all administration departments based on status filter
        else if (!status.isEmpty()) {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentEntityFluxWithStatus = slaveAdministrationDepartmentRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
            return slaveAdministrationDepartmentEntityFluxWithStatus
                    .collectList()
                    .flatMap(administrationDepartmentEntity ->
                            slaveAdministrationDepartmentRepository
                                    .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                            (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records Fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

        // Return All administration departments
        else {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentEntityFlux = slaveAdministrationDepartmentRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(administrationDepartmentEntity ->
                            slaveAdministrationDepartmentRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records Fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
    }

    @AuthHasPermission(value = "config_api_v1_administration-departments_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID administrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveAdministrationDepartmentRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    @AuthHasPermission(value = "config_api_v1_administration-departments_store")
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

                    AdministrationDepartmentEntity administrationDepartmentEntity = AdministrationDepartmentEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                            .description(value.getFirst("description").trim())
                            .code(value.getFirst("code").trim())
                            .shortName(value.getFirst("shortName").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .companyUUID(UUID.fromString(value.getFirst("companyUUID").trim()))
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

                    // check if company uuid exists
                    return companyRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentEntity.getCompanyUUID())
                            // check if name is unique
                            .flatMap(checkName -> administrationDepartmentRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(administrationDepartmentEntity.getName(), administrationDepartmentEntity.getCompanyUUID())
                                    .flatMap(name -> responseInfoMsg("Name Already Exist."))
                                    // check if code is unique
                                    .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstByCodeIgnoreCaseAndDeletedAtIsNull(administrationDepartmentEntity.getCode())
                                            .flatMap(CheckName -> responseInfoMsg("Code Already Exist."))))
                                    // check if short name is unique
                                    .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstByShortNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(administrationDepartmentEntity.getShortName(), administrationDepartmentEntity.getCompanyUUID())
                                            .flatMap(CheckName -> responseInfoMsg("Short Name Already Exist."))))
                                    // check if slug is unique
                                    .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(administrationDepartmentEntity.getSlug(), administrationDepartmentEntity.getCompanyUUID())
                                            .flatMap(CheckName -> responseInfoMsg("Slug Already Exist."))))
                                    .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.save(administrationDepartmentEntity))
                                            .flatMap(administrationDepartmentSave -> responseSuccessMsg("Record Stored Successfully", administrationDepartmentEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to store record.Please Contact Developer.")))
                            ).switchIfEmpty(responseInfoMsg("Company does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Company does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    @AuthHasPermission(value = "config_api_v1_administration-departments_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID administrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
            return responseErrorMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseErrorMsg("Unknown user");

        }

        return serverRequest.formData()
                .flatMap(value -> administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentUUID)
                        .flatMap(previousAdministrationDepartmentEntity -> {

                            AdministrationDepartmentEntity updatedAdministrationDepartmentEntity = AdministrationDepartmentEntity.builder()
                                    .uuid(previousAdministrationDepartmentEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .slug(slugifyHelper.slugify(value.getFirst("name").trim()))
                                    .description(value.getFirst("description").trim())
                                    .code(value.getFirst("code").trim())
                                    .shortName(value.getFirst("shortName").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .companyUUID(UUID.fromString(value.getFirst("companyUUID").trim()))
                                    .createdBy(previousAdministrationDepartmentEntity.getCreatedBy())
                                    .createdAt(previousAdministrationDepartmentEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousAdministrationDepartmentEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousAdministrationDepartmentEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousAdministrationDepartmentEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousAdministrationDepartmentEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousAdministrationDepartmentEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousAdministrationDepartmentEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousAdministrationDepartmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousAdministrationDepartmentEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousAdministrationDepartmentEntity.setReqDeletedIP(reqIp);
                            previousAdministrationDepartmentEntity.setReqDeletedPort(reqPort);
                            previousAdministrationDepartmentEntity.setReqDeletedBrowser(reqBrowser);
                            previousAdministrationDepartmentEntity.setReqDeletedOS(reqOs);
                            previousAdministrationDepartmentEntity.setReqDeletedDevice(reqDevice);
                            previousAdministrationDepartmentEntity.setReqDeletedReferer(reqReferer);

                            // check if company uuid exists
                            return companyRepository.findByUuidAndDeletedAtIsNull(updatedAdministrationDepartmentEntity.getCompanyUUID())
                                    // check if name is unique
                                    .flatMap(checkName -> administrationDepartmentRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(updatedAdministrationDepartmentEntity.getName(), updatedAdministrationDepartmentEntity.getCompanyUUID(), updatedAdministrationDepartmentEntity.getUuid())
                                            .flatMap(name -> responseInfoMsg("Name Already Exist."))
                                            // check if code is unique
                                            .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedAdministrationDepartmentEntity.getCode(), updatedAdministrationDepartmentEntity.getUuid())
                                                    .flatMap(CheckName -> responseInfoMsg("Code Already Exist."))))
                                            // check if short name is unique
                                            .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstByShortNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(updatedAdministrationDepartmentEntity.getShortName(), updatedAdministrationDepartmentEntity.getCompanyUUID(), updatedAdministrationDepartmentEntity.getUuid())
                                                    .flatMap(CheckName -> responseInfoMsg("Short Name Already Exist."))))
                                            // check if slug is unique
                                            .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(updatedAdministrationDepartmentEntity.getSlug(), updatedAdministrationDepartmentEntity.getCompanyUUID(), updatedAdministrationDepartmentEntity.getUuid())
                                                    .flatMap(CheckName -> responseInfoMsg("Slug Already Exist."))))
                                            .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.save(previousAdministrationDepartmentEntity)
                                                    .then(administrationDepartmentRepository.save(updatedAdministrationDepartmentEntity))
                                                    .flatMap(administrationDepartmentSave -> responseSuccessMsg("Record Updated Successfully", administrationDepartmentSave))
                                                    .switchIfEmpty(responseInfoMsg("Unable to update record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to update record.Please Contact Developer."))
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("Company does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Company does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    @AuthHasPermission(value = "config_api_v1_administration-departments_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID administrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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

        return administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentUUID)
                // check if administration department exist in sub administration departments
                .flatMap(administrationDepartmentEntity -> subAdministrationDepartmentRepository.findFirstByAdministrationDepartmentUUIDAndDeletedAtIsNull(administrationDepartmentEntity.getUuid())
                        .flatMap(configEntity -> responseInfoMsg("Unable to delete record as the reference exists"))
                        // check if administration department exist in branch administration department pvt
                        .switchIfEmpty(Mono.defer(() -> branchAdministrationDepartmentPvtRepository.findFirstByAdministrationDepartmentUUIDAndDeletedAtIsNull(administrationDepartmentEntity.getUuid())
                                .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete record as the reference exists"))))
                        .switchIfEmpty(Mono.defer(() -> {

                            administrationDepartmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            administrationDepartmentEntity.setDeletedBy(UUID.fromString(userUUID));
                            administrationDepartmentEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            administrationDepartmentEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            administrationDepartmentEntity.setReqDeletedIP(reqIp);
                            administrationDepartmentEntity.setReqDeletedPort(reqPort);
                            administrationDepartmentEntity.setReqDeletedBrowser(reqBrowser);
                            administrationDepartmentEntity.setReqDeletedOS(reqOs);
                            administrationDepartmentEntity.setReqDeletedDevice(reqDevice);
                            administrationDepartmentEntity.setReqDeletedReferer(reqReferer);

                            return administrationDepartmentRepository.save(administrationDepartmentEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record.There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please Contact Developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_administration-departments_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID administrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
        return serverRequest.formData()
                .flatMap(value -> {

                    boolean status = Boolean.parseBoolean(value.getFirst("status"));

                    return administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                AdministrationDepartmentEntity updatedAdministrationDepartmentEntity = AdministrationDepartmentEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .slug(previousEntity.getSlug())
                                        .description(previousEntity.getDescription())
                                        .code(previousEntity.getCode())
                                        .shortName(previousEntity.getShortName())
                                        .status(status == true ? true : false)
                                        .companyUUID(previousEntity.getCompanyUUID())
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

                                return administrationDepartmentRepository.save(previousEntity)
                                        .then(administrationDepartmentRepository.save(updatedAdministrationDepartmentEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status updated successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status"))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status.There is something wrong please try again."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist.Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request!"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please contact developer."));
    }


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


