package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;

import java.util.UUID;

/**
 * This Custom Repository will extend in Slave Translation Repository
 **/
@Repository
public interface SlaveCustomTranslationRepository {
    /**
     * Show Mapped Translations against Country UUID with and without status filter
     **/
//    Flux<SlaveTranslationEntity> listOfTranslationsAgainstCountry(UUID countryUUID, String key, String description, Integer size, Long page, String dp, String d);
//
//    Flux<SlaveTranslationEntity> listOfTranslationsAgainstCountryWithStatusFilter(UUID countryUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d);


}
