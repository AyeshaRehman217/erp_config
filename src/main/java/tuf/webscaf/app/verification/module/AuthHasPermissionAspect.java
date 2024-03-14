package tuf.webscaf.app.verification.module;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Aspect
@Component
public class AuthHasPermissionAspect {

    @Value("${server.ssl-status}")
    private String sslStatus;

    @Value("${app.verification.url}")
    private String verificationURL;


    String token = null;
    String type = null;
    String auid = null;
    String moduleUUID = null;
    String reqCompanyUUID = null;
    String reqBranchUUID = null;

    @Around("@annotation(authHasPermission)")
    public Object doAround(ProceedingJoinPoint joinPoint, AuthHasPermission authHasPermission) {
        Object[] args = joinPoint.getArgs();
        for (Object obj : args) {
            if (obj instanceof ServerRequest) {
                token = ((ServerRequest) obj).headers().firstHeader("Authorization");
                type = ((ServerRequest) obj).headers().firstHeader("tokenType");
                auid = ((ServerRequest) obj).headers().firstHeader("auid");
                moduleUUID = ((ServerRequest) obj).headers().firstHeader("moduleUUID");
                reqCompanyUUID = ((ServerRequest) obj).headers().firstHeader("reqCompanyUUID");
                reqBranchUUID = ((ServerRequest) obj).headers().firstHeader("reqBranchUUID");
            }
        }

        if (token != null) {
            if (token.equals("Bearer "+"eyJhbGciOiJQUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.ecyIfJCrVTu4WP6qdPLpOqfyOLZjL7Qv9UvS4Ifdqt6F_i4jkBPsteSEcr97TyRf_MeKl4nsddup2RE6SvnUpjWyPXbwS9Gx1JZsKemPe8bqc6961mbn7J-iF6xnXlBLmt0zz3YNpCQMlhS4qU-hYRHJmGtkQaBxFXhowyQ1Ii1Kw69P42IX7Sa4e4tJrcu2IO_9koR6B2zYEs4uzMaLw-ThBxCg-RetvoE9BY0h2oeOKlutmGnCvcpVLchPCSCJXM9vDRITjuB-TUcteyVDs9uTjln4v5j10fJFuWkOzr5aXGDsUnUQ8zzvHt5hz_nIUs8RIX-tg4-8PmgGcL_xzw")) {
                try {
                    return (Mono) joinPoint.proceed();
                } catch (Throwable e) {
                    return Mono.from(ServerResponse.status(HttpStatus.FORBIDDEN).build());
                }
            }

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("auid", auid);
            formData.add("moduleUUID", moduleUUID);
            formData.add("reqCompanyUUID", reqCompanyUUID);
            formData.add("reqBranchUUID", reqBranchUUID);
            formData.add("perm", authHasPermission.value());
            formData.add("tokenType", type);
            WebClient webClient = initWebClient();
            return webClient.post()
                    .uri(verificationURL)
                    .header("Authorization", token)
                    .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(JsonNode.class).flatMap(value -> {
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            JsonNode jsonNode = objectMapper.readTree(value.toString());

                            JsonNode objectNode = jsonNode.get("appResponse");
                            JsonNode arrNode = objectNode.get("message");
                            Integer msgCode = Integer.valueOf(arrNode.get(0).get("messageCode").toString());
                            if (msgCode == 99200) {
                                JsonNode dataNode = jsonNode.get("data");
                                if (dataNode.isArray()) {
                                    for (JsonNode objNode : dataNode) {
                                        if (objNode.get("isPerm") != null) {
                                            if (objNode.get("isPerm").asBoolean()) {
                                                try {
                                                    return (Mono) joinPoint.proceed();
                                                } catch (Throwable e) {
                                                    return Mono.from(ServerResponse.status(HttpStatus.FORBIDDEN).build());
                                                }
                                            } else {
                                                return Mono.from(ServerResponse.status(HttpStatus.FORBIDDEN).build());
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (JsonProcessingException e) {
                            return Mono.from(ServerResponse.status(HttpStatus.FORBIDDEN).build());
                        }
                        return Mono.from(ServerResponse.status(HttpStatus.FORBIDDEN).build());
                    }).onErrorResume(ex -> Mono.from(ServerResponse.status(HttpStatus.UNAUTHORIZED).build()))
                    .switchIfEmpty(Mono.from(ServerResponse.status(HttpStatus.UNAUTHORIZED).build()));
        } else {
            return Mono.from(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
        }
    }

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

}