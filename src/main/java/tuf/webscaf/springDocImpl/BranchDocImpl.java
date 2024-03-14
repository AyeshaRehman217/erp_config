package tuf.webscaf.springDocImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BranchDocImpl {

    @Schema(required = true)
    private String name;

    private boolean status;

    private String description;

    @Schema(required = true)
    private LocalDateTime establishmentDate;

    @Schema(required = true)
    private UUID companyUUID;

    @Schema(required = true)
    private UUID locationUUID;

    @Schema(required = true)
    private UUID languageUUID;

}
