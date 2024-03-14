package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAdministrationDepartmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAdministrationDepartmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchAdministrationDepartmentPvtRepository;

import java.util.UUID;

@Repository
public interface SlaveAdministrationDepartmentRepository extends ReactiveCrudRepository<SlaveAdministrationDepartmentEntity, Long>, SlaveCustomAdministrationDepartmentRepository
        , SlaveCustomBranchAdministrationDepartmentPvtRepository {

    Mono<SlaveAdministrationDepartmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAdministrationDepartmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    Flux<SlaveAdministrationDepartmentEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Flux<SlaveAdministrationDepartmentEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    @Query("select count(*) from administration_departments " +
            "join companies  on administration_departments.company_uuid = companies.uuid " +
            "where administration_departments.deleted_at is null and companies.deleted_at is null " +
            "and (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%'))" +
            "and companies.uuid = :companyUUID")
    Mono<Long> countAdministrationDepartmentAgainstCompanyEntity(UUID companyUUID, String name, String description);

    @Query("select count(*) from administration_departments " +
            "join companies  on administration_departments.company_uuid = companies.uuid " +
            "where administration_departments.deleted_at is null and companies.deleted_at is null " +
            " and administration_departments.status = :status " +
            " and (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%'))" +
            "and companies.uuid = :companyUUID")
    Mono<Long> countAdministrationDepartmentAgainstCompanyEntityAndStatus(UUID companyUUID, Boolean status, String name, String description);


    /**
     * Count AdministrationDepartment's that are un-mapped for Given Branch UUID without Status Filter
     **/
    @Query("SELECT count(*) FROM administration_departments\n" +
            "WHERE administration_departments.uuid NOT IN(\n" +
            "SELECT administration_departments.uuid FROM administration_departments\n" +
            "LEFT JOIN branch_administration_department_pvt\n" +
            "ON branch_administration_department_pvt.administration_department_uuid = administration_departments.uuid \n" +
            "WHERE branch_administration_department_pvt.branch_uuid = :branchUUID\n" +
            "AND branch_administration_department_pvt.deleted_at IS NULL\n" +
            "AND administration_departments.deleted_at IS NULL )\n" +
            "AND administration_departments.deleted_at IS NULL " +
            "AND (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%') ) \n")
    Mono<Long> countUnMappedBranchAdministrationDepartmentRecords(UUID branchUUID, String name, String description);

    /**
     * Count AdministrationDepartment's that are un-mapped for Given Branch UUID with Status Filter
     **/
    @Query("SELECT count(*) FROM administration_departments\n" +
            "WHERE administration_departments.uuid NOT IN(\n" +
            "SELECT administration_departments.uuid FROM administration_departments\n" +
            "LEFT JOIN branch_administration_department_pvt\n" +
            "ON branch_administration_department_pvt.administration_department_uuid = administration_departments.uuid \n" +
            "WHERE branch_administration_department_pvt.branch_uuid = :branchUUID\n" +
            "AND branch_administration_department_pvt.deleted_at IS NULL\n" +
            "AND administration_departments.deleted_at IS NULL )\n" +
            "AND administration_departments.deleted_at IS NULL " +
            "AND administration_departments.status = :status \n" +
            "AND (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%') ) \n")
    Mono<Long> countUnMappedBranchAdministrationDepartmentRecordsWithStatus(UUID branchUUID, String name, String description, Boolean status);


    /**
     * Count AdministrationDepartment that are mapped for Given Branch UUID without Status Filter
     **/
    @Query("select count(*) from administration_departments\n" +
            "join branch_administration_department_pvt\n" +
            "on administration_departments.uuid = branch_administration_department_pvt.administration_department_uuid\n" +
            "where branch_administration_department_pvt.branch_uuid = :branchUUID\n" +
            "and administration_departments.deleted_at is null\n" +
            "and branch_administration_department_pvt.deleted_at is null\n" +
            "AND (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%') ) \n")
    Mono<Long> countMappedBranchAdministrationDepartment(UUID branchUUID, String name, String description);

    /**
     * Count AdministrationDepartment that are mapped for Given Branch UUID with Status Filter
     **/
    @Query("select count(*) from administration_departments\n" +
            "join branch_administration_department_pvt\n" +
            "on administration_departments.uuid = branch_administration_department_pvt.administration_department_uuid\n" +
            "where branch_administration_department_pvt.branch_uuid = :branchUUID\n" +
            "and administration_departments.deleted_at is null\n" +
            "and administration_departments.status = :status " +
            "and branch_administration_department_pvt.deleted_at is null\n" +
            "AND (administration_departments.name ILIKE concat('%',:name,'%') " +
            "or administration_departments.description ILIKE concat('%',:description,'%') ) \n")
    Mono<Long> countMappedBranchAdministrationDepartmentWithStatus(UUID branchUUID, String name, String description, Boolean status);

}
