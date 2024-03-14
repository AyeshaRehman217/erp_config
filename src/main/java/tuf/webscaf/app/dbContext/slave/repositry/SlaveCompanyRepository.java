package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCompanyEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCompanyWithCompanyProfileRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SlaveCompanyRepository extends ReactiveCrudRepository<SlaveCompanyEntity, Long>, SlaveCustomCompanyWithCompanyProfileRepository {
    Mono<SlaveCompanyEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveCompanyEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveCompanyEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNullAndUuidNotIn(String name, Boolean status, List<UUID> uuidList, String description, Boolean status2, List<UUID> uuidList2);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotInOrDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(String name, List<UUID> uuidList, String description, List<UUID> uuidList2);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(Pageable pageable, String name, String description, List<UUID> uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status1);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndIdNotIn(String name, List<Long> ids);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidNotIn(String name, String description, List<UUID> ids);

    Flux<SlaveCompanyEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(Pageable pageable, String name, String description, List<UUID> uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullAndDescriptionContainingIgnoreCaseAndDeletedAtIsNullAndUuidIn(String name, String description, List<UUID> uuids);

    Mono<SlaveCompanyEntity> findFirstByDocImageAndDeletedAtIsNull(UUID docImage);

}
