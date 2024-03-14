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
public class CalendarCategoryService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    public Mono<ServerResponse> seedCalendarCategoryService(){

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Islamic");
        formData.add("description", "This is a Islamic Calendar");
        formData.add("status", "true");

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Gregorian");
        formData1.add("description", "This is a Gregorian Calendar");
        formData1.add("status", "true");

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Jewish");
        formData2.add("description", "This is a Jewish Calendar");
        formData2.add("status", "true");

        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Chinese");
        formData3.add("description", "This is a Chinese Calendar");
        formData3.add("status", "true");

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Julian");
        formData4.add("description", "This is a Julian Calendar");
        formData4.add("status", "true");

        MultiValueMap<String, String> formData5 = new LinkedMultiValueMap<>();
        formData5.add("name", "Indian");
        formData5.add("description", "This is a Indian Calendar");
        formData5.add("status", "true");

        formDataList.add(formData);
        formDataList.add(formData1);
        formDataList.add(formData2);
        formDataList.add(formData3);
        formDataList.add(formData4);
        formDataList.add(formData5);

        Flux<Boolean> fluxRes = Flux.just(false);

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<Boolean> res = seederService
                    .seedData(configBaseURI+"api/v1/calendar-categories/store", formDataList.get(i));
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