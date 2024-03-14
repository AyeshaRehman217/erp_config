package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveStateEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;

import java.util.UUID;

/**
 * This Slave  Custom Repository will extends in State Repository
 **/
@Repository
public interface SlaveCustomStateRepository {
    /**
     * This Function  Fetch Records with pagination based on Country UUID
     **/
    Flux<SlaveStateEntity> indexStateAgainstCountry(UUID countryUUID, String name, String type, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveStateEntity> indexStateAgainstCountryWithStatus(UUID countryUUID,Boolean status, String name, String type, String description, Integer size, Long page, String dp, String d);
}
