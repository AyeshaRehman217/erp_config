package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.SubRegionEntity;

import java.util.UUID;

@Repository
public interface SubRegionRepository extends ReactiveCrudRepository<SubRegionEntity, Long> {

    Mono<SubRegionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SubRegionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SubRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SubRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

//    Mono<SubRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<SubRegionEntity> findFirstByRegionUUIDAndDeletedAtIsNull(UUID regionUUID);

}
