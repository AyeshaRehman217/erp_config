package tuf.webscaf.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;

import static io.r2dbc.pool.PoolingConnectionFactoryProvider.MAX_SIZE;
import static io.r2dbc.spi.ConnectionFactoryOptions.*;


@PropertySource("classpath:application.properties")
@SpringBootApplication(exclude = SqlInitializationAutoConfiguration.class)
@EnableR2dbcRepositories(basePackages = "tuf.webscaf.app.dbContext.master", entityOperationsRef = "masterR2dbcEntityOperations")
@Configuration
public class MasterKernal {

    @Value("${master.data.postgres.driver}") private String driver;
    @Value("${master.data.postgres.protocol}") private String protocol;
    @Value("${master.data.postgres.host}") private String host;
    @Value("${master.data.postgres.port}") private int port;
    @Value("${master.data.postgres.user}") private String user;
    @Value("${master.data.postgres.password}") private String password;
    @Value("${master.data.postgres.database}") private String database;
    @Value("${master.data.postgres.maxconectionpoolsize}") private int maxconectionpoolsize;

    @Bean
    @Qualifier("master")
    public ConnectionFactory masterConnectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER,this.driver)
                .option(PROTOCOL,this.protocol)
                .option(HOST, this.host)
                .option(PORT, this.port)
                .option(USER, this.user)
                .option(PASSWORD, this.password)
                .option(DATABASE, this.database)
                .option(MAX_SIZE, this.maxconectionpoolsize)
                .build());
    }

    @Bean
    @Qualifier("masterR2dbcEntityOperations")
    public R2dbcEntityOperations masterR2dbcEntityOperations(@Qualifier("master") ConnectionFactory connectionFactory) {

        DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(PostgresDialect.INSTANCE);
        DatabaseClient databaseClient = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
//                .dataAccessStrategy(strategy)
                .build();

        return new R2dbcEntityTemplate(databaseClient, strategy);
    }


}
