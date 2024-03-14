package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends ReactiveCrudRepository<CompanyEntity, Long> {
//    @Query("select * from companies where deleted_at is null and id=:id")
//    Mono<CompanyEntity> findById(Long id);
    Mono<CompanyEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CompanyEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<CompanyEntity> findAllByIdInAndDeletedAtIsNull(List<Long> id);

    Flux<CompanyEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Mono<CompanyEntity> findByCompanyProfileUUIDAndDeletedAtIsNull(UUID companyProfileUUID);

    Mono<CompanyEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CompanyEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    @Query("SELECT companies.uuid AS companyUUID\n" +
            "FROM companies\n" +
            "WHERE companies.uuid IN (:uuidList)\n" +
            "AND companies.deleted_at IS NULL\n" +
            "AND companies.status = TRUE")
    Flux<UUID> getUUIDsOfExitingRecords(List<UUID> uuidList);
}
