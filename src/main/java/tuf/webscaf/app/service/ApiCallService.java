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
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.config.service.response.CustomResponse;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ApiCallService {

    @Autowired
    CustomResponse appresponse;

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${webclient.backend.token}")
    private String token;

    public WebClient initWebClient() {
        try {
            SslContext context = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(t -> t.sslContext(context));

            if (sslStatus.equals("enable")) {
                return WebClient.builder()
                        .clientConnector(
                                new ReactorClientHttpConnector(httpClient)
                        )
                        .build();
            } else {
                return WebClient.builder()
                        .build();
            }
        } catch (SSLException e) {
            return WebClient.builder()
                    .build();
        }
    }

    public Mono<JsonNode> getResponseData(String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getDataWithId(String url, Long id, String userUUID, String reqCompanyUUID, String reqBranchUUID) {
        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + id)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<UUID> checkDocId(JsonNode jsonNode) {
        UUID docId = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("docId") != null) {
                        docId = UUID.fromString(objNode.get("docId").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (docId != null) {
            return Mono.just(docId);
        } else {
            return Mono.empty();
        }
    }

    public Mono<JsonNode> updateDataList(MultiValueMap<String, String> formData, String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.put()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public List<UUID> getUUIDList(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<UUID>>() {
        });
        List<UUID> listOfUUID = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            try {
                listOfUUID = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return listOfUUID;
    }

    public Mono<JsonNode> getDataWithName(String url, String name, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + name)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getData(String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> getDataWithUUID(String url, UUID uuid, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.get()
                .uri(url + uuid)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(jsonData -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public List<Long> getList(JsonNode jsonNode) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<List<Long>>() {
        });
        List<Long> listOfIds = new ArrayList<>();
        if (!jsonNode.get("data").isEmpty()) {
            listOfIds = reader.readValue(objectNode);
        }
        return listOfIds;
    }

    public Mono<String> checkStatus(JsonNode jsonNode) {
        String response = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode objectNode = jsonNode.get("appResponse");
            final JsonNode arrNode = objectNode.get("message");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    response = String.valueOf(objNode.get("message")).replaceAll("\"", "");
                }
            }
        }

        if (response != null) {
            return Mono.just(response);
        } else {
            return Mono.empty();
        }
    }

    public Mono<Long> getModuleId(JsonNode jsonNode) {
        Long moduleId = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("moduleId") != null) {
                        moduleId = Long.valueOf(objNode.get("moduleId").toString().replaceAll("\"", ""));
                    }
                }
            }
        }
        if (moduleId != null) {
            return Mono.just(moduleId);
        } else {
            return Mono.empty();
        }
    }

    public Mono<UUID> getModuleUUID(JsonNode jsonNode) {
        UUID moduleUUID = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("moduleUUID") != null) {
                        moduleUUID = UUID.fromString((objNode.get("moduleUUID").toString().replaceAll("\"", "")));
                    }
                }
            }
        }
        if (moduleUUID != null) {
            return Mono.just(moduleUUID);
        } else {
            return Mono.empty();
        }
    }

    public Mono<UUID> getUUID(JsonNode jsonNode) {
        UUID uuid = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("uuid") != null) {
                        uuid = UUID.fromString(objNode.get("uuid").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (uuid != null) {
            return Mono.just(uuid);
        } else {
            return Mono.empty();
        }
    }

    public Mono<Long> getId(JsonNode jsonNode) {
        Long id = null;
        Integer status = Integer.valueOf(jsonNode.get("status").toString());
        if (status.equals(200)) {
            final JsonNode arrNode = jsonNode.get("data");
            if (arrNode.isArray()) {
                for (final JsonNode objNode : arrNode) {
                    if (objNode.get("id") != null) {
                        id = Long.valueOf(objNode.get("id").toString().replaceAll("\"", ""));
                    }
                }
            }
        }

        if (id != null) {
            return Mono.just(id);
        } else {
            return Mono.empty();
        }
    }


    public Mono<JsonNode> postDataList(MultiValueMap<String, String> formData, String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> putDataList(MultiValueMap<String, String> formData, UUID uuid, String url, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.put()
                .uri(url + uuid)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }

    public Mono<JsonNode> deleteData(String url, UUID uuid, String userUUID, String reqCompanyUUID, String reqBranchUUID) {

        WebClient webClient = initWebClient();
        return webClient.delete()
                .uri(url + uuid)
                .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("auid", userUUID)
                .header("reqCompanyUUID", reqCompanyUUID)
                .header("reqBranchUUID", reqBranchUUID)
                .retrieve()
                .bodyToMono(JsonNode.class).flatMap(jsonData -> {

                    ObjectMapper objectMapper = new ObjectMapper();

                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(jsonData.toString());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(jsonNode);

                });
    }


}
