package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveSubAdministrativeDepartmentDto {
    private UUID uuid;
    private UUID administrativeDeptUUID;
    private UUID subAdministrativeDeptUUID;
}
