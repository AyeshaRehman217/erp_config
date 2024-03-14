package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAdministrationDepartmentMapper;

import java.util.UUID;


public class SlaveCustomAdministrationDepartmentRepositoryImpl implements SlaveCustomAdministrationDepartmentRepository {
    private DatabaseClient client;
    private SlaveAdministrationDepartmentEntity slaveAdministrationDepartmentEntity;

    @Autowired
    public SlaveCustomAdministrationDepartmentRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


    @Override
    public Flux<SlaveAdministrationDepartmentEntity> showAdministrationDepartmentsAgainstCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  * from administration_departments " +
                "left join companies on companies.uuid = administration_departments.company_uuid " +
                " where companies.deleted_at is null and administration_departments.deleted_at is null" +
                " and companies.uuid = '" + companyUUID +
                "' and (administration_departments.name ILIKE  '%" + name + "%'  " +
                " or administration_departments.description ILIKE  '%" + description + "%') " +
                " order by administration_departments." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAdministrationDepartmentEntity> showAdministrationDepartmentsAgainstCompanyAndStatus(UUID companyUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  * from administration_departments " +
                "left join companies on companies.uuid = administration_departments.company_uuid " +
                " where companies.deleted_at is null and administration_departments.deleted_at is null " +
                "and companies.uuid = '" + companyUUID +
                "' and administration_departments.status = " + status +
                " and (administration_departments.name ILIKE  '%" + name + "%'  " +
                "or administration_departments.description ILIKE  '%" + description + "%') " +
                " order by administration_departments." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomAdministrationDepartmentMapper mapper = new SlaveCustomAdministrationDepartmentMapper();

        Flux<SlaveAdministrationDepartmentEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAdministrationDepartmentEntity))
                .all();

        return result;
    }
}

