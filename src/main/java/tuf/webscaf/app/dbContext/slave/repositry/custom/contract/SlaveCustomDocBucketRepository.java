package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.DocBucketEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveDocBucketEntity;

@Repository
public interface SlaveCustomDocBucketRepository {

     Flux<SlaveDocBucketEntity> docBucketIndex(String name, String description, Integer size, Long page, String dp, String d);
     Flux<SlaveDocBucketEntity> docBucketIndexCount(String name, String description, String dp, String d);
//    public Mono<SlaveDocBucketEntity> ShowBranchWithBranchProfile(Long id);
}
