package com.barunsigong.interior.auth.service;

import com.barunsigong.interior.auth.model.Role;
import com.barunsigong.interior.auth.model.EmailVerificationToken;
import com.barunsigong.interior.auth.model.StaffAccount;
import com.barunsigong.interior.auth.model.UsernameLookupAttempt;
import com.barunsigong.interior.auth.repository.StaffAccountRepository;
import com.barunsigong.interior.auth.repository.EmailVerificationTokenRepository;
import com.barunsigong.interior.auth.repository.UsernameLookupAttemptRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountRecoveryService {

    private final StaffAccountRepository accounts;
    private final UsernameLookupAttemptRepository lookupAttempts;
    private final EmailVerificationTokenRepository verificationTokens;
    private final VerificationEmailSender verificationEmailSender;
    private final PasswordEncoder encoder;
    private final Long companyId;
    private final String publicBaseUrl;
    private final SecureRandom random = new SecureRandom();

    public AccountRecoveryService(StaffAccountRepository accounts, UsernameLookupAttemptRepository lookupAttempts,
                                  EmailVerificationTokenRepository verificationTokens,
                                  VerificationEmailSender verificationEmailSender, PasswordEncoder encoder,
                                  @Value("${app.company-id}") Long companyId,
                                  @Value("${app.public-base-url}") String publicBaseUrl) {
        this.accounts = accounts;
        this.lookupAttempts = lookupAttempts;
        this.verificationTokens = verificationTokens;
        this.verificationEmailSender = verificationEmailSender;
        this.encoder = encoder;
        this.companyId = companyId;
        this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
    }

    public boolean usernameAvailable(String username) {
        String value = validUsername(username);
        return !accounts.existsByUsernameIgnoreCase(value);
    }

    @Transactional
    public void register(String name, String phone, String email, String username, String password) {
        String validName = validName(name);
        String validPhone = validPhone(phone);
        String validEmail = validEmail(email);
        String validUsername = validUsername(username);
        if (password == null || password.length() < 4 || password.length() > 72) {
            throw new IllegalArgumentException("비밀번호는 4~72자로 입력해 주세요.");
        }
        if (accounts.existsByUsernameIgnoreCase(validUsername)) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (accounts.existsByEmailIgnoreCase(validEmail)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        StaffAccount account = new StaffAccount(companyId, validUsername, encoder.encode(password), validName, Role.OFFICE);
        account.setPhone(validPhone);
        account.requireEmailVerification(validEmail);
        try {
            accounts.saveAndFlush(account);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("이미 사용 중인 아이디 또는 이메일입니다.", ex);
        }
        issueVerificationEmail(account);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        String normalized;
        try {
            normalized = validEmail(email);
        } catch (IllegalArgumentException ignored) {
            return;
        }
        accounts.findByEmailIgnoreCase(normalized)
            .filter(account -> account.getCompanyId().equals(companyId) && account.getEmailVerifiedAt() == null)
            .ifPresent(account -> {
                long recent = verificationTokens.countByCompanyIdAndStaffAccountIdAndCreatedAtAfter(
                    companyId, account.getId(), Instant.now().minus(1, ChronoUnit.HOURS)
                );
                if (recent >= 3) return;
                verificationTokens.findByCompanyIdAndStaffAccountIdAndUsedAtIsNull(companyId, account.getId())
                    .forEach(EmailVerificationToken::consume);
                issueVerificationEmail(account);
            });
    }

    private void issueVerificationEmail(StaffAccount account) {
        byte[] secret = new byte[32];
        random.nextBytes(secret);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        verificationTokens.saveAndFlush(new EmailVerificationToken(
            companyId, account.getId(), sha256(token), Instant.now().plus(24, ChronoUnit.HOURS)
        ));
        verificationEmailSender.send(account.getEmail(), publicBaseUrl + "/auth/verify-email?token=" + token);
    }

    @Transactional(readOnly = true)
    public boolean canVerifyEmail(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) return false;
        return verificationTokens.findByTokenHash(sha256(token))
            .filter(t -> t.getCompanyId().equals(companyId) && t.isUsable())
            .isPresent();
    }

    @Transactional
    public void verifyEmail(String token) {
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) {
            throw new IllegalArgumentException("유효하지 않은 인증 링크입니다.");
        }
        EmailVerificationToken verification = verificationTokens.findByTokenHash(sha256(token))
            .filter(t -> t.getCompanyId().equals(companyId) && t.isUsable())
            .orElseThrow(() -> new IllegalArgumentException("인증 링크가 만료되었거나 이미 사용되었습니다."));
        StaffAccount account = accounts.findById(verification.getStaffAccountId())
            .filter(a -> a.getCompanyId().equals(companyId))
            .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
        if (account.getEmailVerifiedAt() != null) {
            throw new IllegalArgumentException("이미 확인된 이메일입니다.");
        }
        verification.consume();
        account.verifyEmail();
    }

    public record UsernameLookupResult(String message, boolean limited, boolean found) {}

    @Transactional
    public UsernameLookupResult findUsername(String name, String phone, String clientIp) {
        String ip = clientIp == null || clientIp.isBlank() ? "unknown" : clientIp;
        String key = lookupKey(name, phone);
        Instant windowStart = Instant.now().minus(1, ChronoUnit.HOURS);
        long ipAttempts = lookupAttempts.countByCompanyIdAndClientIpAndCreatedAtAfter(
            companyId, ip, windowStart
        );
        if (ipAttempts >= 30) {
            lookupAttempts.save(new UsernameLookupAttempt(companyId, ip, key, UsernameLookupAttempt.Outcome.BLOCKED));
            return new UsernameLookupResult("조회 요청이 많습니다. 잠시 후 다시 시도하거나 관리자에게 문의해 주세요.", true, false);
        }
        long targetFailures = lookupAttempts.countByCompanyIdAndLookupKeyAndOutcomeAndCreatedAtAfter(
            companyId, key, UsernameLookupAttempt.Outcome.FAILED, windowStart
        );
        if (targetFailures >= 3) {
            lookupAttempts.save(new UsernameLookupAttempt(companyId, ip, key, UsernameLookupAttempt.Outcome.BLOCKED));
            return new UsernameLookupResult("조회에 3회 이상 실패했습니다. 관리자에게 문의해 주세요.", true, false);
        }
        try {
            String username = accounts.findFirstByCompanyIdAndDisplayNameAndPhoneOrderByIdAsc(
                companyId, validName(name), validPhone(phone)
            ).map(StaffAccount::getUsername).orElse(null);
            if (username != null) {
                lookupAttempts.save(new UsernameLookupAttempt(companyId, ip, key, UsernameLookupAttempt.Outcome.FOUND));
                int visibleCharacters = Math.min(2, username.length() - 1);
                return new UsernameLookupResult("아이디는 " + username.substring(0, visibleCharacters)
                    + "*".repeat(username.length() - visibleCharacters) + "입니다.", false, true);
            }
        } catch (IllegalArgumentException ignored) {
            // Invalid lookup input counts as a failed attempt as well.
        }
        lookupAttempts.save(new UsernameLookupAttempt(companyId, ip, key, UsernameLookupAttempt.Outcome.FAILED));
        if (targetFailures == 2) {
            return new UsernameLookupResult("조회에 3회 이상 실패했습니다. 관리자에게 문의해 주세요.", true, false);
        }
        return new UsernameLookupResult("입력한 정보를 확인해 주세요.", false, false);
    }

    private String lookupKey(String name, String phone) {
        String normalizedName = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        String normalizedPhone = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        return sha256(normalizedName + "\u0000" + normalizedPhone);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    private String validEmail(String email) {
        String value = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !value.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")) {
            throw new IllegalArgumentException("올바른 이메일 주소를 입력해 주세요.");
        }
        return value;
    }

    private String validName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.length() < 2 || value.length() > 50) {
            throw new IllegalArgumentException("이름은 2~50자로 입력해 주세요.");
        }
        return value;
    }

    private String validPhone(String phone) {
        String value = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (!value.matches("[0-9]{10,11}")) {
            throw new IllegalArgumentException("전화번호는 숫자 10~11자리로 입력해 주세요.");
        }
        return value;
    }

    private String validUsername(String username) {
        String value = username == null ? "" : username.trim();
        if (!value.matches("[A-Za-z0-9._@-]{4,254}")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, . _ @ -를 사용해 4~254자로 입력해 주세요.");
        }
        return value.toLowerCase(Locale.ROOT);
    }
}
