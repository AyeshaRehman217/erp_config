package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;

import java.util.UUID;

/**
 * This Custom Repository extends in Slave Country-Translation Pvt Repository
 **/
@Repository
public interface SlaveCustomCountryTranslationPvtRepository {

    Flux<SlaveTranslationEntity> showUnMappedTranslationListAgainstCountry(UUID countryUUID, String key, String dp, String d, Integer size, Long page);

    Flux<SlaveTranslationEntity> showUnMappedTranslationListAgainstCountryWithStatus(UUID countryUUID, String key, Boolean status, String dp, String d, Integer size, Long page);

    Flux<SlaveTranslationEntity> showMappedTranslationListAgainstCountry(UUID countryUUID, String key, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveTranslationEntity> showMappedTranslationListAgainstCountryWithStatus(UUID countryUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d);


}
