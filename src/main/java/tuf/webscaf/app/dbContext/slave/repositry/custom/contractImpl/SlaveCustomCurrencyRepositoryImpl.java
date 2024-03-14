package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCurrencyEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCurrencyRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomCurrencyMapper;


public class SlaveCustomCurrencyRepositoryImpl implements SlaveCustomCurrencyRepository {
    private DatabaseClient client;
    private SlaveCurrencyEntity slaveCurrencyEntity;

    @Autowired
    public SlaveCustomCurrencyRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCurrencyEntity> currencyIndex(String currency_name, String description, Integer size, Long page, String dp, String d) {
        String query = "select * from currencies " +
                "where currencies.deleted_at is null " +
                "and (currencies.currency_name ILIKE  '%"+currency_name+"%' or currencies.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d+" limit "+size+" offset "+page;


        SlaveCustomCurrencyMapper mapper = new SlaveCustomCurrencyMapper();

        Flux<SlaveCurrencyEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCurrencyEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveCurrencyEntity> currencyIndexCount(String currency_name, String description, String dp, String d) {
        String query = "select * from currencies " +
                "where currencies.deleted_at is null " +
                "and (currencies.currency_name ILIKE  '%"+currency_name+"%' or currencies.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d;


        SlaveCustomCurrencyMapper mapper = new SlaveCustomCurrencyMapper();

        Flux<SlaveCurrencyEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveCurrencyEntity))
                .all();

        return result;
    }
}

