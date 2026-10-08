package com.roofandwall.interior.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpVerificationEmailSender implements VerificationEmailSender {

    private final JavaMailSender sender;
    private final String from;
    private final String apiKey;

    public SmtpVerificationEmailSender(JavaMailSender sender,
                                       @Value("${app.mail-from}") String from,
                                       @Value("${spring.mail.password}") String apiKey) {
        this.sender = sender;
        this.from = from;
        this.apiKey = apiKey;
    }

    @Override
    public void send(String recipient, String verificationUrl) {
        if (from.isBlank() || apiKey.isBlank()) {
            throw new IllegalStateException("이메일 발송 설정이 완료되지 않았습니다.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(recipient);
        message.setSubject("[지붕과벽] 이메일 주소 확인");
        message.setText("회원가입을 완료하려면 아래 링크를 열고 이메일 확인 버튼을 눌러 주세요.\n"
            + "링크는 24시간 동안 유효합니다.\n\n" + verificationUrl
            + "\n\n요청한 적이 없다면 이 메일을 무시해 주세요.");
        sender.send(message);
    }
}
