package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.CompanyEntity;
import tuf.webscaf.app.dbContext.master.entity.CompanyProfileEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.seeder.model.Company;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CompanyService {

    @Autowired
    LanguageRepository languageRepository;

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    CompanyProfileRepository companyProfileRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Value("${server.zone}")
    private String zone;


    public Mono<String> saveAllCompanies() {
        Flux<String> fres = Flux.just("");
        Company companyA = Company.builder()
                .uuid(UUID.fromString("fe9a7354-b784-4a1f-854f-2240d4457c93"))
                .city("Faisalabad")
                .company("TUF")
                .desc("The University of Faisalabad")
                .build();

        Company companyB = Company.builder()
                .uuid(UUID.fromString("6b7f7aee-b1e3-4b96-b6ee-12c0b351a9f9"))
                .city("Lahore")
                .company("GIU")
                .desc("Green International University")
                .build();

        ArrayList<Company> companyList = new ArrayList<>();
        companyList.add(companyA);
        companyList.add(companyB);

        for (int i = 0; i < companyList.size(); i++) {
            Company cc = companyList.get(i);
            Mono<String> res = checkCompany(cc.getUuid(),cc.getCity(), cc.getCompany(), cc.getDesc());
            fres = fres.concatWith(res);
        }
        return fres.last();
    }

    public Mono<String> checkCompany(UUID uuid,String city, String company, String desc) {
        return companyRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(company)
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveCompany(uuid,city, company, desc));
    }

    private Mono<String> saveCompany(UUID uuid,String city, String company, String desc) {

        return languageRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull("English")
                .flatMap(language -> {
                    return currencyRepository.findFirstByCurrencyIgnoreCaseAndDeletedAtIsNull("PKR")
                            .flatMap(currency -> {
                                return cityRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(city)
                                        .flatMap(cityEntity -> {

                                            CompanyProfileEntity companyProfileEntity = CompanyProfileEntity.builder()
                                                    .uuid(UUID.randomUUID())
                                                    .establishmentDate(LocalDateTime.now(ZoneId.of(zone)))
                                                    .languageUUID(language.getUuid())
                                                    .currencyUUID(currency.getUuid())
                                                    .countryUUID(cityEntity.getCountryUUID())
                                                    .stateUUID(cityEntity.getStateUUID())
                                                    .cityUUID(cityEntity.getUuid())
                                                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                                    .build();

                                            return companyProfileRepository.save(companyProfileEntity)
                                                    .flatMap(entity -> {
                                                        CompanyEntity companyEntity = CompanyEntity.builder()
                                                                .uuid(uuid)
                                                                .name(company)
                                                                .description(desc)
                                                                .companyProfileUUID(entity.getUuid())
                                                                .status(true)
                                                                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                                                .build();


                                                        return companyRepository.save(companyEntity)
                                                                .flatMap(companyEntity1 -> {
                                                                    return Mono.just("");
                                                                });

                                                    });
                                        });
                            });
                });
    }

}