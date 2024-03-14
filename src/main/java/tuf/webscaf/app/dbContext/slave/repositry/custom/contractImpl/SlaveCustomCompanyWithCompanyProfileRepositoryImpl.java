package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveCompanyWithCompanyProfileDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCompanyWithCompanyProfileRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomCompanyWithCompanyProfileMapper;

import java.util.UUID;


public class SlaveCustomCompanyWithCompanyProfileRepositoryImpl implements SlaveCustomCompanyWithCompanyProfileRepository {
    private DatabaseClient client;
    private SlaveCompanyWithCompanyProfileDto slaveCompanyWithCompanyProfileDto;

    @Autowired
    public SlaveCustomCompanyWithCompanyProfileRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCompanyWithCompanyProfileDto> CompanyWithCompanyProfileIndex(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select  companies.id as id,companies.uuid as uuid,  companies.version as version,companies.status as status" +
                ",companies.name as name,companies.description as description, companies.company_profile_uuid," +
                "companies.doc_image as CompanyImageID,\n" +
                "company_profiles.establishment_date as establishmentDate,\n" +
                "company_profiles.language_uuid as languageUUID," +
                "company_profiles.location_uuid as locationUUID," +
                "company_profiles.country_uuid as countryUUID,company_profiles.city_uuid as cityUUID," +
                "company_profiles.currency_uuid as currencyUUID,company_profiles.state_uuid as stateUUID," +
                "companies.created_by as createdBy ,companies.created_at as createdAt," +
                "companies.updated_by as updatedBy,companies.updated_at as updatedAt," +
                "companies.req_branch_uuid as reqBranchUUID, " +
                "companies.req_created_browser as reqCreatedBrowser,companies.req_created_ip as reqCreatedIP," +
                "companies.req_created_port as reqCreatedPort,companies.req_created_os as reqCreatedOS," +
                "companies.req_created_device as reqCreatedDevice,companies.req_created_referer as reqCreatedReferer," +
                "companies.req_updated_browser as reqUpdatedBrowser,companies.req_updated_ip as reqUpdatedIP," +
                "companies.req_updated_port as reqUpdatedPort,companies.req_updated_os as reqUpdatedOS," +
                "companies.req_updated_device as reqUpdatedDevice,companies.req_updated_referer as reqUpdatedReferer," +
                "companies.archived as archived,companies.editable as editable,companies.deletable as deletable\n" +
                "from companies\n" +
                "left join company_profiles on company_profiles.uuid=companies.company_profile_uuid \n" +
                "where company_profiles.deleted_at is null and companies.deleted_at is null \n" +
                "and (companies.name ILIKE  '%" + name + "%' or companies.description ILIKE  '%" + description + "%') " +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCompanyWithCompanyProfileMapper mapper = new SlaveCustomCompanyWithCompanyProfileMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCompanyWithCompanyProfileDto))
                .all();
    }

    @Override
    public Flux<SlaveCompanyWithCompanyProfileDto> CompanyWithCompanyProfileIndexWithStatusFilter(String name, String description, Boolean status, Integer size, Long page, String dp, String d) {
        String query = "select  companies.id as id,companies.uuid as uuid, companies.version as version,companies.status as status" +
                ",companies.name as name,companies.description as description, companies.company_profile_uuid," +
                "companies.doc_image as CompanyImageID,\n" +
                "companies.uuid as CompanyUUID, \n" +
                "company_profiles.establishment_date as establishmentDate,\n" +
                "company_profiles.language_uuid as languageUUID," +
                "company_profiles.location_uuid as locationUUID," +
                "company_profiles.country_uuid as countryUUID,company_profiles.city_uuid as cityUUID," +
                "company_profiles.currency_uuid as currencyUUID,company_profiles.state_uuid as stateUUID," +
                "companies.created_by as createdBy ,companies.created_at as createdAt," +
                "companies.updated_by as updatedBy,companies.updated_at as updatedAt," +
                "companies.req_branch_uuid as reqBranchUUID, " +
                "companies.req_created_browser as reqCreatedBrowser,companies.req_created_ip as reqCreatedIP," +
                "companies.req_created_port as reqCreatedPort,companies.req_created_os as reqCreatedOS," +
                "companies.req_created_device as reqCreatedDevice,companies.req_created_referer as reqCreatedReferer," +
                "companies.req_updated_browser as reqUpdatedBrowser,companies.req_updated_ip as reqUpdatedIP," +
                "companies.req_updated_port as reqUpdatedPort,companies.req_updated_os as reqUpdatedOS," +
                "companies.req_updated_device as reqUpdatedDevice,companies.req_updated_referer as reqUpdatedReferer," +
                "companies.archived as archived,companies.editable as editable,companies.deletable as deletable\n" +
                "from companies\n" +
                "left join company_profiles on company_profiles.uuid = companies.company_profile_uuid \n" +
                "where company_profiles.deleted_at is null" +
                " and companies.deleted_at is null \n" +
                " and companies.status = " + status +
                " and (companies.name ILIKE  '%" + name + "%' " +
                "or companies.description ILIKE  '%" + description + "%') " +
                "order by " + dp + " " + d + " limit " + size + " offset " + page;

        SlaveCustomCompanyWithCompanyProfileMapper mapper = new SlaveCustomCompanyWithCompanyProfileMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCompanyWithCompanyProfileDto))
                .all();
    }

    @Override
    public Mono<SlaveCompanyWithCompanyProfileDto> ShowByUuidCompanyWithCompanyProfile(UUID uuid) {
        String query = "select  companies.id as id, companies.uuid as uuid,companies.version as version, companies.status as status" +
                ",companies.name as name,companies.description as description,companies.company_profile_uuid,\n" +
                "companies.doc_image as CompanyImageID,\n" +
                "companies.uuid as CompanyUUID, \n" +
                "company_profiles.establishment_date as establishmentDate,\n" +
                "company_profiles.language_uuid as languageUUID,\n" +
                "company_profiles.location_uuid as locationUUID," +
                "company_profiles.country_uuid as countryUUID,company_profiles.city_uuid as cityUUID," +
                "company_profiles.currency_uuid as currencyUUID,company_profiles.state_uuid as stateUUID," +
                "companies.created_by as createdBy ,companies.created_at as createdAt," +
                "companies.updated_by as updatedBy,companies.updated_at as updatedAt," +
                "companies.req_branch_uuid as reqBranchUUID, " +
                "companies.req_created_browser as reqCreatedBrowser,companies.req_created_ip as reqCreatedIP," +
                "companies.req_created_port as reqCreatedPort,companies.req_created_os as reqCreatedOS," +
                "companies.req_created_device as reqCreatedDevice,companies.req_created_referer as reqCreatedReferer," +
                "companies.req_updated_browser as reqUpdatedBrowser,companies.req_updated_ip as reqUpdatedIP," +
                "companies.req_updated_port as reqUpdatedPort,companies.req_updated_os as reqUpdatedOS," +
                "companies.req_updated_device as reqUpdatedDevice,companies.req_updated_referer as reqUpdatedReferer," +
                "companies.archived as archived,companies.editable as editable,companies.deletable as deletable\n" +
                "from companies\n" +
                "left join company_profiles on company_profiles.uuid=companies.company_profile_uuid \n" +
                "where company_profiles.deleted_at is null and companies.deleted_at is null " +
                "and companies.uuid ='" + uuid +
                "'";

        SlaveCustomCompanyWithCompanyProfileMapper mapper = new SlaveCustomCompanyWithCompanyProfileMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCompanyWithCompanyProfileDto)).one();
    }

}

