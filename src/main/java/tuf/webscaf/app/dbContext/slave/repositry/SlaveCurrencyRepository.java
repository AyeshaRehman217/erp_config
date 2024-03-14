package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCurrencyEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCurrencyRepository;

import java.util.UUID;


@Repository
public interface SlaveCurrencyRepository extends ReactiveCrudRepository<SlaveCurrencyEntity, Long>, SlaveCustomCurrencyRepository {

    Mono<SlaveCurrencyEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCurrencyEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByCurrencyNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String currencyName, String description);

    Mono<Long> countByCurrencyNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
            (String currencyName, Boolean status, String description, Boolean status1);

    Flux<SlaveCurrencyEntity> findAllByCurrencyNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String currencyName, String description);

    Flux<SlaveCurrencyEntity> findAllByCurrencyNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull
            (Pageable pageable, String currencyName, Boolean status, String description, Boolean status1);
}
