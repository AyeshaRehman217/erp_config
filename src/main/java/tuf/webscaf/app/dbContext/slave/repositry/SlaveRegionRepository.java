package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.RegionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveRegionEntity;

import java.util.UUID;

@Repository
public interface SlaveRegionRepository extends ReactiveCrudRepository<SlaveRegionEntity, Long> {

    Mono<SlaveRegionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveRegionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveRegionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Flux<SlaveRegionEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name,String description);

    Flux<SlaveRegionEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

}
