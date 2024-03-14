package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;

import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarDateEntity;

public interface SlaveCustomCalendarDateRepository {
    Flux<SlaveCalendarDateEntity> showAllRecordsWithSearchFilter(String date, String monthName, String dayName, String year, String dp, String d, Integer size, Long page);

}
