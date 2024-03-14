package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCurrencyEntity;

@Repository
public interface SlaveCustomCurrencyRepository {

    public Flux<SlaveCurrencyEntity> currencyIndex(String name, String description, Integer size, Long page, String dp, String d);
    public Flux<SlaveCurrencyEntity> currencyIndexCount(String name, String description, String dp, String d);
}
