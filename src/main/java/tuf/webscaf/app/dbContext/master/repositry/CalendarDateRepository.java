package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CalendarDateEntity;

import java.sql.Timestamp;
import java.util.UUID;


@Repository
public interface CalendarDateRepository extends ReactiveCrudRepository <CalendarDateEntity, Long>{

    Mono<CalendarDateEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<CalendarDateEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<CalendarDateEntity> findFirstByCalendarUUIDAndDeletedAtIsNull(UUID uuid);

}
