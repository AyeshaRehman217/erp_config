package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;

import java.util.UUID;

@Repository
public interface DocBucketRepository extends ReactiveCrudRepository<DocBucketEntity, Long> {

    Mono<DocBucketEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<DocBucketEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<DocBucketEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNull(String  slug);

    Mono<DocBucketEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String slug, UUID uuid);

    Mono<DocBucketEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<DocBucketEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<DocBucketEntity> findBySlugAndDeletedAtIsNull(String slug);

    Flux<DocBucketEntity> findAllByIsActiveAndDeletedAtIsNull(Boolean isActive);

    Mono<DocBucketEntity> findByIsActiveAndDeletedAtIsNull(Boolean isActive);
}
