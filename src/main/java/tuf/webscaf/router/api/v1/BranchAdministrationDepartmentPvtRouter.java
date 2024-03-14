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
import tuf.webscaf.app.http.handler.BranchAdministrationDepartmentPvtHandler;
import tuf.webscaf.app.http.validationFilters.branchAdministrationDepartmentPvt.ShowBranchAdministrationDepartmentPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.branchAdministrationDepartmentPvt.StoreBranchAdministrationDepartmentPvtHandlerFilter;
import tuf.webscaf.springDocImpl.BranchAdministrationDepartmentDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class BranchAdministrationDepartmentPvtRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/branch-administration-departments/un-mapped/show/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchAdministrationDepartmentPvtHandler.class,
                            beanMethod = "showUnMappedAdministrationDepartmentAgainstBranch",
                            operation = @Operation(
                                    operationId = "showUnMappedAdministrationDepartmentAgainstBranch",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAdministrationDepartmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show AdministrationDepartments that are Un-Mapped for given Branch",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/branch-administration-departments/mapped/show/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchAdministrationDepartmentPvtHandler.class,
                            beanMethod = "showMappedAdministrationDepartmentAgainstBranch",
                            operation = @Operation(
                                    operationId = "showMappedAdministrationDepartmentAgainstBranch",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAdministrationDepartmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show AdministrationDepartment That Are Mapped With Given Branch's",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/branch-administration-departments/store/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = BranchAdministrationDepartmentPvtHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = AdministrationDepartmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create AdministrationDepartments for a Branch",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = BranchAdministrationDepartmentDocImpl.class)
                                            )),
                                    description = "Store AdministrationDepartments Against a Given Branch",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID"),
//
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branch-administration-departments/delete/{branchUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = BranchAdministrationDepartmentPvtHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = AdministrationDepartmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete AdministrationDepartments Against a Given Branch",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "branchUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "administrationDepartmentUUID"),
//
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> branchAdministrationDepartmentPvtRoutes(BranchAdministrationDepartmentPvtHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/branch-administration-departments/un-mapped/show/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedAdministrationDepartmentAgainstBranch).filter(new ShowBranchAdministrationDepartmentPvtHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/branch-administration-departments/mapped/show/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedAdministrationDepartmentAgainstBranch).filter(new ShowBranchAdministrationDepartmentPvtHandlerFilter()))
                .and(RouterFunctions.route(POST("config/api/v1/branch-administration-departments/store/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreBranchAdministrationDepartmentPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/branch-administration-departments/delete/{branchUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete));
    }

}
