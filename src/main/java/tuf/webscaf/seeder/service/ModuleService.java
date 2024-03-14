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
public class ModuleService {


    @Autowired
    SeederService seederService;

    @Autowired
    CustomResponse customResponse;

    @Value("${server.erp_config_module.uri}")
    private String configBaseURI;

    public Mono<ServerResponse> seedModuleService(){

        List<MultiValueMap<String, String>> formDataList = new ArrayList<>();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "Auth");
        formData.add("description", "Authentication Module");
        formData.add("status", "true");
        formData.add("slug", "auth");
        formData.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData.add("baseURL", "http://172.15.10.200/auth");
        formData.add("infoURL", "http://172.15.10.195/auth/api/v1/info/show");
        formData.add("hostAddress", "172.15.10.200");
        formData.add("port", "8080");
        formData.add("mversion", "1.0");


        MultiValueMap<String, String> formData1 = new LinkedMultiValueMap<>();
        formData1.add("name", "Config");
        formData1.add("description", "Configuration Module");
        formData1.add("status", "true");
        formData1.add("slug", "config");
        formData1.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData1.add("baseURL", "http://172.15.10.200/config");
        formData1.add("infoURL", "http://172.15.10.195/config/api/v1/info/show");
        formData1.add("hostAddress", "172.15.10.200");
        formData1.add("port", "8080");
        formData1.add("mversion", "1.0");

        MultiValueMap<String, String> formData2 = new LinkedMultiValueMap<>();
        formData2.add("name", "Drive");
        formData2.add("description", "Drive Module");
        formData2.add("status", "true");
        formData2.add("slug", "drive");
        formData2.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData2.add("baseURL", "http://172.15.10.200/drive");
        formData2.add("infoURL", "http://172.15.10.195/drive/api/v1/info/show");
        formData2.add("hostAddress", "172.15.10.200");
        formData2.add("port", "8080");
        formData2.add("mversion", "1.0");


        MultiValueMap<String, String> formData3 = new LinkedMultiValueMap<>();
        formData3.add("name", "Account");
        formData3.add("description", "Account Module");
        formData3.add("status", "true");
        formData3.add("slug", "account");
        formData3.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData3.add("baseURL", "http://172.15.10.200/account");
        formData3.add("infoURL", "http://172.15.10.195/account/api/v1/info/show");
        formData3.add("hostAddress", "172.15.10.200");
        formData3.add("port", "8080");
        formData3.add("mversion", "1.0");

        MultiValueMap<String, String> formData4 = new LinkedMultiValueMap<>();
        formData4.add("name", "Academic");
        formData4.add("description", "Academic Module");
        formData4.add("status", "true");
        formData4.add("slug", "academic");
        formData4.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData4.add("baseURL", "http://172.15.10.200/academic");
        formData4.add("infoURL", "http://172.15.10.195/academic/api/v1/info/show");
        formData4.add("hostAddress", "172.15.10.200");
        formData4.add("port", "8080");
        formData4.add("mversion", "1.0");

        MultiValueMap<String, String> formData5 = new LinkedMultiValueMap<>();
        formData5.add("name", "Student Financial");
        formData5.add("description", "Student Financial Module");
        formData5.add("status", "true");
        formData5.add("slug", "student-financial");
        formData5.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData5.add("baseURL", "http://172.15.10.200/student/financial");
        formData5.add("infoURL", "http://172.15.10.195/student/financial/api/v1/info/show");
        formData5.add("hostAddress", "172.15.10.200");
        formData5.add("port", "8080");
        formData5.add("mversion", "1.0");

        MultiValueMap<String, String> formData6 = new LinkedMultiValueMap<>();
        formData6.add("name", "Inventory");
        formData6.add("description", "Inventory Module");
        formData6.add("status", "true");
        formData6.add("slug", "inventory");
        formData6.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData6.add("baseURL", "http://172.15.10.200/inventory");
        formData6.add("infoURL", "http://172.15.10.195/inventory/api/v1/info/show");
        formData6.add("hostAddress", "172.15.10.200");
        formData6.add("port", "8080");
        formData6.add("mversion", "1.0");

        MultiValueMap<String, String> formData7 = new LinkedMultiValueMap<>();
        formData7.add("name", "sale");
        formData7.add("description", "Sale Module");
        formData7.add("status", "true");
        formData7.add("slug", "sale");
        formData7.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData7.add("baseURL", "http://172.15.10.200/sale");
        formData7.add("infoURL", "http://172.15.10.195/sale/api/v1/info/show");
        formData7.add("hostAddress", "172.15.10.200");
        formData7.add("port", "8080");
        formData7.add("mversion", "1.0");

        MultiValueMap<String, String> formData8 = new LinkedMultiValueMap<>();
        formData8.add("name", "Purchase");
        formData8.add("description", "Purchase Module");
        formData8.add("status", "true");
        formData8.add("slug", "purchase");
        formData8.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData8.add("baseURL", "http://172.15.10.200/purchase");
        formData8.add("infoURL", "http://172.15.10.195/purchase/api/v1/info/show");
        formData8.add("hostAddress", "172.15.10.200");
        formData8.add("port", "8080");
        formData8.add("mversion", "1.0");


        MultiValueMap<String, String> formData9 = new LinkedMultiValueMap<>();
        formData9.add("name", "Gate");
        formData9.add("description", "Gate Module");
        formData9.add("status", "true");
        formData9.add("slug", "gate");
        formData9.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData9.add("baseURL", "http://172.15.10.200/gate");
        formData9.add("infoURL", "http://172.15.10.195/gate/api/v1/info/show");
        formData9.add("hostAddress", "172.15.10.200");
        formData9.add("port", "8080");
        formData9.add("mversion", "1.0");


        MultiValueMap<String, String> formData10 = new LinkedMultiValueMap<>();
        formData10.add("name", "Stock Requisition");
        formData10.add("description", "Stock Requisition Module");
        formData10.add("status", "true");
        formData10.add("slug", "stock-requisition");
        formData10.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData10.add("baseURL", "http://172.15.10.200/stock/requisition");
        formData10.add("infoURL", "http://172.15.10.195/stock/requisition/api/v1/info/show");
        formData10.add("hostAddress", "172.15.10.200");
        formData10.add("port", "8080");
        formData10.add("mversion", "1.0");

        MultiValueMap<String, String> formData11 = new LinkedMultiValueMap<>();
        formData11.add("name", "HR");
        formData11.add("description", "HR Module");
        formData11.add("status", "true");
        formData11.add("slug", "hr");
        formData11.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData11.add("baseURL", "http://172.15.10.200/hr");
        formData11.add("infoURL", "http://172.15.10.195/hr/api/v1/info/show");
        formData11.add("hostAddress", "172.15.10.200");
        formData11.add("port", "8080");
        formData11.add("mversion", "1.0");


        MultiValueMap<String, String> formData12 = new LinkedMultiValueMap<>();
        formData12.add("name", "Employee Financial");
        formData12.add("description", "Employee Financial Module");
        formData12.add("status", "true");
        formData12.add("slug", "emp-financial");
        formData12.add("icon", "8fa23383-8952-4d30-9853-7571be4016e0");
        formData12.add("baseURL", "http://172.15.10.200/emp/financial");
        formData12.add("infoURL", "http://172.15.10.195/emp/financial/api/v1/info/show");
        formData12.add("hostAddress", "172.15.10.200");
        formData12.add("port", "8080");
        formData12.add("mversion", "1.0");

        formDataList.add(formData);
        formDataList.add(formData1);
        formDataList.add(formData2);
        formDataList.add(formData3);
        formDataList.add(formData4);
        formDataList.add(formData5);
        formDataList.add(formData6);
        formDataList.add(formData7);
        formDataList.add(formData8);
        formDataList.add(formData9);
        formDataList.add(formData10);
        formDataList.add(formData11);
        formDataList.add(formData12);

        Flux<Boolean> fluxRes = Flux.just(false);

        for (int i = 0; i < formDataList.size(); i++) {
            Mono<Boolean> res = seederService
                    .seedData(configBaseURI+"api/v1/modules/store", formDataList.get(i));
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