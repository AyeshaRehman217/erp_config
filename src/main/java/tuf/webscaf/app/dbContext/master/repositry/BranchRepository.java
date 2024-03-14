package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.BranchEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveBranchEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface BranchRepository extends ReactiveCrudRepository<BranchEntity, Long> {
    //   @Query("select * from branches where deleted_at is null and id=:id")
//    Mono<BranchEntity> findById(Long id);
    Mono<BranchEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<BranchEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<BranchEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Mono<BranchEntity> findByBranchProfileUUIDAndDeletedAtIsNull(UUID branchProfileUUID);

    Mono<BranchEntity> findFirstByCompanyUUIDAndDeletedAtIsNull(UUID companyUUID);

    Mono<BranchEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<BranchEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<BranchEntity> findAllByIdInAndDeletedAtIsNull(List<Long> ids);

    @Query("SELECT branches.uuid AS branchUUID\n" +
            "FROM branches\n" +
            "WHERE branches.uuid IN (:uuidList)\n" +
            "AND branches.deleted_at IS NULL\n" +
            "AND branches.status = TRUE")
    Flux<UUID> getUUIDsOfExitingRecords(List<UUID> uuidList);
}
