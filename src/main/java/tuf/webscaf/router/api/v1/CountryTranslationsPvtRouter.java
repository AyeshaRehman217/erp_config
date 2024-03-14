package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.http.handler.CountryTranslationPvtHandler;
import tuf.webscaf.app.http.validationFilters.countryTranslationPvtHandler.DeleteCountryTranslationPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.countryTranslationPvtHandler.ShowCountryTranslationPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.countryTranslationPvtHandler.StoreCountryTranslationPvtHandlerFilter;
import tuf.webscaf.springDocImpl.TranslationDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class CountryTranslationsPvtRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/country-translation/un-mapped/show/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryTranslationPvtHandler.class,
                            beanMethod = "showUnMappedTranslationsAgainstCountry",
                            operation = @Operation(
                                    operationId = "showUnMappedTranslationsAgainstCountry",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show un-mapped Translations Against given Country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with key"),
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/country-translation/mapped/show/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryTranslationPvtHandler.class,
                            beanMethod = "showMappedTranslationsAgainstCountry",
                            operation = @Operation(
                                    operationId = "showMappedTranslationsAgainstCountry",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show mapped Translations Against given Country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with key"),
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/country-translation/store/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CountryTranslationPvtHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Translations for a Country",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TranslationDocImpl.class)
                                            )),
                                    description = "Store Translations Against a Country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/country-translation/delete/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CountryTranslationPvtHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Translations against a given country",
                                    parameters = {
//
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, required = true, name = "translationUUID"),

                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> countryTranslationRoutes(CountryTranslationPvtHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/country-translation/un-mapped/show/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedTranslationsAgainstCountry).filter(new ShowCountryTranslationPvtHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/country-translation/mapped/show/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedTranslationsAgainstCountry).filter(new ShowCountryTranslationPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/country-translation/store/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCountryTranslationPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/country-translation/delete/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCountryTranslationPvtHandlerFilter()));
    }

}
