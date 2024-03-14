package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveBranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchWithBranchProfileRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomBranchWithBranchProfileMapper;

import java.util.UUID;


public class SlaveCustomBranchWithBranchProfileRepositoryImpl implements SlaveCustomBranchWithBranchProfileRepository {
    private DatabaseClient client;
    private SlaveBranchWithBranchProfileDto slaveBranchWithBranchProfileDto;

    @Autowired
    public SlaveCustomBranchWithBranchProfileRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> branchWithBranchProfileIndex(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "join companies on companies.uuid=branches.company_uuid " +
                "join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                "where branch_profiles.deleted_at is null " +
                " and branches.deleted_at is null \n" +
                " and companies.deleted_at is null \n" +
                "and (branches.name ILIKE  '%" + name + "%'  or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> branchWithBranchProfileIndexWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "join companies on companies.uuid=branches.company_uuid " +
                "join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                "where branch_profiles.deleted_at is null" +
                " and branches.deleted_at is null" +
                " and companies.deleted_at is null \n" +
                " and branches.status =" + status +
                " and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Mono<SlaveBranchWithBranchProfileDto> ShowBranchWithBranchProfile(Long id) {
        String query = "select branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate,\n" +
                ",branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID\n" +
                "from branches\n" +
                "left join companies on companies.uuid=branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                "where branch_profiles.deleted_at is null and branches.deleted_at is null and branches.id=" + id;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Mono<SlaveBranchWithBranchProfileDto> result1 = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto)).one();
        return result1;
    }

    @Override
    public Mono<SlaveBranchWithBranchProfileDto> ShowByUuidBranchWithBranchProfile(UUID uuid) {
        String query = "select branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate,\n" +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID\n" +
                "from branches\n" +
                " join companies on companies.uuid=branches.company_uuid " +
                " join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                "where branch_profiles.deleted_at is null " +
                "and branches.uuid= '" + uuid +
                "' and branches.deleted_at is null and companies.deleted_at is null";

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Mono<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto)).one();
        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> IndexBranchesBasedOnCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.* ,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID," +
                "companies.uuid as companyUUID \n" +
                " from branches\n" +
                " left join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                " left join companies on companies.uuid = branches.company_uuid \n" +
                " where branch_profiles.deleted_at is null" +
                " and branches.deleted_at is null " +
                " and companies.uuid='" + companyUUID +
                "' and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> companyWithBranchIndexAndCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.* ,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID," +
                "companies.uuid as companyUUID \n" +
                "from branches\n" +
                "join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                "join companies on companies.uuid = branches.company_uuid \n" +
                "where branch_profiles.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and companies.uuid='" + companyUUID +
                "' and (branches.name ILIKE  '%" + name + "%'  or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;


    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> companyWithBranchIndexAndCompanyAndStatusFilter(UUID companyUUID, Boolean status, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.* ,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID," +
                "companies.uuid as companyUUID \n" +
                "from branches\n" +
                " join branch_profiles on branch_profiles.uuid=branches.branch_profile_uuid \n" +
                " join companies on companies.uuid = branches.company_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and branches.status = " + status +
                " and companies.uuid='" + companyUUID +
                "' and (branches.name ILIKE  '%" + name + "%'  or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }


    @Override
    public Flux<SlaveBranchWithBranchProfileDto> branchAgainstCompanyAndVouchers(UUID companyUUID, String branchUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and companies.uuid ='" + companyUUID +
                "' and branches.uuid  IN(" + branchUUID + ")" +
                "and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> branchAgainstVouchers(String branchUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and branches.uuid  IN(" + branchUUID + ")" +
                "and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    //This Function is used to check the un Mapped Branches Against Vouchers and Company
    @Override
    public Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstBranchAndCompany(UUID companyUUID, String branchUUID, String name, String description, Integer size, Long page, String dp, String d) {

        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and companies.uuid ='" + companyUUID +
                "' and branches.uuid  NOT IN(" + branchUUID + ")" +
                "and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstCompany(UUID companyUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and companies.uuid ='" + companyUUID +
                "' and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherListAgainstBranch(String branchUUID, String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and branches.uuid  NOT IN(" + branchUUID + ")" +
                "and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveBranchWithBranchProfileDto> unMappedVoucherList(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  branches.*, companies.uuid as companyUUID,\n" +
                "branch_profiles.establishment_date as establishmentDate," +
                "branch_profiles.language_uuid as languageUUID,branch_profiles.location_uuid as locationUUID \n" +
                "from branches\n" +
                "left join companies on companies.uuid = branches.company_uuid " +
                "left join branch_profiles on branch_profiles.uuid = branches.branch_profile_uuid \n" +
                " where branch_profiles.deleted_at is null " +
                " and companies.deleted_at is null " +
                " and branches.deleted_at is null " +
                " and companies.deleted_at is null" +
                " and (branches.name ILIKE  '%" + name + "%'  " +
                "or branches.description ILIKE  '%" + description + "%')" +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomBranchWithBranchProfileMapper mapper = new SlaveCustomBranchWithBranchProfileMapper();

        Flux<SlaveBranchWithBranchProfileDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveBranchWithBranchProfileDto))
                .all();

        return result;
    }

}

