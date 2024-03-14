package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchAdministrationDepartmentPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface BranchAdministrationDepartmentPvtRepository extends ReactiveCrudRepository<BranchAdministrationDepartmentPvtEntity, Long> {

    Mono<BranchAdministrationDepartmentPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<BranchAdministrationDepartmentPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<BranchAdministrationDepartmentPvtEntity> findFirstByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Flux<BranchAdministrationDepartmentPvtEntity> findAllByBranchUUIDAndDeletedAtIsNull(UUID branchUUID);

    Mono<BranchAdministrationDepartmentPvtEntity> findFirstByBranchUUIDAndAdministrationDepartmentUUIDAndDeletedAtIsNull(UUID branchUUID, UUID administrationDepartmentUUID);

    Flux<BranchAdministrationDepartmentPvtEntity> findAllByBranchUUIDAndAdministrationDepartmentUUIDInAndDeletedAtIsNull(UUID branchUUID, List<UUID> ids);

    Mono<BranchAdministrationDepartmentPvtEntity> findFirstByAdministrationDepartmentUUIDAndDeletedAtIsNull(UUID administrationDepartmentUUID);

}
