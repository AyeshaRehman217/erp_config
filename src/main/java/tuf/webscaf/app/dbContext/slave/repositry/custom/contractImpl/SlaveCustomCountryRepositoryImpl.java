package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCountryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomCountryMapper;

import java.util.UUID;


public class SlaveCustomCountryRepositoryImpl implements SlaveCustomCountryRepository {
    private DatabaseClient client;
    private SlaveCountryEntity slaveCountryEntity;
    private SlaveTimezoneEntity slaveTimeZoneEntity;
    private SlaveTranslationEntity slaveTranslationEntity;

    @Autowired
    public SlaveCustomCountryRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCountryEntity> listOfCountriesAgainstTimezone(UUID timezoneUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select countries.* from countries " +
                "join country_timezone_pvt on countries.uuid = country_timezone_pvt.country_uuid " +
                "join timezones on country_timezone_pvt.timezone_uuid = timezones.uuid " +
                "where timezones.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_timezone_pvt.deleted_at is null " +
                "and timezones.uuid = '" + timezoneUUID +
                "' and (countries.name ilike  '%" + name + "%' " +
                "or " +
                "countries.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCountryMapper mapper = new SlaveCustomCountryMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCountryEntity))
                .all();
    }

    @Override
    public Flux<SlaveCountryEntity> listOfCountriesAgainstTimezoneWithStatusFilter(UUID timezoneUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select countries.* from countries " +
                "join country_timezone_pvt on countries.uuid = country_timezone_pvt.country_uuid " +
                "join timezones on country_timezone_pvt.timezone_uuid = timezones.uuid " +
                "where timezones.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_timezone_pvt.deleted_at is null " +
                "and timezones.uuid = '" + timezoneUUID +
                "' and countries.status = " + status +
                " and (countries.name ilike  '%" + name + "%' or countries.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCountryMapper mapper = new SlaveCustomCountryMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCountryEntity))
                .all();
    }

    @Override
    public Flux<SlaveCountryEntity> listOfCountriesAgainstTranslation(UUID translationUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select countries.* from countries " +
                "join country_translation_pvt on countries.uuid = country_translation_pvt.country_uuid " +
                "join translations on country_translation_pvt.translation_uuid = translations.uuid " +
                "where translations.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_translation_pvt.deleted_at is null " +
                "and translations.uuid ='" + translationUUID +
                "' and (countries.name ilike  '%" + name + "%' or countries.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCountryMapper mapper = new SlaveCustomCountryMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCountryEntity))
                .all();
    }

    @Override
    public Flux<SlaveCountryEntity> listOfCountriesAgainstTranslationWithStatusFilter(UUID translationUUID, String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select countries.* from countries " +
                "join country_translation_pvt on countries.uuid = country_translation_pvt.country_uuid " +
                "join translations on country_translation_pvt.translation_uuid = translations.uuid " +
                "where translations.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_translation_pvt.deleted_at is null " +
                "and translations.uuid ='" + translationUUID +
                "' and countries.status =" + status +
                " and (countries.name ilike  '%" + name + "%' " +
                "or countries.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCountryMapper mapper = new SlaveCustomCountryMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCountryEntity))
                .all();
    }
}

