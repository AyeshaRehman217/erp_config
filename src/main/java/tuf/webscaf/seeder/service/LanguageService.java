package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LanguageEntity;
import tuf.webscaf.app.dbContext.master.repositry.LanguageRepository;
import tuf.webscaf.seeder.model.Language;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
public class LanguageService {

    @Autowired
    LanguageRepository languageRepository;

    @Value("${server.zone}")
    private String zone;


    public Mono<String> saveAllLanguages(Map<String, Language> languageMap) {
        Flux<String> fres = Flux.just("");
        ArrayList<String> keys = new ArrayList<>(languageMap.keySet());
        for (int i = 0; i < keys.size(); i++) {
            LanguageEntity languageEntity = LanguageEntity.builder()
                    .uuid(UUID.randomUUID())
                    .name(languageMap.get(keys.get(i)).getName())
                    .languageCode(keys.get(i))
                    .description("Language")
                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                    .build();

            if(languageMap.get(keys.get(i)).getName().equals("English")){
                languageEntity.setUuid(UUID.fromString("90a6e568-6165-441e-a94d-d200a1489d3e"));
            }

            Mono<String> res = checkLanguage(languageEntity);
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkLanguage(LanguageEntity language) {
        return languageRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(language.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveLanguage(language));
    }

    public Mono<String> saveLanguage(LanguageEntity language) {
        return languageRepository.save(language)
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(Mono.just(""));
    }

}
