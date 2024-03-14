package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTranslationPvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomTranslationRepository;

import java.util.UUID;

@Repository
public interface SlaveTranslationRepository extends ReactiveCrudRepository<SlaveTranslationEntity, Long>, SlaveCustomCountryTranslationPvtRepository {

    Mono<SlaveTranslationEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveTranslationEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String key, String description);

    Flux<SlaveTranslationEntity> findAllByKeyContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String key, String description);

    Flux<SlaveTranslationEntity> findAllByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String key, Boolean status, String description, Boolean status1);

    Mono<Long> countByKeyContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String key, Boolean status, String description, Boolean status1);

    /**
     * Count Mapped Translations against Country UUID With and Without Status Filter
     **/
    @Query("select count(*) from translations " +
            "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
            "join countries on country_translation_pvt.country_uuid = countries.uuid " +
            "where translations.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_translation_pvt.deleted_at is null " +
            " and (translations.key ILIKE concat('%',:key,'%') " +
            "or translations.description ILIKE concat('%',:description,'%'))" +
            "and country_translation_pvt.deleted_at is null and countries.uuid = :countryUUID")
    Mono<Long> countMappedTranslationEntity(UUID countryUUID, String key, String description);

    @Query("select count(*) from translations " +
            "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
            "join countries on country_translation_pvt.country_uuid = countries.uuid " +
            "where translations.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_translation_pvt.deleted_at is null " +
            " and (translations.key ILIKE concat('%',:key,'%') " +
            "or translations.description ILIKE concat('%',:description,'%'))" +
            "and country_translation_pvt.deleted_at is null " +
            "and countries.uuid = :countryUUID and translations.status=:status")
    Mono<Long> countMappedTranslationEntityWithStatusFilter(UUID countryUUID, String key, String description, Boolean status);


    /**
     * Show Translations Records Against Country UUID that are not mapped yet
     **/
    @Query("SELECT count(*) FROM translations\n" +
            "WHERE translations.uuid NOT IN(\n" +
            "SELECT translations.uuid FROM translations\n" +
            "LEFT JOIN country_translation_pvt\n" +
            "ON country_translation_pvt.translation_uuid = translations.uuid \n" +
            "WHERE country_translation_pvt.country_uuid = :countryUUID\n" +
            "AND country_translation_pvt.deleted_at IS NULL\n" +
            "AND translations.deleted_at IS NULL )\n" +
            "AND translations.deleted_at IS NULL " +
            "AND translations.key ILIKE concat('%',:key,'%') \n")
    Mono<Long> countUnMappedTranslationRecordsAgainstCountry(UUID countryUUID, String key);

    /**
     * Show Translations Records Against Country UUID that are not mapped yet with Status Filter
     **/
    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM translations\n" +
            "WHERE translations.uuid NOT IN(\n" +
            "SELECT translations.uuid FROM translations\n" +
            "LEFT JOIN country_translation_pvt\n" +
            "ON country_translation_pvt.translation_uuid = translations.uuid \n" +
            "WHERE country_translation_pvt.country_uuid = :countryUUID\n" +
            "AND country_translation_pvt.deleted_at IS NULL\n" +
            "AND translations.deleted_at IS NULL )\n" +
            "AND translations.deleted_at IS NULL " +
            "AND translations.status= :status" +
            " AND translations.key ILIKE concat('%',:key,'%') \n")
    Mono<Long> countUnMappedTranslationRecordsWithStatusFilter(UUID countryUUID, String key, Boolean status);


}
