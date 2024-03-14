package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchProfileEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchProfileEntity;

import java.util.UUID;

@Repository
public interface SlaveBranchProfileRepository extends ReactiveCrudRepository<SlaveBranchProfileEntity, Long> {
    Mono<SlaveBranchProfileEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
