package tuf.webscaf;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.repositry.ConfigRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveConfigRepository;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.seeder.model.Country;
import tuf.webscaf.seeder.model.Language;
import tuf.webscaf.seeder.service.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
class WebScafApplicationTests {

    @Autowired
    SlaveConfigRepository slaveConfigRepository;

    @Autowired
    JsonToDBService jsonToDBService;




    @Test
    void contextLoads() {

//        Long moduleId = 1L;
//        String searchKeyWord = "aa";
//        int pageSize = 10;
//        Long pageOffset = 10L;
//
//
//        Flux<Object> slaveConfigEntityFlux = slaveConfigRepository.
////                listOfConfigs(moduleId, searchKeyWord, pageSize, pageOffset);
////                listOfConfigs()
//        findAll()
////        log.warn(slaveConfigEntityFlux.toString());
////       slaveConfigEntityFlux
////                .collectList()
//                .flatMapIterable(moduleEntity -> {
////                    if (moduleEntity.isEmpty()) {
////                        log.warn("Record not found");
////                    } else {
////                        log.warn("Record fetched successfully!");
////                    }
//                    log.warn(moduleEntity.toString());
//                  return null;
//                })
//               .onErrorResume(ex -> {
//                   log.warn("excception");
//                   log.warn(String.valueOf(ex));
//                   return Mono.just("sdf");
//               })
//               .switchIfEmpty( Mono.defer(()->{
//                   log.warn("empty");
//                   return Mono.just("Record does not exist");
//               }));
    }
}