package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomDocBucketRepository;

import java.util.UUID;


@Repository
public interface SlaveDocBucketRepository extends ReactiveCrudRepository<SlaveDocBucketEntity, Long>, SlaveCustomDocBucketRepository {

    Flux<SlaveDocBucketEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description, Pageable pageable);

    Mono<SlaveDocBucketEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveDocBucketEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveDocBucketEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);


}
