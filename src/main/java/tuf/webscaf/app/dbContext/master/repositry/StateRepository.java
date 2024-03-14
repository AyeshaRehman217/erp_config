package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.StateEntity;

import java.util.UUID;

@Repository
public interface StateRepository extends ReactiveCrudRepository<StateEntity, Long> {
    Mono<StateEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<StateEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<StateEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<StateEntity> findFirstByNameIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(String name, UUID countryUUID);

    Mono<StateEntity> findFirstByNameIgnoreCaseAndCountryUUIDAndDeletedAtIsNullAndUuidIsNot(String name, UUID countryUUID, UUID uuid);
}
