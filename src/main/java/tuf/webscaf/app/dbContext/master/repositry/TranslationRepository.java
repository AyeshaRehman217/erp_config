package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TranslationEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranslationRepository extends ReactiveCrudRepository<TranslationEntity, Long> {

    Mono<TranslationEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<TranslationEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<TranslationEntity> findFirstByKeyIgnoreCaseAndDeletedAtIsNull(String key);

    Mono<TranslationEntity> findFirstByKeyIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String key, UUID uuid);

    Flux<TranslationEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<TranslationEntity> findAllByDeletedAtIsNull();

}
