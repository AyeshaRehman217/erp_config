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
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.http.handler.CountryTimezonePvtHandler;
import tuf.webscaf.app.http.validationFilters.countryTimezonePvtHandler.DeleteCountryTimezonePvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.countryTimezonePvtHandler.ShowCountryTimezonePvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.countryTimezonePvtHandler.StoreCountryTimezonePvtHandlerFilter;
import tuf.webscaf.springDocImpl.TimezoneDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class CountryTimezonesPvtRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/country-time-zone/un-mapped/show/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryTimezonePvtHandler.class,
                            beanMethod = "showUnMappedTimezonesAgainstCountry",
                            operation = @Operation(
                                    operationId = "showUnMappedTimezonesAgainstCountry",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTimezoneEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show un-mapped timezones against given country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with zone_name"),
                                            @Parameter(in = ParameterIn.PATH, required = true, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/country-time-zone/mapped/show/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CountryTimezonePvtHandler.class,
                            beanMethod = "showMappedTimezonesAgainstCountry",
                            operation = @Operation(
                                    operationId = "showMappedTimezonesAgainstCountry",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTimezoneEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show mapped timezones against given country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with zone_name"),
                                            @Parameter(in = ParameterIn.PATH, required = true, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/country-time-zone/store/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CountryTimezonePvtHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTimezoneEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Timezones for a Country",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TimezoneDocImpl.class)
                                            )),
                                    description = "Store Timezones Against a Country",
                                    parameters = {
//
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/country-time-zone/delete/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CountryTimezonePvtHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveTimezoneEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete timezones against a given country",
                                    parameters = {
//
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, required = true, name = "timezoneUUID")

                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> countryTimezoneRoutes(CountryTimezonePvtHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/country-time-zone/un-mapped/show/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedTimezonesAgainstCountry).filter(new ShowCountryTimezonePvtHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/country-time-zone/mapped/show/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedTimezonesAgainstCountry).filter(new ShowCountryTimezonePvtHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/country-time-zone/store/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCountryTimezonePvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/country-time-zone/delete/{countryUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCountryTimezonePvtHandlerFilter()));
    }

}
