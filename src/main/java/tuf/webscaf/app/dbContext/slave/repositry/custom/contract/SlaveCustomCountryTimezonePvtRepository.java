package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;

import java.util.UUID;

/**
 * This Custom Repository extends in Slave Country-Timezone Pvt Repository
 **/
@Repository
public interface SlaveCustomCountryTimezonePvtRepository {

    Flux<SlaveTimezoneEntity> showUnMappedTimezoneListAgainstCountry(UUID countryUUID, String zoneName, String dp, String d, Integer size, Long page);

    Flux<SlaveTimezoneEntity> showUnMappedTimezoneListAgainstCountryWithStatus(UUID countryUUID, String zoneName, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveTimezoneEntity> showMappedTimezoneListAgainstCountry(UUID countryUUID, String zoneName, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveTimezoneEntity> showMappedTimezoneListAgainstCountryWithStatus(UUID countryUUID, String zoneName, String description, Boolean status, Integer size, Long page, String dp, String d);



}

