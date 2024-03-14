package tuf.webscaf.seeder.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.seeder.model.Country;
import tuf.webscaf.seeder.model.Language;
import tuf.webscaf.seeder.service.*;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class SeederHandler {

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    JsonToDBService jsonToDBService;

    @Autowired
    LanguageService languageService;

    @Autowired
    CompanyService companyService;

    @Autowired
    BranchService branchService;

    @Autowired
    locationService locationService;

    @Autowired
    CalendarCategoryService calendarCategoryService;

    @Autowired
    DocBucketService docBucketService;

    @Autowired
    AdministrationDepartmentService administrationDepartmentService;

    @Autowired
    CalendarService calendarService;

    @Autowired
    CalendarDateService calendarDateService;

    @Autowired
    ModuleService moduleService;

    public Mono<ServerResponse> storeCountries(ServerRequest serverRequest) {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Country>> typeReference = new TypeReference<List<Country>>() {
        };
        InputStream inputStream = TypeReference.class.getResourceAsStream("/data/countries.json");

        try {
            List<Country> countryList = mapper.readValue(inputStream, typeReference);
            Mono<String> res = jsonToDBService.saveTo(countryList);

            var messages = List.of(
                    new AppResponseMessage(
                            AppResponse.Response.SUCCESS,
                            "Successful"
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
                    res
            );
        } catch (Exception e) {
            var messages = List.of(
                    new AppResponseMessage(
                            AppResponse.Response.ERROR,
                            "Seeder fail to seed DB"
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
                    Mono.just("")
            );
        }
    }

    public Mono<ServerResponse> storeLanguages(ServerRequest serverRequest) {

        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Language>> typeReference = new TypeReference<Map<String, Language>>() {
        };
        InputStream inputStream = TypeReference.class.getResourceAsStream("/data/languages.json");
        try {
            Map<String, Language> languageMap = (Map<String, Language>) mapper.readValue(inputStream, typeReference);

            Mono<String> res = languageService.saveAllLanguages(languageMap);

            var messages = List.of(
                    new AppResponseMessage(
                            AppResponse.Response.SUCCESS,
                            "Successful"
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
                    res
            );
        } catch (Exception e) {
            var messages = List.of(
                    new AppResponseMessage(
                            AppResponse.Response.ERROR,
                            "Seeder fail to seed DB"
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
                    Mono.just("")
            );
        }
    }

    public Mono<ServerResponse> storeCompanies(ServerRequest serverRequest) {

        Mono<String> res = companyService.saveAllCompanies();

        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
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
                res
        );
    }

    public Mono<ServerResponse> storeBranches(ServerRequest serverRequest) {

        return branchService.seedBranchService();

    }

    public Mono<ServerResponse> storeOffices(ServerRequest serverRequest) {

        Mono<String> res = locationService.saveAllOffices();

        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
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
                res
        );
    }

    public Mono<ServerResponse> storeCalendarCategory(ServerRequest serverRequest) {
        return calendarCategoryService
                .seedCalendarCategoryService();
    }

    public Mono<ServerResponse> storeDocBucket(ServerRequest serverRequest) {
        return docBucketService
                .seedDocBucketService();
    }

    public Mono<ServerResponse> storeAdministrationDepartment(ServerRequest serverRequest) {
        return administrationDepartmentService
                .seedAdmissionDepartmentService();
    }

    public Mono<ServerResponse> storeCalendar(ServerRequest serverRequest) {
        return calendarService
                .seedCalendarService();
    }

    public Mono<ServerResponse> storeCalendarDate(ServerRequest serverRequest) {
        return calendarDateService
                .seedCalendarDateService();
    }

    public Mono<ServerResponse> storeModule(ServerRequest serverRequest) {
        return moduleService
                .seedModuleService();
    }

}