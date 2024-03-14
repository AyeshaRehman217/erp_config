package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarCategoryEntity;

import java.util.List;
import java.util.UUID;


@Repository
public interface CalendarCategoryRepository extends ReactiveCrudRepository <CalendarCategoryEntity, Long>{

    Mono<CalendarCategoryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<CalendarCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
