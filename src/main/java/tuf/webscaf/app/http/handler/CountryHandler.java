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
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCountryTimezonePvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveTimezoneRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveTranslationRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Tag(name = "countryHandler")
public class CountryHandler {

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SlaveCountryRepository slaveCountryRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    BranchProfileRepository branchProfileRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    CountryTimezonePvtRepository countryTimeZonePvtRepository;

    @Autowired
    SlaveCountryTimezonePvtRepository slaveCountryTimeZonePvtRepository;

    @Autowired
    CountryTranslationPvtRepository countryTranslationPvtRepository;

    @Autowired
    TimezoneRepository timeZoneRepository;

    @Autowired
    SlaveTimezoneRepository slaveTimeZoneRepository;

    @Autowired
    SlaveTranslationRepository slaveTranslationRepository;

    @Autowired
    TranslationRepository translationRepository;

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "config_api_v1_countries_index")
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

            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        } else {
            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository.countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull
                                    (searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer"));
        }

    }

    @AuthHasPermission(value = "config_api_v1_countries_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID countryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveCountryRepository.findByUuidAndDeletedAtIsNull(countryUUID)
                .flatMap(value1 -> responseSuccessMsg("Record Fetched Successfully", value1))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }


    //Show Countries for timezone id
    @AuthHasPermission(value = "config_api_v1_countries_timezone_mapped_show")
    public Mono<ServerResponse> listOfCountriesForTimezone(ServerRequest serverRequest) {

        final UUID timezoneUUID = UUID.fromString(serverRequest.pathVariable("timezoneUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }
        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        if (!status.isEmpty()) {
            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .listOfCountriesAgainstTimezoneWithStatusFilter(timezoneUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository.countCountryAgainstTimezoneWithStatusFilter(searchKeyWord, searchKeyWord, timezoneUUID, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .listOfCountriesAgainstTimezone(timezoneUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository.countCountryAgainstTimezone(searchKeyWord, searchKeyWord, timezoneUUID)
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    /**
     * This Function List Mapped Countries for given Translation UUID
     **/
    @AuthHasPermission(value = "config_api_v1_countries_translation_mapped_show")
    public Mono<ServerResponse> listOfCountriesForTranslation(ServerRequest serverRequest) {
        final UUID translationUUID = UUID.fromString(serverRequest.pathVariable("translationUUID").trim());

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        //Optional Query Parameter Based of Status
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("size").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("page").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;
        if (page < 0) {
            return responseErrorMsg("Invalid Page No");
        }
        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        Pageable pageable = PageRequest.of(page, size);

        if (!status.isEmpty()) {
            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .listOfCountriesAgainstTranslationWithStatusFilter
                            (translationUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status), pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository.countCountryAgainstTranslationWithStatusFilter
                                    (translationUUID, searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        } else {
            Flux<SlaveCountryEntity> slaveCountryEntityFlux = slaveCountryRepository
                    .listOfCountriesAgainstTranslation(translationUUID, searchKeyWord, searchKeyWord, pageable.getPageSize(), pageable.getOffset(), directionProperty, d);
            return slaveCountryEntityFlux
                    .collectList()
                    .flatMap(countryEntity -> slaveCountryRepository.countCountryAgainstTranslation(translationUUID, searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (countryEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count, 0L);

                                } else {

                                    return responseIndexSuccessMsg("All Records fetched successfully!", countryEntity, count, 0L);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
        }
    }

    @AuthHasPermission(value = "config_api_v1_countries_store")
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

                    // get encoded value of NativeName from request
                    String encodedNativeName = value.getFirst("nativeName");

                    String decodedNativeName = "";

                    // decode the value of grade
                    try {
                        decodedNativeName = URLDecoder.decode(encodedNativeName, "UTF-8");
                    } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }


                    // get encoded value of Phone Code from request
                    String encodedPhoneCode = value.getFirst("phoneCode");

                    String decodedPhoneCode = "";

                    // decode the value of grade
                    try {
                        decodedPhoneCode = URLDecoder.decode(encodedPhoneCode, "UTF-8");
                    } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }


                    // get encoded value of Emoji from request
                    String encodedEmoji = value.getFirst("emoji");

                    String decodedEmoji = "";

                    // decode the value of emoji
                    try {
                        decodedEmoji = URLDecoder.decode(encodedEmoji, "UTF-8");
                    } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }


                    // get encoded value of Emoji Unicode from request
                    String encodedEmojiUniCode = value.getFirst("emojiU");

                    String decodedEmojiUniCode = "";

                    // decode the value of grade
                    try {
                        decodedEmojiUniCode = URLDecoder.decode(encodedEmojiUniCode, "UTF-8");
                    } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                        e.printStackTrace();
                    }

                    Double longitude = null;

                    Double latitude = null;

                    if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                        longitude = Double.valueOf(value.getFirst("longitude").trim());
                    }
                    if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                        latitude = Double.valueOf(value.getFirst("latitude").trim());
                    }

                    CountryEntity countryEntity = CountryEntity.builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .iso2(value.getFirst("iso2").trim().toUpperCase())
                            .iso3(value.getFirst("iso3").trim().toUpperCase())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .numericCode(Integer.valueOf(value.getFirst("numericCode")))
                            .phoneCode(decodedPhoneCode)
                            .capital(value.getFirst("capital").trim())
                            .tld(value.getFirst("tld").trim())
                            .nativeName(decodedNativeName)
                            .longitude(longitude)
                            .latitude(latitude)
                            .emoji(decodedEmoji)
                            .emojiU(decodedEmojiUniCode)
                            .currencyUUID(UUID.fromString(value.getFirst("currencyUUID").trim()))
                            .regionUUID(UUID.fromString(value.getFirst("regionUUID").trim()))
                            .subRegionUUID(UUID.fromString(value.getFirst("subRegionUUID").trim()))
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
                    return countryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(countryEntity.getName())
                            .flatMap(officeRecord -> responseInfoMsg("Name Already Exist!"))
                            //check if ISO 2 is unique
                            .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByIso2IgnoreCaseAndDeletedAtIsNull(countryEntity.getIso2())
                                    .flatMap(checkCurrencyCode -> responseInfoMsg("Iso2 already exist"))))
                            //check if ISO 3 is unique
                            .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByIso3IgnoreCaseAndDeletedAtIsNull(countryEntity.getIso3())
                                    .flatMap(checkIsoNumberCode -> responseInfoMsg("Iso3 already exist"))))
                            //check if Numeric Code is unique
                            .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByNumericCodeIgnoreCaseAndDeletedAtIsNull(countryEntity.getNumericCode())
                                    .flatMap(checkIsoNumberCode -> responseInfoMsg("Numeric Code already exist"))))
                            //check if Currency exists
                            .switchIfEmpty(Mono.defer(() -> currencyRepository.findByUuidAndDeletedAtIsNull(countryEntity.getCurrencyUUID())
                                    //check if Region exists
                                    .flatMap(checkRegion -> regionRepository.findByUuidAndDeletedAtIsNull(countryEntity.getRegionUUID())
                                            //check if Sub region exists
                                            .flatMap(checkSubRegion -> subRegionRepository.findByUuidAndDeletedAtIsNull(countryEntity.getSubRegionUUID())
                                                    //Save Country
                                                    .flatMap(save -> countryRepository.save(countryEntity)
                                                            .flatMap(saveCountryEntity -> responseSuccessMsg("Record stored successfully!", saveCountryEntity))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please contact developer."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                                    )
                                                    .switchIfEmpty(responseInfoMsg("Sub-Region does not exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("Sub-Region does not exist.Please Contact Developer."))
                                            )
                                            .switchIfEmpty(responseInfoMsg("Region does not exist"))
                                            .onErrorResume(ex -> responseErrorMsg("Region does not exist.Please Contact Developer."))
                                    )
                                    .switchIfEmpty(responseInfoMsg("Currency does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Currency does not exist.Please Contact Developer."))));
                }).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));

    }

    @AuthHasPermission(value = "config_api_v1_countries_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID countryUUID = UUID.fromString((serverRequest.pathVariable("uuid")).trim());

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
                .flatMap(value -> countryRepository.findByUuidAndDeletedAtIsNull(countryUUID)
                        .flatMap(previousCountryEntity -> {


                            // get encoded value of NativeName from request
                            String encodedNativeName = value.getFirst("nativeName");

                            String decodedNativeName = "";

                            // decode the value of grade
                            try {
                                decodedNativeName = URLDecoder.decode(encodedNativeName, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }


                            // get encoded value of Phone Code from request
                            String encodedPhoneCode = value.getFirst("phoneCode");

                            String decodedPhoneCode = "";

                            // decode the value of grade
                            try {
                                decodedPhoneCode = URLDecoder.decode(encodedPhoneCode, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }


                            // get encoded value of Emoji from request
                            String encodedEmoji = value.getFirst("emoji");

                            String decodedEmoji = "";

                            // decode the value of emoji
                            try {
                                decodedEmoji = URLDecoder.decode(encodedEmoji, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }


                            // get encoded value of Emoji Unicode from request
                            String encodedEmojiUniCode = value.getFirst("emojiU");

                            String decodedEmojiUniCode = "";

                            // decode the value of grade
                            try {
                                decodedEmojiUniCode = URLDecoder.decode(encodedEmojiUniCode, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }


                            Double longitude = null;
                            Double latitude = null;
                            if ((value.containsKey("longitude") && (value.getFirst("longitude") != ""))) {
                                longitude = Double.valueOf(value.getFirst("longitude").trim());
                            }
                            if ((value.containsKey("latitude") && (value.getFirst("latitude") != ""))) {
                                latitude = Double.valueOf(value.getFirst("latitude").trim());
                            }

                            CountryEntity updatedCountryEntity = CountryEntity.builder()
                                    .uuid(previousCountryEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .iso2(value.getFirst("iso2").trim().toUpperCase())
                                    .iso3(value.getFirst("iso3").trim().toUpperCase())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .numericCode(Integer.valueOf(value.getFirst("numericCode")))
                                    .phoneCode(decodedPhoneCode)
                                    .capital(value.getFirst("capital").trim())
                                    .tld(value.getFirst("tld").trim())
                                    .nativeName(decodedNativeName)
                                    .longitude(longitude)
                                    .latitude(latitude)
                                    .emoji(decodedEmoji)
                                    .emojiU(decodedEmojiUniCode)
                                    .currencyUUID(UUID.fromString(value.getFirst("currencyUUID").trim()))
                                    .regionUUID(UUID.fromString(value.getFirst("regionUUID").trim()))
                                    .subRegionUUID(UUID.fromString(value.getFirst("subRegionUUID").trim()))
                                    .createdBy(previousCountryEntity.getCreatedBy())
                                    .createdAt(previousCountryEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .reqCreatedIP(previousCountryEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousCountryEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousCountryEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousCountryEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousCountryEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousCountryEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousCountryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousCountryEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousCountryEntity.setReqDeletedIP(reqIp);
                            previousCountryEntity.setReqDeletedPort(reqPort);
                            previousCountryEntity.setReqDeletedBrowser(reqBrowser);
                            previousCountryEntity.setReqDeletedOS(reqOs);
                            previousCountryEntity.setReqDeletedDevice(reqDevice);
                            previousCountryEntity.setReqDeletedReferer(reqReferer);

                            //check if name is unique
                            return countryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCountryEntity.getName(), updatedCountryEntity.getUuid())
                                    .flatMap(officeRecord -> responseInfoMsg("Name Already Exist!"))
                                    //check if ISO 2 is unique
                                    .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByIso2IgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCountryEntity.getIso2(), updatedCountryEntity.getUuid())
                                            .flatMap(checkCurrencyCode -> responseInfoMsg("Iso2 already exist"))))
                                    //check if ISO 3 is unique
                                    .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByIso3IgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCountryEntity.getIso3(), updatedCountryEntity.getUuid())
                                            .flatMap(checkIsoNumberCode -> responseInfoMsg("Iso3 already exist"))))
                                    //check if Numeric Code is unique
                                    .switchIfEmpty(Mono.defer(() -> countryRepository.findFirstByNumericCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedCountryEntity.getNumericCode(), updatedCountryEntity.getUuid())
                                            .flatMap(checkIsoNumberCode -> responseInfoMsg("Numeric Code already exist"))))
                                    //check if Currency UUID exists
                                    .switchIfEmpty(Mono.defer(() -> currencyRepository.findByUuidAndDeletedAtIsNull(updatedCountryEntity.getCurrencyUUID())
                                            //check if Region UUID exists
                                            .flatMap(checkRegion -> regionRepository.findByUuidAndDeletedAtIsNull(updatedCountryEntity.getRegionUUID())
                                                    //check if Sub Region UUID exists
                                                    .flatMap(checkSubRegion -> subRegionRepository.findByUuidAndDeletedAtIsNull(updatedCountryEntity.getSubRegionUUID())
                                                            .flatMap(regionEntity -> countryRepository.save(previousCountryEntity)
                                                                    .then(countryRepository.save(updatedCountryEntity))
                                                                    .flatMap(subRegionSave -> responseSuccessMsg("Record updated successfully!", subRegionSave))
                                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                                            )
                                                            .switchIfEmpty(responseInfoMsg("Sub-Region does not exist"))
                                                            .onErrorResume(ex -> responseErrorMsg("Sub-Region does not exist.Please Contact Developer.")))
                                                    .switchIfEmpty(responseInfoMsg("Region does not exist"))
                                                    .onErrorResume(ex -> responseErrorMsg("Region does not exist.Please Contact Developer.")))
                                            .switchIfEmpty(responseInfoMsg("Currency does not exist"))
                                            .onErrorResume(ex -> responseErrorMsg("Currency does not exist.Please Contact Developer."))));
                        })
                        .switchIfEmpty(Mono.defer(() -> responseInfoMsg("Record does not exist")))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer.")))
                .switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request.Please Contact Developer."));
    }

    // Delete function for country
    @AuthHasPermission(value = "config_api_v1_countries_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {

        UUID countryUUID = UUID.fromString((serverRequest.pathVariable("uuid")).trim());

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

        return countryRepository.findByUuidAndDeletedAtIsNull(countryUUID)
                         // If Country Reference exists in Company Profile
                .flatMap(countryEntity -> companyProfileRepository.findFirstByCurrencyUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                .flatMap(companyProfileEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))
//                                 //If Country Reference exists in Branch Profile
//                                .switchIfEmpty(Mono.defer(() -> branchProfileRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
//                                        .flatMap(branchProfileEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                 // If Country Reference exists in location Table
                                .switchIfEmpty(Mono.defer(() -> locationRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                        .flatMap(officeEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                // If Country Reference exists in Country Table
                                .switchIfEmpty(Mono.defer(() -> stateRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                        .flatMap(stateEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                // If Country Reference exists in City Table
                                .switchIfEmpty(Mono.defer(() -> cityRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                        .flatMap(cityEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                // If Country Reference exists in country-timezone-pvt Table
                                .switchIfEmpty(Mono.defer(() -> countryTimeZonePvtRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                        .flatMap(cityEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                // If Country Reference exists in country-translation-pvt Table
                                .switchIfEmpty(Mono.defer(() -> countryTranslationPvtRepository.findFirstByCountryUUIDAndDeletedAtIsNull(countryEntity.getUuid())
                                        .flatMap(cityEntity -> responseInfoMsg("Unable to delete.Reference of record exists."))))
                                // If Country uuid exists in student profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-academic-records/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student guardian profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in nationalities in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/nationalities/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher mother profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-mother-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student guardian academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-guardian-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher father profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher father academic records in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-father-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher sibling profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student mother academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-mother-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-sibling-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher child profiles in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-profiles/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher child academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-child-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in teacher academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/teacher-academic-records/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student father academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-father-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                // If Country uuid exists in student sibling academic history in academic module
                                .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/student-sibling-academic-histories/country/show/", countryEntity.getUuid(), userUUID, reqCompanyUUID, reqBranchUUID)
                                        .flatMap(jsonNode -> apiCallService.checkStatus(jsonNode)
                                                .flatMap(checkBranchIDApiMsg -> responseInfoMsg("Unable to delete.Reference of record exists.")))))
                                .switchIfEmpty(Mono.defer(() -> {

                                    countryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    countryEntity.setDeletedBy(UUID.fromString(userUUID));
                                    countryEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                    countryEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                    countryEntity.setReqDeletedIP(reqIp);
                                    countryEntity.setReqDeletedPort(reqPort);
                                    countryEntity.setReqDeletedBrowser(reqBrowser);
                                    countryEntity.setReqDeletedOS(reqOs);
                                    countryEntity.setReqDeletedDevice(reqDevice);
                                    countryEntity.setReqDeletedReferer(reqReferer);

                                    return countryRepository.save(countryEntity)
                                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                                }))
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "config_api_v1_countries_status")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        final UUID countryUUID = UUID.fromString(serverRequest.pathVariable("uuid").trim());

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

                    return countryRepository.findByUuidAndDeletedAtIsNull(countryUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                CountryEntity updatedCountryEntity = CountryEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
                                        .iso2(previousEntity.getIso2())
                                        .iso3(previousEntity.getIso3())
                                        .status(status == true ? true : false)
                                        .numericCode(previousEntity.getNumericCode())
                                        .phoneCode(previousEntity.getPhoneCode())
                                        .capital(previousEntity.getCapital())
                                        .tld(previousEntity.getTld())
                                        .nativeName(previousEntity.getNativeName())
                                        .longitude(previousEntity.getLongitude())
                                        .latitude(previousEntity.getLatitude())
                                        .emoji(previousEntity.getEmoji())
                                        .emojiU(previousEntity.getEmojiU())
                                        .currencyUUID(previousEntity.getCurrencyUUID())
                                        .regionUUID(previousEntity.getRegionUUID())
                                        .subRegionUUID(previousEntity.getSubRegionUUID())
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

                                return countryRepository.save(previousEntity)
                                        .then(countryRepository.save(updatedCountryEntity))
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
