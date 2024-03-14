package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;

import java.util.UUID;

/**
 * This Slave Custom Repository will extend in Custom Slave Repository
 **/
@Repository
public interface SlaveCustomCountryRepository {

    /**
     * List Mapped Countries Based on Timezone UUID with and without Status Filter
     **/
    Flux<SlaveCountryEntity> listOfCountriesAgainstTimezone(UUID timezoneUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCountryEntity> listOfCountriesAgainstTimezoneWithStatusFilter(UUID timezoneUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    /**
     * List Mapped Countries Based on Translation UUID with and without Status Filter
     **/
    Flux<SlaveCountryEntity> listOfCountriesAgainstTranslation(UUID translationUUID, String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCountryEntity> listOfCountriesAgainstTranslationWithStatusFilter(UUID translationUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d);


}
