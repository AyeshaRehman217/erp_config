package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CityEntity;

import java.util.UUID;

@Repository
public interface CityRepository extends ReactiveCrudRepository<CityEntity, Long> {
    Mono<CityEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CityEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CityEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<CityEntity> findFirstByStateUUIDAndDeletedAtIsNull(UUID stateUUID);

    Mono<CityEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CityEntity> findFirstByNameIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(String name, UUID countryUUID, UUID stateUUID);

    Mono<CityEntity> findFirstByNameIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullAndUuidIsNot(String name, UUID countryUUID, UUID stateUUID, UUID uuid);
}
