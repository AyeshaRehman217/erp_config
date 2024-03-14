package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarEntity;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SlaveCalendarRepository extends ReactiveCrudRepository<SlaveCalendarEntity, Long> {

    Mono<SlaveCalendarEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCalendarEntity> findByNameAndDeletedAtIsNull(String name);

    Mono<SlaveCalendarEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveCalendarEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable,String name);

    Mono<Long> countAllByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

//    Flux<SlaveCalendarEntity> findAllByDateAndDeletedAtIsNullOrNameContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, LocalDate localDateTime,String name);

    Flux<SlaveCalendarEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<Long> countAllByDeletedAtIsNull();
}
