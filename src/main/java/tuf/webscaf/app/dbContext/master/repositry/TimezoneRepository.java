package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.TimezoneEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TimezoneRepository extends ReactiveCrudRepository<TimezoneEntity, Long> {

    Mono<TimezoneEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<TimezoneEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<TimezoneEntity> findFirstByZoneNameIgnoreCaseAndDeletedAtIsNull(String zoneName);

    Mono<TimezoneEntity> findFirstByZoneNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String zoneName, UUID uuid);

    Flux<TimezoneEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuid);

    Flux<TimezoneEntity> findAllByDeletedAtIsNull();

}
