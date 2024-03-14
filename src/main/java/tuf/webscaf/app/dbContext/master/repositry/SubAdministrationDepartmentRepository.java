package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.master.entity.BranchAdministrationDepartmentPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.SubAdministrationDepartmentEntity;

import java.util.UUID;

@Repository
public interface SubAdministrationDepartmentRepository extends ReactiveCrudRepository<SubAdministrationDepartmentEntity, Long> {

    Mono<SubAdministrationDepartmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SubAdministrationDepartmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SubAdministrationDepartmentEntity> findFirstBySubAdministrationDepartmentUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNull(UUID subAdministrationDepartmentUUID,UUID administrationDepartmentUUID);

    Mono<SubAdministrationDepartmentEntity> findFirstBySubAdministrationDepartmentUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNullAndUuidIsNot(UUID subAdministrationDepartmentUUID,UUID administrationDepartmentUUID, UUID uuid);

    Mono<SubAdministrationDepartmentEntity> findFirstByAdministrationDepartmentUUIDAndDeletedAtIsNull(UUID administrationDepartmentUUID);
}
