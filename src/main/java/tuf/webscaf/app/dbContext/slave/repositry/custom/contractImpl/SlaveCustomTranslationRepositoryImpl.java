package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTranslationEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomTranslationRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomTranslationMapper;

import java.util.UUID;


public class SlaveCustomTranslationRepositoryImpl implements SlaveCustomTranslationRepository {
    private DatabaseClient client;
    private SlaveTranslationEntity slaveCountryEntity;

    @Autowired
    public SlaveCustomTranslationRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

//
//    @Override
//    public Flux<SlaveTranslationEntity> listOfTranslationsAgainstCountry(UUID countryUUID, String key, String description, Integer size, Long page, String dp, String d) {
//        String query = "select translations.* from translations " +
//                "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
//                "join countries on country_translation_pvt.country_uuid = countries.uuid " +
//                "where translations.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and country_translation_pvt.deleted_at is null " +
//                "and countries.uuid = '" + countryUUID +
//                "' and (translations.key ilike  '%" + key + "%' or" +
//                " translations.description ilike '%" + description + "%')" +
//                "order by " + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();
//
//        return client.sql(query)
//                .map(row -> mapper.apply(row, slaveCountryEntity))
//                .all();
//    }
//
//    @Override
//    public Flux<SlaveTranslationEntity> listOfTranslationsAgainstCountryWithStatusFilter(UUID countryUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d) {
//        String query = "select translations.* from translations " +
//                "join country_translation_pvt on translations.uuid = country_translation_pvt.translation_uuid " +
//                "join countries on country_translation_pvt.country_uuid = countries.uuid " +
//                "where translations.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and country_translation_pvt.deleted_at is null " +
//                "and countries.uuid = '" + countryUUID +
//                "' and translations.status = " + status +
//                " and (translations.key ilike  '%" + key + "%' or translations.description ilike '%" + description + "%')" +
//                "order by " + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomTranslationMapper mapper = new SlaveCustomTranslationMapper();
//
//        return client.sql(query)
//                .map(row -> mapper.apply(row, slaveCountryEntity))
//                .all();
//    }

}

