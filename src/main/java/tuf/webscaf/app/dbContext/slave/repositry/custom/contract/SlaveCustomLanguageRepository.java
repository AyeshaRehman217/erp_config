package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;

@Repository
public interface SlaveCustomLanguageRepository {

    public Flux<SlaveLanguageEntity> languageIndex(String name, String description, Integer size, Long page, String dp, String d);
    public Flux<SlaveLanguageEntity> languageIndexCount(String name, String description, String dp, String d);

}
