package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;

import java.util.UUID;

@Repository
public interface SlaveCustomCompanyWithCompanyProfileRepository {

    Flux<SlaveCompanyWithCompanyProfileDto> CompanyWithCompanyProfileIndex(String name, String description, Integer size, Long page, String dp, String d);

    Flux<SlaveCompanyWithCompanyProfileDto> CompanyWithCompanyProfileIndexWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d);

//    Mono<SlaveCompanyWithCompanyProfileDto> ShowCompanyWithCompanyProfile(Long id);

    Mono<SlaveCompanyWithCompanyProfileDto> ShowByUuidCompanyWithCompanyProfile(UUID uuid);

}
