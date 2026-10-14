package com.roofandwall.interior;

import static org.assertj.core.api.Assertions.assertThat;
import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

class EstimateMigrationUpgradeTest {
    @Test void appliedV12RemainsValidAndV13AddsIncompleteFields() throws Exception {
        String url = "jdbc:h2:mem:estimate_upgrade_" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").target("12").load().migrate();
        try (var connection = DriverManager.getConnection(url, "sa", ""); var statement = connection.createStatement()) {
            try (var result = statement.executeQuery("select checksum from \"flyway_schema_history\" where \"version\" = '12'")) {
                assertThat(result.next()).isTrue();
                // This is the checksum already applied to the user's database.
                assertThat(result.getInt(1)).isEqualTo(-796633756);
            }
            var flyway = Flyway.configure().dataSource(url, "sa", "").locations("classpath:db/migration").load();
            assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
            try (var result = statement.executeQuery("select count(*) from information_schema.columns where table_name = 'estimate_items' and column_name in ('quantity_missing', 'unit_price_missing')")) {
                result.next(); assertThat(result.getInt(1)).isEqualTo(2);
            }
            flyway.validate();
        }
    }
}
