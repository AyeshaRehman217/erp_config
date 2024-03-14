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
import tuf.webscaf.app.dbContext.master.dto.CompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyProfileEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
import tuf.webscaf.app.dbContext.slave.repositry.*;
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
@Tag(name = "companyHandler")
public class CompanyHandler {

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    SlaveCompanyProfileRepository slaveCompanyProfileRepository;

    @Autowired
    SlaveBranchRepository slaveBranchRepository;

    @Autowired
    SlaveCompanyRepository slaveCompanyRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    LanguageRepository languageRepository;

    @Autowired
    SlaveLanguageRepository slaveLanguageRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    SlaveCurrencyRepository slaveCurrencyRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    SlaveLocationRepository slaveLocationRepository;

    @Autowired
    AdministrationDepartmentRepository administrationDepartmentRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${server.erp_account_module.uri}")
    private String accountUri;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_student_financial_module.uri}")
    private String studentFinancialModuleUri;

    @Value("${server.erp_employee_financial_module.uri}")
    private String employeeFinancialModuleUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_companies_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {
            Flux<SlaveCompanyWithCompanyProfileDto> slaveCompanyEntityFlux = slaveCompanyRepository
                    .CompanyWithCompanyProfileIndexWithStatusFilter
                            (searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCompanyEntityFlux
                    .collectList()
                    .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (companyEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", companyEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please Contact Developer."));
        } else {
            Flux<SlaveCompanyWithCompanyProfileDto> slaveCompanyEntityFlux = slaveCompanyRepository
                    .CompanyWithCompanyProfileIndex(searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCompanyEntityFlux
                    .collectList()
                    .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (companyEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {
                                    return responseIndexSuccessMsg("All Records fetched successfully!", companyEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "config_api_v1_companies_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID companyUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveCompanyRepository.ShowByUuidCompanyWithCompanyProfile(companyUUID)
                .flatMap(companyEntity -> responseSuccessMsg("Record Fetched Successfully", companyEntity))
                .switchIfEmpty(responseErrorMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_companies_list")
//    This route is used By Student Financial Module in Financial Voucher Company Handler
    public Mono<ServerResponse> fetchCompanyList(ServerRequest serverRequest) {
        List<String> companyUUID = serverRequest.queryParams().get("uuid");
        List<UUID> uuidList = new ArrayList<>();
        for (String uuid : companyUUID) {
            uuidList.add(UUID.fromString(uuid));
        }
        return slaveCompanyRepository.findAllByUuidInAndDeletedAtIsNull(uuidList)
                .collectList()
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

    //  Get Mapped Companies Against Financial Voucher UUID
    @AuthHasPermission(value = "config_api_v1_companies_student-financial-voucher_mapped_show")
    public Mono<ServerResponse> showMappedCompanyAgainstFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        return apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-voucher-company/list/show/", financialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Fetch Company Records Based on Status
                    if (!status.isEmpty()) {

                        Flux<SlaveCompanyEntity> slaveStudentEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveStudentEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records fetched successfully!", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {

                        Flux<SlaveCompanyEntity> companyEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);


                        return companyEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {

                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records fetched successfully!", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //  Get Unmapped Companies Against Financial Voucher UUID
    @AuthHasPermission(value = "config_api_v1_companies_student-financial-voucher_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCompanyAgainstFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID financialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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


        return apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-voucher-company/list/show/", financialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Fetch Company Records Based on Status
                    if (!status.isEmpty()) {

                        Flux<SlaveCompanyEntity> slaveStudentEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveStudentEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {

                        Flux<SlaveCompanyEntity> companyEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);


                        return companyEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {

                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    //  Get Mapped Companies Against Financial Voucher UUID
    @AuthHasPermission(value = "config_api_v1_companies_emp-financial-voucher_mapped_show")
    public Mono<ServerResponse> showMappedCompanyAgainstEmpFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID empFinancialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        return apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-companies/list/show/", empFinancialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Fetch Company Records Based on Status
                    if (!status.isEmpty()) {

                        Flux<SlaveCompanyEntity> slaveStudentEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveStudentEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {

                        Flux<SlaveCompanyEntity> companyEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);


                        return companyEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {

                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));

                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //  Get Mapped Companies Against Financial Voucher UUID
    @AuthHasPermission(value = "config_api_v1_companies_emp-financial-voucher_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCompanyAgainstEmpFinancialVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID empFinancialVoucherUUID = UUID.fromString(serverRequest.pathVariable("financialVoucherUUID"));
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        return apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-companies/list/show/", empFinancialVoucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    //Fetch Company Records Based on Status
                    if (!status.isEmpty()) {

                        Flux<SlaveCompanyEntity> slaveStudentEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveStudentEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {

                        Flux<SlaveCompanyEntity> companyEntityFlux = slaveCompanyRepository
                                .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);


                        return companyEntityFlux
                                .collectList()
                                .flatMap(companyEntityDBList -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {

                                            if (companyEntityDBList.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntityDBList, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));

                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    //Check Doc Id In Drive Module
    @AuthHasPermission(value = "config_api_v1_companies_doc-image_show")
    public Mono<ServerResponse> getDocImage(ServerRequest serverRequest) {
        UUID documentId = UUID.fromString(serverRequest.pathVariable("uuid"));

        return serverRequest.formData()
                .flatMap(value -> slaveCompanyRepository.findFirstByDocImageAndDeletedAtIsNull(documentId)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist"));
    }

//
//    //  Check companies(whether exist or not) for voucher id
//    public Mono<ServerResponse> showCompanyList(ServerRequest serverRequest) {
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    List<String> listOfDocuments = value.get("company");
//                    if (value.containsKey("company")) {
//                        listOfDocuments.removeIf(s -> s.equals(""));
//                    }
//                    List<Long> l_list = new ArrayList<>();
//                    if (value.get("company") != null) {
//                        for (String docTypeId : listOfDocuments) {
//                            l_list.add(Long.valueOf(docTypeId));
//                        }
//                    }
//                    return companyRepository.findAllByIdInAndDeletedAtIsNull(l_list)
//                            .collectList()
//                            .flatMap(companyEntities -> responseSuccessMsg("Records Fetched Successfully!", companyEntities))
//                            .switchIfEmpty(responseInfoMsg("Record does not exist")).onErrorResume(err -> responseErrorMsg("Record does not exist"));
//                }).switchIfEmpty(responseInfoMsg("Unable to read the request"))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request"));
//
//    }

    //This Function Is used By Account Module to Check if Company UUID exists in Voucher Mapping
    @AuthHasPermission(value = "config_api_v1_companies_list_show")
    public Mono<ServerResponse> showExistingCompanyListInModule(ServerRequest serverRequest) {

        List<String> uuids = serverRequest.queryParams().get("uuid");

        //This is Company List to paas in the query
        List<UUID> companyList = new ArrayList<>();
        if (uuids != null) {
            for (String company : uuids) {
                companyList.add(UUID.fromString(company));
            }
        }

        return companyRepository.getUUIDsOfExitingRecords(companyList)
                .collectList()
                .flatMap(companyUUIDs -> responseSuccessMsg("Records Fetched Successfully", companyUUIDs))
                .switchIfEmpty(responseInfoMsg("Unable to read the request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request.Please Contact Developer."));
    }


    //       Show mapped companies for voucher id
    @AuthHasPermission(value = "config_api_v1_companies_voucher_mapped_show")
    public Mono<ServerResponse> showMappedCompaniesAgainstVoucher(ServerRequest serverRequest) {

        System.out.println("----------------------------------------------------0");
        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));

        // Optional Query Parameter of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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
        return apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-company/list/show/", voucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveCompanyEntity> slaveCompanyEntityFlux = slaveCompanyRepository.
                                findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveCompanyEntityFlux
                                .collectList()
                                .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntity, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {
                        Flux<SlaveCompanyEntity> slaveCompanyEntityFlux = slaveCompanyRepository.
                                findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);

                        return slaveCompanyEntityFlux
                                .collectList()
                                .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntity, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    //  Show  unmapped companies for voucher id
    @AuthHasPermission(value = "config_api_v1_companies_voucher_un-mapped_show")
    public Mono<ServerResponse> showUnMappedCompaniesAgainstVoucher(ServerRequest serverRequest) {

        String userUUID = serverRequest.headers().firstHeader("auid");
        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");

        UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));

        // Optional Query Parameter of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Page index must not be less than zero");
        }
        if (size < 1) {
            return responseErrorMsg("Page size must not be less than one");
        }

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
        return apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-company/list/show/", voucherUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                .flatMap(jsonNode -> {
                    List<UUID> listOfUUIDs = new ArrayList<>(apiCallService.getUUIDList(jsonNode));

                    if (!status.isEmpty()) {
                        Flux<SlaveCompanyEntity> slaveCompanyEntityFlux = slaveCompanyRepository.
                                findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs);

                        return slaveCompanyEntityFlux
                                .collectList()
                                .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, Boolean.valueOf(status), listOfUUIDs, searchKeyWord, Boolean.valueOf(status), listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntity, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    } else {
                        Flux<SlaveCompanyEntity> slaveCompanyEntityFlux = slaveCompanyRepository.
                                findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(pageable, searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs);

                        return slaveCompanyEntityFlux
                                .collectList()
                                .flatMap(companyEntity -> slaveCompanyRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(searchKeyWord, listOfUUIDs, searchKeyWord, listOfUUIDs)
                                        .flatMap(count ->
                                        {
                                            if (companyEntity.isEmpty()) {
                                                return responseIndexInfoMsg("Record does not exist", count, 0L);

                                            } else {
                                                return responseIndexSuccessMsg("Records Fetched Successfully", companyEntity, count, 0L);
                                            }
                                        })
                                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."));
                    }
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(err -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    //Check if branch exist against given company id In Account Module
//    public Mono<ServerResponse> getCompanyId(ServerRequest serverRequest) {
//        final UUID voucherUUID = UUID.fromString(serverRequest.pathVariable("voucherUUID"));
//
//        return serverRequest.formData()
//                .flatMap(value -> apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-company/list/show/", voucherUUID)
//                        .flatMap(jsonNode -> {
//
//                            //List of Mapped Companies with Vouchers From Account Module
//                            List<UUID> listOfMappedCompanies = new ArrayList<>(apiCallService.getUUIDList(jsonNode));
//
//                            //This is List of Company id from Query Parameter
//                            List<UUID> frontCompany = new ArrayList<>();
//                            frontCompany.add(UUID.fromString(value.getFirst("companyUUID").trim()));
//
//                            //Check if the company id from Query Parameter matches with the Mapped List
//                            listOfMappedCompanies.retainAll(frontCompany);
//
//
//                            if (!(listOfMappedCompanies.isEmpty())) {
//                                return slaveBranchRepository.findAllByCompanyUUIDInAndDeletedAtIsNull(listOfMappedCompanies)
//                                        .collectList()
//                                        .flatMap(branchEntityList -> {
//                                            List<UUID> branchUUIDList = new ArrayList<>();
//                                            for (SlaveBranchEntity branch : branchEntityList) {
//                                                branchUUIDList.add(branch.getUuid());
//                                            }
//                                            if (!branchUUIDList.isEmpty()) {
//                                                return responseSuccessMsg("All Records fetched successfully!", branchUUIDList);
//                                            } else {
//                                                return responseInfoMsg("Record Does not Exist");
//                                            }
//                                        }).switchIfEmpty(responseInfoMsg("Branch does not exist"))
//                                        .onErrorResume(ex -> responseErrorMsg("Branch does not exist. Please contact developer"));
//                            } else {
//
//                                return responseInfoMsg("Record does not exist");
//                            }
//
//                        })
//                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
//
//    }


    public Mono<CompanyWithCompanyProfileDto> companyWithCompanyProfileDto(CompanyEntity companyEntity, CompanyProfileEntity companyProfileEntity, Long companyId) {

        CompanyWithCompanyProfileDto companyWithCompanyProfileDtoEntity = CompanyWithCompanyProfileDto.builder()
                .id(companyId)
                .uuid(companyEntity.getUuid())
                .version(companyEntity.getVersion())
                .docImage(companyEntity.getDocImage())
                .status(companyEntity.getStatus())
                .name(companyEntity.getName())
                .description(companyEntity.getDescription())
                .companyProfileUUID(companyProfileEntity.getUuid())
                .establishmentDate(companyProfileEntity.getEstablishmentDate())
                .countryUUID(companyProfileEntity.getCountryUUID())
                .currencyUUID(companyProfileEntity.getCurrencyUUID())
                .cityUUID(companyProfileEntity.getCityUUID())
                .stateUUID(companyProfileEntity.getStateUUID())
                .locationUUID(companyProfileEntity.getLocationUUID())
                .languageUUID(companyProfileEntity.getLanguageUUID())
                .createdBy(companyEntity.getCreatedBy())
                .createdAt(companyEntity.getCreatedAt())
                .updatedBy(companyEntity.getUpdatedBy())
                .updatedAt(companyEntity.getUpdatedAt())
                .archived(companyEntity.getArchived())
                .deletable(companyEntity.getDeletable())
                .editable(companyEntity.getEditable())
                .build();

        return Mono.just(companyWithCompanyProfileDtoEntity);

    }

    @AuthHasPermission(value = "config_api_v1_companies_store")
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

                    CompanyProfileEntity companyProfileEntity = CompanyProfileEntity.builder()
                            .uuid(UUID.randomUUID())
                            .establishmentDate(LocalDateTime.parse((value.getFirst("establishmentDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .languageUUID(UUID.fromString(value.getFirst("languageUUID").trim()))
                            .locationUUID(UUID.fromString(value.getFirst("locationUUID").trim()))
                            .cityUUID(UUID.fromString(value.getFirst("cityUUID").trim()))
                            .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
                            .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                            .currencyUUID(UUID.fromString(value.getFirst("currencyUUID").trim()))
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

                    //Check if language exists
                    return languageRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getLanguageUUID())
                            //Check if country exists
                            .flatMap(language -> countryRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getCountryUUID())
                                            //Check if currency exists
                                            .flatMap(country -> currencyRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getCurrencyUUID())
                                                            //Check if state exists
                                                            .flatMap(currency -> stateRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getStateUUID())
                                                                            //Check if city exists
                                                                            .flatMap(state -> cityRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getCityUUID())
                                                                                            //Check if location exists
                                                                                            .flatMap(city -> locationRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getLocationUUID())
                                                                                                            .flatMap(locationEntity -> {
                                                                                                                        //check if City Lie Against this Country and State
                                                                                                                        if (!city.getCountryUUID().equals(country.getUuid())) {

                                                                                                                            return responseInfoMsg("City Does not Exist Against This Country!");

                                                                                                                        } else if (!city.getStateUUID().equals(state.getUuid())) {

                                                                                                                            return responseInfoMsg("City Does not exist Against this State!");

                                                                                                                        } else {

                                                                                                                            return companyProfileRepository.save(companyProfileEntity)
                                                                                                                                    .flatMap(companyProfileEntityData -> {

                                                                                                                                        UUID documentImage = null;
                                                                                                                                        if ((value.getFirst("docImage")) != null && (value.getFirst("docImage") != "")) {
                                                                                                                                            documentImage = UUID.fromString(value.getFirst("docImage"));
                                                                                                                                        }

                                                                                                                                        CompanyEntity companyEntity = CompanyEntity.builder()
                                                                                                                                                .uuid(UUID.randomUUID())
                                                                                                                                                .name(value.getFirst("name").trim())
                                                                                                                                                .docImage(documentImage)
                                                                                                                                                .description(value.getFirst("description").trim())
                                                                                                                                                .status(Boolean.valueOf(value.getFirst("status")))
                                                                                                                                                .companyProfileUUID(companyProfileEntityData.getUuid())
                                                                                                                                                .createdBy(UUID.fromString(userUUID))
                                                                                                                                                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                                                                                                                                .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                                                                                                                .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                                                                                                                                .reqCreatedIP(reqIp)
                                                                                                                                                .reqCreatedPort(reqPort)
                                                                                                                                                .reqCreatedBrowser(reqBrowser)
                                                                                                                                                .reqCreatedOS(reqOs)
                                                                                                                                                .reqCreatedDevice(reqDevice)
                                                                                                                                                .reqCreatedReferer(reqReferer)
                                                                                                                                                .build();


                                                                                                                                        UUID finalDocumentImageUUID = documentImage;
                                                                                                                                        //Check if Company Name Already Exists or not
                                                                                                                                        return companyRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(companyEntity.getName())
                                                                                                                                                .flatMap(name -> companyProfileRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getUuid())
                                                                                                                                                        //delete company profile when name already exist (because it stores data in company profile)
                                                                                                                                                        .flatMap(deletePreviousProfile -> companyProfileRepository.delete(deletePreviousProfile)
                                                                                                                                                                .flatMap(delMsg -> responseInfoMsg("Unable to Store Company.There is Something wrong please try again."))
                                                                                                                                                                .switchIfEmpty(responseInfoMsg("Name Already Exist.")))
                                                                                                                                                ).switchIfEmpty(Mono.defer(() -> {

                                                                                                                                                    //Check if User Selects Document Image UUID and This document UUID exists in Drive Module
                                                                                                                                                    if (finalDocumentImageUUID != null && !finalDocumentImageUUID.equals("")) {
                                                                                                                                                        return apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", finalDocumentImageUUID, userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                                                                                .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                                                                                                                                        .flatMap(documentUUID -> {

                                                                                                                                                                                    //Sending Document ids in Form data to check if document Id's exist
                                                                                                                                                                                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data

                                                                                                                                                                                    sendFormData.add("docId", String.valueOf(documentUUID));//iterating over multiple values and then adding in list

                                                                                                                                                                                    //update Document Submitted Status
                                                                                                                                                                                    return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                                                                                                            .flatMap(updatedDocument -> companyRepository.save(companyEntity)
                                                                                                                                                                                                    .flatMap(companyDB -> companyWithCompanyProfileDto(companyDB, companyProfileEntity, companyDB.getId())
                                                                                                                                                                                                            .flatMap(companyWithProfileDto -> responseSuccessMsg("Record stored successfully", companyWithProfileDto)
                                                                                                                                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Company.There is something wrong please try again."))
                                                                                                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Company.Please Contact Developer."))
                                                                                                                                                                                                            ))
                                                                                                                                                                                            );
                                                                                                                                                                                }
                                                                                                                                                                        ).switchIfEmpty(Mono.defer(() -> companyProfileRepository.findByUuidAndDeletedAtIsNull(companyProfileEntity.getUuid())
//                                                                                                                                                                               delete company profile when image is not uploaded
                                                                                                                                                                                        .flatMap(delete -> companyProfileRepository.delete(delete)
                                                                                                                                                                                                .flatMap(delMsg -> responseInfoMsg("Unable to Store Company. There is Something wrong please try again."))
                                                                                                                                                                                                //Check if Document Does not Exist in Drive
                                                                                                                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Upload Image."))))
                                                                                                                                                                        ));
                                                                                                                                                    } else {
                                                                                                                                                        //Check if Document Image is Empty the Store Company
                                                                                                                                                        return companyRepository.save(companyEntity)
                                                                                                                                                                .flatMap(companyEntityDB -> companyWithCompanyProfileDto(companyEntity, companyProfileEntity, companyEntityDB.getId())
                                                                                                                                                                        .flatMap(companyWithProfileDto -> responseSuccessMsg("Record stored successfully", companyWithProfileDto)
                                                                                                                                                                                .switchIfEmpty(responseInfoMsg("Unable to Store Company.There is something wrong please try again."))
                                                                                                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Store Company.Please Contact Developer."))
                                                                                                                                                                        ));
                                                                                                                                                    }
                                                                                                                                                }));
                                                                                                                                    }).switchIfEmpty(responseInfoMsg("Unable to create company"))
                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to create company. Please Contact Developer."));
                                                                                                                        }
                                                                                                                    }
                                                                                                            ).switchIfEmpty(responseInfoMsg("Location does not exist."))
                                                                                                            .onErrorResume(ex -> responseErrorMsg("Location does not exist.Please Contact Developer."))
                                                                                            ).switchIfEmpty(responseInfoMsg("City does not exist."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("City does not exist.Please Contact Developer."))
                                                                            ).switchIfEmpty(responseInfoMsg("State does not exist."))
                                                                            .onErrorResume(ex -> responseErrorMsg("State does not exist.Please Contact Developer."))
                                                            ).switchIfEmpty(responseInfoMsg("currency does not exist."))
                                                            .onErrorResume(ex -> responseErrorMsg("Currency does not exist.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("country does not exist."))
                                            .onErrorResume(ex -> responseErrorMsg("Country does not exist.Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("language does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Language does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    public Mono<CompanyProfileEntity> updateCompanyProfileEntity(ServerRequest serverRequest, UUID userUUID) {

        return serverRequest
                .formData()
                .flatMap(value -> {

                    CompanyProfileEntity updatedCompanyProfileEntity = CompanyProfileEntity.builder()
                            .establishmentDate(LocalDateTime.parse((value.getFirst("establishmentDate")), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .languageUUID(UUID.fromString(value.getFirst("languageUUID").trim()))
                            .locationUUID(UUID.fromString(value.getFirst("locationUUID").trim()))
                            .cityUUID(UUID.fromString(value.getFirst("cityUUID").trim()))
                            .stateUUID(UUID.fromString(value.getFirst("stateUUID").trim()))
                            .countryUUID(UUID.fromString(value.getFirst("countryUUID").trim()))
                            .currencyUUID(UUID.fromString(value.getFirst("currencyUUID").trim()))
                            .locationUUID(UUID.fromString(value.getFirst("locationUUID").trim()))
                            .updatedBy(userUUID)
                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                            .build();

                    return Mono.just(updatedCompanyProfileEntity);
                });
    }

    @AuthHasPermission(value = "config_api_v1_companies_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        final UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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
                .flatMap(value -> companyRepository.findByUuidAndDeletedAtIsNull(companyUUID)
                                .flatMap(previousCompanyEntity -> {

                                    UUID documentImage = null;

                                    if ((value.getFirst("docImage")) != null && (value.getFirst("docImage") != "")) {
                                        documentImage = UUID.fromString(value.getFirst("docImage"));
                                    }

                                    CompanyEntity updatedCompanyEntity = CompanyEntity.builder()
                                            .uuid(previousCompanyEntity.getUuid())
                                            .name(value.getFirst("name").trim())
                                            .description(value.getFirst("description").trim())
                                            .status(Boolean.valueOf(value.getFirst("status")))
                                            .docImage(documentImage)
                                            .companyProfileUUID(previousCompanyEntity.getCompanyProfileUUID())
                                            .updatedBy(UUID.fromString(userUUID))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .createdBy(previousCompanyEntity.getCreatedBy())
                                            .createdAt(previousCompanyEntity.getCreatedAt())
                                            .reqCreatedIP(previousCompanyEntity.getReqCreatedIP())
                                            .reqCreatedPort(previousCompanyEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(previousCompanyEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(previousCompanyEntity.getReqCreatedOS())
                                            .reqCreatedDevice(previousCompanyEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(previousCompanyEntity.getReqCreatedReferer())
//                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                            .reqUpdatedIP(reqIp)
                                            .reqUpdatedPort(reqPort)
                                            .reqUpdatedBrowser(reqBrowser)
                                            .reqUpdatedOS(reqOs)
                                            .reqUpdatedDevice(reqDevice)
                                            .reqUpdatedReferer(reqReferer)
                                            .build();

                                    previousCompanyEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousCompanyEntity.setDeletedBy(UUID.fromString(userUUID));
                                    previousCompanyEntity.setReqDeletedIP(reqIp);
                                    previousCompanyEntity.setReqDeletedPort(reqPort);
                                    previousCompanyEntity.setReqDeletedBrowser(reqBrowser);
                                    previousCompanyEntity.setReqDeletedOS(reqOs);
                                    previousCompanyEntity.setReqDeletedDevice(reqDevice);
                                    previousCompanyEntity.setReqDeletedReferer(reqReferer);

                                    //check if Name is unique
                                    UUID finalDocumentImage = documentImage;

                                    return companyRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCompanyEntity.getName(), companyUUID)
                                            .flatMap(checkNameMsg -> responseInfoMsg("Name Already Exist"))
                                            //check Company Profile Existence
                                            .switchIfEmpty(Mono.defer(() -> companyProfileRepository.findByUuidAndDeletedAtIsNull(updatedCompanyEntity.getCompanyProfileUUID())
                                                            .flatMap(previousCompanyProfileEntity -> updateCompanyProfileEntity(serverRequest, UUID.fromString(userUUID))
                                                                    //Check if Country exists in countries table
                                                                    .flatMap(updatedCompanyProfile -> countryRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getCountryUUID())
                                                                                    //Check if language exists in languages table
                                                                                    .flatMap(country -> languageRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getLanguageUUID())
                                                                                                    //Check if currency exists in currencies table
                                                                                                    .flatMap(language -> currencyRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getCurrencyUUID())
                                                                                                                    //Check if city exists in cities table
                                                                                                                    .flatMap(currency -> cityRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getCityUUID())
                                                                                                                                    //Check if state exists in states table
                                                                                                                                    .flatMap(city -> stateRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getStateUUID())
                                                                                                                                                    //Check if location exists in location table
                                                                                                                                                    .flatMap(state -> locationRepository.findByUuidAndDeletedAtIsNull(updatedCompanyProfile.getLocationUUID())
                                                                                                                                                                    .flatMap(location -> {
                                                                                                                                                                                //Check if User Selects Document Image UUID and This document UUID exists in Drive Module
                                                                                                                                                                                if (finalDocumentImage != null && !finalDocumentImage.equals("")) {

                                                                                                                                                                                    return apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", finalDocumentImage, userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                                                                                                            .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                                                                                                                                                                    .flatMap(documentUUID -> {

                                                                                                                                                                                                                //Sending Document ids in Form data to check if document Id's exist
                                                                                                                                                                                                                MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data

                                                                                                                                                                                                                sendFormData.add("docId", String.valueOf(documentUUID));//iterating over multiple values and then adding in list

                                                                                                                                                                                                                //update Document Submitted Status
                                                                                                                                                                                                                return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userUUID, reqCompanyUUID, reqBranchUUID)
                                                                                                                                                                                                                        //Save Previous Company Entity
                                                                                                                                                                                                                        .flatMap(updatedDocument -> companyRepository.save(previousCompanyEntity)
                                                                                                                                                                                                                                //Save Updated Company Entity
                                                                                                                                                                                                                                .then(companyRepository.save(updatedCompanyEntity))
                                                                                                                                                                                                                                .flatMap(updatedCompany -> {
                                                                                                                                                                                                                                            updatedCompanyProfile.setUuid(previousCompanyProfileEntity.getUuid());
                                                                                                                                                                                                                                            updatedCompanyProfile.setCreatedAt(previousCompanyProfileEntity.getCreatedAt());
                                                                                                                                                                                                                                            updatedCompanyProfile.setCreatedBy(previousCompanyProfileEntity.getCreatedBy());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedIP(previousCompanyEntity.getReqCreatedIP());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedPort(previousCompanyEntity.getReqCreatedPort());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedBrowser(previousCompanyEntity.getReqCreatedBrowser());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedOS(previousCompanyEntity.getReqCreatedOS());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedDevice(previousCompanyEntity.getReqCreatedDevice());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCreatedReferer(previousCompanyEntity.getReqCreatedReferer());
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedIP(reqIp);
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedPort(reqPort);
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedBrowser(reqBrowser);
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedOS(reqOs);
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedDevice(reqDevice);
                                                                                                                                                                                                                                            updatedCompanyProfile.setReqUpdatedReferer(reqReferer);

                                                                                                                                                                                                                                            previousCompanyProfileEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                                                                                                                                                                                            previousCompanyProfileEntity.setDeletedBy(UUID.fromString(userUUID));
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedIP(reqIp);
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedPort(reqPort);
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedBrowser(reqBrowser);
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedOS(reqOs);
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedDevice(reqDevice);
                                                                                                                                                                                                                                            previousCompanyEntity.setReqDeletedReferer(reqReferer);

                                                                                                                                                                                                                                            return companyProfileRepository.save(previousCompanyProfileEntity)
                                                                                                                                                                                                                                                    .then(companyProfileRepository.save(updatedCompanyProfile))
                                                                                                                                                                                                                                                    .flatMap(newCompanyProfileEntity -> companyWithCompanyProfileDto(updatedCompany, newCompanyProfileEntity, updatedCompany.getId())
                                                                                                                                                                                                                                                            .flatMap(companyWithProfileDto -> responseSuccessMsg("Record Updated successfully", companyWithProfileDto)
                                                                                                                                                                                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Company.There is something wrong please try again."))
                                                                                                                                                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Company.Please Contact Developer."))
                                                                                                                                                                                                                                                            ));
                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                ));
                                                                                                                                                                                                            }
                                                                                                                                                                                                    ).switchIfEmpty(Mono.defer(() -> companyProfileRepository.findByUuidAndDeletedAtIsNull(previousCompanyProfileEntity.getUuid())
//                                                                                                                                                                                                           delete company profile when image is not uploaded
                                                                                                                                                                                                            .flatMap(delete -> companyProfileRepository.delete(delete)
                                                                                                                                                                                                                            .flatMap(delMsg -> responseInfoMsg("Unable to Store Company.There is Something wrong please try again."))
                                                                                                                                                                                                                    //Check if Document Does not Exist in Drive.switchIfEmpty(responseInfoMsg("Unable to Upload Image."))))
                                                                                                                                                                                                            ))));
                                                                                                                                                                                } else {

                                                                                                                                                                                    //Check if Document Image is Empty the Store Company
                                                                                                                                                                                    return companyRepository.save(previousCompanyEntity)
                                                                                                                                                                                            //Save Updated Company Entity
                                                                                                                                                                                            .then(companyRepository.save(updatedCompanyEntity))
                                                                                                                                                                                            .flatMap(updatedCompany -> {
                                                                                                                                                                                                updatedCompanyProfile.setUuid(previousCompanyProfileEntity.getUuid());
                                                                                                                                                                                                updatedCompanyProfile.setCreatedAt(previousCompanyProfileEntity.getCreatedAt());
                                                                                                                                                                                                updatedCompanyProfile.setCreatedBy(previousCompanyProfileEntity.getCreatedBy());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedIP(previousCompanyEntity.getReqCreatedIP());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedPort(previousCompanyEntity.getReqCreatedPort());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedBrowser(previousCompanyEntity.getReqCreatedBrowser());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedOS(previousCompanyEntity.getReqCreatedOS());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedDevice(previousCompanyEntity.getReqCreatedDevice());
                                                                                                                                                                                                updatedCompanyProfile.setReqCreatedReferer(previousCompanyEntity.getReqCreatedReferer());
                                                                                                                                                                                                updatedCompanyProfile.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                                                                                                                                                updatedCompanyProfile.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedIP(reqIp);
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedPort(reqPort);
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedBrowser(reqBrowser);
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedOS(reqOs);
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedDevice(reqDevice);
                                                                                                                                                                                                updatedCompanyProfile.setReqUpdatedReferer(reqReferer);

                                                                                                                                                                                                previousCompanyProfileEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                                                                                                                                                                previousCompanyProfileEntity.setDeletedBy(UUID.fromString(userUUID));
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedIP(reqIp);
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedPort(reqPort);
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedBrowser(reqBrowser);
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedOS(reqOs);
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedDevice(reqDevice);
                                                                                                                                                                                                previousCompanyEntity.setReqDeletedReferer(reqReferer);

                                                                                                                                                                                                return companyProfileRepository.save(previousCompanyProfileEntity)
                                                                                                                                                                                                        .then(companyProfileRepository.save(updatedCompanyProfile))
                                                                                                                                                                                                        .flatMap(newCompanyProfileEntity -> companyWithCompanyProfileDto(updatedCompany, newCompanyProfileEntity, updatedCompany.getId())
                                                                                                                                                                                                                .flatMap(companyWithProfileDto -> responseSuccessMsg("Record Updated successfully", companyWithProfileDto)
                                                                                                                                                                                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Company.There is something wrong please try again."))
                                                                                                                                                                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Company.Please Contact Developer."))
                                                                                                                                                                                                                ));
                                                                                                                                                                                            });
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                    ).switchIfEmpty(responseInfoMsg("Location Does not Exist"))
                                                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Location Does not exist.Please Contact Developer."))
                                                                                                                                                    ).switchIfEmpty(responseInfoMsg("State Does not Exist"))
                                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("State Does not exist.Please Contact Developer."))
                                                                                                                                    ).switchIfEmpty(responseInfoMsg("City Does not Exist"))
                                                                                                                                    .onErrorResume(ex -> responseErrorMsg("City Does not exist.Please Contact Developer."))
                                                                                                                    ).switchIfEmpty(responseInfoMsg("Currency Does not Exist"))
                                                                                                                    .onErrorResume(ex -> responseErrorMsg("Currency Does not exist.Please Contact Developer."))
                                                                                                    ).switchIfEmpty(responseInfoMsg("Language Does not Exist"))
                                                                                                    .onErrorResume(ex -> responseErrorMsg("Language Does not exist.Please Contact Developer."))
                                                                                    ).switchIfEmpty(responseInfoMsg("Country Does not Exist"))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Country Does not exist.Please Contact Developer."))
                                                                    )).switchIfEmpty(responseInfoMsg("Company Profile Does not Exist"))
                                                            .onErrorResume(ex -> responseErrorMsg("Company Profile Does not Exist.Please Contact Developer."))
                                            ));
                                }).switchIfEmpty(responseInfoMsg("Company Does not Exist."))
                                .onErrorResume(ex -> responseErrorMsg("Company Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_companies_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        final UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());
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

        //check if Company Exists in Companies
        return companyRepository.findByUuidAndDeletedAtIsNull(companyUUID)
                //check if Company Profile Exists in Companies
                .flatMap(companyEntity -> companyProfileRepository.findByUuidAndDeletedAtIsNull(companyEntity.getCompanyProfileUUID())
                        //check if company Exists in branch
                        .flatMap(companyProfileEntity -> branchRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyEntity.getUuid())
                                        .flatMap(branchMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!"))
                                        // check if company reference exists in financial accounts in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-accounts/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        // check if company reference exists in financial cost centers in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-cost-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        // check if company reference exists in financial profit centers in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-profit-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        // check if company reference exists in student financial accounts in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-student-accounts/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        // check if company reference exists in financial transaction in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-transactions/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        // check if company reference exists in Financial Voucher Company Pvt in student financial module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(studentFinancialModuleUri + "api/v1/financial-voucher-company/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Accounts in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-accounts/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Cost Centers in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-cost-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Profit Centers in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-profit-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Employee Accounts in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-employee-accounts/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Transactions in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-transactions/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
                                        //Checks if Company Reference exists in Financial Voucher Company Pvt in Emp Financial Module
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(employeeFinancialModuleUri + "api/v1/financial-voucher-companies/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkCompanyApiMsg -> responseInfoMsg("Unable to delete Record as Reference of record Exists")))))
//                                check company exist in administration_departments
                                        .switchIfEmpty(Mono.defer(() -> administrationDepartmentRepository.findFirstByCompanyUUIDAndDeletedAtIsNull(companyEntity.getUuid())
                                                .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete record as the Reference of record exists!"))))
//                                    find company id in account from account api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/accounts/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
//                                    find company id in job from account api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/jobs/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
//                                    find company id in profit Center from account api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/profit-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
//                                    find company id in Cost Center from account api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/cost-centers/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
//                                     find company id in account from transaction api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/transactions/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
//                                     find company id in account from voucher-company api
                                        .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(accountUri + "api/v1/voucher-company/company/show/", companyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                                .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                        .flatMap(checkBranchUUIDApiMsg -> responseInfoMsg("Unable to delete Record.Reference of record exists!")))))
                                        .switchIfEmpty(Mono.defer(() -> {

                                                    companyProfileEntity.setDeletedBy(UUID.fromString(userUUID));
                                                    companyProfileEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                                    companyProfileEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                    companyProfileEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                    companyProfileEntity.setReqDeletedIP(reqIp);
                                                    companyProfileEntity.setReqDeletedPort(reqPort);
                                                    companyProfileEntity.setReqDeletedBrowser(reqBrowser);
                                                    companyProfileEntity.setReqDeletedOS(reqOs);
                                                    companyProfileEntity.setReqDeletedDevice(reqDevice);
                                                    companyProfileEntity.setReqDeletedReferer(reqReferer);

                                                    return companyProfileRepository.save(companyProfileEntity);

                                                })
                                                .then(Mono.defer(() -> {

                                                    companyEntity.setDeletedBy(UUID.fromString(userUUID));
                                                    companyEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                                    companyEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                    companyEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                    companyEntity.setReqDeletedIP(reqIp);
                                                    companyEntity.setReqDeletedPort(reqPort);
                                                    companyEntity.setReqDeletedBrowser(reqBrowser);
                                                    companyEntity.setReqDeletedOS(reqOs);
                                                    companyEntity.setReqDeletedDevice(reqDevice);
                                                    companyEntity.setReqDeletedReferer(reqReferer);

                                                    return companyRepository.save(companyEntity)
                                                            .flatMap(value1 -> responseSuccessMsg("Record deleted successfully!", value1))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Delete Record.There is Something wrong please try again."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Delete Record.Please Contact Developer."));
                                                })))
                        )).switchIfEmpty(responseInfoMsg("Record Does not Exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not Exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_companies_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID companyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return companyRepository.findByUuidAndDeletedAtIsNull(companyUUID)
                            .flatMap(previousCompanyEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousCompanyEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CompanyEntity updatedCompanyEntity = CompanyEntity.builder()
                                        .uuid(previousCompanyEntity.getUuid())
                                        .name(previousCompanyEntity.getName())
                                        .description(previousCompanyEntity.getDescription())
                                        .status(status == true ? true : false)
                                        .docImage(previousCompanyEntity.getDocImage())
                                        .companyProfileUUID(previousCompanyEntity.getCompanyProfileUUID())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .createdBy(previousCompanyEntity.getCreatedBy())
                                        .createdAt(previousCompanyEntity.getCreatedAt())
//                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousCompanyEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousCompanyEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousCompanyEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousCompanyEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousCompanyEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousCompanyEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                previousCompanyEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousCompanyEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousCompanyEntity.setReqDeletedIP(reqIp);
                                previousCompanyEntity.setReqDeletedPort(reqPort);
                                previousCompanyEntity.setReqDeletedBrowser(reqBrowser);
                                previousCompanyEntity.setReqDeletedOS(reqOs);
                                previousCompanyEntity.setReqDeletedDevice(reqDevice);
                                previousCompanyEntity.setReqDeletedReferer(reqReferer);

                                return companyRepository.save(previousCompanyEntity)
                                        .then(companyRepository.save(updatedCompanyEntity))
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
