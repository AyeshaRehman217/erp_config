package tuf.webscaf.springDocImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDocImpl {

    @Schema(required = true)
    private String name;

    private boolean status;

    private String description;

    @Schema(required = true)
    private LocalDateTime establishmentDate;

    @Schema(required = true)
    private UUID languageUUID;

    private UUID docImage;

    @Schema(required = true)
    private UUID countryUUID;

    @Schema(required = true)
    private UUID currencyUUID;

    @Schema(required = true)
    private UUID stateUUID;

    @Schema(required = true)
    private UUID cityUUID;

    @Schema(required = true)
    private UUID locationUUID;
}
