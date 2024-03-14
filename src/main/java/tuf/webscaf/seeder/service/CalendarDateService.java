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
import tuf.webscaf.app.dbContext.slave.repositry.SlaveCalendarRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarDateService {
    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    @Autowired
    private SlaveCalendarRepository slaveCalendarRepository;

    public Mono<ServerResponse> seedCalendarDateService(){

        return slaveCalendarRepository.findByNameAndDeletedAtIsNull("Calendar 2022")
                .flatMap(slaveCalendarEntity -> {

                   return slaveCalendarRepository.findByNameAndDeletedAtIsNull("Calendar 2023")
                            .flatMap(slaveCalendarEntity1 -> {
                               return slaveCalendarRepository.findByNameAndDeletedAtIsNull("Calendar 2024")
                                        .flatMap(slaveCalendarEntity2 -> {

                                            List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

                                            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                                            formData.add("calendarUUID", String.valueOf(slaveCalendarEntity.getUuid()));

                                            MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
                                            formData1.add("calendarUUID", String.valueOf(slaveCalendarEntity1.getUuid()));

                                            MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
                                            formData2.add("calendarUUID", String.valueOf(slaveCalendarEntity2.getUuid()));


                                            formDataList.add(formData);
                                            formDataList.add(formData1);
                                            formDataList.add(formData2);

                                            Flux<Boolean> fluxRes = Flux.just(false);

                                            for (int i = 0; i < formDataList.size(); i++) {
                                                Mono<Boolean> res = seederService
                                                        .seedData(configBaseURI+"api/v1/calendar-dates/store", formDataList.get(i));
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
                            });
                });
    }
}
