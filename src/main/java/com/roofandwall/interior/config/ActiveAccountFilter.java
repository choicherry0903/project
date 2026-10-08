package com.roofandwall.interior.config;

import com.roofandwall.interior.auth.repository.StaffAccountRepository;
import com.roofandwall.interior.auth.service.StaffPrincipal;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Recheck active status and role on every authenticated request, including existing sessions. */
public class ActiveAccountFilter extends OncePerRequestFilter {

    private final StaffAccountRepository accounts;

    public ActiveAccountFilter(StaffAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof StaffPrincipal principal) {
            var account = accounts.findById(principal.getAccountId());
            boolean valid = account
                .filter(
                    a ->
                        a.isActive() &&
                        (a.getEmail() == null || a.getEmailVerifiedAt() != null) &&
                        a.getCompanyId().equals(principal.getCompanyId()) &&
                        principal
                            .getAuthorities()
                            .stream()
                            .anyMatch(role -> role.getAuthority().equals("ROLE_" + a.getRole().name()))
                )
                .isPresent();
            if (!valid) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login?expired");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
