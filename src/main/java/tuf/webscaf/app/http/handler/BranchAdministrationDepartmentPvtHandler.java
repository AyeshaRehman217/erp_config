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
import tuf.webscaf.app.dbContext.master.entity.BranchAdministrationDepartmentPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.AdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.master.repositry.BranchRepository;
import tuf.webscaf.app.dbContext.master.repositry.BranchAdministrationDepartmentPvtRepository;
import tuf.webscaf.app.dbContext.master.repositry.AdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveBranchRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveBranchAdministrationDepartmentPvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAdministrationDepartmentRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "branchAdministrationDepartmentPvtHandler")
public class BranchAdministrationDepartmentPvtHandler {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    SlaveBranchRepository slaveBranchRepository;

    @Autowired
    BranchAdministrationDepartmentPvtRepository branchAdministrationDepartmentPvtRepository;

    @Autowired
    SlaveBranchAdministrationDepartmentPvtRepository slaveBranchAdministrationDepartmentPvtRepository;

    @Autowired
    AdministrationDepartmentRepository administrationDepartmentRepository;

    @Autowired
    SlaveAdministrationDepartmentRepository slaveAdministrationDepartmentRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;


    //This Function is used to check the unmapped AdministrationDepartment List Against branch UUID
    @AuthHasPermission(value = "config_api_v1_branch-administration-departments_un-mapped_show")
    public Mono<ServerResponse> showUnMappedAdministrationDepartmentAgainstBranch(ServerRequest serverRequest) {

        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size, Sort.by(d, directionProperty));


        if (!status.isEmpty()) {
            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentEntityFlux = slaveAdministrationDepartmentRepository
                    .unMappedAdministrationDepartmentListWithStatusFilterAgainstBranch
                            (branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(administrationDepartmentEntity -> slaveAdministrationDepartmentRepository.countUnMappedBranchAdministrationDepartmentRecordsWithStatus(branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        } else {
            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentEntityFlux = slaveAdministrationDepartmentRepository
                    .unMappedAdministrationDepartmentListAgainstBranch(branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAdministrationDepartmentEntityFlux
                    .collectList()
                    .flatMap(administrationDepartmentEntity -> slaveAdministrationDepartmentRepository.countUnMappedBranchAdministrationDepartmentRecords(branchUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                        if (administrationDepartmentEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", administrationDepartmentEntity, count, 0L);
                                        }
                                    }
                            )
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to Read Request.Please Contact Developer."));
        }

    }

    //This Function is used to check the mapped AdministrationDepartment List Against branch UUID
    @AuthHasPermission(value = "config_api_v1_branch-administration-departments_mapped_show")
    public Mono<ServerResponse> showMappedAdministrationDepartmentAgainstBranch(ServerRequest serverRequest) {

        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID"));

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentFlux = slaveAdministrationDepartmentRepository
                    .mappedAdministrationDepartmentListWithStatusFilterAgainstBranch(branchUUID, searchKeyWord, searchKeyWord,
                            Boolean.valueOf(status), directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAdministrationDepartmentFlux
                    .collectList()
                    .flatMap(branchEntity -> slaveAdministrationDepartmentRepository
                            .countMappedBranchAdministrationDepartmentWithStatus(branchUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (branchEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact developer."));
        } else {
            Flux<SlaveAdministrationDepartmentEntity> slaveAdministrationDepartmentFlux = slaveAdministrationDepartmentRepository
                    .mappedAdministrationDepartmentListAgainstBranch(branchUUID, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAdministrationDepartmentFlux
                    .collectList()
                    .flatMap(branchEntity -> slaveAdministrationDepartmentRepository.countMappedBranchAdministrationDepartment(branchUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (branchEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please contact developer."));
        }
    }

    //   Map administrationDepartment Against branch
    @AuthHasPermission(value = "config_api_v1_branch-administration-departments_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");

        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID"));

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");


        if (userUUID == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return serverRequest.formData()
                .flatMap(value -> branchRepository.findByUuidAndDeletedAtIsNull(branchUUID)
                        .flatMap(branchEntity -> {
                            //getting List of AdministrationDepartment Ids From Front
                            List<String> listOfAdministrationDepartments = value.get("administrationDepartmentUUID");

                            List<UUID> l_list = new ArrayList<>();

                            List<BranchAdministrationDepartmentPvtEntity> listPvt = new ArrayList<>();

                            if (value.containsKey("administrationDepartmentUUID")) {
                                listOfAdministrationDepartments.removeIf(s -> s.equals(""));

                                for (String getAdministrationDepartmentUUID : listOfAdministrationDepartments) {
                                    l_list.add(UUID.fromString(getAdministrationDepartmentUUID));
                                }

                                return branchAdministrationDepartmentPvtRepository.findAllByBranchUUIDAndAdministrationDepartmentUUIDInAndDeletedAtIsNull(branchUUID, l_list)
                                        .collectList()
                                        .flatMap(administrationDepartmentListRemove -> {
                                            //Removing Already Existing AdministrationDepartments From List to Avoid Duplicate Entries
                                            for (BranchAdministrationDepartmentPvtEntity pvtEntity : administrationDepartmentListRemove) {
                                                l_list.remove(pvtEntity.getAdministrationDepartmentUUID());
                                            }
                                            return administrationDepartmentRepository.findAllByUuidInAndDeletedAtIsNull(l_list)
                                                    .collectList().flatMap(administrationDepartmentEntityDB -> {
                                                        for (AdministrationDepartmentEntity administrationDepartment : administrationDepartmentEntityDB) {

                                                            BranchAdministrationDepartmentPvtEntity accountWithGroupEntity = BranchAdministrationDepartmentPvtEntity
                                                                    .builder()
                                                                    .branchUUID(branchUUID)
                                                                    .administrationDepartmentUUID(administrationDepartment.getUuid())
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

                                                            listPvt.add(accountWithGroupEntity);

                                                        }
                                                        return branchAdministrationDepartmentPvtRepository.saveAll(listPvt)
                                                                .collectList()
                                                                .flatMap(groupList -> branchAdministrationDepartmentPvtRepository.findAllByBranchUUIDAndDeletedAtIsNull(branchUUID)
                                                                        .collectList()
                                                                        .flatMap(mappedRecords -> {

                                                                            List<UUID> resultList = new ArrayList<>();

                                                                            for (BranchAdministrationDepartmentPvtEntity pvtEntityDB : mappedRecords) {
                                                                                resultList.add(pvtEntityDB.getAdministrationDepartmentUUID());
                                                                            }
                                                                            return administrationDepartmentRepository.findAllByUuidInAndDeletedAtIsNull(resultList)
                                                                                    .collectList()
                                                                                    .flatMap(administrationDepartmentRecords -> {
                                                                                        if (!l_list.isEmpty()) {
                                                                                            return responseSuccessMsg("Record Stored Successfully", administrationDepartmentRecords);
                                                                                        } else {
                                                                                            return responseSuccessMsg("Record Already exists", administrationDepartmentRecords);
                                                                                        }
                                                                                    });
                                                                        }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."))
                                                                ).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                    }).switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                        }).switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select AdministrationDepartments First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Teacher Child Profile does not exist"))
                        .onErrorResume(err -> responseInfoMsg("Teacher Child Profile does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    //   Delete function for branch and administrationDepartment mapping
    @AuthHasPermission(value = "config_api_v1_branch-administration-departments_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID branchUUID = UUID.fromString(serverRequest.pathVariable("branchUUID"));
        UUID administrationDepartmentUUID = UUID.fromString(serverRequest.queryParam("administrationDepartmentUUID").map(String::toString).orElse(""));
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
            return responseWarningMsg("Unknown user");
        } else if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return administrationDepartmentRepository.findByUuidAndDeletedAtIsNull(administrationDepartmentUUID)
                .flatMap(administrationDepartmentEntity -> branchAdministrationDepartmentPvtRepository
                        .findFirstByBranchUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNull(branchUUID, administrationDepartmentUUID)
                        .flatMap(branchAdministrationDepartmentPvtEntity -> {
                            branchAdministrationDepartmentPvtEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            branchAdministrationDepartmentPvtEntity.setDeletedBy(UUID.fromString(userUUID));
                            branchAdministrationDepartmentPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            branchAdministrationDepartmentPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            branchAdministrationDepartmentPvtEntity.setReqDeletedIP(reqIp);
                            branchAdministrationDepartmentPvtEntity.setReqDeletedPort(reqPort);
                            branchAdministrationDepartmentPvtEntity.setReqDeletedBrowser(reqBrowser);
                            branchAdministrationDepartmentPvtEntity.setReqDeletedOS(reqOs);
                            branchAdministrationDepartmentPvtEntity.setReqDeletedDevice(reqDevice);
                            branchAdministrationDepartmentPvtEntity.setReqDeletedReferer(reqReferer);
                            return branchAdministrationDepartmentPvtRepository.save(branchAdministrationDepartmentPvtEntity)
                                    .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", administrationDepartmentEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("AdministrationDepartment Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("AdministrationDepartment Record does not exist.Please Contact Developer."));

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
