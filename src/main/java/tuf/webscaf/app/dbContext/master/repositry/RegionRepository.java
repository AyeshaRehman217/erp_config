package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.master.entity.RegionEntity;

import java.util.UUID;

@Repository
public interface RegionRepository extends ReactiveCrudRepository<RegionEntity, Long> {

    Mono<RegionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<RegionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<RegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<RegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
