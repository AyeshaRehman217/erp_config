//package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;
//
//import io.r2dbc.spi.ConnectionFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.r2dbc.core.DatabaseClient;
//import reactor.core.publisher.Flux;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveCityEntity;
//import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCityRepository;
//import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomCityMapper;
//
//import java.util.UUID;
//
//
//public class SlaveCustomCityRepositoryImpl implements SlaveCustomCityRepository {
//    private DatabaseClient client;
//    private SlaveCityEntity slaveCityEntity;
//
//    @Autowired
//    public SlaveCustomCityRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
//        this.client = DatabaseClient.create(cf);
//    }
//
//
//    @Override
//    public Flux<SlaveCityEntity> showCitiesAgainstStateAndCountryWithStatusFilter(UUID countryUUID, UUID stateUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d) {
//        String query = "select  * from cities " +
//                "join states on states.uuid = cities.state_uuid " +
//                "join" +
//                " countries on states.country_uuid=countries.uuid  " +
//                "where states.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and cities.deleted_at is null and states.uuid = '" + stateUUID +
//                "' and countries.uuid= '" + countryUUID +
//                "' and cities.status = " + status +
//                " and (cities.name ILIKE  '%" + name + "%'  " +
//                "or" +
//                " cities.description ILIKE  '%" + description + "%') " +
//                " order by cities." + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomCityMapper mapper = new SlaveCustomCityMapper();
//
//        Flux<SlaveCityEntity> result = client.sql(query)
//                .map(row -> mapper.apply(row, slaveCityEntity))
//                .all();
//
//        return result;
//    }
//
//    @Override
//    public Flux<SlaveCityEntity> showCitiesAgainstStateAndCountryWithOutStatusFilter(UUID countryUUID, UUID stateUUID, String name, String description, Integer size, Long page, String dp, String d) {
//        String query = "select  * from cities " +
//                "join states on states.uuid = cities.state_uuid " +
//                "join" +
//                " countries on states.country_uuid=countries.uuid  " +
//                "where states.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and cities.deleted_at is null and states.uuid = '" + stateUUID +
//                "' and countries.uuid= '" + countryUUID +
//                "' and (cities.name ILIKE  '%" + name + "%'  " +
//                "or" +
//                " cities.description ILIKE  '%" + description + "%') " +
//                " order by cities." + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomCityMapper mapper = new SlaveCustomCityMapper();
//
//        Flux<SlaveCityEntity> result = client.sql(query)
//                .map(row -> mapper.apply(row, slaveCityEntity))
//                .all();
//
//        return result;
//    }
//
//    @Override
//    public Flux<SlaveCityEntity> showCitiesAgainstState(UUID stateUUID, String name, String description, Integer size, Long page, String dp, String d) {
//        String query = "select  * from cities " +
//                "left join states on states.uuid = cities.state_uuid " +
//                " where states.deleted_at is null and cities.deleted_at is null and states.uuid = '" + stateUUID +
//                "' and " +
//                "(cities.name ILIKE  '%" + name + "%'  " +
//                "or" +
//                " cities.description ILIKE  '%" + description + "%') " +
//                " order by cities." + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomCityMapper mapper = new SlaveCustomCityMapper();
//
//        Flux<SlaveCityEntity> result = client.sql(query)
//                .map(row -> mapper.apply(row, slaveCityEntity))
//                .all();
//
//        return result;
//    }
//
//    @Override
//    public Flux<SlaveCityEntity> showCitiesAgainstStateAndStatus(UUID stateUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d) {
//        String query = "select  * from cities " +
//                "left join states on states.uuid = cities.state_uuid " +
//                " where states.deleted_at is null and cities.deleted_at is null and states.uuid = '" + stateUUID +
//                "' and cities.status = " + status +
//                " and (cities.name ILIKE  '%" + name + "%'  " +
//                "or" +
//                " cities.description ILIKE  '%" + description + "%') " +
//                " order by cities." + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomCityMapper mapper = new SlaveCustomCityMapper();
//
//        Flux<SlaveCityEntity> result = client.sql(query)
//                .map(row -> mapper.apply(row, slaveCityEntity))
//                .all();
//
//        return result;
//    }
//}
//
