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
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;
import tuf.webscaf.app.http.handler.CountryHandler;
import tuf.webscaf.app.http.validationFilters.countryHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class CountriesRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/countries/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryHandler.class,
                            beanMethod = "index",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name,description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryHandler.class,
                            beanMethod = "show",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Records for the given Id",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/countries/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = CountryHandler.class,
//                            beanMethod = "showByUUID",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "showByUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCountryEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show Records for the given UUID",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/timezone/mapped/show/{timezoneUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryHandler.class,
                            beanMethod = "listOfCountriesForTimezone",
                            operation = @Operation(
                                    operationId = "listOfCountriesForTimezone",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns mapped countries for given Timezone UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "timezoneUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "size"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/translation/mapped/show/{translationUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryHandler.class,
                            beanMethod = "listOfCountriesForTranslation",
                            operation = @Operation(
                                    operationId = "listOfCountriesForTranslation",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns mapped countries for given Translation UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "translationUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "size"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CountryHandler.class,
                            beanMethod = "store",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Country",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CountryEntity.class)
                                            )),
                                    parameters = {
//
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = CountryHandler.class,
                            beanMethod = "update",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//
                                    },
                                    requestBody = @RequestBody(
                                            description = "Update country",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CountryEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CountryHandler.class,
                            beanMethod = "delete",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "delete country",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCountryEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/countries/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = CountryHandler.class,
                            beanMethod = "status",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = StatusDocImpl.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//
                                    },
                                    requestBody = @RequestBody(
                                            description = "Updating The Status",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
                                            ))
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> countryRoutes(CountryHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/countries/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexCountryHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/countries/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidCountryHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/countries/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUUID).filter(new ShowWithUuidCountryHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/countries/timezone/mapped/show/{timezoneUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfCountriesForTimezone).filter(new IndexMappedCountriesAgainstTimezoneHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/countries/translation/mapped/show/{translationUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfCountriesForTranslation).filter(new IndexMappedCountriesAgainstTranslationHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/countries/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCountryHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/countries/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateCountryHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/countries/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidCountryHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/countries/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCountryHandlerFilter()));
    }
}
