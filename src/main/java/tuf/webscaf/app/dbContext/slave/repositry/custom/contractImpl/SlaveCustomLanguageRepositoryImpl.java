package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLanguageEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomLanguageRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomLanguageMapper;


public class SlaveCustomLanguageRepositoryImpl implements SlaveCustomLanguageRepository {
    private DatabaseClient client;
    private SlaveLanguageEntity slaveLanguageEntity;

    @Autowired
    public SlaveCustomLanguageRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveLanguageEntity> languageIndex(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select *  from languages "+
                "where languages.deleted_at is null " +
                "and (languages.name ILIKE  '%"+name+"%' or languages.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d+" limit "+size+" offset "+page;


        SlaveCustomLanguageMapper mapper = new SlaveCustomLanguageMapper();

        Flux<SlaveLanguageEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveLanguageEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveLanguageEntity> languageIndexCount(String name, String description, String dp, String d) {
        String query = "select * from languages "+
                "where languages.deleted_at is null " +
                "and (languages.name ILIKE  '%"+name+"%' or languages.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d;


        SlaveCustomLanguageMapper mapper = new SlaveCustomLanguageMapper();

        Flux<SlaveLanguageEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveLanguageEntity))
                .all();

        return result;
    }
}

