package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveSubAdministrativeDepartmentDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAdministrationDepartmentEntity;

import java.util.UUID;

/**
 * This Custom Repository will extend in Slave SubAdministrationDepartment Repository
 **/
@Repository
public interface SlaveCustomSubAdministrationDepartmentRepository {

    /**
     * Fetch All Sub Departments(Child) of the Given Department(Parent)
     **/
    Flux<SlaveSubAdministrativeDepartmentDto> showAllSubAdministrativeDepartmentOfDept(UUID deptUUID);

    /**
     * This Function Show SubAdministrationDepartment Records Based on AdministrationDepartment Uuid
     **/
    Flux<SlaveSubAdministrationDepartmentEntity> showSubAdministrationDepartmentsAgainstAdministrationDepartment(UUID administrationDepartmentUUID, Integer size, Long page, String dp, String d);

    /**
     * This Function Show SubAdministrationDepartment Records Based on AdministrationDepartment Uuid and Status
     **/
    Flux<SlaveSubAdministrationDepartmentEntity> showSubAdministrationDepartmentsAgainstAdministrationDepartmentAndStatus(UUID administrationDepartmentUUID, Boolean status, Integer size, Long page, String dp, String d);

}
