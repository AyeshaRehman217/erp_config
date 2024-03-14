package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.ConfigEntity;

import java.util.UUID;

@Repository
public interface ConfigRepository extends ReactiveCrudRepository<ConfigEntity, Long> {
    //    @Override
//    @Query("select * from configs where deleted_at is null and id=:id")
//    Mono<ConfigEntity> findById(Long id);

    Mono<ConfigEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<ConfigEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<ConfigEntity> findFirstByModuleUUIDAndDeletedAtIsNull(UUID moduleUUID);

    Mono<ConfigEntity> findFirstByKeyIgnoreCaseAndDeletedAtIsNull(String key);

    Mono<ConfigEntity> findFirstByKeyIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String key,UUID uuid);
}
