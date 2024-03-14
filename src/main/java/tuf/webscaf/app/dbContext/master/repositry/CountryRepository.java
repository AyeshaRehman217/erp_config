package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface CountryRepository extends ReactiveCrudRepository<CountryEntity, Long> {

    Mono<CountryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CountryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<CountryEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<CountryEntity> findAllByDeletedAtIsNull();

    Flux<CountryEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Mono<CountryEntity> findByUuidAndStatusAndDeletedAtIsNull(UUID uuid, Boolean status);

    Mono<CountryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CountryEntity> findFirstByIso2IgnoreCaseAndDeletedAtIsNull(String iso2);

    Mono<CountryEntity> findFirstByIso3IgnoreCaseAndDeletedAtIsNull(String iso3);

    Mono<CountryEntity> findFirstByNumericCodeIgnoreCaseAndDeletedAtIsNull(Integer numericCode);

    Mono<CountryEntity> findFirstByPhoneCodeAndDeletedAtIsNull(String phoneCode);

    Mono<CountryEntity> findFirstByCurrencyUUIDAndDeletedAtIsNull(UUID currencyUUID);

    Mono<CountryEntity> findFirstByRegionUUIDAndDeletedAtIsNull(UUID regionUUID);

    Mono<CountryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<CountryEntity> findFirstByIso2IgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String iso2, UUID uuid);

    Mono<CountryEntity> findFirstByIso3IgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String iso3, UUID uuid);

    Mono<CountryEntity> findFirstByNumericCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(Integer numericCode, UUID uuid);

    Mono<CountryEntity> findFirstByPhoneCodeAndDeletedAtIsNullAndIdIsNot(String phoneCode, Long id);
}
