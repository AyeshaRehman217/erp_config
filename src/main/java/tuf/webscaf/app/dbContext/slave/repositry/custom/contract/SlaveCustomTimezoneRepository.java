package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;

import java.util.UUID;

/**
 * This Custom Repository Extends in Slave Timezone Repository
 **/
@Repository
public interface SlaveCustomTimezoneRepository {
    /**
     * This Function Show Mapped Timezones against Country UUID with and without status filter
     **/
//    Flux<SlaveTimezoneEntity> listOfTimezonesAgainstCountry(UUID countryUUID, String zoneName, String description, Integer size, Long page, String dp, String d);
//
//    Flux<SlaveTimezoneEntity> listOfTimezonesAgainstCountryWithStatusFilter(UUID countryUUID, String zoneName, String description, Boolean status, Integer size, Long page, String dp, String d);


}
