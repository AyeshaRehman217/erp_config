package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.BranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveBranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveBranchProfileRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveBranchRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCompanyRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@Tag(name = "branchHandler")
public class BranchHandler {

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    SlaveBranchProfileRepository slaveBranchProfileRepository;

    @Autowired
    BranchAdministrationDepartmentPvtRepository branchAdministrationDepartmentPvtRepository;

    @Autowired
    SlaveBranchRepository slaveBranchRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    LanguageRepository languageRepository;

    @Autowired
    SlaveCompanyRepository slaveCompanyRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${server.erp_account_module.uri}")
    private String accountUri;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.erp_employee_financial_module.uri}")
    private String employeeFinancialModuleUri;

    @Value("${server.zone}")
    private String zone;


    @AuthHasPermission(value = "config_api_v1_branches_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        //Optional Query Parameter Based of Company
        String companyUUID = serverRequest.queryParam("companyUUID").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (size > 100) {
            size = 100;
        }

        Pageable pageable = PageRequest.of(page, size);

//        Return All branches
        if (!companyUUID.isEmpty() && !status.isEmpty()) {
            Flux<SlaveBranchWithBranchProfileDto> slaveCompanyWithBranchEntityFlux = slaveBranchRepository
                    .companyWithBranchIndexAndCompanyAndStatusFilter(UUID.fromString(companyUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveCompanyWithBranchEntityFlux
                    .collectList()
                    .flatMap(branchEntity ->
                            slaveBranchRepository.countBranchAgainstCompanyAndStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord, UUID.fromString(companyUUID))
                                    .flatMap(count -> {
                                        if (branchEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else if (!companyUUID.isEmpty()) {
            Flux<SlaveBranchWithBranchProfileDto> slaveCompanyWithBranchEntityFlux = slaveBranchRepository
                    .companyWithBranchIndexAndCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

            return slaveCompanyWithBranchEntityFlux
                    .collectList()
                    .flatMap(branchEntity ->
                            slaveBranchRepository.countBranchAgainstCompany(searchKeyWord, searchKeyWord, UUID.fromString(companyUUID))
                                    .flatMap(count -> {
                                        if (branchEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

//     Return All branches with status filter
        else if (!status.isEmpty()) {
            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFlux = slaveBranchRepository
                    .branchWithBranchProfileIndexWithStatusFilter(searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveBranchEntityFlux
                    .collectList()
                    .flatMap(branchEntity ->
                            slaveBranchRepository.countBranchWithStatusFilter(searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                                    .flatMap(count -> {
                                        if (branchEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully!", branchEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));

        }

//      Return All branches
        else {
            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFlux = slaveBranchRepository
                    .branchWithBranchProfileIndex(searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveBranchEntityFlux
                    .collectList()
                    .flatMap(branchEntity ->
                            slaveBranchRepository.countBranchWithOutStatusFilter(searchKeyWord, searchKeyWord)
                                    .flatMap(count -> {
                                        if (branchEntity.isEmpty()) {
                                            return responseIndexInfoMsg("Record does not exist", count, 0L);

                                        } else {
                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                        }
                                    })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

    }

    //check if the Branch exists for the entered company
    @AuthHasPermission(value = "config_api_v1_branch-for-company_show")
    public Mono<ServerResponse> showCompanyBranch(ServerRequest serverRequest) {
        final UUID companyUUID = UUID.fromString(serverRequest.pathVariable("companyUUID"));
        UUID branchUUID = UUID.fromString(serverRequest.queryParam("branchUUID").map(String::toString).orElse(""));

        return slaveBranchRepository.findFirstByCompanyUUIDAndUuidAndDeletedAtIsNull(companyUUID, branchUUID)
                .flatMap(value1 -> responseSuccessMsg("Record fetched successfully", value1))
                .switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_branches_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID branchUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveBranchRepository.ShowByUuidBranchWithBranchProfile(branchUUID)
                .flatMap(branchEntity -> responseSuccessMsg("Record Fetched Successfully", branchEntity))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_uuid_company_branches_show")
    public Mono<ServerResponse> showBranchesListAgainstCompany(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString((serverRequest.pathVariable("companyUUID")));

        return slaveBranchRepository.findAllByCompanyUUIDAndDeletedAtIsNull(companyUUID)
                .collectList()
                .flatMap(branchEntity -> {
                    List<UUID> branchUUIDList = new ArrayList<>();

                    for (SlaveBranchEntity branch : branchEntity) {
                        branchUUIDList.add(branch.getUuid());
                    }
                    if (!branchUUIDList.isEmpty()) {
                        return responseSuccessMsg("Record Fetched Successfully!", branchUUIDList);
                    } else {
                        return responseSuccessMsg("Record Does Not exist.", branchUUIDList);
                    }

                })
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }
//
//    //  Check branches(whether exist or not) for voucher id
//    public Mono<ServerResponse> showBranchList(ServerRequest serverRequest) {
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    List<String> listOfBranches = value.get("branch");
//                    if (value.containsKey("branch")) {
//                        listOfBranches.removeIf(s -> s.equals(""));
//                    }
//                    List<Long> l_list = new ArrayList<>();
//                    if (value.get("branch") != null) {
//                        for (String branchId : listOfBranches) {
//                            l_list.add(Long.valueOf(branchId));
//                        }
//                    }
//                    return branchRepository.findAllByIdInAndDeletedAtIsNull(l_list)
//                            .collectList()
//                            .flatMap(branchEntities -> responseSuccessMsg("Records Fetched Successfully", branchEntities));
//                }).switchIfEmpty(responseInfoMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
//    }

    //This Function Is used By Account Module to Check if Branch UUID exists in Financial Voucher Mapping
    @AuthHasPermission(value = "config_api_v1_branches_uuid_list_show")
    public Mono<ServerResponse> showBranchListInAccountModule(ServerRequest serverRequest) {

        List<String> uuids = serverRequest.queryParams().get("uuid");

        //This is Branch List to paas in the query
        List<UUID> branchList = new ArrayList<>();
        if (uuids != null) {
            for (String branch : uuids) {
                branchList.add(UUID.fromString(branch));
            }
        }

        return branchRepository.getUUIDsOfExitingRecords(branchList)
                .collectList()
                .flatMap(branchUUIDs -> responseSuccessMsg("Records Fetched Successfully", branchUUIDs))
                .switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }


    //    Show mapped branches for voucher uuid
    @AuthHasPermission(value = "config_api_v1_branches_voucher_mapped_show")
    public Mono<ServerResponse> showMappedRecordsAgainstVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company
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


        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-branch/list/show/", voucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {

                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Creating an Empty String for Transaction List
                    String branchList = "";
                    //Iterating Over The Transaction List
                    for (UUID val : listOfUUIDs) {
                        //Getting the last index of list size
                        if (listOfUUIDs.indexOf(val) == listOfUUIDs.size() - 1) {
                            //Adding '' around each value of list
                            branchList = branchList + "'" + val + "'";
                        } else {
                            //Separating the list values with comma
                            branchList = branchList + "'" + val + "' ,";
                        }
                    }

//            Check if Company is Present, then return all mapped records according to given company
                    if (!listOfUUIDs.isEmpty()) {
                        if (!companyUUID.isEmpty()) {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    branchAgainstCompanyAndVouchers(UUID.fromString(companyUUID), branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countBranchAgainstCompanyAndVoucher(UUID.fromString(companyUUID), listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        } else {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    branchAgainstVouchers(branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countBranchAgainstVoucher(listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

                        }
                    } else {
                        return responseInfoMsg("Record Does Not Exist");
                    }

                }).switchIfEmpty(responseInfoMsg("Record Does Not Exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does Not Exist.Please Contact Developer."));
    }

    //    Show  unmapped branches for voucher uuid
    @AuthHasPermission(value = "config_api_v1_branches_voucher_un-mapped_show")
    public Mono<ServerResponse> showUnMappedRecordsAgainstVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company
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
        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-branch/list/show/", voucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    //Creating an Empty String for Transaction List
                    String branchList = "";
                    //Iterating Over The Transaction List
                    for (UUID val : listOfUUIDs) {
                        //Getting the last index of list size
                        if (listOfUUIDs.indexOf(val) == listOfUUIDs.size() - 1) {
                            //Adding '' around each value of list
                            branchList = branchList + "'" + val + "'";
                        } else {
                            //Separating the list values with comma
                            branchList = branchList + "'" + val + "' ,";
                        }
                    }

                    // Check if Company is Present, then return all mapped records according to given company
                    if (!companyUUID.isEmpty()) {
                        if (!listOfUUIDs.isEmpty()) {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstBranchAndCompany(UUID.fromString(companyUUID), branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstBranchAndCompany(UUID.fromString(companyUUID), listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if branch uuids list is empty
                        else {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    } else {

                        if (!listOfUUIDs.isEmpty()) {
                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstBranch(branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstBranch(listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if branch uuids list is empty
                        else {
                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherList(searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherList(searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    }

                }).switchIfEmpty(responseInfoMsg("Record Does Not Exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does Not Exist.Please Contact Developer."));
    }

    //    Show mapped branches for voucher uuid
    @AuthHasPermission(value = "config_api_v1_branches_emp-financial-voucher_mapped_show")
    public Mono<ServerResponse> showMappedRecordsAgainstEmpFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        final UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company
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


        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-branches/list/show/", financialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {

                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Creating an Empty String for Transaction List
                    String branchList = "";
                    //Iterating Over The Transaction List
                    for (UUID val : listOfUUIDs) {
                        //Getting the last index of list size
                        if (listOfUUIDs.indexOf(val) == listOfUUIDs.size() - 1) {
                            //Adding '' around each value of list
                            branchList = branchList + "'" + val + "'";
                        } else {
                            //Separating the list values with comma
                            branchList = branchList + "'" + val + "' ,";
                        }
                    }

//            Check if Company is Present, then return all mapped records according to given company
                    if (!listOfUUIDs.isEmpty()) {
                        if (!companyUUID.isEmpty()) {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    branchAgainstCompanyAndVouchers(UUID.fromString(companyUUID), branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countBranchAgainstCompanyAndVoucher(UUID.fromString(companyUUID), listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        } else {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    branchAgainstVouchers(branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countBranchAgainstVoucher(listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

                        }
                    } else {
                        return responseInfoMsg("Record Does Not Exist");
                    }

                }).switchIfEmpty(responseInfoMsg("Record Does Not Exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does Not Exist.Please Contact Developer."));
    }

    //    This route returns branches that are not mapped for given financial Voucher And Company
    @AuthHasPermission(value = "config_api_v1_branches_emp_financial_voucher_un_mapped_show")
    public Mono<ServerResponse> showUnMappedRecordsAgainstEmpFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        final UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Company
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
        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-branches/list/show/", financialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));


                    //Creating an Empty String for Transaction List
                    String branchList = "";
                    //Iterating Over The Transaction List
                    for (UUID val : listOfUUIDs) {
                        //Getting the last index of list size
                        if (listOfUUIDs.indexOf(val) == listOfUUIDs.size() - 1) {
                            //Adding '' around each value of list
                            branchList = branchList + "'" + val + "'";
                        } else {
                            //Separating the list values with comma
                            branchList = branchList + "'" + val + "' ,";
                        }
                    }

                    // Check if Company is Present, then return all mapped records according to given company
                    if (!companyUUID.isEmpty()) {
                        if (!listOfUUIDs.isEmpty()) {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstBranchAndCompany(UUID.fromString(companyUUID), branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstBranchAndCompany(UUID.fromString(companyUUID), listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if branch uuids list is empty
                        else {

                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstCompany(UUID.fromString(companyUUID), searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    } else {

                        if (!listOfUUIDs.isEmpty()) {
                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherListAgainstBranch(branchList, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherListAgainstBranch(listOfUUIDs, searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }

                        // if branch uuids list is empty
                        else {
                            Flux<SlaveBranchWithBranchProfileDto> slaveBranchEntityFluxWithCompany = slaveBranchRepository.
                                    unMappedVoucherList(searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);

                            return slaveBranchEntityFluxWithCompany
                                    .collectList()
                                    .flatMap(branchEntity ->
                                            slaveBranchRepository
                                                    .countUnMappedVoucherList(searchKeyWord, searchKeyWord)
                                                    .flatMap(count -> {
                                                        if (branchEntity.isEmpty()) {
                                                            return responseIndexInfoMsg("Record does not exist", count, 0L);
                                                        } else {
                                                            return responseIndexSuccessMsg("All Records fetched successfully", branchEntity, count, 0L);
                                                        }
                                                    })
                                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
                        }
                    }

                }).switchIfEmpty(responseInfoMsg("Record Does Not Exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does Not Exist.Please Contact Developer."));
    }


    // Used to store branch record and build branch with branch profile dto, then return response
    public Mono<ServerResponse> storeBranchRecord(BranchEntity branchEntity, BranchProfileEntity branchProfileEntity, String reqCompanyUUID, String reqIp, String reqPort, String reqBrowser, String reqOs, String reqDevice, String reqReferer) {
        return branchRepository.save(branchEntity)
                .flatMap(branchRecord -> {


                    BranchWithBranchProfileDto branchWithBranchProfileDto = BranchWithBranchProfileDto.builder()
                            .id(branchRecord.getId())
                            .uuid(branchRecord.getUuid())
                            .version(branchRecord.getVersion())
                            .status(branchRecord.getStatus())
                            .name(branchRecord.getName())
                            .description(branchRecord.getDescription())
                            .establishmentDate(branchProfileEntity.getEstablishmentDate())
                            .branchProfileUUID(branchProfileEntity.getUuid())
                            .languageUUID(branchProfileEntity.getLanguageUUID())
                            .locationUUID(branchProfileEntity.getLocationUUID())
                            .companyUUID(branchRecord.getCompanyUUID())
                            .createdBy(branchRecord.getCreatedBy())
                            .createdAt(branchRecord.getCreatedAt())
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .archived(branchRecord.getArchived())
                            .deletable(branchRecord.getDeletable())
                            .editable(branchRecord.getEditable())
                            .build();

                    return responseSuccessMsg("Record Stored Successfully", branchWithBranchProfileDto)
                            .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to store record. Please contact developer."));
                });
    }

    // Used to delete branch profile entity if branch record is not stored
    public Mono<ServerResponse> deleteBranchProfile(UUID branchProfileUUID, String msg) {
        return branchProfileRepository.findByUuidAndDeletedAtIsNull(branchProfileUUID)
                .flatMap(delete -> branchProfileRepository.delete(delete)
                        .flatMap(delMsg -> responseInfoMsg("Branch does not exist"))
                        .switchIfEmpty(responseInfoMsg(msg + "There is something wrong please try again."))
                        .onErrorResume(ex -> responseErrorMsg(msg + "Please Contact Developer."))
                );
    }

    @AuthHasPermission(value = "config_api_v1_branches_store")
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

                    // create branch profile Entity
                    BranchProfileEntity branchProfileEntity = BranchProfileEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .establishmentDate(LocalDateTime.parse((value.getFirst("establishmentDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .locationUUID(UUID.fromString(value.getFirst("locationUUID").trim()))
                            .languageUUID(UUID.fromString(value.getFirst("languageUUID").trim()))
                            .createdBy(UUID.fromString(userUUID))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    // MultiValueMap used to set request form data to post request
                    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

                    // set campus fields to request form data
//                    formData.add("companyUUID", value.getFirst("companyUUID"));
                    formData.add("establishmentDate", value.getFirst("establishmentDate"));
                    formData.add("locationUUID", value.getFirst("locationUUID"));
                    formData.add("languageUUID", value.getFirst("languageUUID"));
                    formData.add("name", value.getFirst("name"));
                    formData.add("description", value.getFirst("description"));
                    formData.add("status", value.getFirst("status"));

                    //check language
                    return languageRepository.findByUuidAndDeletedAtIsNull(branchProfileEntity.getLanguageUUID())
                            // check location uuid
                            .flatMap(languageEntity -> locationRepository.findByUuidAndDeletedAtIsNull(branchProfileEntity.getLocationUUID())
                                    // store branch profile
                                    .flatMap(locationEntity -> branchProfileRepository.save(branchProfileEntity)
                                            // Build branch Entity
                                            .flatMap(branchProfileEntityData -> {
                                                BranchEntity branchEntity = BranchEntity
                                                        .builder()
                                                        .uuid(UUID.randomUUID())
                                                        .name(value.getFirst("name").trim())
                                                        .status(Boolean.valueOf(value.getFirst("status")))
                                                        .description(value.getFirst("description").trim())
                                                        .companyUUID(UUID.fromString(value.getFirst("companyUUID").trim()))
                                                        .branchProfileUUID(branchProfileEntityData.getUuid())
                                                        .createdBy(UUID.fromString(userUUID))
                                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                        .reqCreatedIP(reqIp)
                                                        .reqCreatedPort(reqPort)
                                                        .reqCreatedBrowser(reqBrowser)
                                                        .reqCreatedOS(reqOs)
                                                        .reqCreatedDevice(reqDevice)
                                                        .reqCreatedReferer(reqReferer)
                                                        .build();

                                                // check condition company uuid exists or not
                                                return companyRepository.findByUuidAndDeletedAtIsNull(branchEntity.getCompanyUUID())
                                                        // check name is unique or not
                                                        .flatMap(companyEntity -> branchRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(branchEntity.getName())
                                                                .flatMap(nameExists -> branchProfileRepository.findByUuidAndDeletedAtIsNull(branchProfileEntity.getUuid())
                                                                        // delete branch profile when name already exist(because it stores data in branch profile)
                                                                        .flatMap(delete -> branchProfileRepository.delete(delete)
                                                                                .flatMap(delMsg -> responseInfoMsg("Branch not stored"))
                                                                                .switchIfEmpty(responseInfoMsg("Name already exist"))))
                                                                // When name is unique
                                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getData(academicUri + "api/v1/info/show", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                        // checks if academic module UUID exists
                                                                        .flatMap(moduleJsonNode -> apiCallService.getModuleUUID(moduleJsonNode)
                                                                                // if academic module UUID exists, store campus record with branch
                                                                                .flatMap(moduleUUID -> {
                                                                                    // add company uuid to post request form data
                                                                                    formData.add("companyUUID", companyEntity.getUuid().toString());

                                                                                    // store campus record in academic module
                                                                                    return apiCallService.postDataList(formData, academicUri + "api/v1/campuses/store", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                            .flatMap(jsonNode -> apiCallService.getUUID(jsonNode)
                                                                                                    .flatMap(campus ->
                                                                                                    {
                                                                                                        return storeBranchRecord(branchEntity, branchProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer);
                                                                                                    })
                                                                                                    .switchIfEmpty(deleteBranchProfile(branchProfileEntity.getUuid(), "Unable to store record."))
                                                                                                    .onErrorResume(err -> deleteBranchProfile(branchProfileEntity.getUuid(), "Unable to store record."))
                                                                                            ).switchIfEmpty(deleteBranchProfile(branchProfileEntity.getUuid(), "Unable to store record."))
                                                                                            .onErrorResume(err -> deleteBranchProfile(branchProfileEntity.getUuid(), "Unable to store record."));

                                                                                    //else if academic module UUID does not exist store branch only
                                                                                }).switchIfEmpty(storeBranchRecord(branchEntity, branchProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                .onErrorResume(err -> storeBranchRecord(branchEntity, branchProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                        ).switchIfEmpty(storeBranchRecord(branchEntity, branchProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                        .onErrorResume(err -> storeBranchRecord(branchEntity, branchProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                ))
                                                        ).switchIfEmpty(deleteBranchProfile(branchProfileEntity.getUuid(), "Requested company does not exist"));
                                            }).switchIfEmpty(responseInfoMsg("Branch does not stored. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Branch not stored. Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Location does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Location does not exist. Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("Language does not exist"))
                            .onErrorResume(ex -> responseErrorMsg("Language does not exist. Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please Contact Developer."));

    }


    // Used to update branch record and build branch with branch profile dto, then return response
    public Mono<ServerResponse> updateBranchRecord(BranchEntity branchEntity, BranchEntity previousBranchEntity, BranchProfileEntity branchProfileEntity, BranchProfileEntity previousBranchProfileEntity, String reqCompanyUUID, String reqIp, String reqPort, String reqBrowser, String reqOs, String reqDevice, String reqReferer) {

        return branchRepository.save(previousBranchEntity)
                .then(branchRepository.save(branchEntity))
                .flatMap(branchRecord -> branchProfileRepository.save(previousBranchProfileEntity)
                        .then(branchProfileRepository.save(branchProfileEntity))
                        .flatMap(branchProfileRecord -> {

                            BranchWithBranchProfileDto branchWithBranchProfileDto = BranchWithBranchProfileDto.builder()
                                    .id(branchRecord.getId())
                                    .uuid(branchRecord.getUuid())
                                    .version(branchRecord.getVersion())
                                    .status(branchRecord.getStatus())
                                    .name(branchRecord.getName())
                                    .description(branchRecord.getDescription())
                                    .establishmentDate(branchProfileRecord.getEstablishmentDate())
                                    .branchProfileUUID(branchProfileRecord.getUuid())
                                    .languageUUID(branchProfileRecord.getLanguageUUID())
                                    .locationUUID(branchProfileRecord.getLocationUUID())
                                    .companyUUID(branchRecord.getCompanyUUID())
                                    .createdBy(branchRecord.getCreatedBy())
                                    .createdAt(branchRecord.getCreatedAt())
                                    .updatedBy(branchRecord.getUpdatedBy())
                                    .updatedAt(branchRecord.getUpdatedAt())
                                    .reqCreatedIP(branchRecord.getReqCreatedIP())
                                    .reqCreatedPort(branchRecord.getReqCreatedPort())
                                    .reqCreatedBrowser(branchRecord.getReqCreatedBrowser())
                                    .reqCreatedOS(branchRecord.getReqCreatedOS())
                                    .reqCreatedDevice(branchRecord.getReqCreatedDevice())
                                    .reqCreatedReferer(branchRecord.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .archived(branchRecord.getArchived())
                                    .deletable(branchRecord.getDeletable())
                                    .editable(branchRecord.getEditable())
                                    .build();

                            return responseSuccessMsg("Record Updated Successfully", branchWithBranchProfileDto)
                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."));
                        }));
    }

    public Mono<BranchProfileEntity> updateBranchProfileEntity(ServerRequest serverRequest, BranchProfileEntity previousBranchProfileEntity, UUID userUUID, String reqCompanyUUID, String reqIp, String reqPort, String reqBrowser, String reqOs, String reqDevice, String reqReferer) {

        return serverRequest
                .formData()
                .flatMap(value -> {

                    // create branch profile Entity
                    BranchProfileEntity updatedBranchProfileEntity = BranchProfileEntity.builder()
                            .uuid(previousBranchProfileEntity.getUuid())
                            .establishmentDate(LocalDateTime.parse((value.getFirst("establishmentDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .locationUUID(UUID.fromString(value.getFirst("locationUUID").trim()))
                            .languageUUID(UUID.fromString(value.getFirst("languageUUID").trim()))
                            .createdBy(previousBranchProfileEntity.getCreatedBy())
                            .createdAt(previousBranchProfileEntity.getCreatedAt())
                            .updatedBy(userUUID)
                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                            .reqCreatedIP(previousBranchProfileEntity.getReqCreatedIP())
                            .reqCreatedPort(previousBranchProfileEntity.getReqCreatedPort())
                            .reqCreatedBrowser(previousBranchProfileEntity.getReqCreatedBrowser())
                            .reqCreatedOS(previousBranchProfileEntity.getReqCreatedOS())
                            .reqCreatedDevice(previousBranchProfileEntity.getReqCreatedDevice())
                            .reqCreatedReferer(previousBranchProfileEntity.getReqCreatedReferer())
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqUpdatedIP(reqIp)
                            .reqUpdatedPort(reqPort)
                            .reqUpdatedBrowser(reqBrowser)
                            .reqUpdatedOS(reqOs)
                            .reqUpdatedDevice(reqDevice)
                            .reqUpdatedReferer(reqReferer)
                            .build();

                    previousBranchProfileEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    previousBranchProfileEntity.setDeletedBy(userUUID);
                    previousBranchProfileEntity.setReqDeletedIP(reqIp);
                    previousBranchProfileEntity.setReqDeletedPort(reqPort);
                    previousBranchProfileEntity.setReqDeletedBrowser(reqBrowser);
                    previousBranchProfileEntity.setReqDeletedOS(reqOs);
                    previousBranchProfileEntity.setReqDeletedDevice(reqDevice);
                    previousBranchProfileEntity.setReqDeletedReferer(reqReferer);

                    return Mono.just(updatedBranchProfileEntity);
                });
    }

    @AuthHasPermission(value = "config_api_v1_branches_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {

        final UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> branchRepository.findByUuidAndDeletedAtIsNull(branchUUID)
                        .flatMap(previousBranchEntity -> {

                            //The previous Branch Name need to Compare with the Campus with the same name and get Record to Updated
                            String previousCampusName = previousBranchEntity.getName();

                            //creating New Branch Entity
                            BranchEntity updatedBranchEntity = BranchEntity
                                    .builder()
                                    .uuid(previousBranchEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .description(value.getFirst("description").trim())
                                    .companyUUID(UUID.fromString(value.getFirst("companyUUID").trim()))
                                    .branchProfileUUID(previousBranchEntity.getBranchProfileUUID())
                                    .createdBy(previousBranchEntity.getCreatedBy())
                                    .createdAt(previousBranchEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousBranchEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousBranchEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousBranchEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousBranchEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousBranchEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousBranchEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            //Deleting the previous Branch Entity
                            previousBranchEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousBranchEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousBranchEntity.setReqDeletedIP(reqIp);
                            previousBranchEntity.setReqDeletedPort(reqPort);
                            previousBranchEntity.setReqDeletedBrowser(reqBrowser);
                            previousBranchEntity.setReqDeletedOS(reqOs);
                            previousBranchEntity.setReqDeletedDevice(reqDevice);
                            previousBranchEntity.setReqDeletedReferer(reqReferer);

                            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

                            // set campus fields to request form data
                            formData.add("companyUUID", value.getFirst("companyUUID"));
                            formData.add("establishmentDate", value.getFirst("establishmentDate"));
                            formData.add("locationUUID", value.getFirst("locationUUID"));
                            formData.add("languageUUID", value.getFirst("languageUUID"));
                            formData.add("name", value.getFirst("name"));
                            formData.add("description", value.getFirst("description"));
                            formData.add("status", value.getFirst("status"));

                            return branchProfileRepository.findByUuidAndDeletedAtIsNull(updatedBranchEntity.getBranchProfileUUID())
                                    .flatMap(previousProfileEntity -> {

                                        //check if Company Exists in Companies table
                                        return companyRepository.findByUuidAndDeletedAtIsNull(updatedBranchEntity.getCompanyUUID())
                                                .flatMap(companyEntity -> updateBranchProfileEntity(serverRequest, previousProfileEntity, UUID.fromString(userUUID), reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer)
                                                        .flatMap(newBranchProfile -> languageRepository.findByUuidAndDeletedAtIsNull(newBranchProfile.getLanguageUUID())
                                                                //check if location Exists
                                                                .flatMap(languageEntity -> locationRepository.findByUuidAndDeletedAtIsNull(newBranchProfile.getLocationUUID())
                                                                        // check name already exist
                                                                        .flatMap(locationEntity -> branchRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedBranchEntity.getName(), updatedBranchEntity.getUuid())
                                                                                .flatMap(checkName -> responseErrorMsg("Name already exist"))
                                                                                // when name is unique
                                                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getData(academicUri + "api/v1/info/show", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                        // checks if academic module UUID exists
                                                                                        .flatMap(moduleJsonNode -> apiCallService.getModuleUUID(moduleJsonNode)
                                                                                                // if academic module UUID exists, update campus record with branch
                                                                                                .flatMap(moduleUUID -> {

                                                                                                    // add company uuid to post request form data
                                                                                                    formData.add("companyUUID", companyEntity.getUuid().toString());

                                                                                                    //getting Same Campus Name as Branch name
                                                                                                    return apiCallService.getDataWithName(academicUri + "api/v1/campuses/name/show/", previousCampusName, userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                            .flatMap(campusJsonNode -> apiCallService.getUUID(campusJsonNode)
                                                                                                                    //updating Campus Data to Academic Module
                                                                                                                    .flatMap(campusUUID -> apiCallService.putDataList(formData, campusUUID, academicUri + "api/v1/campuses/update/", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                                            .flatMap(jsonNode -> apiCallService.getUUID(jsonNode)
                                                                                                                                    .flatMap(campus -> updateBranchRecord(updatedBranchEntity, previousBranchEntity, newBranchProfile, previousProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                                                                                    .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer."))
                                                                                                                            ).switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                                                                            .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer."))
                                                                                                                    ).switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                                                                    .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer."))
                                                                                                            ).switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                                                            .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer."));

                                                                                                    //else if academic module UUID does not exist update branch only
                                                                                                }).switchIfEmpty(updateBranchRecord(updatedBranchEntity, previousBranchEntity, newBranchProfile, previousProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                                .onErrorResume(err -> updateBranchRecord(updatedBranchEntity, previousBranchEntity, newBranchProfile, previousProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                        ).switchIfEmpty(updateBranchRecord(updatedBranchEntity, previousBranchEntity, newBranchProfile, previousProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                        .onErrorResume(err -> updateBranchRecord(updatedBranchEntity, previousBranchEntity, newBranchProfile, previousProfileEntity, reqCompanyUUID, reqIp, reqPort, reqBrowser, reqOs, reqDevice, reqReferer))
                                                                                ))
                                                                        ).switchIfEmpty(responseInfoMsg("Location does not exist."))
                                                                        .onErrorResume(ex -> responseErrorMsg("Location does not exist. Please contact developer."))
                                                                ).switchIfEmpty(responseInfoMsg("Language does not exist."))
                                                                .onErrorResume(ex -> responseErrorMsg("Language does not exist. Please contact developer."))
                                                        )
                                                ).switchIfEmpty(responseInfoMsg("Requested company does not exist."))
                                                .onErrorResume(ex -> responseErrorMsg("Requested company does not exist. Please contact developer."));
                                    });
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("unable to read request. Please contact developer."));
    }

    @AuthHasPermission(value = "config_api_v1_branches_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        final UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

//        find branch uuid
        return branchRepository.findByUuidAndDeletedAtIsNull(branchUUID)
//                        find branch profile uuid
                .flatMap(branchEntity -> branchProfileRepository.findByUuidAndDeletedAtIsNull(branchEntity.getBranchProfileUUID())
//                                find branch in account from account api
                                .flatMap(branchProfileEntity -> apiCallService.getDataWithUUID(accountUri + "api/v1/accounts/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))
//                                             find branch in transaction from account api call
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/transactions/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))))
//                                             find branch in job from account api call
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/jobs/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))))
//                                              find branch in profit center from account api call
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/profit-centers/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))))
//                                              find branch in cost center from account api call
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/cost-centers/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))))
//                                             find branch in voucher-branch from account api call
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-branch/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists.")))))
                                                // check if branch exists in branch administration department
                                                .switchIfEmpty(Mono.defer(() -> branchAdministrationDepartmentPvtRepository.findFirstByBranchUUIDAndDeletedAtIsNull(branchEntity.getUuid())
                                                        .flatMap(checkMsg -> responseInfoMsg("Unable to delete Record as the Reference of record exists."))))
                                                //Checks if Branch Reference exists in Financial Accounts in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-accounts/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                //Checks if Branch Reference exists in Financial Cost Centers in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-cost-centers/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                //Checks if Branch Reference exists in Financial Profit Centers in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-profit-centers/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                //Checks if Branch Reference exists in Financial Employee Accounts in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-employee-accounts/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                //Checks if Branch Reference exists in Financial Transactions in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-transactions/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                //Checks if Branch Reference exists in Financial Voucher Branch Pvt in Emp Financial Module
                                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-branches/branch/show/", branchEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                                .flatMap(checkBranchApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                                .switchIfEmpty(Mono.defer(() -> {

                                                    branchProfileEntity.setDeletedBy(UUID.fromString(userUUID));
                                                    branchProfileEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                    branchProfileEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                    branchProfileEntity.setReqDeletedIP(reqIp);
                                                    branchProfileEntity.setReqDeletedPort(reqPort);
                                                    branchProfileEntity.setReqDeletedBrowser(reqBrowser);
                                                    branchProfileEntity.setReqDeletedOS(reqOs);
                                                    branchProfileEntity.setReqDeletedDevice(reqDevice);
                                                    branchProfileEntity.setReqDeletedReferer(reqReferer);

                                                    branchEntity.setDeletedBy(UUID.fromString(userUUID));
                                                    branchEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                    branchEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                    branchEntity.setReqDeletedIP(reqIp);
                                                    branchEntity.setReqDeletedPort(reqPort);
                                                    branchEntity.setReqDeletedBrowser(reqBrowser);
                                                    branchEntity.setReqDeletedOS(reqOs);
                                                    branchEntity.setReqDeletedDevice(reqDevice);
                                                    branchEntity.setReqDeletedReferer(reqReferer);

                                                    return apiCallService.getData(academicUri + "api/v1/info/show", userUUID, reqCompanyUUID, reqBranchUUID)
                                                            // checks if academic module I'd exists
                                                            .flatMap(moduleJsonNode -> apiCallService.getModuleUUID(moduleJsonNode)
                                                                    // if academic module I'd exist, delete campus record with branch
                                                                    .flatMap(moduleUUID -> apiCallService.getDataWithName(academicUri + "api/v1/campuses/name/show/", branchEntity.getName(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                    .flatMap(campusJsonNode -> apiCallService.getUUID(campusJsonNode)
                                                                                            // delete the campus record before deleting the branch
                                                                                            .flatMap(campusUUID -> apiCallService.deleteData(academicUri + "api/v1/campuses/delete/", campusUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                    .flatMap(jsonNode -> apiCallService.getUUID(jsonNode)
                                                                                                            .flatMap(campus -> deleteBranchRecord(branchEntity, branchProfileEntity)
                                                                                                            ).switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."))
                                                                                                    ).switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."))
                                                                                            ).switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."))
                                                                                    ).switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."))
                                                                            //else if academic module UUID does not exist update branch only
                                                                    ).switchIfEmpty(deleteBranchRecord(branchEntity, branchProfileEntity))
                                                                    .onErrorResume(err -> deleteBranchRecord(branchEntity, branchProfileEntity))
                                                            ).switchIfEmpty(deleteBranchRecord(branchEntity, branchProfileEntity))
                                                            .onErrorResume(err -> deleteBranchRecord(branchEntity, branchProfileEntity));
                                                }))
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));

    }

    // Used to delete branch record and build branch with branch profile dto, then return response
    public Mono<ServerResponse> deleteBranchRecord(BranchEntity branchEntity, BranchProfileEntity branchProfileEntity) {

        return branchRepository.save(branchEntity)
                .flatMap(branchRecord -> branchProfileRepository.save(branchProfileEntity)
                        .flatMap(branchProfileRecord -> {
                            BranchWithBranchProfileDto branchWithBranchProfileDto = BranchWithBranchProfileDto
                                    .builder()
                                    .id(branchRecord.getId())
                                    .uuid(branchRecord.getUuid())
                                    .version(branchRecord.getVersion())
                                    .status(branchRecord.getStatus())
                                    .name(branchRecord.getName())
                                    .description(branchRecord.getDescription())
                                    .establishmentDate(branchProfileRecord.getEstablishmentDate())
                                    .companyUUID(branchRecord.getCompanyUUID())
                                    .createdBy(branchRecord.getCreatedBy())
                                    .createdAt(branchRecord.getCreatedAt())
                                    .updatedBy(branchRecord.getUpdatedBy())
                                    .updatedAt(branchRecord.getUpdatedAt())
                                    .archived(branchRecord.getArchived())
                                    .deletable(branchRecord.getDeletable())
                                    .editable(branchRecord.getEditable())
                                    .build();

                            return responseSuccessMsg("Record Deleted Successfully", branchWithBranchProfileDto)
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                        }));
    }

    @AuthHasPermission(value = "config_api_v1_branches_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID branchUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
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

                    return branchRepository.findByUuidAndDeletedAtIsNull(branchUUID)
                            .flatMap(previousBranchEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousBranchEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                //creating New Branch Entity
                                BranchEntity updatedBranchEntity = BranchEntity.builder()
                                        .uuid(previousBranchEntity.getUuid())
                                        .name(previousBranchEntity.getName())
                                        .status(status == true ? true : false)
                                        .description(previousBranchEntity.getDescription())
                                        .companyUUID(previousBranchEntity.getCompanyUUID())
                                        .branchProfileUUID(previousBranchEntity.getBranchProfileUUID())
                                        .createdBy(previousBranchEntity.getCreatedBy())
                                        .createdAt(previousBranchEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqCreatedIP(previousBranchEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousBranchEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousBranchEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousBranchEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousBranchEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousBranchEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                //Deleting the previous Branch Entity
                                previousBranchEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousBranchEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousBranchEntity.setReqDeletedIP(reqIp);
                                previousBranchEntity.setReqDeletedPort(reqPort);
                                previousBranchEntity.setReqDeletedBrowser(reqBrowser);
                                previousBranchEntity.setReqDeletedOS(reqOs);
                                previousBranchEntity.setReqDeletedDevice(reqDevice);
                                previousBranchEntity.setReqDeletedReferer(reqReferer);

                                return branchRepository.save(previousBranchEntity)
                                        .then(branchRepository.save(updatedBranchEntity))
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

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter, Long
            totalDataRowsWithoutFilter) {
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

//        Map<String,String> session = new HashMap<>();
//        session.put("id","10");
//        session.put("name","hello");

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
//                ,session
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

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long
            totalDataRowsWithFilter, Long totalDataRowsWithoutFilter) {
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

