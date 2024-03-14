package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.*;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomLanguageRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveLanguageRepository extends ReactiveCrudRepository<SlaveLanguageEntity, Long>, SlaveCustomLanguageRepository {

    Mono<SlaveLanguageEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveLanguageEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveLanguageEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Mono<SlaveLanguageEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndIdIsNot(String name, Long id);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    Flux<SlaveLanguageEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> languageList);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveLanguageEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    @Query("SELECT languages.uuid AS languageUUID\n" +
            "FROM languages\n" +
            "WHERE languages.uuid IN (:uuidList)\n" +
            "AND languages.deleted_at IS NULL\n" +
            "AND languages.status = TRUE")
    Flux<UUID> getUUIDsOfExitingRecords(List<UUID> uuidList);
}
