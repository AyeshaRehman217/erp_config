package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchProfileEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyProfileEntity;

import java.util.UUID;

@Repository
public interface BranchProfileRepository extends ReactiveCrudRepository<BranchProfileEntity, Long> {
    Mono<BranchProfileEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<BranchProfileEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

//    Mono<BranchProfileEntity> findFirstByCityUUIDAndDeletedAtIsNull(UUID cityUUID);
//
//    Mono<BranchProfileEntity> findFirstByStateUUIDAndDeletedAtIsNull(UUID stateUUID);
//
//    Mono<BranchProfileEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Mono<BranchProfileEntity> findFirstByLanguageUUIDAndDeletedAtIsNull(UUID languageId);

    Mono<BranchProfileEntity> findFirstByLocationUUIDAndDeletedAtIsNull(UUID locationUUID);
}
