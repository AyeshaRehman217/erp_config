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
import tuf.webscaf.app.dbContext.master.dto.CompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
import tuf.webscaf.app.http.handler.CompanyHandler;
import tuf.webscaf.app.http.validationFilters.companyHandler.*;
import tuf.webscaf.springDocImpl.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class CompaniesRouter {


    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/companies/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
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
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
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
                            path = "/config/api/v1/companies/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
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
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Companies Based on ID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/list/show",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showExistingCompanyListInModule",
                            operation = @Operation(
                                    operationId = "showExistingCompanyListInModule",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record Does Not exist.",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route is used By Account and Financial Module in Voucher Company Pvt Handler to check if Company UUIDs Exists",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "uuid",
                                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UUID.class))))
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/companies/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = CompanyHandler.class,
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
//                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show company for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/doc-image/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "getDocImage",
                            operation = @Operation(
                                    operationId = "getDocImage",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route is used by Drive Module in Delete Function to check if Reference exists",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/voucher/un-mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showUnMappedCompaniesAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedCompaniesAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route return Companies that are not mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/voucher/mapped/show/{voucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showMappedCompaniesAgainstVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedCompaniesAgainstVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route return Companies that are mapped for given Voucher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
//                    @RouterOperation(
//                            path = "/config/api/v1/companies/check-company/show/{voucherUUID}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = CompanyHandler.class,
//                            beanMethod = "getCompanyId",
//                            operation = @Operation(
//                                    operationId = "getCompanyId",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveCompanyEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Get Company Records for a Given Voucher",
//                                            required = true,
//                                            content = @Content(
////                                                    mediaType = "multipart/form-data",
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = CheckCompanyDocImpl.class)
//                                            )),
//                                    description = "This Route is used by Delete Function in Account Module to Check If branch Exist against company",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "voucherUUID")
//                                    }
//                            )
//                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/student-financial-voucher/mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showMappedCompanyAgainstFinancialVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedCompanyAgainstFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route is used to Fetch Companies that are mapped against Financial Voucher UUID From Student Financial Module",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/student-financial-voucher/un-mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showUnMappedCompanyAgainstFinancialVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedCompanyAgainstFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route is used to Fetch Companies that are not mapped against Financial Voucher UUID From Student Financial Module",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/emp-financial-voucher/mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showMappedCompanyAgainstEmpFinancialVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showMappedCompanyAgainstEmpFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Companies that are Mapped against Financial Voucher UUID From Emp Financial Module",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/emp-financial-voucher/un-mapped/show/{financialVoucherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "showUnMappedCompanyAgainstEmpFinancialVoucher",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "showUnMappedCompanyAgainstEmpFinancialVoucher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show Companies that are not Mapped against Financial Voucher UUID From Emp Financial Module",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "financialVoucherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = CompanyHandler.class,
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
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Company",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CompanyDocImpl.class)
                                            )),
                                    parameters = {
//
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/list",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = CompanyHandler.class,
                            beanMethod = "fetchCompanyList",
//                            consumes = { "APPLICATION_FORM_URLENCODED" },
                            operation = @Operation(
                                    operationId = "fetchCompanyList",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveCompanyEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route is used By Student Financial Module in Financial Voucher Company Handler",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "uuid",
                                                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UUID.class)))),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/companies/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = CompanyHandler.class,
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
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
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
                                            description = "Update Company Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = CompanyDocImpl.class)
                                            ))
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/companies/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = CompanyHandler.class,
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
                                                            implementation = SlaveCompanyWithCompanyProfileDto.class
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
                            path = "/config/api/v1/companies/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = CompanyHandler.class,
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

    public RouterFunction<ServerResponse> companyRoutes(CompanyHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/companies/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexCompanyHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/companies/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/student-financial-voucher/mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCompanyAgainstFinancialVoucher).filter(new ShowCompaniesAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/student-financial-voucher/un-mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCompanyAgainstFinancialVoucher).filter(new ShowCompaniesAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/emp-financial-voucher/mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCompanyAgainstEmpFinancialVoucher).filter(new ShowCompaniesAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/emp-financial-voucher/un-mapped/show/{financialVoucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCompanyAgainstEmpFinancialVoucher).filter(new ShowCompaniesAgainstFinancialVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/list").and(accept(APPLICATION_FORM_URLENCODED)), handle::fetchCompanyList).filter(new FetchCompanyListHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/companies/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUUID).filter(new ShowWithUuidCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/doc-image/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getDocImage).filter(new ShowWithUuidCompanyHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/list/show").and(accept(APPLICATION_FORM_URLENCODED)), handle::showExistingCompanyListInModule).filter(new ShowCompanyListHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/voucher/un-mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showUnMappedCompaniesAgainstVoucher).filter(new ShowCompanyAgainstVoucherHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/companies/voucher/mapped/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showMappedCompaniesAgainstVoucher).filter(new ShowCompanyAgainstVoucherHandlerFilter()))
//                .and(RouterFunctions.route(POST("config/api/v1/companies/check-company/show/{voucherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getCompanyId))
                .and(RouterFunctions.route(POST("config/api/v1/companies/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreCompanyHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/companies/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateCompanyHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/companies/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidCompanyHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/companies/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteCompanyHandlerFilter()));
    }

}
