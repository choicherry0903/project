package com.barunsigong.interior.auth.service;

public interface VerificationEmailSender {
    void send(String recipient, String verificationUrl);
}
