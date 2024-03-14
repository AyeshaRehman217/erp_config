package tuf.webscaf.seeder.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CurrencyEntity;
import tuf.webscaf.app.dbContext.master.entity.RegionEntity;
import tuf.webscaf.app.dbContext.master.repositry.RegionRepository;
import tuf.webscaf.seeder.model.Country;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class RegionService {

    @Autowired
    RegionRepository regionRepository;

    @Value("${server.zone}")
    private String zone;

    public Mono<String> saveAllRegion(List<Country> countries) {
        Flux<String> fres = Flux.just("");
        for (int i = 0; i < countries.size(); i++) {
            Country data = countries.get(i);
            RegionEntity regionEntity = RegionEntity.builder()
                    .uuid(UUID.randomUUID())
                    .name(data.getRegion())
                    .description("Region")
                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                    .build();

                    if(data.getRegion().equals("Asia")){
                        regionEntity.setUuid(UUID.fromString("84f8880b-919e-4096-bc1e-655bb1c7b1a6"));
                    }

            Mono<String> res = checkRegion(regionEntity);
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkRegion(RegionEntity region) {
        return regionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(region.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveRegion(region));
    }

    public Mono<String> saveRegion(RegionEntity region) {
        return regionRepository.save(region)
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(Mono.just(""));
    }
}