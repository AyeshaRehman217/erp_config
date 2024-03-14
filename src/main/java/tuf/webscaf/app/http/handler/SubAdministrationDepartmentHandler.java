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
import tuf.webscaf.app.dbContext.master.entity.SubAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.master.repositry.AdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.master.repositry.AdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveSubAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAdministrationDepartmentRepository;
import tuf.webscaf.app.service.DepartmentSubDepartmentApiService;
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
@Tag(name = "subAdministrationDepartmentHandler")
public class SubAdministrationDepartmentHandler {

    @Autowired
    SlaveSubAdministrationDepartmentRepository slaveSubAdministrationDepartmentRepository;

    @Autowired
    SubAdministrationDepartmentRepository subAdministrationDepartmentRepository;

    @Autowired
    AdministrationDepartmentRepository administrationDepartmentRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    DepartmentSubDepartmentApiService deptSubDepartmentApiService;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of administration Department
        String administrationDepartmentUUID = serverRequest.queryParam("administrationDepartmentUUID").map(String::toString).orElse("").trim();

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


        //   This function returns all subAdministrationDepartment for a  given administrationDepartments
        if (!administrationDepartmentUUID.isEmpty() && !status.isEmpty()) {

            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveSubAdministrationDepartmentEntity> slaveSubAdministrationDepartmentWithAdministrationDepartmentEntityFlux = slaveSubAdministrationDepartmentRepository
                    .showSubAdministrationDepartmentsAgainstAdministrationDepartmentAndStatus(UUID.fromString(administrationDepartmentUUID), Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveSubAdministrationDepartmentWithAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(subAdministrationDepartmentEntity ->
                            slaveSubAdministrationDepartmentRepository.countSubAdministrationDepartmentAgainstAdministrationDepartmentEntityAndStatus(UUID.fromString(administrationDepartmentUUID), Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (subAdministrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", subAdministrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        } else if (!administrationDepartmentUUID.isEmpty()) {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveSubAdministrationDepartmentEntity> slaveSubAdministrationDepartmentWithAdministrationDepartmentEntityFlux = slaveSubAdministrationDepartmentRepository
                    .showSubAdministrationDepartmentsAgainstAdministrationDepartment(UUID.fromString(administrationDepartmentUUID), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveSubAdministrationDepartmentWithAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(subAdministrationDepartmentEntity ->
                            slaveSubAdministrationDepartmentRepository.countSubAdministrationDepartmentAgainstAdministrationDepartmentEntity(UUID.fromString(administrationDepartmentUUID))
                                    .flatMap(count -> {
                                        if (subAdministrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All records fetched successfully!", subAdministrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
//        This method returns all subAdministrationDepartment based on status filter
        else if (!status.isEmpty()) {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveSubAdministrationDepartmentEntity> slaveSubAdministrationDepartmentEntityFluxWithStatus = slaveSubAdministrationDepartmentRepository
                    .findAllByDeletedAtIsNullAndStatus(pageable, Boolean.valueOf(status));
            return slaveSubAdministrationDepartmentEntityFluxWithStatus
                    .collectList()
                    .flatMap(subAdministrationDepartmentEntity ->
                            slaveSubAdministrationDepartmentRepository
                                    .countByDeletedAtIsNullAndStatus(Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (subAdministrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", subAdministrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

//        Return All subAdministrationDepartment
        else {
            String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

            Flux<SlaveSubAdministrationDepartmentEntity> slaveSubAdministrationDepartmentEntityFlux = slaveSubAdministrationDepartmentRepository
                    .findAllByDeletedAtIsNull(pageable);
            return slaveSubAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(subAdministrationDepartmentEntity ->
                            slaveSubAdministrationDepartmentRepository.countByDeletedAtIsNull()
                                    .flatMap(count -> {
                                        if (subAdministrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", subAdministrationDepartmentEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }
    }

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID subAdministrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveSubAdministrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_store")
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

                    SubAdministrationDepartmentEntity subAdministrationDepartmentEntity = SubAdministrationDepartmentEntity.builder()
                            .uuid(UUID.randomUUID())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .administrationDepartmentUUID(UUID.fromString(value.getFirst("administrationDepartmentUUID").trim()))
                            .subAdministrationDepartmentUUID(UUID.fromString(value.getFirst("subAdministrationDepartmentUUID").trim()))
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

                    //check administration-department exist
                    return administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentEntity.getAdministrationDepartmentUUID())
                            //check sub-administration-department exist
                            .flatMap(administrationDepartmentEntity -> administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())
                                    .flatMap(checkSubAdministrationDepartmentEntity -> subAdministrationDepartmentRepository.findFirstBySubAdministrationDepartmentUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNull
                                                    (subAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID(), subAdministrationDepartmentEntity.getAdministrationDepartmentUUID())
                                            .flatMap(checkSubAdministrationDepartmentIsUnique -> responseInfoMsg("Sub Department already exist"))
                                            .switchIfEmpty(Mono.defer(() -> deptSubDepartmentApiService.gettingSubAdministrativeDepartmentList(subAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())
                                                    .flatMap(childList -> {
                                                        //check if the entered Administrative department and sub department UUID both are same
                                                        if (subAdministrationDepartmentEntity.getAdministrationDepartmentUUID().equals(subAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())) {
                                                            return responseInfoMsg("Sub Administrative Department cannot be same as Administrative Department");
                                                        } //   check sub-administration can't be parented of its parent
                                                        else if (childList.contains(subAdministrationDepartmentEntity.getAdministrationDepartmentUUID())) {
                                                            return responseInfoMsg(administrationDepartmentEntity.getName() + " can't be parent of " + checkSubAdministrationDepartmentEntity.getName());
                                                        } else {
                                                            return subAdministrationDepartmentRepository.save(subAdministrationDepartmentEntity)
                                                                    .flatMap(subSubAdministrationDepartmentSave -> responseSuccessMsg("Record stored successfully!", subSubAdministrationDepartmentSave))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record. There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record. Please Contact Developer."));
                                                        }
                                                    })
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("Sub Administration Department does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Sub Administration Department does not exist. Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("Administration Department does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Administration Department does not exist. Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID subAdministrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> subAdministrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentUUID)
                        .flatMap(previousSubAdministrationDepartmentEntity -> {

                            SubAdministrationDepartmentEntity updatedSubAdministrationDepartmentEntity = SubAdministrationDepartmentEntity.builder()
                                    .uuid(previousSubAdministrationDepartmentEntity.getUuid())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .administrationDepartmentUUID(UUID.fromString(value.getFirst("administrationDepartmentUUID").trim()))
                                    .subAdministrationDepartmentUUID(UUID.fromString(value.getFirst("subAdministrationDepartmentUUID").trim()))
                                    .createdBy(previousSubAdministrationDepartmentEntity.getCreatedBy())
                                    .createdAt(previousSubAdministrationDepartmentEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousSubAdministrationDepartmentEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousSubAdministrationDepartmentEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousSubAdministrationDepartmentEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousSubAdministrationDepartmentEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousSubAdministrationDepartmentEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousSubAdministrationDepartmentEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousSubAdministrationDepartmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousSubAdministrationDepartmentEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousSubAdministrationDepartmentEntity.setReqDeletedIP(reqIp);
                            previousSubAdministrationDepartmentEntity.setReqDeletedPort(reqPort);
                            previousSubAdministrationDepartmentEntity.setReqDeletedBrowser(reqBrowser);
                            previousSubAdministrationDepartmentEntity.setReqDeletedOS(reqOs);
                            previousSubAdministrationDepartmentEntity.setReqDeletedDevice(reqDevice);
                            previousSubAdministrationDepartmentEntity.setReqDeletedReferer(reqReferer);

                            //check administration-department exist
                            return administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(updatedSubAdministrationDepartmentEntity.getAdministrationDepartmentUUID())
                                    //check sub-administration-department exist
                                    .flatMap(administrationDepartmentEntity -> administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(updatedSubAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())
                                            .flatMap(checkSubAdministrationDepartmentEntity -> subAdministrationDepartmentRepository.findFirstBySubAdministrationDepartmentUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNullAndUuidIsNot
                                                            (updatedSubAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID(), updatedSubAdministrationDepartmentEntity.getAdministrationDepartmentUUID(), subAdministrationDepartmentUUID)
                                                    .flatMap(checkSubAdministrationDepartmentIsUnique -> responseInfoMsg("Sub Department already exist"))
                                                            .switchIfEmpty(Mono.defer(() -> deptSubDepartmentApiService.gettingSubAdministrativeDepartmentList(updatedSubAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())
                                                                    .flatMap(childList -> {
                                                                        //check if the entered Administrative department and sub department UUID both are same
                                                                        if (updatedSubAdministrationDepartmentEntity.getAdministrationDepartmentUUID().equals(updatedSubAdministrationDepartmentEntity.getSubAdministrationDepartmentUUID())) {
                                                                            return responseInfoMsg("Sub Administrative Department cannot be same as Administrative Department");
                                                                        } //   check sub-administration can't be parented of its parent
                                                                        else if (childList.contains(updatedSubAdministrationDepartmentEntity.getAdministrationDepartmentUUID())) {
                                                                            return responseInfoMsg(administrationDepartmentEntity.getName() + " can't be parent of " + checkSubAdministrationDepartmentEntity.getName());
                                                                        } else {
                                                                            return subAdministrationDepartmentRepository.save(previousSubAdministrationDepartmentEntity)
                                                                                    .then(subAdministrationDepartmentRepository.save(updatedSubAdministrationDepartmentEntity))
                                                                                    .flatMap(subSubAdministrationDepartmentSave -> responseSuccessMsg("Record Updated successfully!", subSubAdministrationDepartmentSave))
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record. There is something wrong please try again."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record. Please Contact Developer."));
                                                                        }
                                                                    })
                                                            ))
                                            ).switchIfEmpty(responseInfoMsg("Sub Administration Department does not exist."))
                                            .onErrorResume(ex -> responseErrorMsg("Sub Administration Department does not exist. Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Administration Department does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Administration Department does not exist. Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID subAdministrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return subAdministrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentUUID)
                .flatMap(subAdministrationDepartmentEntity -> {

                    subAdministrationDepartmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    subAdministrationDepartmentEntity.setDeletedBy(UUID.fromString(userUUID));
                    subAdministrationDepartmentEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    subAdministrationDepartmentEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    subAdministrationDepartmentEntity.setReqDeletedIP(reqIp);
                    subAdministrationDepartmentEntity.setReqDeletedPort(reqPort);
                    subAdministrationDepartmentEntity.setReqDeletedBrowser(reqBrowser);
                    subAdministrationDepartmentEntity.setReqDeletedOS(reqOs);
                    subAdministrationDepartmentEntity.setReqDeletedDevice(reqDevice);
                    subAdministrationDepartmentEntity.setReqDeletedReferer(reqReferer);

                    return subAdministrationDepartmentRepository.save(subAdministrationDepartmentEntity)
                            .flatMap(value1 -> responseSuccessMsg("Record deleted successfully", value1))
                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_sub-administration-departments_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID subAdministrationDepartmentUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return subAdministrationDepartmentRepository.findByUuidAndDeletedAtIsNull(subAdministrationDepartmentUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                SubAdministrationDepartmentEntity updatedSubAdministrationDepartmentEntity = SubAdministrationDepartmentEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .status(status == true ? true : false)
                                        .administrationDepartmentUUID(previousEntity.getAdministrationDepartmentUUID())
                                        .subAdministrationDepartmentUUID(previousEntity.getSubAdministrationDepartmentUUID())
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

                                return subAdministrationDepartmentRepository.save(previousEntity)
                                        .then(subAdministrationDepartmentRepository.save(updatedSubAdministrationDepartmentEntity))
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


