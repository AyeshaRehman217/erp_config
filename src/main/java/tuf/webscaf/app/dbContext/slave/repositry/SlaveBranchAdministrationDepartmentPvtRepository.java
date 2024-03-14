package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchAdministrationDepartmentPvtEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchAdministrationDepartmentPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveBranchAdministrationDepartmentPvtRepository extends ReactiveCrudRepository<SlaveBranchAdministrationDepartmentPvtEntity, Long> {

    Mono<SlaveBranchAdministrationDepartmentPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveBranchAdministrationDepartmentPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Flux<SlaveBranchAdministrationDepartmentPvtEntity> findAllByDeletedAtIsNull(Pageable pageable);

}
