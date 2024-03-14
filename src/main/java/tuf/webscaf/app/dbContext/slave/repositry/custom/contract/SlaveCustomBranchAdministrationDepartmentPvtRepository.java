package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;

import java.util.UUID;

/**
 * This Custom Repository extends in Slave Branch-Administration-Department-Pvt Repository
 **/
@Repository
public interface SlaveCustomBranchAdministrationDepartmentPvtRepository {

    Flux<SlaveAdministrationDepartmentEntity> unMappedAdministrationDepartmentListAgainstBranch(UUID branchUUID, String name,String description, String dp, String d, Integer size, Long page);

    Flux<SlaveAdministrationDepartmentEntity> unMappedAdministrationDepartmentListWithStatusFilterAgainstBranch(UUID branchUUID, String name,String description, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveAdministrationDepartmentEntity> mappedAdministrationDepartmentListAgainstBranch(UUID branchUUID, String name,String description, String dp, String d, Integer size, Long page);

    Flux<SlaveAdministrationDepartmentEntity> mappedAdministrationDepartmentListWithStatusFilterAgainstBranch(UUID branchUUID, String name,String description, Boolean status, String dp, String d, Integer size, Long page);
}
