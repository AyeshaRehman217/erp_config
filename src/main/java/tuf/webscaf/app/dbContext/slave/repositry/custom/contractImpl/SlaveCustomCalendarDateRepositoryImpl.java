package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.entity.SlaveCalendarDateEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomCalendarDateRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomCalendarDateMapper;


public class SlaveCustomCalendarDateRepositoryImpl implements SlaveCustomCalendarDateRepository {
    private DatabaseClient client;
    private SlaveCalendarDateEntity slaveCalendarDateEntity;

    @Autowired
    public SlaveCustomCalendarDateRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveCalendarDateEntity> showAllRecordsWithSearchFilter(String date, String monthName, String dayName, String year, String dp, String d, Integer size, Long page) {
        String query = "SELECT calendar_dates.* FROM calendar_dates\n" +
                "WHERE calendar_dates.deleted_at IS NULL\n" +
                "AND (calendar_dates.date::text ILIKE '%" + date + "%'\n" +
                "OR calendar_dates.month_name ILIKE '%" + monthName + "%'\n" +
                "OR calendar_dates.day_name ILIKE '%" + dayName + "%'\n" +
                "OR calendar_dates.year::text ILIKE '%" + year + "%')" +
                "ORDER BY calendar_dates." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomCalendarDateMapper mapper = new SlaveCustomCalendarDateMapper();

        return client.sql(query)
                .map(row -> mapper.apply(row, slaveCalendarDateEntity))
                .all();
    }

}

