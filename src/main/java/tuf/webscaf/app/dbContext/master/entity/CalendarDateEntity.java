package tuf.webscaf.app.dbContext.master.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("public.\"calendar_dates\"")

public class CalendarDateEntity {
    @Id
    @Column
    @Schema(hidden = true)
    private Long id;

    @Version
    @Schema(hidden = true)
    private Long version;

    @Column("uuid")
    @Schema(hidden = true)
    private UUID uuid;

    @Column("calendar_uuid")
    @Schema(required = true)
    private UUID calendarUUID;

    @Column("date")
    private LocalDate date;

    @Column("month_name")
    private String monthName;

    @Column("day_name")
    private String dayName;

    @Column("year")
    private Integer year;

    @Column("month")
    private Integer month;

    @Column("day")
    private Integer day;

    @Column("quarter")
    private Integer quarter;

    @Column("week")
    private Integer week;

    @Column("day_of_week")
    private Integer dayOfWeek;

    @Column("day_of_year")
    private Integer dayOfYear;

    @Column("week_of_year")
    private Integer weekOfYear;

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
