package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveBranchWithBranchProfileDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveDocBucketRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomBranchWithBranchProfileRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomDocBucketRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomBranchWithBranchProfileMapper;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomDocBucketMapper;


public class SlaveCustomDocBucketRepositoryImpl implements SlaveCustomDocBucketRepository {
    private DatabaseClient client;
    private SlaveDocBucketEntity slaveDocBucketEntity;

    @Autowired
    public SlaveCustomDocBucketRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveDocBucketEntity> docBucketIndex(String name, String description, Integer size, Long page, String dp, String d) {
        String query = "select * from doc_buckets "+
                "where doc_buckets.deleted_at is null " +
                "and (doc_buckets.name ILIKE  '%"+name+"%' or doc_buckets.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d+" limit "+size+" offset "+page;


        SlaveCustomDocBucketMapper mapper = new SlaveCustomDocBucketMapper();

        Flux<SlaveDocBucketEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveDocBucketEntity))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveDocBucketEntity> docBucketIndexCount(String name, String description, String dp, String d) {
        String query = "select * from doc_buckets "+
                "where doc_buckets.deleted_at is null " +
                "and (doc_buckets.name ILIKE  '%"+name+"%' or doc_buckets.description ILIKE  '%"+description+"%') " +
                "order by "+dp+" "+d;


        SlaveCustomDocBucketMapper mapper = new SlaveCustomDocBucketMapper();

        Flux<SlaveDocBucketEntity> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveDocBucketEntity))
                .all();

        return result;
    }
}

