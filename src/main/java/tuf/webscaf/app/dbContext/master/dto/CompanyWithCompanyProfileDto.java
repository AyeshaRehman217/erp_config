package tuf.webscaf.app.dbContext.master.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyWithCompanyProfileDto {

    private Long id;

    private UUID uuid;

    private Long version;

    private Boolean status;

    private String name;

    private String description;

    private LocalDateTime establishmentDate;

    private UUID companyProfileUUID;

    private UUID docImage;

    private UUID languageUUID;

    private UUID locationUUID;

    private UUID countryUUID;

    private UUID currencyUUID;

    private UUID cityUUID;

    private UUID stateUUID;

    private UUID createdBy;

    private UUID updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

//    private UUID reqCompanyUUID;

    private UUID reqBranchUUID;

    private String reqCreatedBrowser;

    private String reqCreatedIP;

    private String reqCreatedPort;

    private String reqCreatedOS;

    private String reqCreatedDevice;

    private String reqCreatedReferer;

    private String reqUpdatedBrowser;

    private String reqUpdatedIP;

    private String reqUpdatedPort;

    private String reqUpdatedOS;

    private String reqUpdatedDevice;

    private String reqUpdatedReferer;

    private Boolean editable;

    private Boolean archived;

    private Boolean deletable;
}
