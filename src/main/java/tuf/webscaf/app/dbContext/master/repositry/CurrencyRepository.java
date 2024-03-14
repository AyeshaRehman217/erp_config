package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;

import java.util.UUID;


@Repository
public interface CurrencyRepository extends ReactiveCrudRepository<CurrencyEntity, Long> {

    Mono<CurrencyEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CurrencyEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CurrencyEntity> findFirstByCurrencyNameIgnoreCaseAndDeletedAtIsNull(String currencyName);

    Mono<CurrencyEntity> findFirstByCurrencyIgnoreCaseAndDeletedAtIsNull(String currency);

    Mono<CurrencyEntity> findFirstByIsoNumberAndDeletedAtIsNull(Integer isoNumber);

    Mono<CurrencyEntity> findFirstByCurrencyNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String currencyName, Long id);

    Mono<CurrencyEntity> findFirstByIsoNumberAndDeletedAtIsNullAndIdIsNot(Integer isoNumber, Long id);

    Mono<CurrencyEntity> findFirstByCurrencyIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String currency, Long id);

    Mono<CurrencyEntity> findFirstByCurrencyNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String currencyName, UUID uuid);

    Mono<CurrencyEntity> findFirstByCurrencyIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String currency, UUID uuid);

    Mono<CurrencyEntity>  findByUuidAndStatusAndDeletedAtIsNull(UUID uuid, Boolean status);

//    Mono<CurrencyEntity> findFirstByCountryIdAndDeletedAtIsNull(Long countryId);


}
