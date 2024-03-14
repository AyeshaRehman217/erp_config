package tuf.webscaf.app.dbContext.slave.repositry.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomCountryMapper implements BiFunction<Row, Object, SlaveCountryEntity> {

    @Override
    public SlaveCountryEntity apply(Row source, Object o) {

        return SlaveCountryEntity.builder()
                .id(source.get("id", Long.class))
                .version(source.get("version", Long.class))
                .uuid(source.get("uuid", UUID.class))
                .jsonId(source.get("json_id", Long.class))
                .status(source.get("status", Boolean.class))
                .name(source.get("name", String.class))
                .description(source.get("description", String.class))
                .iso2(source.get("iso2", String.class))
                .iso3(source.get("iso3", String.class))
                .numericCode(source.get("numeric_code", Integer.class))
                .phoneCode(source.get("phone_code", String.class))
                .capital(source.get("capital", String.class))
                .tld(source.get("tld", String.class))
                .nativeName(source.get("native_name", String.class))
                .longitude(source.get("longitude", Double.class))
                .latitude(source.get("latitude", Double.class))
                .emoji(source.get("emoji", String.class))
                .emojiU(source.get("emoji_u", String.class))
                .currencyUUID(source.get("currency_uuid", UUID.class))
                .regionUUID(source.get("region_uuid", UUID.class))
                .subRegionUUID(source.get("sub_region_uuid", UUID.class))
                .createdBy(source.get("created_by", UUID.class))
                .updatedBy(source.get("updated_by", UUID.class))
                .createdAt(source.get("created_at", LocalDateTime.class))
                .updatedAt(source.get("updated_at", LocalDateTime.class))
                .archived(source.get("archived", Boolean.class))
                .editable(source.get("editable", Boolean.class))
                .deletable(source.get("deletable", Boolean.class))
                .reqCompanyUUID(source.get("req_company_uuid", UUID.class))
                .reqBranchUUID(source.get("req_branch_uuid", UUID.class))
                .reqCreatedIP(source.get("req_created_ip", String.class))
                .reqCreatedPort(source.get("req_created_port", String.class))
                .reqCreatedBrowser(source.get("req_created_browser", String.class))
                .reqCreatedOS(source.get("req_created_os", String.class))
                .reqCreatedDevice(source.get("req_created_device", String.class))
                .reqCreatedReferer(source.get("req_created_referer", String.class))
                .reqUpdatedIP(source.get("req_updated_ip", String.class))
                .reqUpdatedPort(source.get("req_updated_port", String.class))
                .reqUpdatedBrowser(source.get("req_updated_browser", String.class))
                .reqUpdatedOS(source.get("req_updated_os", String.class))
                .reqUpdatedDevice(source.get("req_updated_device", String.class))
                .reqUpdatedReferer(source.get("req_updated_referer", String.class))
                .build();
    }
}
