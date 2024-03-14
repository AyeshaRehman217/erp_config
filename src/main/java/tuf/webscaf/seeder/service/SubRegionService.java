package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.SubRegionEntity;
import tuf.webscaf.app.dbContext.master.repositry.RegionRepository;
import tuf.webscaf.app.dbContext.master.repositry.SubRegionRepository;
import tuf.webscaf.seeder.model.Country;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class SubRegionService {

    @Autowired
    SubRegionRepository subRegionRepository;

    @Autowired
    RegionRepository regionRepository;

    @Value("${server.zone}")
    private String zone;

    public Mono<String> saveAllSubRegion(List<Country> countries) {
        Flux<String> fres = Flux.just("");
        for (int i = 0; i < countries.size(); i++) {
            Country data = countries.get(i);

            SubRegionEntity subRegionEntity = SubRegionEntity.builder()
                    .uuid(UUID.randomUUID())
                    .name(data.getSubregion())
                    .description("Sub Region")
                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                    .build();


            if(data.getSubregion().equals("Southern Asia")){
                subRegionEntity.setUuid(UUID.fromString("71949c17-3700-403f-905d-f2850327e247"));
            }

            Mono<String> res = checkSubRegion(subRegionEntity, data.getRegion());
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkSubRegion(SubRegionEntity subRegion, String region) {
        return subRegionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(subRegion.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveSubRegion(subRegion, region));
    }

    public Mono<String> saveSubRegion(SubRegionEntity subRegion, String region) {
        return regionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(region)
                .flatMap(region1 -> {
                    subRegion.setRegionUUID(region1.getUuid());
                    return subRegionRepository.save(subRegion)
                            .flatMap(value -> {
                                return Mono.just("");
                            }).switchIfEmpty(Mono.just(""));
                });
    }
}