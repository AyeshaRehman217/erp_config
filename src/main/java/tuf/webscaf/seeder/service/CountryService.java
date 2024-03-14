package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CountryEntity;
import tuf.webscaf.app.dbContext.master.repositry.CountryRepository;
import tuf.webscaf.app.dbContext.master.repositry.CurrencyRepository;
import tuf.webscaf.app.dbContext.master.repositry.RegionRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubRegionRepository;
import tuf.webscaf.seeder.model.Country;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class CountryService {

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    RegionRepository regionRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CountryInfoService countryInfoService;

    @Value("${server.zone}")
    private String zone;

    public Mono<String> saveAllCountries(List<Country> countries){
        Flux<String> fres = Flux.just("");
        for (int i=0; i<countries.size(); i++){
            Country data = countries.get(i);

            if(data.getLongitude()==null){
                data.setLongitude("77.2105407");
            }
            if(data.getLatitude()==null){
                data.setLatitude("-33.7456166");
            }
            CountryEntity countryEntity = CountryEntity.builder()
                    .jsonId(Long.valueOf(data.getId()))
                    .uuid(UUID.randomUUID())
                    .name(data.getName())
                    .description("Country")
                    .iso2(data.getIso2())
                    .iso3(data.getIso3())
                    .numericCode(Integer.parseInt(data.getNumericCode()))
                    .phoneCode(data.getPhoneCode())
                    .capital(data.getCapital())
                    .tld(data.getTld())
                    .nativeName(data.getNative())
                    .longitude(Double.valueOf(data.getLongitude()))
                    .latitude(Double.valueOf(data.getLatitude()))
                    .emoji(data.getEmoji())
                    .emojiU(data.getEmojiU())
                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                    .build();

            if(data.getName().equals("Pakistan")){
                countryEntity.setUuid(UUID.fromString("943b60b8-cd04-4f69-b8b2-7f5b4a2f28d8"));
            }

            Mono<String> res = checkCountry(countryEntity,data);
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkCountry(CountryEntity countryEntity,Country data){
        return countryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(countryEntity.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveCountry(countryEntity,data));
    }

    public Mono<String> saveCountry(CountryEntity countryEntity,Country data){
       return currencyRepository.findFirstByCurrencyIgnoreCaseAndDeletedAtIsNull(data.getCurrency())
                .flatMap(currency -> {
                    countryEntity.setCurrencyUUID(currency.getUuid());
                    return regionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(data.getRegion())
                            .flatMap(region1 -> {
                                countryEntity.setRegionUUID(region1.getUuid());
                               return subRegionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(data.getSubregion())
                                        .flatMap(subRegionEntity -> {
                                            countryEntity.setSubRegionUUID(subRegionEntity.getUuid());
                                            return countryRepository.save(countryEntity)
                                                    .flatMap(value -> {
                                                        return countryInfoService.saveCountryInfo(value,data);
                                                    }).switchIfEmpty(Mono.just(""));
                                        });
                            });
                });
    }
}