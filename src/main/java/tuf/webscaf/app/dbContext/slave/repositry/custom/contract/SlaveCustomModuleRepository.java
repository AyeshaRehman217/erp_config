package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;

@Repository
public interface SlaveCustomModuleRepository {

    public Flux<SlaveModuleEntity> moduleIndex(String name, String description, Integer size, Long page, String dp, String d);
    public Flux<SlaveModuleEntity> moduleIndexCount(String name, String description, String dp, String d);

}
