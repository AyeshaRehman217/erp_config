package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveStateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomStateRepository;

import java.util.UUID;

@Repository
public interface SlaveStateRepository extends ReactiveCrudRepository<SlaveStateEntity, Long>, SlaveCustomStateRepository {
    Mono<SlaveStateEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveStateEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveStateEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);


    /**
     * Fetch and Count Records Based on Name,Type and Description filter without Status Filter
     **/
    Flux<SlaveStateEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String type, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String type, String description);

    /**
     * Fetch and Count Records Based on Name,Type,Status and Description filter
     **/
    Flux<SlaveStateEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String type, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrTypeContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String type, Boolean status1, String description, Boolean status2);

    @Query("select count(*) from states " +
            "join countries  on states.country_uuid = countries.uuid " +
            "where states.deleted_at is null and countries.deleted_at is null " +
            "and (states.name ILIKE concat('%',:name,'%') " +
            " or states.type ILIKE concat('%',:type,'%')" +
            " or states.description ILIKE concat('%',:description,'%'))" +
            " and countries.uuid = :countryUUID")
    Mono<Long> countStateAgainstCountryEntity(UUID countryUUID, String name, String type, String description);

    @Query("select count(*) from states " +
            "join countries  on states.country_uuid = countries.uuid " +
            "where states.deleted_at is null and countries.deleted_at is null " +
            " and states.status=:status" +
            " and (states.name ILIKE concat('%',:name,'%') " +
            " or states.type ILIKE concat('%',:type,'%')" +
            " or states.description ILIKE concat('%',:description,'%'))" +
            " and countries.uuid = :countryUUID")
    Mono<Long> countStateAgainstCountryEntityWithStatus(UUID countryUUID, Boolean status, String name, String type, String description);


}
