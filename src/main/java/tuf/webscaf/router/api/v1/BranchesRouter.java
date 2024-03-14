package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import tuf.webscaf.app.dbContext.slave.dto.SlaveBranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;
import tuf.webscaf.app.http.handler.BranchHandler;
import tuf.webscaf.app.http.validationFilters.branchHandler.*;
import tuf.webscaf.springDocImpl.BranchDocImpl;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class BranchesRouter {


    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/branches/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
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
                                                            implementation = SlaveBranchWithBranchProfileDto.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/branches/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
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
                                                            implementation = SlaveBranchWithBranchProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show branch for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ), @RouterOperation(
                    path = "/config/api/v1/branch-for-company/show/{companyUUID}",
                    produces = {
                            MediaType.APPLICATION_FORM_URLENCODED_VALUE
                    },
                    method = RequestMethod.GET,
                    beanClass = BranchHandler.class,
                    beanMethod = "showCompanyBranch",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                    operation = @Operation(
                            operationId = "showCompanyBranch",
                            security = {@SecurityRequirement(name = "bearer")},
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "successful operation",
                                            content = @Content(schema = @Schema(
                                                    implementation = SlaveBranchEntity.class
                                            ))
                                    ),
                                    @ApiResponse(responseCode = "404", description = "Records not found!",
                                            content = @Content(schema = @Schema(hidden = true))
                                    )
                            },
                            description = "Check if Branch Exists for Given Company",
                            parameters = {
                                    @Parameter(in = ParameterIn.PATH, name = "companyUUID", required = true),
                                    @Parameter(in = ParameterIn.QUERY, name = "branchUUID", required = true)
                            }
                    )
            ),
                    @RouterOperation(
                            path = "/config/api/v1/branches/uuid/list/show",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showBranchListInAccountModule",
                            operation = @Operation(
                                    operationId = "showBranchListInAccountModule",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record Does Not exist.",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route is used By Account Module in Voucher Branch Pvt Handler to check if Branch UUIDs Exists",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "uuid",
                                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UUID.class))))
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/uuid/company/branches/show/{companyUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showBranchesListAgainstCompany",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showBranchesListAgainstCompany",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchWithBranchProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show branch UUID List Against Company to be used by Voucher Company Handler",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "companyUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/branches/voucher/un-mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showUnMappedRecordsAgainstVoucher",
                            operation = @Operation(
                                    operationId = "showUnMappedRecordsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns branches that are not mapped for given Voucher And Company",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branches/voucher/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showMappedRecordsAgainstVoucher",
                            operation = @Operation(
                                    operationId = "showMappedRecordsAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns branches that are  mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branches/emp-financial-voucher/un-mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showUnMappedRecordsAgainstEmpFinancialVoucher",
                            operation = @Operation(
                                    operationId = "showUnMappedRecordsAgainstEmpFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns branches that are not mapped for given financial Voucher And Company",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branches/emp-financial-voucher/mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = BranchHandler.class,
                            beanMethod = "showMappedRecordsAgainstEmpFinancialVoucher",
                            operation = @Operation(
                                    operationId = "showMappedRecordsAgainstEmpFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveBranchEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns branches that are  mapped for given financial Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "companyUUID"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/branches/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = BranchHandler.class,
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
                                                            implementation = SlaveBranchWithBranchProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Store a branch",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = BranchDocImpl.class)
                                            )),
                                    parameters = {
//
                                    }
                            )
                    ),

//                    @RouterOperation(
//                            path = "/config/api/v1/branches/list/show",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = BranchHandler.class,
//                            beanMethod = "showBranchList",
////                            consumes = { "APPLICATION_FORM_URLENCODED" },
//                            operation = @Operation(
//                                    operationId = "showBranchList",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveBranchEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "List of Branches",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = BranchesListDocImpl.class)
//                                            )),
//                                    description = "This route is used to check branches exist for voucher id"
//                            )
//                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branches/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = BranchHandler.class,
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
                                                            implementation = SlaveBranchWithBranchProfileDto.class
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
                                            description = "Update Branch",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = BranchDocImpl.class)
                                            ))
                            )
                    ),


                    @RouterOperation(
                            path = "/config/api/v1/branches/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = BranchHandler.class,
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
                                                            implementation = SlaveBranchWithBranchProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Branch",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//

                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/branches/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = BranchHandler.class,
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
                                    description = "Updating the Branch Status",
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

    public RouterFunction<ServerResponse> branchRoutes(BranchHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/branches/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexBranchWithCompanyAndStatusHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/branches/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidBranchHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/uuid/company/branches/show/{companyUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showBranchesListAgainstCompany).filter(new ListBranchAgainstCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branches/voucher/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedRecordsAgainstVoucher).filter(new ShowBranchAgainstVoucherAndCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branches/voucher/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedRecordsAgainstVoucher).filter(new ShowBranchAgainstVoucherAndCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branches/emp-financial-voucher/un-mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedRecordsAgainstEmpFinancialVoucher).filter(new ShowMappedBranchAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branches/emp-financial-voucher/mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedRecordsAgainstEmpFinancialVoucher).filter(new ShowMappedBranchAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branch-for-company/show/{companyUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showCompanyBranch).filter(new ShowBranchAgainstGivenCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/branches/uuid/list/show").and(accept(APPLICATION_FORM_URLENCODED)), handle::showBranchListInAccountModule))
                .and(RouterFunctions.route(POST("config/api/v1/branches/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreBranchHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/branches/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateBranchHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/branches/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidBranchHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/branches/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteBranchHandlerFilter()));
    }


}
