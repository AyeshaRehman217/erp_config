package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomTimezoneRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomTimeZoneMapper;

import java.util.UUID;


public class SlaveCustomTimezoneRepositoryImpl implements SlaveCustomTimezoneRepository {
    private DatabaseClient client;
    private SlaveTimezoneEntity slaveCountryEntity;

    @Autowired
    public SlaveCustomTimezoneRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }


//    @Override
//    public Flux<SlaveTimezoneEntity> listOfTimezonesAgainstCountry(UUID countryUUID, String zoneName, String description, Integer size, Long page, String dp, String d) {
//        String query = "select timezones.* from timezones " +
//                "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
//                "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
//                "where timezones.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and country_timezone_pvt.deleted_at is null " +
//                "and countries.uuid = '" + countryUUID +
//                "' and (timezones.zone_name ilike  '%" + zoneName + "%' or timezones.description ilike '%" + description + "%')" +
//                "order by " + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();
//
//        return client.sql(query)
//                .map(row -> mapper.apply(row, slaveCountryEntity))
//                .all();
//    }
//
//    @Override
//    public Flux<SlaveTimezoneEntity> listOfTimezonesAgainstCountryWithStatusFilter(UUID countryUUID, String zoneName, String description, Boolean status, Integer size, Long page, String dp, String d) {
//        String query = "select timezones.* from timezones " +
//                "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
//                "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
//                "where timezones.deleted_at is null " +
//                "and countries.deleted_at is null " +
//                "and country_timezone_pvt.deleted_at is null " +
//                "and countries.uuid = '" + countryUUID +
//                "' and timezones.status = " + status +
//                " and (timezones.zone_name ilike  '%" + zoneName + "%'" +
//                " or timezones.description ilike '%" + description + "%')" +
//                "order by " + dp + " " + d + " limit " + size + " offset " + page;
//
//        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();
//
//        return client.sql(query)
//                .map(row -> mapper.apply(row, slaveCountryEntity))
//                .all();
//    }

}

