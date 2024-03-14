package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveConfigModuleDto {

    Long moduleId;
    UUID moduleUUID;
    String baseUrl;
    String infoUrl;
    String hostAddress;
}
