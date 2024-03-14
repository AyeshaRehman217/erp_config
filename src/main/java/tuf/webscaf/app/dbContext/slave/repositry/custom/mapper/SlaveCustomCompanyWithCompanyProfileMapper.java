package tuf.webscaf.app.dbContext.slave.repositry.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;

import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyProfileEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomCompanyWithCompanyProfileMapper implements BiFunction<Row, Object, SlaveCompanyWithCompanyProfileDto> {

    @Override
    public SlaveCompanyWithCompanyProfileDto apply(Row source, Object o) {


        return SlaveCompanyWithCompanyProfileDto.builder()
                .id(source.get("id", Long.class))
                .uuid(source.get("uuid",UUID.class))
                .docImage(source.get("CompanyImageID", UUID.class))
                .version(source.get("version", Long.class))
                .status(source.get("status", Boolean.class))
                .name(source.get("name", String.class))
                .description(source.get("description", String.class))
                .establishmentDate(source.get("establishmentDate", LocalDateTime.class))
                .companyProfileUUID(source.get("company_profile_uuid", UUID.class))
                .languageUUID(source.get("languageUUID", UUID.class))
                .locationUUID(source.get("locationUUID", UUID.class))
                .countryUUID(source.get("countryUUID", UUID.class))
                .stateUUID(source.get("stateUUID", UUID.class))
                .cityUUID(source.get("cityUUID", UUID.class))
                .currencyUUID(source.get("currencyUUID", UUID.class))
                .createdBy(source.get("createdBy", UUID.class))
                .updatedBy(source.get("updatedBy", UUID.class))
                .createdAt(source.get("createdAt", LocalDateTime.class))
                .updatedAt(source.get("updatedAt", LocalDateTime.class))
                .archived(source.get("archived", Boolean.class))
                .editable(source.get("editable", Boolean.class))
//                .reqCompanyUUID(source.get("req_company_uuid", UUID.class))
                .reqBranchUUID(source.get("reqBranchUUID", UUID.class))
                .reqCreatedIP(source.get("reqCreatedIP", String.class))
                .reqCreatedPort(source.get("reqCreatedPort", String.class))
                .reqCreatedBrowser(source.get("reqCreatedBrowser", String.class))
                .reqCreatedOS(source.get("reqCreatedOS", String.class))
                .reqCreatedDevice(source.get("reqCreatedDevice", String.class))
                .reqCreatedReferer(source.get("reqCreatedReferer", String.class))
                .reqUpdatedIP(source.get("reqUpdatedIP", String.class))
                .reqUpdatedPort(source.get("reqUpdatedPort", String.class))
                .reqUpdatedBrowser(source.get("reqUpdatedBrowser", String.class))
                .reqUpdatedOS(source.get("reqUpdatedOS", String.class))
                .reqUpdatedDevice(source.get("reqUpdatedDevice", String.class))
                .reqUpdatedReferer(source.get("reqUpdatedReferer", String.class))
                .build();
    }
}
