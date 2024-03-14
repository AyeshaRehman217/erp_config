package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchAdministrationDepartmentPvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAdministrationDepartmentMapper;

import java.util.UUID;


public class SlaveCustomBranchAdministrationDepartmentPvtRepositoryImpl implements SlaveCustomBranchAdministrationDepartmentPvtRepository {
    private DatabaseClient client;
    private SlaveAdministrationDepartmentEntity slaveAdministrationDepartmentEntity;

    @Autowired
    public SlaveCustomBranchAdministrationDepartmentPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the unmapped AdministrationDepartment List Against branch uuid
    @Override
    public Flux<SlaveAdministrationDepartmentEntity> unMappedAdministrationDepartmentListAgainstBranch(UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "SELECT administration_departments.* FROM administration_departments\n" +
                "WHERE administration_departments.uuid Not In (\n" +
                "SELECT administration_departments.uuid FROM administration_departments\n" +
                "LEFT JOIN branch_administration_department_pvt\n" +
                "ON branch_administration_department_pvt.administration_department_uuid = administration_departments.uuid\n" +
                "WHERE branch_administration_department_pvt.branch_uuid = '" + branchUUID +
                "' AND branch_administration_department_pvt.deleted_at IS NULL\n" +
                "AND administration_departments.deleted_at IS NULL ) \n" +
                "AND administration_departments.deleted_at is null " +
                "AND (administration_departments.name ILIKE '%" + name + "%' \n" +
                " or administration_departments.description ILIKE '%" + description + "%') \n" +
                "ORDER BY administration_departments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAdministrationDepartmentEntity> unMappedAdministrationDepartmentListWithStatusFilterAgainstBranch(UUID branchUUID, String name,String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT administration_departments.* FROM administration_departments\n" +
                "WHERE administration_departments.uuid Not In (\n" +
                "SELECT administration_departments.uuid FROM administration_departments\n" +
                "LEFT JOIN branch_administration_department_pvt\n" +
                "ON branch_administration_department_pvt.administration_department_uuid = administration_departments.uuid\n" +
                "WHERE branch_administration_department_pvt.branch_uuid = '" + branchUUID +
                "' AND branch_administration_department_pvt.deleted_at IS NULL\n" +
                "AND administration_departments.deleted_at IS NULL ) \n" +
                "AND administration_departments.deleted_at is null " +
                "AND administration_departments.status = " + status +
                " AND (administration_departments.name ILIKE '%" + name + "%' \n" +
                " or administration_departments.description ILIKE '%" + description + "%') \n" +
                "ORDER BY administration_departments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAdministrationDepartmentEntity> mappedAdministrationDepartmentListAgainstBranch(UUID branchUUID, String name, String description, String dp, String d, Integer size, Long page) {
        String query = "select administration_departments.* from administration_departments\n" +
                " left join branch_administration_department_pvt \n" +
                " on administration_departments.uuid = branch_administration_department_pvt.administration_department_uuid\n" +
                " where branch_administration_department_pvt.branch_uuid = '" + branchUUID +
                "' and administration_departments.deleted_at is null\n" +
                "and branch_administration_department_pvt.deleted_at is null\n" +
                "AND (administration_departments.name ILIKE '%" + name + "%' " +
                "or administration_departments.description ILIKE '%" + description + "%')" +
                "order by administration_departments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAdministrationDepartmentEntity> mappedAdministrationDepartmentListWithStatusFilterAgainstBranch(UUID branchUUID, String name, String description, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "select administration_departments.* from administration_departments\n" +
                "left join branch_administration_department_pvt \n" +
                "on administration_departments.uuid = branch_administration_department_pvt.administration_department_uuid\n" +
                "where branch_administration_department_pvt.branch_uuid = '" + branchUUID +
                "' and administration_departments.deleted_at is null\n" +
                "and branch_administration_department_pvt.deleted_at is null\n" +
                "and administration_departments.status = " + status +
                " AND (administration_departments.name ILIKE '%" + name + "%' " +
                "or administration_departments.description ILIKE '%" + description + "%')" +
                "order by administration_departments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }
}

