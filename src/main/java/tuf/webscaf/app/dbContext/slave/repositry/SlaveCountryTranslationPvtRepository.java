package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryTranslationPvtEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTranslationPvtRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveCountryTranslationPvtRepository extends ReactiveCrudRepository<SlaveCountryTranslationPvtEntity, Long> {
    Mono<SlaveCountryTranslationPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
