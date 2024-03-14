package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLocationEntity;

import java.util.UUID;

@Repository
public interface SlaveLocationRepository extends ReactiveCrudRepository<SlaveLocationEntity, Long> {
    Mono<SlaveLocationEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveLocationEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String address, String description);

    Flux<SlaveLocationEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String address, String description);

    Flux<SlaveLocationEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status,  String address, Boolean status1, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrAddressContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String address, Boolean status1, String description, Boolean status2);


}


