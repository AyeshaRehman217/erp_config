package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.master.entity.BranchEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdministrationDepartmentRepository extends ReactiveCrudRepository<AdministrationDepartmentEntity, Long> {
    Mono<AdministrationDepartmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AdministrationDepartmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AdministrationDepartmentEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(String name, UUID companyUUID);

    Mono<AdministrationDepartmentEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(String name, UUID companyUUID, UUID uuid);

    Mono<AdministrationDepartmentEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AdministrationDepartmentEntity> findFirstByCodeIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Mono<AdministrationDepartmentEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(String slug, UUID companyUUID);

    Mono<AdministrationDepartmentEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(String slug, UUID companyUUID, UUID uuid);

    Mono<AdministrationDepartmentEntity> findFirstByShortNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUID(String shortName, UUID companyUUID);

    Mono<AdministrationDepartmentEntity> findFirstByShortNameIgnoreCaseAndDeletedAtIsNullAndCompanyUUIDAndUuidIsNot(String shortName, UUID companyUUID, UUID uuid);

    Flux<AdministrationDepartmentEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    Mono<AdministrationDepartmentEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

}
