package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;

import java.util.UUID;

/**
 * This Custom Repository will extend in Slave AdministrationDepartment Repository
 **/
@Repository
public interface SlaveCustomAdministrationDepartmentRepository {

    /**
     * This Function Show AdministrationDepartment Records Based on Company Uuid
     **/
    Flux<SlaveAdministrationDepartmentEntity> showAdministrationDepartmentsAgainstCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d);

    /**
     * This Function Show AdministrationDepartment Records Based on Company Uuid and Status
     **/
    Flux<SlaveAdministrationDepartmentEntity> showAdministrationDepartmentsAgainstCompanyAndStatus(UUID companyUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d);

}
