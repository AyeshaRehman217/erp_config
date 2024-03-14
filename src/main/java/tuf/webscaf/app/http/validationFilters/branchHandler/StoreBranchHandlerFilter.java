package tuf.webscaf.app.http.validationFilters.branchHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

public class StoreBranchHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();


                    if (value.containsKey("name")) {
                        if (value.getFirst("name").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Name Field is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("name").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Name Field is Required"
                                )
                        );
                    }

                    if (value.containsKey("establishmentDate")) {
                        if (value.getFirst("establishmentDate").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Establishment Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("establishmentDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Establishment Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Establishment Date Field Required"
                                )
                        );
                    }

//                    if (value.containsKey("address")) {
//                        if (value.getFirst("address").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "Address is Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("address").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid Address"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Address is Required"
//                                )
//                        );
//                    }

                    if (value.containsKey("description")) {
                        if (!value.getFirst("description").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Description"
                                    )
                            );
                        }
                    }

//                    if (value.containsKey("cityUUID")) {
//                        if (value.getFirst("cityUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "City is Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("cityUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid City"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "City is Required"
//                                )
//                        );
//                    }
//
//                    if (value.containsKey("countryUUID")) {
//                        if (value.getFirst("countryUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "Country is Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("countryUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid Country"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Country is Required"
//                                )
//                        );
//                    }
//
//                    if (value.containsKey("stateUUID")) {
//                        if (value.getFirst("stateUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "State is Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("stateUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid State"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "State is Required"
//                                )
//                        );
//                    }

                    if (value.containsKey("companyUUID")) {
                        if (value.getFirst("companyUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Company is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("companyUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Company"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Company is Required"
                                )
                        );
                    }

                    if (value.containsKey("locationUUID")) {
                        if (value.getFirst("locationUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Location is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("locationUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Location"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Location is Required"
                                )
                        );
                    }

                    if (value.containsKey("languageUUID")) {
                        if (value.getFirst("languageUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Language is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("languageUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Language"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Language is Required"
                                )
                        );
                    }

                    if (messages.isEmpty() != true) {
                        CustomResponse appresponse = new CustomResponse();
                        return appresponse.set(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.name(),
                                null,
                                "eng",
                                "token",
                                0L,
                                0L,
                                messages,
                                Mono.empty()
                        );
                    }
                    return next.handle(request);
                });
    }
}
