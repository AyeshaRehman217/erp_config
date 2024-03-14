package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LanguageEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCurrencyEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;

import java.util.UUID;

@Repository
public interface LanguageRepository extends ReactiveCrudRepository<LanguageEntity, Long> {

    Mono<LanguageEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LanguageEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LanguageEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<LanguageEntity> findFirstByLanguageCodeIgnoreCaseAndDeletedAtIsNull(String languageCode);

//    Mono<LanguageEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<LanguageEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<LanguageEntity> findFirstByLanguageCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String languageCode, UUID uuid);

}
