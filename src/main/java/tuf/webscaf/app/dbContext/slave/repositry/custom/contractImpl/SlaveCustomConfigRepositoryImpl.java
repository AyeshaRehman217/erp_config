package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomConfigRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomConfigMapper;

import java.util.UUID;


public class SlaveCustomConfigRepositoryImpl implements SlaveCustomConfigRepository {
    private DatabaseClient client;
    private SlaveConfigEntity slaveConfigEntity;

    @Autowired
    public SlaveCustomConfigRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveConfigEntity> listOfConfigsAgainstModule(UUID moduleUUID, String key, String description, Integer size, Long page, String dp, String d) {
        String query = "select * from configs\n" +
                "left join modules on configs.module_uuid = modules.uuid " +
                "where configs.deleted_at is null and modules.deleted_at is null" +
                " and modules.uuid ='" + moduleUUID +
                "' and (configs.key ILIKE  '%" + key + "%' or configs.description ILIKE  '%" + description + "%') " +
                "order by configs." + dp + " " + d + " limit " + size + " offset " + page;


        SlaveCustomConfigMapper mapper = new SlaveCustomConfigMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveConfigEntity))
                .all();
    }

    @Override
    public Flux<SlaveConfigEntity> listOfConfigsWithStatusFilterAgainstModule(UUID moduleUUID, String key, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select * from configs\n" +
                "left join modules on configs.module_uuid = modules.uuid " +
                "where configs.deleted_at is null and" +
                " modules.deleted_at is null" +
                " and modules.uuid ='" + moduleUUID +
                "' and modules.status =" + status +
                " and (configs.key ILIKE  '%" + key + "%' " +
                "or configs.description ILIKE  '%" + description + "%') " +
                "order by configs." + dp + " " + d + " limit " + size + " offset " + page;


        SlaveCustomConfigMapper mapper = new SlaveCustomConfigMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveConfigEntity))
                .all();
    }
}

