package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTranslationPvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomTranslationMapper;

import java.util.UUID;


public class SlaveCustomCountryTranslationPvtRepositoryImpl implements SlaveCustomCountryTranslationPvtRepository {
    private DatabaseClient client;
    private SlaveTranslationEntity slaveTranslationEntity;

    @Autowired
    public SlaveCustomCountryTranslationPvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveTranslationEntity> showUnMappedTranslationListAgainstCountry(UUID countryUUID, String key, String dp, String d, Integer size, Long page) {
        String query = "SELECT translations.* FROM translations\n" +
                "WHERE translations.uuid NOT IN (\n" +
                "SELECT translations.uuid FROM translations\n" +
                "LEFT JOIN country_translation_pvt\n" +
                "ON country_translation_pvt.translation_uuid = translations.uuid\n" +
                "WHERE country_translation_pvt.country_uuid = '" + countryUUID +
                "' AND country_translation_pvt.deleted_at IS NULL\n" +
                "AND translations.deleted_at IS NULL ) \n" +
                "AND translations.deleted_at is null " +
                "AND translations.key ILIKE '%" + key + "%' \n" +
                "ORDER BY translations." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();

        Flux<SlaveTranslationEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTranslationEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTranslationEntity> showUnMappedTranslationListAgainstCountryWithStatus(UUID countryUUID, String key, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT translations.* FROM translations\n" +
                "WHERE translations.uuid NOT IN (\n" +
                "SELECT translations.uuid FROM translations\n" +
                "LEFT JOIN country_translation_pvt\n" +
                "ON country_translation_pvt.translation_uuid = translations.uuid\n" +
                "WHERE country_translation_pvt.country_uuid = '" + countryUUID +
                "' AND country_translation_pvt.deleted_at IS NULL\n" +
                "AND translations.deleted_at IS NULL ) \n" +
                "AND translations.deleted_at is null " +
                "AND translations.status = " + status +
                " AND translations.key ILIKE '%" + key + "%' \n" +
                "ORDER BY translations." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();

        Flux<SlaveTranslationEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTranslationEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTranslationEntity> showMappedTranslationListAgainstCountry(UUID countryUUID, String key, String description, Integer size, Long page, String dp, String d) {
        String query = "select translations.* from translations " +
                "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
                "join countries on country_translation_pvt.country_uuid = countries.uuid " +
                "where translations.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_translation_pvt.deleted_at is null " +
                "and countries.uuid = '" + countryUUID +
                "' and (translations.key ilike  '%" + key + "%' or" +
                " translations.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveTranslationEntity))
                .all();
    }

    @Override
    public Flux<SlaveTranslationEntity> showMappedTranslationListAgainstCountryWithStatus(UUID countryUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select translations.* from translations " +
                "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
                "join countries on country_translation_pvt.country_uuid = countries.uuid " +
                "where translations.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_translation_pvt.deleted_at is null " +
                "and countries.uuid = '" + countryUUID +
                "' and translations.status = " + status +
                " and (translations.key ilike  '%" + key + "%' or translations.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveTranslationEntity))
                .all();
    }
}

