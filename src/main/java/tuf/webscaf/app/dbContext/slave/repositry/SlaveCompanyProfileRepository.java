package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CompanyProfileEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyProfileEntity;

import java.util.UUID;

@Repository
public interface SlaveCompanyProfileRepository extends ReactiveCrudRepository<SlaveCompanyProfileEntity, Long> {
    Mono<SlaveCompanyProfileEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCompanyProfileEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCompanyProfileEntity> findFirstByLanguageUUIDAndDeletedAtIsNull(UUID languageUUID);

    Mono<SlaveCompanyProfileEntity> findFirstByCurrencyUUIDAndDeletedAtIsNull(UUID currencyUUID);

}
