package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;


import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveCityRepository extends ReactiveCrudRepository<SlaveCityEntity, Long> {
    Mono<SlaveCityEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCityEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCityEntity> findFirstByCountryUUIDAndDeletedAtIsNull(UUID countryUUID);

    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    //Fetch Records Against Country and State and Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID countryUUID1, UUID stateUUID1, Boolean status1, String description, UUID countryUUID2, UUID stateUUID2, Boolean status2);
    //Count Records Against Country and State and Status
    Mono<Long> countByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndStatusAndDeletedAtIsNull(String name, UUID countryUUID1, UUID stateUUID1, Boolean status1, String description, UUID countryUUID2, UUID stateUUID2, Boolean status2);

    //Fetch and Count Records Against Country and State Without Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID countryUUID1, UUID stateUUID1, String description, UUID countryUUID2, UUID stateUUID2);
    Mono<Long> countByNameContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStateUUIDAndDeletedAtIsNull(String name, UUID countryUUID1, UUID stateUUID1, String description, UUID countryUUID2, UUID stateUUID2);

    //Fetch Records Against Country and Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID stateUUID1, Boolean status1, String description, UUID stateUUID2, Boolean status2);
    //Count Records Against Country and Status
    Mono<Long> countByNameContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndStatusAndDeletedAtIsNull(String name, UUID stateUUID1, Boolean status1, String description, UUID stateUUID2, Boolean status2);

    //Fetch and Count Records Against Country Without Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID countryUUID1, String description, UUID countryUUID2);
    Mono<Long> countByNameContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndCountryUUIDAndDeletedAtIsNull(String name, UUID countryUUID1, String description, UUID countryUUID2);

    //Fetch Records Against Country and Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID stateUUID1, Boolean status1, String description, UUID stateUUID2, Boolean status2);
    //Count Records Against Country and State and Status
    Mono<Long> countByNameContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndStatusAndDeletedAtIsNull(String name, UUID stateUUID1, Boolean status1, String description, UUID stateUUID2, Boolean status2);

    //Fetch and Count Records Against State Without Status
    Flux<SlaveCityEntity> findAllByNameContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID stateUUID1, String description, UUID stateUUID2);
    Mono<Long> countByNameContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStateUUIDAndDeletedAtIsNull(String name, UUID stateUUID1, String description, UUID stateUUID2);

//    @Query("select count(*) from cities " +
//            "join states  on cities.state_uuid = states.uuid " +
//            "where cities.deleted_at is null and states.deleted_at is null " +
//            "and (cities.name ILIKE concat('%',:name,'%') " +
//            "or cities.description ILIKE concat('%',:description,'%'))" +
//            "and states.uuid = :stateUUID")
//    Mono<Long> countCityAgainstStateEntity(UUID stateUUID, String name, String description);
//
//    @Query("select count(*) from cities " +
//            "join states  on cities.state_uuid = states.uuid " +
//            "where cities.deleted_at is null and states.deleted_at is null " +
//            " and cities.status = :status " +
//            " and (cities.name ILIKE concat('%',:name,'%') " +
//            "or cities.description ILIKE concat('%',:description,'%'))" +
//            "and states.uuid = :stateUUID")
//    Mono<Long> countCityAgainstStateEntityAndStatus(UUID stateUUID, Boolean status, String name, String description);
//
//    /**
//     * Count Cities with State Country and Status Filter
//     **/
//    @Query("select count(*) from cities " +
//            "join states  on cities.state_uuid = states.uuid " +
//            "join countries on states.country_uuid=countries.uuid " +
//            " where cities.deleted_at is null and states.deleted_at is null " +
//            "and countries.deleted_at is null " +
//            " and cities.status = :status " +
//            " and (cities.name ILIKE concat('%',:name,'%') " +
//            "or cities.description ILIKE concat('%',:description,'%'))" +
//            " and states.uuid = :stateUUID" +
//            " and countries.uuid =:countryUUID ")
//    Mono<Long> countCityAgainstStateAndCountryWithStatus(UUID countryUUID, UUID stateUUID, Boolean status, String name, String description);
//
//    /**
//     * Count Cities with State Country Filter
//     **/
//    @Query("select count(*) from cities " +
//            "join states  on cities.state_uuid = states.uuid " +
//            "join countries on states.country_uuid=countries.uuid " +
//            " where cities.deleted_at is null and states.deleted_at is null " +
//            "and countries.deleted_at is null " +
//            " and (cities.name ILIKE concat('%',:name,'%') " +
//            "or cities.description ILIKE concat('%',:description,'%'))" +
//            " and states.uuid = :stateUUID" +
//            " and countries.uuid =:countryUUID ")
//    Mono<Long> countCityAgainstStateAndCountryWithOutStatus(UUID countryUUID, UUID stateUUID, String name, String description);
}
