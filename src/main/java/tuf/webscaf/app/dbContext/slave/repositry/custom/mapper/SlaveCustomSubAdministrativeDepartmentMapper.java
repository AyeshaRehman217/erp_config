package tuf.webscaf.app.dbContext.slave.repositry.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAdministrativeDepartmentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomSubAdministrativeDepartmentMapper implements BiFunction<Row, Object, SlaveSubAdministrativeDepartmentDto> {

    @Override
    public SlaveSubAdministrativeDepartmentDto apply(Row source, Object o) {

        return SlaveSubAdministrativeDepartmentDto.builder()
                .uuid(source.get("uuid",UUID.class))
                .administrativeDeptUUID(source.get("administration_department_uuid",UUID.class))
                .subAdministrativeDeptUUID(source.get("sub_administration_department_uuid",UUID.class))
                .build();
    }
}
