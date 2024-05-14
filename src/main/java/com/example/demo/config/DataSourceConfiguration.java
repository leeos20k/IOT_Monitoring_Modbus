package com.example.demo.config;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfiguration {

    @Bean(name = "dataSource")
    public HikariDataSource dataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:log4jdbc:mysql://localhost:3306/tb_s050?allowMultiQueries=true");
        hikariDataSource.setDriverClassName("net.sf.log4jdbc.sql.jdbcapi.DriverSpy");
        hikariDataSource.setUsername("root");
        hikariDataSource.setPassword("1234");
        hikariDataSource.setMaximumPoolSize(60);

        return hikariDataSource;
    }
}
