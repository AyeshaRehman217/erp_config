package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAdministrativeDepartmentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomSubAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomSubAdministrationDepartmentMapper;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomSubAdministrativeDepartmentMapper;

import java.util.UUID;


public class SlaveCustomSubAdministrationDepartmentRepositoryImpl implements SlaveCustomSubAdministrationDepartmentRepository {
    private DatabaseClient client;
    private SlaveSubAdministrationDepartmentEntity slaveSubAdministrationDepartmentEntity;
    private SlaveSubAdministrativeDepartmentDto slaveSubDepartmentDto;

    @Autowired
    public SlaveCustomSubAdministrationDepartmentRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    @Override
    public Flux<SlaveSubAdministrativeDepartmentDto> showAllSubAdministrativeDepartmentOfDept(UUID deptUUID) {
        String query = "WITH RECURSIVE fetch_all_sub_departments_cte (uuid) as\n" +
                "( SELECT sub_administration_departments.uuid, sub_administration_departments.sub_administration_department_uuid, sub_administration_departments.administration_department_uuid from sub_administration_departments \n" +
                " where sub_administration_departments.administration_department_uuid= '" + deptUUID +
                "' and sub_administration_departments.deleted_at is null \n" +
                " UNION ALL\n" +
                " SELECT sub_administration_departments.uuid, sub_administration_departments.sub_administration_department_uuid, sub_administration_departments.administration_department_uuid from fetch_all_sub_departments_cte, sub_administration_departments \n" +
                " where sub_administration_departments.administration_department_uuid= fetch_all_sub_departments_cte.sub_administration_department_uuid\n" +
                "   and sub_administration_departments.deleted_at is null  )\n" +
                "SELECT uuid,administration_department_uuid,sub_administration_department_uuid FROM fetch_all_sub_departments_cte";

        SlaveCustomSubAdministrativeDepartmentMapper mapper = new SlaveCustomSubAdministrativeDepartmentMapper();

        Flux<SlaveSubAdministrativeDepartmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveSubDepartmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveSubAdministrationDepartmentEntity> showSubAdministrationDepartmentsAgainstAdministrationDepartment(UUID administrationDepartmentUUID, Integer size, Long page, String dp, String d) {
        String query = "select  * from sub_administration_departments " +
                "left join administration_departments on administration_departments.uuid = sub_administration_departments.administration_department_uuid " +
                " where administration_departments.deleted_at is null " +
                " and sub_administration_departments.deleted_at is null " +
                " and administration_departments.uuid = '" + administrationDepartmentUUID +
                "' order by sub_administration_departments." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomSubAdministrationDepartmentMapper mapper = new SlaveCustomSubAdministrationDepartmentMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAdministrationDepartmentEntity))
                .all();
    }

    @Override
    public Flux<SlaveSubAdministrationDepartmentEntity> showSubAdministrationDepartmentsAgainstAdministrationDepartmentAndStatus(UUID administrationDepartmentUUID, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select  * from sub_administration_departments " +
                " left join administration_departments on administration_departments.uuid = sub_administration_departments.administration_department_uuid " +
                " where administration_departments.deleted_at is null and sub_administration_departments.deleted_at is null " +
                " and administration_departments.uuid = '" + administrationDepartmentUUID +
                "' and sub_administration_departments.status = " + status +
                " order by sub_administration_departments." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomSubAdministrationDepartmentMapper mapper = new SlaveCustomSubAdministrationDepartmentMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveSubAdministrationDepartmentEntity))
                .all();
    }
}

