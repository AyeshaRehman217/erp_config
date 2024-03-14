package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveBranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;

import java.util.List;
import java.util.UUID;

/**
 * This Custom repository extends in Slave Branch repository
 **/
@Repository
public interface SlaveCustomBranchWithBranchProfileRepository {

    /**
     * Fetch Branch and Branch profile without status Filter
     **/
    Flux<SlaveBranchWithBranchProfileDto> branchWithBranchProfileIndex(String name, String description, Integer size, Long page, String dp, String d);

    /**
     * Fetch Branch and Branch profile based on Status Filter
     **/
    Flux<SlaveBranchWithBranchProfileDto> branchWithBranchProfileIndexWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d);

    //Show Mapped Branches Against Voucher UUID from Account Module
    Flux<SlaveBranchWithBranchProfileDto> branchAgainstCompanyAndVouchers(UUID companyUUID, String branchUUID, String name, String description, Integer size, Long page, String dp, String d);

    //Show Mapped Branches Against Voucher UUID from Account Module
    Flux<SlaveBranchWithBranchProfileDto> branchAgainstVouchers(String branchUUID, String name, String description, Integer size, Long page, String dp, String d);

//    Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstBranchAndCompany(UUID companyUUID, String branchUUID, String name, String description, Integer size, Long page, String dp, String d);

    Mono<SlaveBranchWithBranchProfileDto> ShowBranchWithBranchProfile(Long id);

    Mono<SlaveBranchWithBranchProfileDto> ShowByUuidBranchWithBranchProfile(UUID uuid);

    /**
     * Fetch Branch Based on Company Filter
     **/
    Flux<SlaveBranchWithBranchProfileDto> IndexBranchesBasedOnCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d);

    //Fetch Branches based on Company UUID
    Flux<SlaveBranchWithBranchProfileDto> companyWithBranchIndexAndCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d);

    //Fetch Branches based on Company UUID and Status
    Flux<SlaveBranchWithBranchProfileDto> companyWithBranchIndexAndCompanyAndStatusFilter(UUID companyUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d);

    //This Function is used to check the un Mapped Branches Against Vouchers and Company
    Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstBranchAndCompany(UUID companyUUID, String branchUUID, String name, String description, Integer size, Long page, String dp, String d);

    //This Function is used to check the un Mapped Branches Against Vouchers and Company
    Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d);

    //This Function is used to check the un Mapped Branches Against Vouchers and Company
    Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstBranch(String branchUUID, String name, String description, Integer size, Long page, String dp, String d);

    //This Function is used to check the un Mapped Branches Against Vouchers and Company
    Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherList(String name, String description, Integer size, Long page, String dp, String d);
}
