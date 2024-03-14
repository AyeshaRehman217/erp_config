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
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCalendarCategoryRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    @Autowired
    private SlaveCalendarCategoryRepository slaveCalendarCategoryRepository;

    public Mono<ServerResponse> seedCalendarService(){

        return slaveCalendarCategoryRepository.findByNameAndDeletedAtIsNull("Gregorian")
                .flatMap(slaveCalendarCategoryEntity -> {
                    List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

                    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

                    formData.add("date", "01-01-2022");
                    formData.add("calendarCategoryUUID", String.valueOf(slaveCalendarCategoryEntity.getUuid()));

                    MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
                    formData1.add("date", "01-01-2023");
                    formData1.add("calendarCategoryUUID", String.valueOf(slaveCalendarCategoryEntity.getUuid()));

                    MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
                    formData2.add("date", "01-01-2024");
                    formData2.add("calendarCategoryUUID", String.valueOf(slaveCalendarCategoryEntity.getUuid()));


                    formDataList.add(formData);
                    formDataList.add(formData1);
                    formDataList.add(formData2);

                    Flux<Boolean> fluxRes = Flux.just(false);

                    for (int i = 0; i < formDataList.size(); i++) {
                        Mono<Boolean> res = seederService
                                .seedData(configBaseURI+"api/v1/calendars/store", formDataList.get(i));
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
                });
    }
}
