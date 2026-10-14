package com.roofandwall.interior;

import com.roofandwall.interior.auth.service.StaffPrincipal;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

final class TestStaff {
    private TestStaff() {}

    static RequestPostProcessor signedIn() {
        return user(new StaffPrincipal(1, 1, "test-office", "unused-test-hash", "테스트 직원", "OFFICE", true));
    }
}
