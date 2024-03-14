package tuf.webscaf.app.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAdministrativeDepartmentDto;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveSubAdministrationDepartmentRepository;
import tuf.webscaf.config.service.response.CustomResponse;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class DepartmentSubDepartmentApiService {

    @Autowired
    SlaveSubAdministrationDepartmentRepository slaveSubAdministrativeDepartment;

    //Getting sub-items(Child) of all the given item(Parent)
    public Mono<List<UUID>> gettingSubAdministrativeDepartmentList(UUID departmentUUID) {

        return slaveSubAdministrativeDepartment.showAllSubAdministrativeDepartmentOfDept(departmentUUID)
                .collectList()
                .flatMap(childSubAdministrativeRecords -> {
                    List<UUID> childSubDepartmentUUIDList = new ArrayList<>();
                    for (SlaveSubAdministrativeDepartmentDto child : childSubAdministrativeRecords) {
                        childSubDepartmentUUIDList.add(child.getSubAdministrativeDeptUUID());
                    }
                    return Mono.just(childSubDepartmentUUIDList);
                });
    }

}
