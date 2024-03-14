package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveSubAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomSubAdministrationDepartmentRepository;

import java.util.UUID;

@Repository
public interface SlaveSubAdministrationDepartmentRepository extends ReactiveCrudRepository<SlaveSubAdministrationDepartmentEntity, Long>, SlaveCustomSubAdministrationDepartmentRepository {

    Mono<SlaveSubAdministrationDepartmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveSubAdministrationDepartmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<Long> countByDeletedAtIsNullAndStatus(Boolean status);

    Flux<SlaveSubAdministrationDepartmentEntity> findAllByDeletedAtIsNullAndStatus(Pageable pageable, Boolean status);

    Flux<SlaveSubAdministrationDepartmentEntity> findAllByDeletedAtIsNull(Pageable pageable);

    @Query("select count(*) from sub_administration_departments " +
            "join administration_departments  on sub_administration_departments.administration_department_uuid = administration_departments.uuid " +
            "where sub_administration_departments.deleted_at is null" +
            " and administration_departments.deleted_at is null " +
            "and administration_departments.uuid = :companyUUID")
    Mono<Long> countSubAdministrationDepartmentAgainstAdministrationDepartmentEntity(UUID companyUUID);

    @Query("select count(*) from sub_administration_departments " +
            "join administration_departments  on sub_administration_departments.administration_department_uuid = administration_departments.uuid " +
            "where sub_administration_departments.deleted_at is null " +
            "and administration_departments.deleted_at is null " +
            " and sub_administration_departments.status = :status " +
            "and administration_departments.uuid = :companyUUID")
    Mono<Long> countSubAdministrationDepartmentAgainstAdministrationDepartmentEntityAndStatus(UUID companyUUID, Boolean status);

}
