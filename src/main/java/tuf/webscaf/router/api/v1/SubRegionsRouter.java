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
import tuf.webscaf.app.dbContext.master.entity.SubRegionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubRegionEntity;
import tuf.webscaf.app.http.handler.SubRegionHandler;
import tuf.webscaf.app.http.validationFilters.subRegionHandler.IndexSubRegionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.subRegionHandler.ShowWithUuidSubRegionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.subRegionHandler.StoreSubRegionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.subRegionHandler.UpdateSubRegionHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SubRegionsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SubRegionHandler.class,
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
                                                            implementation = SlaveSubRegionEntity.class))
                                            ),
                                            @ApiResponse(responseCode = "404", description = " Record does not exist", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "regionUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = SubRegionHandler.class,
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
                                                            implementation = SlaveSubRegionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist with given id", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    description = "Show Records based on UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/sub-regions/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = SubRegionHandler.class,
//                            beanMethod = "showByUUID",
//                            operation = @Operation(
//                                    operationId = "showByUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveSubRegionEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404",
//                                                    description = "Record does not exist with given id", content = @Content(schema = @Schema(hidden = true)))
//                                    },
//                                    description = "Show the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = SubRegionHandler.class,
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
                                                            implementation = SlaveSubRegionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not stored with given id", content = @Content(schema = @Schema(hidden = true)))
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
                                                    schema = @Schema(type = "object", implementation = SubRegionEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = SubRegionHandler.class,
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
                                                            implementation = SlaveSubRegionEntity.class
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
                                            description = "update Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "Config", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = SubRegionEntity.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = SubRegionHandler.class,
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
                                                            implementation = SlaveSubRegionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist with given id", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/sub-regions/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = SubRegionHandler.class,
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
    public RouterFunction<ServerResponse> subRegionRoutes(SubRegionHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/sub-regions/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexSubRegionHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/sub-regions/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidSubRegionHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/sub-regions/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUUID).filter(new ShowWithUuidSubRegionHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/sub-regions/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreSubRegionHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/sub-regions/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateSubRegionHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/sub-regions/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidSubRegionHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/sub-regions/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowWithUuidSubRegionHandlerFilter()));
    }


}
