package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CountryTimezonePvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CountryTimezonePvtRepository extends ReactiveCrudRepository<CountryTimezonePvtEntity, Long> {


    Flux<CountryTimezonePvtEntity> findByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Flux<CountryTimezonePvtEntity> findAllByCountryUUIDAndTimezoneUUIDInAndDeletedAtIsNull(UUID countryUUID, List<UUID> timeZoneUUID);

    Mono<CountryTimezonePvtEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<CountryTimezonePvtEntity> findFirstByCountryUUIDAndTimezoneUUIDAndDeletedAtIsNull(UUID countryUUID, UUID timeZoneUUID);

    Mono<CountryTimezonePvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CountryTimezonePvtEntity> findFirstByTimezoneUUIDAndDeletedAtIsNull(UUID timezoneUUID);

}
