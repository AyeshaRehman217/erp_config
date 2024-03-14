package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryRepository;

import java.util.UUID;


@Repository
public interface SlaveCountryRepository extends ReactiveCrudRepository<SlaveCountryEntity, Long>, SlaveCustomCountryRepository {
    Mono<SlaveCountryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCountryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Flux<SlaveCountryEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveCountryEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);


    /**
     * Count Mapped Countries Against Timezone UUID With and Without Status Filter
     **/
    @Query("select count(*) from countries " +
            "join country_timezone_pvt on countries.uuid = country_timezone_pvt.country_uuid " +
            "join timezones on country_timezone_pvt.timezone_uuid = timezones.uuid " +
            "where timezones.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_timezone_pvt.deleted_at is null " +
            "and timezones.uuid = :timezoneUUID" +
            " and (countries.name ILIKE concat('%',:name,'%') or countries.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCountryAgainstTimezone(String name, String description, UUID timezoneUUID);

    @Query("select count(*) from countries " +
            "join country_timezone_pvt on countries.uuid = country_timezone_pvt.country_uuid " +
            "join timezones on country_timezone_pvt.timezone_uuid = timezones.uuid " +
            "where timezones.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_timezone_pvt.deleted_at is null " +
            "and timezones.uuid = :timezoneUUID" +
            " and countries.status = :status" +
            " and (countries.name ILIKE concat('%',:name,'%') or countries.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCountryAgainstTimezoneWithStatusFilter(String name, String description, UUID timezoneUUID, Boolean status);

    /**
     * Count Mapped Country Records based on Translation UUID
     **/
    @Query("select count(*) from countries " +
            "join country_translation_pvt on countries.uuid = country_translation_pvt.country_uuid " +
            "join translations on country_translation_pvt.translation_uuid = translations.uuid " +
            "where translations.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_translation_pvt.deleted_at is null " +
            "and translations.uuid =:translationUUID" +
            " and (countries.name ILIKE concat('%',:name,'%') or countries.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCountryAgainstTranslation(UUID translationUUID, String name, String description);

    @Query("select count(*) from countries " +
            "join country_translation_pvt on countries.uuid = country_translation_pvt.country_uuid " +
            "join translations on country_translation_pvt.translation_uuid = translations.uuid " +
            "where translations.deleted_at is null " +
            "and countries.deleted_at is null " +
            "and country_translation_pvt.deleted_at is null " +
            "and translations.uuid =:translationUUID" +
            " and countries.status =:status" +
            " and (countries.name ILIKE concat('%',:name,'%') or countries.description ILIKE concat('%',:description,'%'))")
    Mono<Long> countCountryAgainstTranslationWithStatusFilter(UUID translationUUID, String name, String description, Boolean status);

}
