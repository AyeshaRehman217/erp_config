package tuf.webscaf.app.dbContext.master.repositry;


import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;
import tuf.webscaf.app.dbContext.master.entity.ModuleEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;

import java.util.UUID;

@Repository
public interface ModuleRepository extends ReactiveCrudRepository<ModuleEntity, Long> {

    Mono<ModuleEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<ModuleEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<ModuleEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNull(String  slug);

    Mono<ModuleEntity> findFirstBySlugIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String slug, UUID uuid);

    Mono<ModuleEntity> findBySlugAndDeletedAtIsNull(String slug);

    Mono<ModuleEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<ModuleEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

}
