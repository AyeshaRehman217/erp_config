package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tuf.webscaf.seeder.model.Country;

import java.util.ArrayList;
import java.util.List;

@Service
public class JsonToDBService {

    @Autowired
    CurrencyService currencyService;

    @Autowired
    RegionService regionService;

    @Autowired
    SubRegionService subRegionService;

    @Autowired
    CountryService countryService;

    public Mono<String> saveTo(List<Country> countries){
       return currencyService.saveAllCurrency(countries)
                .flatMap(s -> regionService.saveAllRegion(countries)
                        .flatMap(s1 -> subRegionService.saveAllSubRegion(countries)
                                .flatMap(s2 -> countryService.saveAllCountries(countries))));
    }
}
