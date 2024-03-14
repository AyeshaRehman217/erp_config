package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LocationEntity;

import java.util.UUID;

@Repository
public interface LocationRepository extends ReactiveCrudRepository<LocationEntity, Long> {
    Mono<LocationEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LocationEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LocationEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<LocationEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<LocationEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

//    Mono<LocationEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<LocationEntity> findFirstByCityUUIDAndDeletedAtIsNull(UUID cityUUID);

    Mono<LocationEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<LocationEntity> findFirstByStateUUIDAndDeletedAtIsNull(UUID stateUUID);
}
