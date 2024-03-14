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
import tuf.webscaf.app.dbContext.master.entity.TranslationEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.http.handler.TranslationHandler;
import tuf.webscaf.app.http.validationFilters.translationHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TranslationsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/translations/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TranslationHandler.class,
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
                                                            implementation = SlaveTranslationEntity.class))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = " Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with key and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/translations/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TranslationHandler.class,
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
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not exist with given UUID",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/translations/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = TranslationHandler.class,
//                            beanMethod = "showByUUID",
//                            operation = @Operation(
//                                    operationId = "showByUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveTranslationEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404",
//                                                    description = "Record does not exist with given id",
//                                                    content = @Content(schema = @Schema(hidden = true)))
//                                    },
//                                    description = "Show the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/config/api/v1/translations/country/mapped/show/{countryUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = TranslationHandler.class,
                            beanMethod = "listOfTranslationsForCountry",
                            operation = @Operation(
                                    operationId = "listOfTranslationsForCountry",
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
                                    description = "List Mapped translations for country",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "countryUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "size"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with key and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/translations/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = TranslationHandler.class,
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
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not stored with given id",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    requestBody = @RequestBody(
                                            description = "Store Translation Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",

                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "Config", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TranslationEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/translations/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = TranslationHandler.class,
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
                                                            implementation = SlaveTranslationEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not updated with given id",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),

                                    },
                                    requestBody = @RequestBody(
                                            description = "update Translations",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "Config", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = TranslationEntity.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/translations/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = TranslationHandler.class,
                            beanMethod = "delete",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
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
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not exist with given id",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/translations/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = TranslationHandler.class,
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
    public RouterFunction<ServerResponse> translationRoutes(TranslationHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/translations/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexTranslationHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/translations/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidTranslationHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/translations/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUUID).filter(new ShowWithUuidTranslationHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/translations/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreTranslationHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/translations/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateTranslationHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/translations/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidTranslationHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/translations/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowWithUuidTranslationHandlerFilter()));
    }


}
