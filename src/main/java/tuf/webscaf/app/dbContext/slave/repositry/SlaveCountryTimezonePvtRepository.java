package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryTimezonePvtEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTimezonePvtRepository;

import java.util.UUID;

@Repository
public interface SlaveCountryTimezonePvtRepository extends ReactiveCrudRepository<SlaveCountryTimezonePvtEntity, Long> {
    Mono<SlaveCountryTimezonePvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
