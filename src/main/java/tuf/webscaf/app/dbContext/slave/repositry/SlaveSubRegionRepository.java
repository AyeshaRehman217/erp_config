package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.SubRegionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveRegionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubRegionEntity;

import java.util.UUID;

@Repository
public interface SlaveSubRegionRepository extends ReactiveCrudRepository<SlaveSubRegionEntity, Long> {

    Mono<SlaveSubRegionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveSubRegionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveSubRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveSubRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Flux<SlaveSubRegionEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Flux<SlaveSubRegionEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    /**
     * Fetch Sub Region Records Against Status Filter and Region UUID
     **/
    Flux<SlaveSubRegionEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUID(Pageable pageable, String name, Boolean status, UUID regionUUID, String description, Boolean status1, UUID regionUUID1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndRegionUUID(String name, Boolean status, UUID regionUUID, String description, Boolean status1, UUID regionUUI1);

    Flux<SlaveSubRegionEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUID(Pageable pageable, String name, UUID regionUUID, String description, UUID regionUUID1);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUIDOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndRegionUUID(String name, UUID regionUUID, String description, UUID regionUUI1);
}
