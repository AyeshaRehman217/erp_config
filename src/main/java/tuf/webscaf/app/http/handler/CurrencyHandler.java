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
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.master.repositry.CompanyProfileRepository;
import tuf.webscaf.app.dbContext.master.repositry.CountryRepository;
import tuf.webscaf.app.dbContext.master.repositry.CurrencyRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCurrencyEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCurrencyRepository;
import tuf.webscaf.app.service.ApiCallService;
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
@Tag(name = "currencyHandler")
public class CurrencyHandler {

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCurrencyRepository slaveCurrencyRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_currencies_index")
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
            Flux<SlaveCurrencyEntity> slaveCurrencyCodeEntityFlux = slaveCurrencyRepository
                    .findAllByCurrencyNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveCurrencyCodeEntityFlux
                    .collectList()
                    .flatMap(currencyCodeEntity -> slaveCurrencyRepository
                            .countByCurrencyNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))

                            .flatMap(count -> {
                                if (currencyCodeEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", currencyCodeEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request"));
        } else {
            Flux<SlaveCurrencyEntity> slaveCurrencyCodeEntityFlux = slaveCurrencyRepository
                    .findAllByCurrencyNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveCurrencyCodeEntityFlux
                    .collectList()
                    .flatMap(currencyCodeEntity -> slaveCurrencyRepository
                            .countByCurrencyNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (currencyCodeEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", currencyCodeEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }

    }

    @AuthHasPermission(value = "config_api_v1_currencies_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID countryCodeUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveCurrencyRepository.findByUuidAndDeletedAtIsNull(countryCodeUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_currencies_store")
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

                    Integer isoNumber = null;
                    if ((value.containsKey("isoNumber") && (value.getFirst("isoNumber") != ""))) {
                        isoNumber = Integer.valueOf(value.getFirst("isoNumber"));
                    }

                    CurrencyEntity currencyEntity = CurrencyEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .currencyName(value.getFirst("currencyName").trim())
                            .description(value.getFirst("description").trim())
                            .currency(value.getFirst("currency").trim())
                            .currencySymbol(value.getFirst("currencySymbol").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .isoNumber(isoNumber)
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

                    //Check if Name is unique
                    return currencyRepository.findFirstByCurrencyNameIgnoreCaseAndDeletedAtIsNull(currencyEntity.getCurrencyName())
                            .flatMap(checkName -> responseInfoMsg("Name already exist"))
                            //check If currency is unique
                            .switchIfEmpty(Mono.defer(() -> currencyRepository.findFirstByCurrencyIgnoreCaseAndDeletedAtIsNull(currencyEntity.getCurrency())
                                    .flatMap(checkCurrencyCode -> responseInfoMsg("Currency already exist"))))
                            .switchIfEmpty(Mono.defer(() -> currencyRepository.save(currencyEntity)
                                    .flatMap(value1 -> responseSuccessMsg("Record Stored Successfully", value1))
                                    .switchIfEmpty(responseInfoMsg("Unable to store record.There is something wrong please try again."))
                                    .onErrorResume(err -> responseErrorMsg("Unable to store record.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }


    @AuthHasPermission(value = "config_api_v1_currencies_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID currencyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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
                .flatMap(value -> currencyRepository.findByUuidAndDeletedAtIsNull(currencyUUID)
                        .flatMap(previousEntity -> {

                            Integer isoNumber = null;
                            if ((value.containsKey("isoNumber") && (value.getFirst("isoNumber") != ""))) {
                                isoNumber = Integer.valueOf(value.getFirst("isoNumber"));
                            }

                            CurrencyEntity updatedCurrencyEntity = CurrencyEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .currencyName(value.getFirst("currencyName").trim())
                                    .description(value.getFirst("description").trim())
                                    .currency(value.getFirst("currency").trim())
                                    .currencySymbol(value.getFirst("currencySymbol").trim())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .isoNumber(isoNumber)
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .createdBy(previousEntity.getCreatedBy())
                                    .createdAt(previousEntity.getCreatedAt())
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

                            //check if name is unique
                            return currencyRepository.findFirstByCurrencyNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCurrencyEntity.getCurrencyName(), currencyUUID)
                                    .flatMap(checkName -> responseInfoMsg("Name already exist"))
                                    //check if currency is unique
                                    .switchIfEmpty(Mono.defer(() -> currencyRepository.findFirstByCurrencyIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCurrencyEntity.getCurrency(), currencyUUID)
                                            .flatMap(checkLanguageCode -> responseInfoMsg("Currency already exist"))))
                                    .switchIfEmpty(Mono.defer(() ->
                                            //Update previous Currency Entity
                                            currencyRepository.save(previousEntity)
                                                    //Update the Updated Currency Entity
                                                    .then(currencyRepository.save(updatedCurrencyEntity))
                                                    .flatMap(saveLanguageEntity -> responseSuccessMsg("Record Updated successfully!", saveLanguageEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))));
                        })
                        .switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_currencies_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID currencyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

        return currencyRepository.findByUuidAndDeletedAtIsNull(currencyUUID)
//                     Currency exist in company profile
                .flatMap(currencyEntity -> companyProfileRepository.findFirstByCurrencyUUIDAndDeletedAtIsNull(currencyEntity.getUuid())
                                .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete. Reference of record exists."))
//                             If Currency Reference exists in Country
                                .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByCurrencyUUIDAndDeletedAtIsNull(currencyEntity.getUuid())
                                        .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete. Reference of record exists"))))
                                // If Currency uuid exists in student job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student father financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student  mother job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student father job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student sibling financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student sibling job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student mother financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student guardian financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in student guardian job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher mother financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher mother job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher father financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher father job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher sibling financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher sibling job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher child financial history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-financial-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in teacher child job history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-job-histories/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                // If Currency uuid exists in subject fees in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/subject-fees/currency/show/", currencyEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete. Reference of record exists.")))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    currencyEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    currencyEntity.setDeletedBy(UUID.fromString(userUUID));
                                    currencyEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    currencyEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    currencyEntity.setReqDeletedIP(reqIp);
                                    currencyEntity.setReqDeletedPort(reqPort);
                                    currencyEntity.setReqDeletedBrowser(reqBrowser);
                                    currencyEntity.setReqDeletedOS(reqOs);
                                    currencyEntity.setReqDeletedDevice(reqDevice);
                                    currencyEntity.setReqDeletedReferer(reqReferer);

                                    return currencyRepository.save(currencyEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please contact developer."));
    }

    @AuthHasPermission(value = "config_api_v1_currencies_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID currencyUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return currencyRepository.findByUuidAndDeletedAtIsNull(currencyUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CurrencyEntity updatedCurrencyEntity = CurrencyEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .currencyName(previousEntity.getCurrencyName())
                                        .description(previousEntity.getDescription())
                                        .currency(previousEntity.getCurrency())
                                        .currencySymbol(previousEntity.getCurrencySymbol())
                                        .status(status == true ? true : false)
                                        .isoNumber(previousEntity.getIsoNumber())
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

                                return currencyRepository.save(previousEntity)
                                        .then(currencyRepository.save(updatedCurrencyEntity))
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

    //    Custom Functions

}
