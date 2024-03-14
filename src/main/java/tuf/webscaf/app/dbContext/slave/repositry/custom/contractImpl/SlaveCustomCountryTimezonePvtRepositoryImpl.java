package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveTimezoneEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCountryTimezonePvtRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomTimeZoneMapper;

import java.util.UUID;


public class SlaveCustomCountryTimezonePvtRepositoryImpl implements SlaveCustomCountryTimezonePvtRepository {
    private DatabaseClient client;
    private SlaveTimezoneEntity slaveTimezoneEntity;

    @Autowired
    public SlaveCustomCountryTimezonePvtRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    //This Function is used to check the unmapped Timezone List Against country Id
    @Override
    public Flux<SlaveTimezoneEntity> showUnMappedTimezoneListAgainstCountry(UUID countryUUID, String zoneName, String dp, String d, Integer size, Long page) {
        String query = "SELECT timezones.* FROM timezones\n" +
                "WHERE timezones.uuid Not In (\n" +
                "SELECT timezones.uuid FROM timezones\n" +
                "LEFT JOIN country_timezone_pvt\n" +
                "ON country_timezone_pvt.timezone_uuid = timezones.uuid\n" +
                "WHERE country_timezone_pvt.country_uuid = '" + countryUUID +
                "' AND country_timezone_pvt.deleted_at IS NULL\n" +
                "AND timezones.deleted_at IS NULL ) \n" +
                "AND timezones.deleted_at is null "+
                "AND timezones.zone_name ILIKE '%"+zoneName+"%' \n" +
                "ORDER BY timezones." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();

        Flux<SlaveTimezoneEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTimezoneEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTimezoneEntity> showUnMappedTimezoneListAgainstCountryWithStatus(UUID countryUUID, String zoneName, Boolean status, String dp, String d, Integer size, Long page) {
        String query = "SELECT timezones.* FROM timezones\n" +
                "WHERE timezones.uuid Not In (\n" +
                "SELECT timezones.uuid FROM timezones\n" +
                "LEFT JOIN country_timezone_pvt\n" +
                "ON country_timezone_pvt.timezone_uuid = timezones.uuid\n" +
                "WHERE country_timezone_pvt.country_uuid = '" + countryUUID +
                "' AND country_timezone_pvt.deleted_at IS NULL\n" +
                "AND timezones.deleted_at IS NULL ) \n" +
                "AND timezones.deleted_at is null "+
                "AND timezones.status = "+status+
                " AND timezones.zone_name ILIKE '%"+zoneName+"%' \n" +
                "ORDER BY timezones." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();

        Flux<SlaveTimezoneEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveTimezoneEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveTimezoneEntity> showMappedTimezoneListAgainstCountry(UUID countryUUID, String zoneName, String description, Integer size, Long page, String dp, String d) {
        String query = "select timezones.* from timezones " +
                "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
                "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
                "where timezones.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_timezone_pvt.deleted_at is null " +
                "and countries.uuid = '" + countryUUID +
                "' and (timezones.zone_name ilike  '%" + zoneName + "%' or timezones.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveTimezoneEntity))
                .all();
    }

    @Override
    public Flux<SlaveTimezoneEntity> showMappedTimezoneListAgainstCountryWithStatus(UUID countryUUID, String zoneName, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select timezones.* from timezones " +
                "join country_timezone_pvt on timezones.uuid = country_timezone_pvt.timezone_uuid " +
                "join countries on country_timezone_pvt.country_uuid = countries.uuid " +
                "where timezones.deleted_at is null " +
                "and countries.deleted_at is null " +
                "and country_timezone_pvt.deleted_at is null " +
                "and countries.uuid = '" + countryUUID +
                "' and timezones.status = " + status +
                " and (timezones.zone_name ilike  '%" + zoneName + "%'" +
                " or timezones.description ilike '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomTimeZoneMapper mapper = new SlaveCustomTimeZoneMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveTimezoneEntity))
                .all();
    }
}

