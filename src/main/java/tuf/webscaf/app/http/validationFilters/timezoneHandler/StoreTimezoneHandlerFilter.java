package tuf.webscaf.app.http.validationFilters.timezoneHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class StoreTimezoneHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.containsKey("zoneName")) {
                        if (value.getFirst("zoneName").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Zone Name is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("zoneName").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Zone Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Zone Name is Required"
                                )
                        );
                    }


                    if (value.containsKey("gmtOffset")) {
                        if (!value.getFirst("gmtOffset").matches("^-?\\d+(\\.\\d+)?$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Gmt Offset"
                                    )
                            );
                        }
                    }

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

                    if (value.containsKey("gmtOffsetName")) {

                        if (value.getFirst("gmtOffsetName").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Gmt Offset Name Field Required"
                                    )
                            );
                        } else {

                            // get encoded value of GMT Offset Name from request
                            String encodedGmtOffsetName = value.getFirst("gmtOffsetName");

                            String decodedGmtOffsetName = "";

                            // decode the value of Gmt Offset Name
                            try {
                                decodedGmtOffsetName = URLDecoder.decode(encodedGmtOffsetName, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if (!decodedGmtOffsetName.matches("UTC[0-9A-Z+#-±]+")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Gmt Offset Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Gmt Offset Name Field Required"
                                )
                        );
                    }

                    if (value.containsKey("abbreviation")) {
                        if (value.getFirst("abbreviation").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Abbreviation is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("abbreviation").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Abbreviation"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Abbreviation is Required"
                                )
                        );
                    }

                    if (value.containsKey("tzName")) {
                        if (value.getFirst("tzName").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Time zone Name is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("tzName").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Time zone Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Time zone Name is Required"
                                )
                        );
                    }



                    if (messages.isEmpty() != true){
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

    }}
