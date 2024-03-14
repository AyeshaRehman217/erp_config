package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchWithBranchProfileRepository;

import java.util.List;
import java.util.UUID;


@Repository
public interface SlaveBranchRepository extends ReactiveCrudRepository<SlaveBranchEntity, Long>, SlaveCustomBranchWithBranchProfileRepository {

    Mono<SlaveBranchEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveBranchEntity> findAllByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndIdNotIn(Pageable pageable, String name, List<Long> ids);

    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, UUID companyUUID, List<UUID> uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndIdNotIn(String name, List<Long> ids);

    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndIdIn(Pageable pageable, String name, List<Long> ids);

    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, String description, List<UUID> ids);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(String name, String description, List<UUID> uuid);

    //Fetch Un Mapped Records Against Branch and Company
    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, String description, List<UUID> ids);

    //Count Un Mapped Records Against Branch and Company
    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(String name, String description, List<UUID> uuid);

    //Fetch Un Mapped Records Against Branch and Company
    Flux<SlaveBranchEntity> findAllByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, UUID companyUUID1, String description, UUID companyUUID2, List<UUID> ids);

    //Count Un Mapped Records Against Branch and Company
    Mono<Long> countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndUuidNotIn(String name, UUID companyUUID1, String description, UUID companyUUID2, List<UUID> uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndUuidNotIn(String name, UUID companyUUID, List<UUID> uuid);

//    Mono<Long> countByNameContainingIgnoreCaseAndCompanyUUIDAndDeletedAtIsNullAndIdIn(String name, UUID companyUUID, List<UUID> uuid);

    Mono<SlaveBranchEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<Long> countByDeletedAtIsNullAndNameContaining(String name);

//    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<SlaveBranchEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Flux<SlaveBranchEntity> findAllByCompanyUUIDInAndDeletedAtIsNull(List<UUID> companyUUID);

    Mono<SlaveBranchEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<SlaveBranchEntity> findFirstByCompanyUUIDAndUuidAndDeletedAtIsNull(UUID companyUUID,UUID branchUUID);


    Mono<SlaveBranchEntity> findByBranchProfileUUIDAndDeletedAtIsNull(UUID branchProfileUUID);

    Mono<SlaveBranchEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    @Query("select  count(*)\n" +
            "from branches\n" +
            "join companies on companies.uuid=branches.company_uuid \n" +
            "join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
            "where branch_profiles.deleted_at is null \n" +
            "and branches.deleted_at is null \n" +
            "and companies.deleted_at is null \n" +
            "and (branches.name ILIKE concat('%',:name,'%') " +
            "or branches.description ILIKE concat('%',:description,'%'))" +
            "and companies.uuid = :companyUUID")
    Mono<Long> countBranchAgainstCompany(String name, String description, UUID companyUUID);

    @Query("select  count(*)\n" +
            "from branches\n" +
            "join companies on companies.uuid=branches.company_uuid \n" +
            "join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
            "where branch_profiles.deleted_at is null \n" +
            "and branches.deleted_at is null \n" +
            "and companies.deleted_at is null \n" +
            " and branches.status = :status " +
            "and (branches.name ILIKE concat('%',:name,'%') " +
            "or branches.description ILIKE concat('%',:description,'%'))" +
            "and companies.uuid = :companyUUID")
    Mono<Long> countBranchAgainstCompanyAndStatus(Boolean status, String name, String description, UUID companyUUID);

    /**
     * Count Branch Records Based on Company UUID
     **/
    @Query("select count(*) from branches " +
            "join companies  on branches.company_uuid = companies.uuid " +
            "where branches.deleted_at is null and companies.deleted_at is null " +
            "and (branches.name ILIKE concat('%',:name,'%') " +
            "or branches.description ILIKE concat('%',:description,'%'))" +
            "and companies.uuid = :companyUUID")
    Mono<Long> countBranchAgainstCompanyUUID(UUID companyUUID, String name, String description);

    @Query("select  count(*) from branches " +
            "  join companies on companies.uuid=branches.company_uuid " +
            "  join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid " +
            "  where branch_profiles.deleted_at is null " +
            "  and branches.deleted_at is null " +
            "  and companies.deleted_at is null " +
            " and branches.status = :status " +
            "and (branches.name ILIKE concat('%',:name,'%') " +
            "or branches.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countBranchWithStatusFilter(String name, String description, Boolean status);

    @Query("select  count(*) from branches " +
            "  join companies on companies.uuid=branches.company_uuid " +
            "  join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid " +
            "  where branch_profiles.deleted_at is null " +
            "  and branches.deleted_at is null " +
            "  and companies.deleted_at is null " +
            "and (branches.name ILIKE concat('%',:name,'%') " +
            "or branches.description ILIKE concat('%',:description,'%') )")
    Mono<Long> countBranchWithOutStatusFilter(String name, String description);

    /**
     * This Function is used to Count Mapped Branches Against Company And Voucher UUID
     **/
    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and companies.uuid = :companyUUID \n" +
            " and branches.uuid IN (:branchUUID)" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countBranchAgainstCompanyAndVoucher(UUID companyUUID, List<UUID> branchUUID, String name, String description);

    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and branches.uuid IN (:branchUUID)" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countBranchAgainstVoucher(List<UUID> branchUUID, String name, String description);

    /**
     * This Function is used to Count Un Mapped Branches Against Company And Voucher UUID
     **/
    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and companies.uuid = :companyUUID \n" +
            " and branches.uuid NOT IN (:branchUUID)" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedVoucherListAgainstBranchAndCompany(UUID companyUUID, List<UUID> branchUUID, String name, String description);

    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and companies.uuid = :companyUUID \n" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedVoucherListAgainstCompany(UUID companyUUID, String name, String description);

    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and branches.uuid NOT IN (:branchUUID)" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedVoucherListAgainstBranch(List<UUID> branchUUID, String name, String description);

    @Query("select  count(*) from branches \n" +
            "left join companies on companies.uuid = branches.company_uuid " +
            "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
            " where branch_profiles.deleted_at is null \n" +
            " and companies.deleted_at is null \n" +
            " and branches.deleted_at is null \n" +
            " and (branches.name ILIKE concat('%',:name,'%') " +
            " or branches.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countUnMappedVoucherList(String name, String description);
}
