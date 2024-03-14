package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarDateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCalendarDateRepository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface SlaveCalendarDateRepository extends ReactiveCrudRepository<SlaveCalendarDateEntity, Long>, SlaveCustomCalendarDateRepository {

    Mono<SlaveCalendarDateEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<SlaveCalendarDateEntity> findByIdAndDeletedAtIsNull(Long id);

    @Query("SELECT string_agg(uuid::text, ',') " +
            "as calendarDateUUID FROM calendar_dates " +
            "WHERE calendar_dates.deleted_at IS NULL " +
            "AND calendar_dates.date BETWEEN :startDate AND :endDate")
    Mono<String> getAllDatesBetween(LocalDate startDate, LocalDate endDate);

//    @Query("SELECT * FROM dates \n" +
//            "WHERE dates.date = :localCalendarTime")
//    Flux<SlaveCalendarDateEntity> findAllByCalendarAndDeletedAtIsNull(Pageable pageable, LocalCalendar localCalendarTime);

//    Mono<Long> countAllByCalendarAndDeletedAtIsNull(LocalCalendar localCalendarTime);

//    Flux<SlaveCalendarDateEntity> findAllByDeletedAtIsNull(Pageable pageable, LocalCalendar localCalendarTime);

    Flux<SlaveCalendarDateEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Flux<SlaveCalendarDateEntity> findAllByYearAndDeletedAtIsNull(Pageable pageable, Integer year);

    Mono<Long> countAllByYearAndDeletedAtIsNull(Integer year);

    @Query("SELECT COUNT(*) FROM calendar_dates\n" +
            "WHERE calendar_dates.deleted_at IS NULL\n" +
            "AND (calendar_dates.date::text ILIKE concat('%',:date,'%')\n" +
            "OR calendar_dates.month_name ILIKE concat('%',:monthName,'%')\n" +
            "OR calendar_dates.day_name ILIKE concat('%',:dayName,'%')\n" +
            "OR calendar_dates.year::text ILIKE concat('%',:year,'%'))")
    Mono<Long> countAllByDeletedAtIsNull(String date, String monthName, String dayName, String year);
}
