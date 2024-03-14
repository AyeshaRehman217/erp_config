package tuf.webscaf.app.http.validationFilters.countryHandler;

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

public class UpdateCountryHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {


        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (!request.pathVariable("uuid").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Invalid UUID"
                                )
                        );
                    }

                    if (value.containsKey("name")) {
                        if (value.getFirst("name").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Name  is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("name").matches("[^<>]*")) {
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

                    if (value.containsKey("iso2")) {
                        if (value.getFirst("iso2").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "iso2 is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("iso2").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid iso2"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "iso2 is Required"
                                )
                        );
                    }

                    if (value.containsKey("iso3")) {
                        if (value.getFirst("iso3").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "iso3 is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("iso3").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid iso3"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "iso3 is Required"
                                )
                        );
                    }


                    if (value.containsKey("numericCode")) {
                        if (value.getFirst("numericCode").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "numericCode is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("numericCode").matches("^[0-9]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid numericCode"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "numericCode is Required"
                                )
                        );
                    }

                    if (value.containsKey("phoneCode")) {

                        if (value.getFirst("phoneCode").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Phone No Field Required"
                                    )
                            );
                        } else {

                            // get encoded value of phone No from request
                            String encodedPhoneNo = value.getFirst("phoneCode");

                            String decodedPhoneNo = "";

                            // decode the value of Phone No
                            try {
                                decodedPhoneNo = URLDecoder.decode(encodedPhoneNo, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if (!decodedPhoneNo.matches("^[\\\\sa-zA-Z0-9*.,!@#$&()_+\\-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Phone Code"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Phone Code Field Required"
                                )
                        );
                    }


                    if (value.containsKey("capital")) {
                        if (value.getFirst("capital").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Capital is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("capital").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Capital"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Capital is Required"
                                )
                        );
                    }


                    if (value.containsKey("tld")) {
                        if (value.getFirst("tld").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "tld is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("tld").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid tld"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "tld is Required"
                                )
                        );
                    }

                    if (value.containsKey("nativeName")) {

                        if (value.getFirst("nativeName").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Native Name Field Required"
                                    )
                            );
                        } else {

                            // get encoded value of nativeName from request
                            String encodedNativeName = value.getFirst("nativeName");

                            String decodedNativeName = "";

                            // decode the value of nativeName
                            try {
                                decodedNativeName = URLDecoder.decode(encodedNativeName, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if (!decodedNativeName.matches("^[\\p{L}\\p{M}\\p{Zs}]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Native Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Native Name Field Required"
                                )
                        );
                    }


                    if (value.containsKey("emoji")) {
                        if (!value.getFirst("emoji").isEmpty()) {

                            // get encoded value of emoji from request
                            String encodedEmoji = value.getFirst("emoji");

                            String decodedEmoji = "";

                            // decode the value of emoji
                            try {
                                decodedEmoji = URLDecoder.decode(encodedEmoji, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if (!decodedEmoji.matches("[^<>]*")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid emoji"
                                        )
                                );
                            }
                        }
                    }


                    if (value.containsKey("emojiU")) {
                        if (!value.getFirst("emoji").isEmpty()) {

                            // get encoded value of emoji from request
                            String encodedEmojiUnicode = value.getFirst("emojiU");

                            String decodedEmojiUnicode = "";

                            // decode the value of emoji
                            try {
                                decodedEmojiUnicode = URLDecoder.decode(encodedEmojiUnicode, "UTF-8");
                            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                                e.printStackTrace();
                            }

                            if (!decodedEmojiUnicode.matches("[^<>]*")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid emojiU"
                                        )
                                );
                            }
                        }
                    }


                    if (value.containsKey("longitude")) {
                        if (!value.getFirst("longitude").matches("^-?\\d+(\\.\\d+)?$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Longitude"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("latitude")) {
                        if (!value.getFirst("latitude").matches("^-?\\d+(\\.\\d+)?$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Latitude"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("currencyUUID")) {
                        if (value.getFirst("currencyUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Currency is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("currencyUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Currency"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Currency is Required"
                                )
                        );
                    }

                    if (value.containsKey("regionUUID")) {
                        if (value.getFirst("regionUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Region is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("regionUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Region"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Region is Required"
                                )
                        );
                    }

                    if (value.containsKey("subRegionUUID")) {
                        if (value.getFirst("subRegionUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Sub Region is Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("subRegionUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Sub Region"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Sub Region is Required"
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
