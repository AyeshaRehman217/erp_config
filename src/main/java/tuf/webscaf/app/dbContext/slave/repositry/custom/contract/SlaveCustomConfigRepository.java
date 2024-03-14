package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;

import java.util.UUID;

/**
 * this Custom Repository will extends in Slave Config Repository
 **/
@Repository
public interface SlaveCustomConfigRepository {
    /**
     * Index list of Config with and without Status Filter against Module UUID
     **/

    Flux<SlaveConfigEntity> listOfConfigsAgainstModule(UUID moduleUUID, String key, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveConfigEntity> listOfConfigsWithStatusFilterAgainstModule(UUID moduleUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d);

}
