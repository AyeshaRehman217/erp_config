package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import tuf.webscaf.seeder.handler.SeederHandler;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class SeederRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/seeder/country/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCountries",
                            operation = @Operation(
                                    operationId = "storeCountries",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/language/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeLanguages",
                            operation = @Operation(
                                    operationId = "storeLanguages",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/company/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCmpanies",
                            operation = @Operation(
                                    operationId = "storeCmpanies",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/branch/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeBranches",
                            operation = @Operation(
                                    operationId = "storeBranches",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/office/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeOffices",
                            operation = @Operation(
                                    operationId = "storeOffices",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/calendar-category/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendarCategory",
                            operation = @Operation(
                                    operationId = "storeCalendarCategory",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/doc-buckets/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeDocBucket",
                            operation = @Operation(
                                    operationId = "storeDocBucket",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/administration-departments/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeAdministrationDepartment",
                            operation = @Operation(
                                    operationId = "storeAdministrationDepartment",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/calendars/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendar",
                            operation = @Operation(
                                    operationId = "storeCalendar",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/calendar-dates/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeCalendarDate",
                            operation = @Operation(
                                    operationId = "storeCalendarDate",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/seeder/modules/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SeederHandler.class,
                            beanMethod = "storeModule",
                            operation = @Operation(
                                    operationId = "storeModule",
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Unsuccessful",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    }
                            )
                    )
            })
    public RouterFunction<ServerResponse> seederRoutes(SeederHandler handle) {
        return RouterFunctions.route(POST("/config/api/v1/seeder/country/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCountries)
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/language/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeLanguages))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/company/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCompanies))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/branch/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeBranches))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/calendar-category/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendarCategory))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/doc-buckets/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeDocBucket))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/administration-departments/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeAdministrationDepartment))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/calendars/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendar))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/calendar-dates/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeCalendarDate))
                .and(RouterFunctions.route(POST("/config/api/v1/seeder/modules/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::storeModule));
    }

}
