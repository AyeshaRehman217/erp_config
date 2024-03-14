package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.master.repositry.CurrencyRepository;
import tuf.webscaf.seeder.model.Country;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class CurrencyService {

    @Autowired
    CurrencyRepository currencyRepository;

    @Value("${server.zone}")
    private String zone;


    public Mono<String> saveAllCurrency(List<Country> countries) {
        Flux<String> fres = Flux.just("");
        for (int i = 0; i < countries.size(); i++) {
            Country data = countries.get(i);
            CurrencyEntity currencyEntity = CurrencyEntity.builder()
                    .uuid(UUID.randomUUID())
                    .currencyName(data.getCurrencyName())
                    .description("Currency")
                    .currency(data.getCurrency())
                    .currencySymbol(data.getCurrencySymbol())
                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                    .build();

            if(data.getCurrency().equals("PKR")){
                currencyEntity.setUuid(UUID.fromString("041deaa3-2fca-40a7-bfbd-5ec827ed66f3"));
            }

            Mono<String> res = checkCurrency(currencyEntity);
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkCurrency(CurrencyEntity currency) {
        return currencyRepository.findFirstByCurrencyIgnoreCaseAndDeletedAtIsNull(currency.getCurrency())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveCurrency(currency));
    }

    public Mono<String> saveCurrency(CurrencyEntity currency) {
        return currencyRepository.save(currency)
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(Mono.just(""));
    }
}