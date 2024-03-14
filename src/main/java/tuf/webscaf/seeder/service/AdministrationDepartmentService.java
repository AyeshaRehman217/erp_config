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
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdministrationDepartmentService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    public Mono<ServerResponse> seedAdmissionDepartmentService(){

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Student Affairs");
        formData.add("description", "This is a Administration Department");
        formData.add("status", "true");
        formData.add("code", "01");
        formData.add("shortName", "SA");
        formData.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Examination");
        formData1.add("description", "This is a Administration Department");
        formData1.add("status", "true");
        formData1.add("code", "02");
        formData1.add("shortName", "EX");
        formData1.add("companyUUID", "fe9a7354-b784-4a1f-854f-2240d4457c93");

        formDataList.add(formData);
        formDataList.add(formData1);

        Flux<Boolean> fluxRes = Flux.just(false);

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<Boolean> res = seederService
                    .seedData(configBaseURI+"api/v1/administration-departments/store", formDataList.get(i));
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