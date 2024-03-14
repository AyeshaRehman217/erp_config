package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarCategoryEntity;

import java.util.UUID;

@Repository
public interface SlaveCalendarCategoryRepository extends ReactiveCrudRepository<SlaveCalendarCategoryEntity, Long> {

    Mono<SlaveCalendarCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCalendarCategoryEntity> findByNameAndDeletedAtIsNull(String name);

    Flux<SlaveCalendarCategoryEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description, Pageable pageable);

    Flux<SlaveCalendarCategoryEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2, Pageable pageable);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);

}
