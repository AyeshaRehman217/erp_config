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
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;
import tuf.webscaf.app.http.handler.ConfigHandler;
import tuf.webscaf.app.http.validationFilters.configHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ConfigsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/configs/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ConfigHandler.class,
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
                                                            implementation = SlaveConfigEntity.class))
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
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with key,description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/configs/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ConfigHandler.class,
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
                                                            implementation = SlaveConfigEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not exist with given id",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/configs/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = ConfigHandler.class,
//                            beanMethod = "showConfigUUID",
//                            operation = @Operation(
//                                    operationId = "showConfigUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveConfigEntity.class
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
                            path = "/config/api/v1/configs/module/show/{moduleUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = ConfigHandler.class,
                            beanMethod = "showListsOfConfig",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showListsOfConfig",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveModuleEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = " Record does not found",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    }, description = "Show Config List Against Module UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "moduleUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/configs/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = ConfigHandler.class,
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
                                                            implementation = SlaveConfigEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not stored with given id", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
//
                                    },

                                    requestBody = @RequestBody(
                                            description = "Store Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",

                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "Config", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = ConfigEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/configs/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = ConfigHandler.class,
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
                                                            implementation = SlaveConfigEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = "Record does not updated with given id",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//
                                    },
                                    requestBody = @RequestBody(
                                            description = "update Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "Config", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = ConfigEntity.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/configs/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = ConfigHandler.class,
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
                                                            implementation = SlaveConfigEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist with given id", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
//
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/configs/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = ConfigHandler.class,
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
//
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
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
    public RouterFunction<ServerResponse> configRoutes(ConfigHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/configs/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexConfigHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/configs/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidConfigHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/configs/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showConfigUUID).filter(new ShowWithUuidConfigHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/configs/module/show/{moduleUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showListsOfConfig).filter(new listConfigWithModuleHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/configs/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreConfigHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/configs/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateConfigHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/configs/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidConfigHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/configs/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteConfigHandlerFilter()));
    }


}
