package tuf.webscaf.app.dbContext.master.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("public.\"currencies\"")
public class CurrencyEntity {
    @Id
    @Column("id")
    @Schema(hidden = true)
    private Long id;

    @Version
    @Schema(hidden = true)
    private Long version;

    @Column("uuid")
    @Schema(hidden = true)
    private UUID uuid;

    @Column("status")
    private Boolean status;

    @Column("currency_name")
    @Schema(required = true)
    private String currencyName;

    @Column
    private String description;

    @Column("currency")
    @Schema(required = true)
    private String currency;

    @Column("currency_symbol")
    @Schema(required = true)
    private String currencySymbol;

    @Column("iso_number")
    private Integer isoNumber;

    @Column("created_by")
    @Schema(hidden = true)
    @CreatedBy
    private UUID createdBy;

    @Column("created_at")
    @Schema(hidden = true)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column("updated_by")
    @Schema(hidden = true)
    @CreatedBy
    private UUID updatedBy;

    @Column("updated_at")
    @Schema(hidden = true)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column("deleted_by")
    @Schema(hidden = true)
    private UUID deletedBy;

    @Column("deleted_at")
    @Schema(hidden = true)
    private LocalDateTime deletedAt;

    @Column("req_company_uuid")
    @Schema(hidden = true)
    private UUID reqCompanyUUID;

    @Column("req_branch_uuid")
    @Schema(hidden = true)
    private UUID reqBranchUUID;

    @Column("req_created_browser")
    @Schema(hidden = true)
    private String reqCreatedBrowser;

    @Column("req_created_ip")
    @Schema(hidden = true)
    private String reqCreatedIP;

    @Column("req_created_port")
    @Schema(hidden = true)
    private String reqCreatedPort;

    @Column("req_created_os")
    @Schema(hidden = true)
    private String reqCreatedOS;

    @Column("req_created_device")
    @Schema(hidden = true)
    private String reqCreatedDevice;

    @Column("req_created_referer")
    @Schema(hidden = true)
    private String reqCreatedReferer;

    @Column("req_updated_browser")
    @Schema(hidden = true)
    private String reqUpdatedBrowser;

    @Column("req_updated_ip")
    @Schema(hidden = true)
    private String reqUpdatedIP;

    @Column("req_updated_port")
    @Schema(hidden = true)
    private String reqUpdatedPort;

    @Column("req_updated_os")
    @Schema(hidden = true)
    private String reqUpdatedOS;

    @Column("req_updated_device")
    @Schema(hidden = true)
    private String reqUpdatedDevice;

    @Column("req_updated_referer")
    @Schema(hidden = true)
    private String reqUpdatedReferer;

    @Column("req_deleted_browser")
    @Schema(hidden = true)
    private String reqDeletedBrowser;

    @Column("req_deleted_ip")
    @Schema(hidden = true)
    private String reqDeletedIP;

    @Column("req_deleted_port")
    @Schema(hidden = true)
    private String reqDeletedPort;

    @Column("req_deleted_os")
    @Schema(hidden = true)
    private String reqDeletedOS;

    @Column("req_deleted_device")
    @Schema(hidden = true)
    private String reqDeletedDevice;

    @Column("req_deleted_referer")
    @Schema(hidden = true)
    private String reqDeletedReferer;


    @Column
    @Schema(hidden = true)
    private Boolean editable;

    @Column
    @Schema(hidden = true)
    private Boolean deletable;

    @Column
    @Schema(hidden = true)
    private Boolean archived;


}
