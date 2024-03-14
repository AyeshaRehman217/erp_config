package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTimezonePvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomTimezoneRepository;

import java.util.UUID;

@Repository
public interface SlaveTimezoneRepository extends ReactiveCrudRepository<SlaveTimezoneEntity, Long>, SlaveCustomCountryTimezonePvtRepository {

    Mono<SlaveTimezoneEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveTimezoneEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByZoneNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String zoneName, String description);

    Flux<SlaveTimezoneEntity> findAllByZoneNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String zoneName, String description);

    Flux<SlaveTimezoneEntity> findAllByZoneNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String zoneName, Boolean status, String description, Boolean status1);

    Mono<Long> countByZoneNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String zoneName, Boolean status, String description, Boolean status1);


    @Query("select count(*) from timezones " +
            "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
            "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
            "where timezones.deleted_at is null and countries.deleted_at is null " +
            "and (timezones.zone_name ILIKE concat('%',:zoneName,'%') " +
            "or timezones.description ILIKE concat('%',:description,'%'))" +
            "and country_timezone_pvt.deleted_at is null and countries.uuid = :countryUUID")
    Mono<Long> countMappedTimezoneAgainstCountry(UUID countryUUID, String zoneName, String description);

    @Query("select count(*) from timezones " +
            "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
            "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
            "where timezones.deleted_at is null and countries.deleted_at is null " +
            "and (timezones.zone_name ILIKE concat('%',:zoneName,'%') " +
            "or timezones.description ILIKE concat('%',:description,'%'))" +
            "and country_timezone_pvt.deleted_at is null " +
            " and timezones.status = :status" +
            " and countries.uuid = :countryUUID")
    Mono<Long> countMappedTimezoneAgainstCountryWithStatus(UUID countryUUID, Boolean status, String zoneName, String description);

    /**
     * Count Un Mapped Timezones against Country UUID with and without Status Filter
     **/
    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM timezones\n" +
            "WHERE timezones.uuid NOT IN(\n" +
            "SELECT timezones.uuid FROM timezones\n" +
            "LEFT JOIN country_timezone_pvt\n" +
            "ON country_timezone_pvt.timezone_uuid = timezones.uuid \n" +
            "WHERE country_timezone_pvt.country_uuid = :countryUUID\n" +
            "AND country_timezone_pvt.deleted_at IS NULL\n" +
            "AND timezones.deleted_at IS NULL )\n" +
            "AND timezones.deleted_at IS NULL " +
            "AND (timezones.zone_name ILIKE concat('%',:zoneName,'%') or timezones.description ILIKE concat('%',:description,'%')) \n")
    Mono<Long> countUnMappedTimezoneRecords(UUID countryUUID, String zoneName, String description);

    //query used in pvt mapping handler
    @Query("SELECT count(*) FROM timezones\n" +
            "WHERE timezones.uuid NOT IN(\n" +
            "SELECT timezones.uuid FROM timezones\n" +
            "LEFT JOIN country_timezone_pvt\n" +
            "ON country_timezone_pvt.timezone_uuid = timezones.uuid \n" +
            "WHERE country_timezone_pvt.country_uuid = :countryUUID\n" +
            "AND country_timezone_pvt.deleted_at IS NULL\n" +
            "AND timezones.deleted_at IS NULL )\n" +
            "AND timezones.deleted_at IS NULL " +
            "AND timezones.status = :status" +
            " AND (timezones.zone_name ILIKE concat('%',:zoneName,'%') " +
            "or timezones.description ILIKE concat('%',:description,'%')) \n")
    Mono<Long> countUnMappedTimezoneRecordsWithStatusFilter(UUID countryUUID, String zoneName, String description, Boolean status);

}
