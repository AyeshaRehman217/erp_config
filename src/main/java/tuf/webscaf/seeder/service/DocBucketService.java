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
public class DocBucketService {

    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    public Mono<ServerResponse> seedDocBucketService(){

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "DocBucket A");
        formData.add("slug", "doc-bucket-a");
        formData.add("description", "This is a Doc Bucket");
        formData.add("status", "true");
        formData.add("url", "https://trello.com/docbucket/a");
        formData.add("port", "1234");

        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "DocBucket B");
        formData1.add("slug", "doc-bucket-b");
        formData1.add("description", "This is a Doc Bucket");
        formData1.add("status", "true");
        formData1.add("url", "https://trello.com/docbucket/b");
        formData1.add("port", "1235");

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "DocBucket C");
        formData2.add("slug", "doc-bucket-c");
        formData2.add("description", "This is a Doc Bucket");
        formData2.add("status", "true");
        formData2.add("url", "https://trello.com/docbucket/c");
        formData2.add("port", "1236");


        formDataList.add(formData);
        formDataList.add(formData1);
        formDataList.add(formData2);

        Flux<Boolean> fluxRes = Flux.just(false);

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<Boolean> res = seederService
                    .seedData(configBaseURI+"api/v1/doc-buckets/store", formDataList.get(i));
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
