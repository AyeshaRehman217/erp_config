package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarEntity;

import java.time.LocalDate;
import java.util.UUID;


@Repository
public interface CalendarRepository extends ReactiveCrudRepository <CalendarEntity, Long>{

    Mono<CalendarEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarEntity> findFirstByDateAndDeletedAtIsNull(LocalDate date);

    Mono<CalendarEntity> findFirstByCalendarCategoryUUIDAndDeletedAtIsNull(UUID calendarCategoryUUID);

    Mono<CalendarEntity> findFirstByDateAndDeletedAtIsNullAndUuidIsNot(LocalDate date, UUID uuid);

}
