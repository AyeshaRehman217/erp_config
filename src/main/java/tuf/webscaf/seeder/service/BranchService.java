package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchEntity;
import tuf.webscaf.app.dbContext.master.entity.BranchProfileEntity;
import tuf.webscaf.app.dbContext.master.repositry.BranchProfileRepository;
import tuf.webscaf.app.dbContext.master.repositry.BranchRepository;
import tuf.webscaf.app.dbContext.master.repositry.CityRepository;
import tuf.webscaf.app.dbContext.master.repositry.CompanyRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.seeder.model.Branch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BranchService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;


    public Mono<ServerResponse> seedBranchService(){

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "TUF Medical Campus");
        formData.add("description", "Medical Campus branch of TUF");
        formData.add("status", "true");
        formData.add("establishmentDate", "25-11-2022 09:42:29");
        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData.add("locationUUID", "385e0547-7253-49c3-b940-fcf57c3a66cc");
        formData.add("languageUUID", "90a6e568-6165-441e-a94d-d200a1489d3e");


        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "TUF Engineering Campus");
        formData1.add("description", "Engineering Campus branch of TUF");
        formData1.add("status", "true");
        formData1.add("establishmentDate", "14-12-2022 09:42:29");
        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");
        formData1.add("locationUUID", "385e0547-7253-49c3-b940-fcf57c3a66cc");
        formData1.add("languageUUID", "90a6e568-6165-441e-a94d-d200a1489d3e");

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "GIU Medical Campus");
        formData2.add("description", "Medical Campus branch of GIU");
        formData2.add("status", "true");
        formData2.add("establishmentDate", "01-01-2023 09:42:29");
        formData2.add("companyUUID", "6b7f7aee-b1e3-4b96-b6ee-12c0b351a9f9");
        formData2.add("locationUUID", "385e0547-7253-49c3-b940-fcf57c3a66cc");
        formData2.add("languageUUID", "90a6e568-6165-441e-a94d-d200a1489d3e");


        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "GIU Engineering Campus");
        formData3.add("description", "Engineering Campus branch of GIU");
        formData3.add("status", "true");
        formData3.add("establishmentDate", "10-01-2023 09:42:29");
        formData3.add("companyUUID", "6b7f7aee-b1e3-4b96-b6ee-12c0b351a9f9");
        formData3.add("locationUUID", "385e0547-7253-49c3-b940-fcf57c3a66cc");
        formData3.add("languageUUID", "90a6e568-6165-441e-a94d-d200a1489d3e");



        formDataList.add(formData);
        formDataList.add(formData1);
        formDataList.add(formData2);
        formDataList.add(formData3);

        Flux<Boolean> fluxRes = Flux.just(false);

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<Boolean> res = seederService
                    .seedData(configBaseURI+"api/v1/branches/store", formDataList.get(i));
            fluxRes = fluxRes.concatWith(res);
        }
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        "Successful"
                )
        );

        return customResponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                fluxRes.last()
        );
    }
}