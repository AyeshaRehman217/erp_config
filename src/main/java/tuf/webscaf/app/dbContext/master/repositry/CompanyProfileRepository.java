package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchProfileEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyProfileEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyProfileEntity;

import java.util.UUID;

@Repository
public interface CompanyProfileRepository extends ReactiveCrudRepository<CompanyProfileEntity, Long> {

    Mono<CompanyProfileEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CompanyProfileEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CompanyProfileEntity> findFirstByLanguageUUIDAndDeletedAtIsNull(UUID languageId);

    Mono<CompanyProfileEntity> findFirstByLocationUUIDAndDeletedAtIsNull(UUID locationUUID);

    Mono<CompanyProfileEntity> findFirstByCurrencyUUIDAndDeletedAtIsNull(UUID currencyUUID);

    Mono<CompanyProfileEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<CompanyProfileEntity> findFirstByCityUUIDAndDeletedAtIsNull(UUID cityUUID);

    Mono<CompanyProfileEntity> findFirstByStateUUIDAndDeletedAtIsNull(UUID id);


}
