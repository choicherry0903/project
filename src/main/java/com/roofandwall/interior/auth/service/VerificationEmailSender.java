package com.roofandwall.interior.auth.service;

public interface VerificationEmailSender {
    void send(String recipient, String verificationUrl);
}
