package com.roofandwall.interior;

import com.roofandwall.interior.customer.dto.CustomerItem;
import com.roofandwall.interior.customer.service.CustomerQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class
}, inheritListeners = false)
@Transactional
class CustomerInsertIntegrationTest {

    @Autowired
    private CustomerQueryService service;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void insertsCustomerWithCompanyAndRequiredFields() {
        var form = new CustomerItem();
        form.setCustomerName("김'고객");
        form.setCustomerPhone("010-1234-5678");
        form.setRequestKey(UUID.randomUUID().toString());
        form.setProjectName("테스트 현장");
        form.setSiteAddress("서울");

        service.insertCustomer(7L, 1L, form);

        var customer = jdbc.queryForMap("SELECT * FROM interior_cms.customers WHERE company_id = 7");
        assertThat(((Number) customer.get("id")).longValue()).isPositive();
        assertThat(customer.get("name")).isEqualTo("김'고객");
        assertThat(customer.get("phone")).isEqualTo("010-1234-5678");
        assertThat(((Number) customer.get("version")).longValue()).isZero();
        assertThat(customer.get("created_at")).isNotNull();
        assertThat(customer.get("updated_at")).isNotNull();
        assertThat(customer.get("archived")).isEqualTo(false);
        assertThat(UUID.fromString((String) customer.get("request_key"))).isNotNull();
    }
}
