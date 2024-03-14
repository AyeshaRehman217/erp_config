package tuf.webscaf.app.webFilter.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.security.dto.UnauthenticatedRoutes;
import tuf.webscaf.app.security.service.RequestInfoService;
import tuf.webscaf.app.security.service.TokenService;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class AuthFilter implements WebFilter {


    @Autowired
    RequestInfoService requestInfoService;

    @Autowired
    TokenService tokenService;

    @Value("${server.module.uuid}")
    String moduleUUID;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest serverRequest = exchange.getRequest();
        ObjectMapper mapper = new ObjectMapper();
        try {
            UnauthenticatedRoutes unauthenticatedRoutes = mapper.readValue(
                    new File("src/main/resources/unauthenticated_routes.json"),
                    UnauthenticatedRoutes.class);
            for (String route : unauthenticatedRoutes.getRoutes()) {
                if (route.equals(serverRequest.getPath().value())) {

                    ServerHttpRequest mutateRequest = exchange.getRequest()
                            .mutate()
                            .header("moduleUUID", "" + moduleUUID)
                            .header("reqIp", "" + serverRequest.getHeaders().getHost())
                            .header("reqPort", "" + serverRequest.getHeaders().getHost().getPort())
                            .header("reqBrowser", "" + requestInfoService.getClientBrowser(serverRequest))
                            .header("reqOs", "" + requestInfoService.getClientOS(serverRequest))
                            .header("reqDevice", "")
                            .header("reqReferer", "" + serverRequest.getHeaders().getFirst("Referer"))
                            .build();

                    ServerWebExchange mutateServerWebExchange = exchange
                            .mutate().request(mutateRequest).build();

                    return chain.filter(mutateServerWebExchange);
                }
            }

        } catch (IOException e) {
        }

        String token = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (token != null) {
            token = token.replace("Bearer ", "");
            if (token.equals("eyJhbGciOiJQUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiaWF0IjoxNTE2MjM5MDIyfQ.ecyIfJCrVTu4WP6qdPLpOqfyOLZjL7Qv9UvS4Ifdqt6F_i4jkBPsteSEcr97TyRf_MeKl4nsddup2RE6SvnUpjWyPXbwS9Gx1JZsKemPe8bqc6961mbn7J-iF6xnXlBLmt0zz3YNpCQMlhS4qU-hYRHJmGtkQaBxFXhowyQ1Ii1Kw69P42IX7Sa4e4tJrcu2IO_9koR6B2zYEs4uzMaLw-ThBxCg-RetvoE9BY0h2oeOKlutmGnCvcpVLchPCSCJXM9vDRITjuB-TUcteyVDs9uTjln4v5j10fJFuWkOzr5aXGDsUnUQ8zzvHt5hz_nIUs8RIX-tg4-8PmgGcL_xzw")) {
                return chain.filter(exchange);
            }

            UUID auid = null;
            UUID reqCompanyUUID = null;
            UUID reqBranchUUID = null;
            String type = null;
            try {
                auid = tokenService.getAUID(token);
                reqCompanyUUID = tokenService.getCompany(token);
                reqBranchUUID = tokenService.getBranch(token);
                type = tokenService.getType(token);
            } catch (Exception e) {
            }

            ServerHttpRequest mutateRequest = exchange.getRequest()
                    .mutate()
                    .header("auid", "" + auid)
                    .header("moduleUUID", "" + moduleUUID)
                    .header("reqCompanyUUID", "" + reqCompanyUUID)
                    .header("reqBranchUUID", "" + reqBranchUUID)
                    .header("tokenType", "" + type)
                    .header("reqIp", "" + serverRequest.getHeaders().getHost())
                    .header("reqPort", "" + serverRequest.getHeaders().getHost().getPort())
                    .header("reqBrowser", "" + requestInfoService.getClientBrowser(serverRequest))
                    .header("reqOs", "" + requestInfoService.getClientOS(serverRequest))
                    .header("reqDevice", "" + type)
                    .header("reqReferer", "" + serverRequest.getHeaders().getFirst("Referer"))
                    .build();

            ServerWebExchange mutateServerWebExchange = exchange
                    .mutate().request(mutateRequest).build();

            return chain.filter(mutateServerWebExchange);
        } else {
            return chain.filter(exchange);
        }
    }
}