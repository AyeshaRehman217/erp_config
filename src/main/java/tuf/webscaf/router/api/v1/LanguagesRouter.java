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
import tuf.webscaf.app.dbContext.master.entity.LanguageEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;
import tuf.webscaf.app.http.handler.LanguageHandler;
import tuf.webscaf.app.http.validationFilters.languageHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class LanguagesRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/config/api/v1/languages/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
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
                                                            implementation = SlaveLanguageEntity.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name and description"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
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
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Show Languages Based on Language UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
//
//                    @RouterOperation(
//                            path = "/config/api/v1/uuid/languages/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = LanguageHandler.class,
//                            beanMethod = "showByUUID",
//                            operation = @Operation(
//                                    operationId = "showByUUID",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveLanguageEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Records not found!",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/list/show",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "showLanguageListInAcademic",
                            operation = @Operation(
                                    operationId = "showLanguageListInAcademic",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route is used By Academic Module to check if Language UUIDs Exist",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "uuid",  content = @Content(array = @ArraySchema(schema = @Schema(implementation = UUID.class))))
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student
                    @RouterOperation(
                            path = "/config/api/v1/languages/student/mapped/show/{studentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudent",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudent",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student/existing/show/{studentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudent",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudent",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student mother
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-mother/mapped/show/{studentMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentMother",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentMotherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-mother/existing/show/{studentMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentMother",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentMotherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student father
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-father/mapped/show/{studentFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentFather",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentFatherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-father/existing/show/{studentFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentFather",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentFatherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student sibling
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-sibling/mapped/show/{studentSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentSibling",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentSiblingUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-sibling/existing/show/{studentSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentSibling",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentSiblingUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student child
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-child/mapped/show/{studentChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentChild",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentChildUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-child/existing/show/{studentChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentChild",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentChildUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student spouse
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-spouse/mapped/show/{studentSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentSpouse",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentSpouseUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-spouse/existing/show/{studentSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentSpouse",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentSpouseUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a student guardian
                    @RouterOperation(
                            path = "/config/api/v1/languages/student-guardian/mapped/show/{studentGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstStudentGuardian",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstStudentGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Student Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentGuardianUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/student-guardian/existing/show/{studentGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstStudentGuardian",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstStudentGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Student Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "studentGuardianUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher/mapped/show/{teacherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacher",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher/existing/show/{teacherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacher",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacher",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher mother
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-mother/mapped/show/{teacherMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherMother",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherMotherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-mother/existing/show/{teacherMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherMother",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherMotherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher father
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-father/mapped/show/{teacherFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherFather",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherFatherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-father/existing/show/{teacherFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherFather",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherFatherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher sibling
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-sibling/mapped/show/{teacherSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherSibling",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherSiblingUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-sibling/existing/show/{teacherSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherSibling",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherSiblingUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher child
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-child/mapped/show/{teacherChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherChild",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherChildUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-child/existing/show/{teacherChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherChild",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherChildUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher spouse
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-spouse/mapped/show/{teacherSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherSpouse",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherSpouseUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-spouse/existing/show/{teacherSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherSpouse",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherSpouseUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a teacher guardian
                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-guardian/mapped/show/{teacherGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstTeacherGuardian",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstTeacherGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Teacher Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherGuardianUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/teacher-guardian/existing/show/{teacherGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfExistingLanguagesAgainstTeacherGuardian",
                            operation = @Operation(
                                    operationId = "listOfExistingLanguagesAgainstTeacherGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Teacher Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "teacherGuardianUUID")
                                    }
                            )
                    ),
                    // These routes return the mapped and un mapped languages for a employee
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee/mapped/show/{employeeUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployee",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployee",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee/un-mapped/show/{employeeUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployee",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployee",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee mother
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-mother/mapped/show/{employeeMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeMother",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeMotherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-mother/un-mapped/show/{employeeMotherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeMother",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeMother",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Mother",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeMotherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee father
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-father/mapped/show/{employeeFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeFather",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeFatherUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-father/un-mapped/show/{employeeFatherUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeFather",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeFather",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Father",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeFatherUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee sibling
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-sibling/mapped/show/{employeeSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeSibling",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeSiblingUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-sibling/un-mapped/show/{employeeSiblingUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeSibling",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeSibling",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Sibling",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeSiblingUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee child
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-child/mapped/show/{employeeChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeChild",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeChildUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-child/un-mapped/show/{employeeChildUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeChild",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeChild",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Child",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeChildUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee spouse
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-spouse/mapped/show/{employeeSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeSpouse",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeSpouseUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-spouse/un-mapped/show/{employeeSpouseUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeSpouse",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeSpouse",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Spouse",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeSpouseUUID")
                                    }
                            )
                    ),

                    // These routes return the mapped and un mapped languages for a employee guardian
                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-guardian/mapped/show/{employeeGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfMappedLanguagesAgainstEmployeeGuardian",
                            operation = @Operation(
                                    operationId = "listOfMappedLanguagesAgainstEmployeeGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Mapped With Given Employee Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeGuardianUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/config/api/v1/languages/employee-guardian/un-mapped/show/{employeeGuardianUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LanguageHandler.class,
                            beanMethod = "listOfUnMappedLanguagesAgainstEmployeeGuardian",
                            operation = @Operation(
                                    operationId = "listOfUnMappedLanguagesAgainstEmployeeGuardian",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This route returns the Languages That Are Not Mapped With Given Employee Guardian",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.PATH, name = "employeeGuardianUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/languages/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = LanguageHandler.class,
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
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create New Language",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LanguageEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/languages/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LanguageHandler.class,
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
                                                            implementation = SlaveLanguageEntity.class
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
                                            description = "Update Language Record",
                                            required = true,
                                            content = @Content(
//                                                    mediaType = "multipart/form-data",
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LanguageEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/languages/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = LanguageHandler.class,
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
                                                            implementation = SlaveLanguageEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),

                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/config/api/v1/languages/status/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LanguageHandler.class,
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

    public RouterFunction<ServerResponse> languageRoutes(LanguageHandler handle) {
        return RouterFunctions.route(GET("config/api/v1/languages/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexLanguageHandlerFilter())
                .and(RouterFunctions.route(GET("config/api/v1/languages/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowWithUuidLanguageHandlerFilter()))
//                .and(RouterFunctions.route(GET("config/api/v1/uuid/languages/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showByUUID).filter(new ShowWithUuidLanguageHandlerFilter()))
                .and(RouterFunctions.route(GET("config/api/v1/languages/list/show").and(accept(APPLICATION_FORM_URLENCODED)), handle::showLanguageListInAcademic))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student/mapped/show/{studentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudent))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student/existing/show/{studentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudent))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-mother/mapped/show/{studentMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-mother/existing/show/{studentMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-father/mapped/show/{studentFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-father/existing/show/{studentFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-sibling/mapped/show/{studentSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-sibling/existing/show/{studentSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-child/mapped/show/{studentChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-child/existing/show/{studentChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-spouse/mapped/show/{studentSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-spouse/existing/show/{studentSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-guardian/mapped/show/{studentGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstStudentGuardian))
                .and(RouterFunctions.route(GET("config/api/v1/languages/student-guardian/existing/show/{studentGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstStudentGuardian))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher/mapped/show/{teacherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacher))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher/existing/show/{teacherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacher))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-mother/mapped/show/{teacherMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-mother/existing/show/{teacherMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-father/mapped/show/{teacherFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-father/existing/show/{teacherFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-sibling/mapped/show/{teacherSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-sibling/existing/show/{teacherSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-child/mapped/show/{teacherChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-child/existing/show/{teacherChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-spouse/mapped/show/{teacherSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-spouse/existing/show/{teacherSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-guardian/mapped/show/{teacherGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstTeacherGuardian))
                .and(RouterFunctions.route(GET("config/api/v1/languages/teacher-guardian/existing/show/{teacherGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfExistingLanguagesAgainstTeacherGuardian))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee/mapped/show/{employeeUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployee))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee/un-mapped/show/{employeeUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployee))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-mother/mapped/show/{employeeMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-mother/un-mapped/show/{employeeMotherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeMother))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-father/mapped/show/{employeeFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-father/un-mapped/show/{employeeFatherUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeFather))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-sibling/mapped/show/{employeeSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-sibling/un-mapped/show/{employeeSiblingUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeSibling))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-child/mapped/show/{employeeChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-child/un-mapped/show/{employeeChildUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeChild))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-spouse/mapped/show/{employeeSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-spouse/un-mapped/show/{employeeSpouseUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeSpouse))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-guardian/mapped/show/{employeeGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfMappedLanguagesAgainstEmployeeGuardian))
                .and(RouterFunctions.route(GET("config/api/v1/languages/employee-guardian/un-mapped/show/{employeeGuardianUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::listOfUnMappedLanguagesAgainstEmployeeGuardian))
                .and(RouterFunctions.route(POST("config/api/v1/languages/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreLanguageHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/languages/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateLanguageHandlerFilter()))
                .and(RouterFunctions.route(PUT("config/api/v1/languages/status/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowWithUuidLanguageHandlerFilter()))
                .and(RouterFunctions.route(DELETE("config/api/v1/languages/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteLanguageHandlerFilter()));
    }

}
