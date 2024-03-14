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
import tuf.webscaf.app.dbContext.master.entity.AdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;
import tuf.webscaf.app.http.handler.AdministrationDepartmentHandler;
import tuf.webscaf.app.http.validationFilters.administrationDepartmentHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AdministrationDepartmentsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/administration-departments/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AdministrationDepartmentHandler.class,
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
                                                            implementation = SlaveAdministrationDepartmentEntity.class))
                                            ),
                                            @ApiResponse(responseCode = "404",
                                                    description = " Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with companyUUID,name and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/administration-departments/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AdministrationDepartmentHandler.class,
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
                                                            implementation = SlaveAdministrationDepartmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist with given id", content = @Content(schema = @Schema(hidden = true)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/administration-departments/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = AdministrationDepartmentHandler.class,
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
                                                            implementation = SlaveAdministrationDepartmentEntity.class
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
                                                            @Encoding(name = "AdministrationDepartment", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = AdministrationDepartmentEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/administration-departments/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AdministrationDepartmentHandler.class,
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
                                                            implementation = SlaveAdministrationDepartmentEntity.class
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
                                                            @Encoding(name = "AdministrationDepartment", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = AdministrationDepartmentEntity.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/administration-departments/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = AdministrationDepartmentHandler.class,
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
                                                            implementation = SlaveAdministrationDepartmentEntity.class
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
                            path = "/config/api/v1/administration-departments/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AdministrationDepartmentHandler.class,
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
    public RouterFunction<ServerResponse> administrationDepartmentRoutes(AdministrationDepartmentHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/administration-departments/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexAdministrationDepartmentHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/administration-departments/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowAdministrationDepartmentHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/administration-departments/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreAdministrationDepartmentHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/administration-departments/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateAdministrationDepartmentHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/administration-departments/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowAdministrationDepartmentHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/administration-departments/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteAdministrationDepartmentHandlerFilter()));
    }


}
