package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveStateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomStateRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomStateMapper;

import java.util.UUID;


public class SlaveCustomStateRepositoryImpl implements SlaveCustomStateRepository {
    private DatabaseClient client;
    private SlaveStateEntity slaveStateEntity;

    @Autowired
    public SlaveCustomStateRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveStateEntity> indexStateAgainstCountry(UUID countryUUID, String name, String type, String description, Integer size, Long page, String dp, String d) {

        String query = "select  * from states " +
                "left join countries on countries.uuid = states.country_uuid " +
                " where countries.deleted_at is null" +
                " and states.deleted_at is null" +
                " and countries.uuid = '" + countryUUID +
                "' and (states.name ILIKE  '%" + name + "%'  " +
                "or states.type ILIKE  '%" + type + "%' " +
                "or states.description ILIKE  '%" + description + "%') " +
                " order by states." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomStateMapper mapper = new SlaveCustomStateMapper();

        Flux<SlaveStateEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveStateEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveStateEntity> indexStateAgainstCountryWithStatus(UUID countryUUID, Boolean status, String name, String type, String description, Integer size, Long page, String dp, String d) {
        String query = "select  * from states " +
                "left join countries on countries.uuid = states.country_uuid " +
                " where countries.deleted_at is null" +
                " and states.deleted_at is null" +
                " and countries.uuid = '" + countryUUID +
                "' and (states.name ILIKE  '%" + name + "%'  " +
                "or states.type ILIKE  '%" + type + "%' " +
                "or states.description ILIKE  '%" + description + "%') " +
                " and countries.status = " + status +
                " order by states." + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomStateMapper mapper = new SlaveCustomStateMapper();

        Flux<SlaveStateEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveStateEntity))
                .all();

        return result;
    }
}

