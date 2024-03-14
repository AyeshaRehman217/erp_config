package tuf.webscaf.seeder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LocationEntity;
import tuf.webscaf.app.dbContext.master.repositry.*;
import tuf.webscaf.seeder.model.Location;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class locationService {

    @Autowired
    CityRepository cityRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    LocationRepository locationRepository;

    @Value("${server.zone}")
    private String zone;


    public Mono<String> saveAllOffices() {

        Flux<String> fres = Flux.just("");

        Location locationA = Location.builder()
                .uuid(UUID.fromString("385e0547-7253-49c3-b940-fcf57c3a66cc"))
                .city("Faisalabad")
                .name("TUF Location")
                .desc("Main office of TUF")
                .address("Sargodha Road Faisalabad")
                .company("TUF")
                .build();

        Location locationB = Location.builder()
                .uuid(UUID.fromString("2223f532-e4e7-4dcc-b624-0c4c97c947d6"))
                .city("Lahore")
                .name("GIU Location")
                .desc("Main office of GIU")
                .address("Raiwind Road, Lahore")
                .company("GIU")
                .build();


        ArrayList<Location> locationList = new ArrayList<>();
        locationList.add(locationA);
        locationList.add(locationB);

        for (int i = 0; i < locationList.size(); i++) {
            Location oo = locationList.get(i);
            Mono<String> res = checkOffice(oo);
            fres = fres.concatWith(res);
        }
        return fres.last();
    }


    public Mono<String> checkOffice(Location o) {
        return locationRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(o.getName())
                .flatMap(value -> {
                    return Mono.just("");
                }).switchIfEmpty(saveOffice(o));
    }

    public Mono<String> saveOffice(Location o) {
        return cityRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(o.getCity())
                .flatMap(cityEntity -> {
                    LocationEntity officeEntity = LocationEntity.builder()
                            .uuid(o.getUuid())
                            .name(o.getName())
                            .description(o.getDesc())
                            .address(o.getAddress())
                            .countryUUID(cityEntity.getCountryUUID())
                            .stateUUID(cityEntity.getStateUUID())
                            .cityUUID(cityEntity.getUuid())
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .createdBy(UUID.fromString("cb1cc73f-eabe-4f37-af88-ebd322d72321"))
                            .build();

                                return locationRepository.save(officeEntity)
                                        .flatMap(officeEntity1 -> {
                                            return Mono.just("");
                                        });

                });
    }
}
