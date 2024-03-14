package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CountryTranslationPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CountryTranslationPvtRepository extends ReactiveCrudRepository<CountryTranslationPvtEntity, Long> {
    Mono<CountryTranslationPvtEntity> findFirstByCountryUUIDAndTranslationUUIDAndDeletedAtIsNull(UUID countryUUID, UUID translationUUID);

    Flux<CountryTranslationPvtEntity> findByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Flux<CountryTranslationPvtEntity> findAllByCountryUUIDAndTranslationUUIDInAndDeletedAtIsNull(UUID countryUUID, List<UUID> translationUUID);

    Mono<CountryTranslationPvtEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<CountryTranslationPvtEntity> findFirstByTranslationUUIDAndDeletedAtIsNull(UUID translationUUID);

    Mono<CountryTranslationPvtEntity> findByIdAndDeletedAtIsNull(Long documentId);



}
