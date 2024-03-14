package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.*;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.config.service.response.CustomResponse;
import tuf.webscaf.seeder.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CountryInfoService {

    @Autowired
    StateRepository stateRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    TimezoneRepository timeZoneRepository;

    @Autowired
    CountryTimezonePvtRepository countryTimeZonePvtRepository;

    @Autowired
    TranslationRepository translationRepository;

    @Autowired
    CountryTranslationPvtRepository countryTranslationPvtRepository;

    @Autowired
    CustomResponse appresponse;

    @Value("${server.zone}")
    private String zone;


    public Mono<String> saveCountryInfo(CountryEntity country, Country data) {

//        CurrencyEntity currencyEntity = CurrencyEntity.builder()
//                .currencyName(data.getCurrencyName())
//                .description("Currency")
//                .currency(data.getCurrency())
//                .currencySymbol(data.getCurrencySymbol())
//                .createdBy(Long.valueOf(1))
//                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                .build();
//
//       return currencyRepository.save(currencyEntity)
//                .flatMap(currency -> {

//                    RegionEntity regionEntity = RegionEntity.builder()
//                            .name(data.getRegion())
//                            .description("Region")
//                            .createdBy(Long.valueOf(1))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .build();
//
//                    return regionRepository.save(regionEntity)
//                            .flatMap(region -> {

//                                SubRegionEntity subRegionEntity = SubRegionEntity.builder()
//                                        .name(data.getSubregion())
//                                        .description("Sub Region")
//                                        .regionId(region.getId())
//                                        .createdBy(Long.valueOf(1))
//                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                        .build();
//
//                                return subRegionRepository.save(subRegionEntity)
//                                        .flatMap(subRegion -> {

//                                            if(data.getLongitude()==null){
//                                                data.setLongitude("");
//                                            }
//                                            if(data.getLatitude()==null){
//                                                data.setLatitude("");
//                                            }
//                                            CountryEntity countryEntity = CountryEntity.builder()
//                                                    .jsonId(Long.valueOf(data.getId()))
//                                                    .name(data.getName())
//                                                    .description("Country")
//                                                    .iso2(data.getIso2())
//                                                    .iso3(data.getIso3())
//                                                    .numericCode(Integer.parseInt(data.getNumericCode()))
//                                                    .phoneCode(data.getPhoneCode())
//                                                    .capital(data.getCapital())
//                                                    .tld(data.getTld())
//                                                    .nativeName(data.getNative())
//                                                    .longitude(Double.valueOf(data.getLongitude()))
//                                                    .latitude(Double.valueOf(data.getLatitude()))
//                                                    .emoji(data.getEmoji())
//                                                    .emojiU(data.getEmojiU())
//                                                    .currencyId(currency.getId())
//                                                    .regionId(region.getId())
//                                                    .subRegionId(subRegion.getId())
//                                                    .createdBy(Long.valueOf(1))
//                                                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                                                    .build();

//                                            return countryRepository.save(countryEntity)
//                                                    .flatMap(country -> {
        ArrayList<StateEntity> stateEntities = new ArrayList<>();
        for (State state : data.getStates()) {
            if (state.getLongitude() == null) {
                state.setLongitude("77.2105407");
            }
            if (state.getLatitude() == null) {
                state.setLatitude("-33.7456166");
            }
            if (state.getType() == null) {
                state.setType("");
            }
            if (state != null) {
                StateEntity stateEntity = StateEntity.builder()
                        .uuid(UUID.randomUUID())
                        .name(state.getName())
                        .type(state.getType())
                        .description("State")
                        .stateCode(state.getStateCode())
                        .longitude(Double.valueOf("" + state.getLongitude()))
                        .latitude(Double.valueOf("" + state.getLatitude()))
                        .countryUUID(country.getUuid())
                        .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                        .build();

                if(state.getName().equals("Punjab") && country.getName().equals("Pakistan")){
                    stateEntity.setUuid(UUID.fromString("65d1bbf1-0a6b-499c-b6c6-4ba319b5e959"));
                }

                stateEntities.add(stateEntity);
            }
        }
        return stateRepository.saveAll(stateEntities).collectList()
                .flatMap(stateEntities1 -> {

                    ArrayList<CityEntity> cityEntities = new ArrayList<>();

                    for (int k = 0; k < data.getStates().size(); k++) {
                        State state = data.getStates().get(k);
                        for (int i = 0; i < state.getCities().size(); i++) {
                            City city = state.getCities().get(i);

                            if (city.getLongitude() == null) {
                                city.setLongitude("77.2105407");
                            }
                            if (city.getLatitude() == null) {
                                city.setLatitude("-33.7456166");
                            }

                            CityEntity cityEntity = CityEntity.builder()
                                    .uuid(UUID.randomUUID())
                                    .name(city.getName())
                                    .description("City")
                                    .latitude(Double.valueOf(city.getLatitude()))
                                    .longitude(Double.valueOf(city.getLongitude()))
                                    .countryUUID(country.getUuid())
                                    .stateUUID(stateEntities1.get(k).getUuid())
                                    .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                    .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .build();

                            if(city.getName().equals("Faisalabad") && country.getName().equals("Pakistan")){
                                cityEntity.setUuid(UUID.fromString("cfaeb31b-e811-4365-9e9d-64ff8ce0d973"));
                            }

                            if(city.getName().equals("Lahore") && country.getName().equals("Pakistan")){
                                cityEntity.setUuid(UUID.fromString("cf4bd673-fe3f-4697-b307-90b37a31d5a0"));
                            }

                            if(city.getName().equals("Islamabad") && country.getName().equals("Pakistan")){
                                cityEntity.setUuid(UUID.fromString("67ad93ff-6a36-42b0-ba9e-23cfbebc4377"));
                            }

                            cityEntities.add(cityEntity);
                        }
                    }

                    return cityRepository.saveAll(cityEntities).collectList()
                            .flatMap(cityEntities1 -> {
                                List<Timezone> timezones = data.getTimezones();
                                ArrayList<TimezoneEntity> timeZoneEntities = new ArrayList<>();
                                for (Timezone timezone : timezones) {
                                    TimezoneEntity timeZoneEntity = TimezoneEntity.builder()
                                            .uuid(UUID.randomUUID())
                                            .zoneName(timezone.getZoneName())
                                            .gmtOffset(String.valueOf(timezone.getGmtOffset()))
                                            .gmtOffsetName(timezone.getGmtOffsetName())
                                            .abbreviation(timezone.getAbbreviation())
                                            .tzName(timezone.getTzName())
                                            .description("TimeZone")
                                            .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .build();

                                    if(timezone.getZoneName().equals("Asia/Karachi")){
                                        timeZoneEntity.setUuid(UUID.fromString("9c24c2da-5672-4f0b-8fec-677d3e22792a"));
                                    }

                                    timeZoneEntities.add(timeZoneEntity);
                                }

                                return timeZoneRepository.saveAll(timeZoneEntities).collectList()
                                        .flatMap(timeZoneEntities1 -> {
                                            ArrayList<CountryTimezonePvtEntity> countryTimeZonePvtEntities
                                                    = new ArrayList<>();
                                            for (TimezoneEntity entity : timeZoneEntities1) {
                                                CountryTimezonePvtEntity countryTimeZonePvtEntity =
                                                        CountryTimezonePvtEntity.builder()
                                                                .uuid(UUID.randomUUID())
                                                                .timezoneUUID(entity.getUuid())
                                                                .countryUUID(country.getUuid())
                                                                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                                                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                                .build();
                                                countryTimeZonePvtEntities.add(countryTimeZonePvtEntity);
                                            }
                                            return countryTimeZonePvtRepository.saveAll(countryTimeZonePvtEntities)
                                                    .collectList().flatMap(countryTimeZonePvtEntities1 -> {
                                                        ArrayList<TranslationEntity> list = getTranslationList(data.getTranslations());

                                                        return translationRepository.saveAll(list).collectList()
                                                                .flatMap(translationEntities -> {
                                                                    ArrayList<CountryTranslationPvtEntity> countryTranslationPvtEntities
                                                                            = new ArrayList<>();
                                                                    for (TranslationEntity entity : translationEntities) {
                                                                        CountryTranslationPvtEntity countryTranslationPvtEntity =
                                                                                CountryTranslationPvtEntity.builder()
                                                                                        .uuid(UUID.randomUUID())
                                                                                        .translationUUID(entity.getUuid())
                                                                                        .countryUUID(country.getUuid())
                                                                                        .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                                                                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                                                        .build();
                                                                        countryTranslationPvtEntities.add(countryTranslationPvtEntity);
                                                                    }
                                                                    return countryTranslationPvtRepository.saveAll(countryTranslationPvtEntities)
                                                                            .collectList().flatMap(countryTranslationPvtEntities1 -> {
                                                                                return Mono.just("");
                                                                            });
                                                                });
                                                    });
                                        });
                            });

                });
//                                                    });


//                                        });
//                            });
//                });


    }

    private ArrayList<TranslationEntity> getTranslationList(Translations trans) {

        ArrayList<TranslationEntity> list = new ArrayList<>();

        TranslationEntity t1 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("kr")
                .value(trans.getKr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t2 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("br")
                .value(trans.getBr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t3 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("pt")
                .value(trans.getPt())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t4 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("nl")
                .value(trans.getNl())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t5 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("hr")
                .value(trans.getHr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t6 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("fa")
                .value(trans.getFa())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t7 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("de")
                .value(trans.getDe())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t8 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("es")
                .value(trans.getEs())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t9 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("fr")
                .value(trans.getFr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();
        TranslationEntity t10 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("br")
                .value(trans.getBr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t11 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("ja")
                .value(trans.getJa())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t12 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("it")
                .value(trans.getIt())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t13 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("cn")
                .value(trans.getCn())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        TranslationEntity t14 = TranslationEntity.builder()
                .uuid(UUID.randomUUID())
                .key("tr")
                .value(trans.getTr())
                .description("Translation")
                .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                .build();

        list.add(t1);
        list.add(t2);
        list.add(t3);
        list.add(t4);
        list.add(t5);
        list.add(t6);
        list.add(t7);
        list.add(t8);
        list.add(t9);
        list.add(t10);
        list.add(t11);
        list.add(t12);
        list.add(t13);
        list.add(t14);

        return list;
    }

}
