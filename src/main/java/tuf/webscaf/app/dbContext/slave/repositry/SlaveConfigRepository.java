package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomConfigRepository;

import java.util.UUID;

@Repository
public interface SlaveConfigRepository extends ReactiveCrudRepository<SlaveConfigEntity, Long>, SlaveCustomConfigRepository {


    Mono<SlaveConfigEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveConfigEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    @Query("SELECT COUNT(*) FROM configs\n" +
            "join modules on modules.uuid = configs.module_uuid\n" +
            "WHERE configs.deleted_at IS NULL " +
            "AND modules.deleted_at is null " +
            "and (configs.key ILIKE concat('%',:key,'%') " +
            "or configs.value ILIKE concat('%',:value,'%')) " +
            "and modules.uuid =:moduleUUID")
    Mono<Long> countConfigAgainstModule(UUID moduleUUID,String key, String value);

    @Query("SELECT COUNT(*) FROM configs\n" +
            "join modules on modules.uuid = configs.module_uuid\n" +
            "WHERE configs.deleted_at IS NULL " +
            "AND modules.deleted_at is null " +
            "AND configs.status =:status " +
            "and (configs.key ILIKE concat('%',:key,'%') " +
            "or configs.value ILIKE concat('%',:value,'%')) " +
            "and modules.uuid =:moduleUUID")
    Mono<Long> countConfigWithStatusFilterAgainstModule(UUID moduleUUID,String key, String value, Boolean status);

    Mono<Long> countByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String key, String description);

    Mono<Long> countByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String key, Boolean status, String description, Boolean status1);

    Flux<SlaveConfigEntity> findAllByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String key, Boolean status, String description, Boolean status1);

    Flux<SlaveConfigEntity> findAllByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String key, String description);

}
