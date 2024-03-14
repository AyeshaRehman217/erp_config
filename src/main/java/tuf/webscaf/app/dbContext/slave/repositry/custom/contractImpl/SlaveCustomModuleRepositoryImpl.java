package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveConfigEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveModuleEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomConfigRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomModuleRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomConfigMapper;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomModuleMapper;


public class SlaveCustomModuleRepositoryImpl implements SlaveCustomModuleRepository {
    private DatabaseClient client;
    private SlaveModuleEntity slaveModuleEntity;

    @Autowired
    public SlaveCustomModuleRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveModuleEntity> moduleIndex(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  * from modules " +
                "where modules.deleted_at is null " +
                "and (modules.name ILIKE  '%" + name + "%' or modules.description ILIKE  '%" + description + "%') " +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;


        SlaveCustomModuleMapper mapper = new SlaveCustomModuleMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveModuleEntity))
                .all();
    }

    @Override
    public Flux<SlaveModuleEntity> moduleIndexCount(String name, String description, String dp, String d) {
        String query = "select  * from modules " +
                "where modules.deleted_at is null " +
                "and (modules.name ILIKE  '%" + name + "%' or modules.description ILIKE  '%" + description + "%') " +
                "order by " + dp + " " + d;


        SlaveCustomModuleMapper mapper = new SlaveCustomModuleMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveModuleEntity))
                .all();
    }

}

