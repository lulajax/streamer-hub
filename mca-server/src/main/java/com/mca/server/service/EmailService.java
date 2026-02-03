package com.mca.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from:noreply@mca-app.com}")
    private String fromEmail;
    
    @Value("${mca.app.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationUrl = frontendUrl + "/verify-email?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify Your MCA Account");
            message.setText(buildVerificationEmailBody(toEmail, verificationUrl));
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset Your MCA Password");
            message.setText(buildPasswordResetEmailBody(toEmail, resetUrl));
            
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }
    
    @Async
    public void sendActivationSuccessEmail(String toEmail, String subscriptionType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to MCA Premium!");
            message.setText(buildActivationSuccessEmailBody(toEmail, subscriptionType));
            
            mailSender.send(message);
            log.info("Activation success email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send activation success email to: {}", toEmail, e);
        }
    }
    
    private String buildVerificationEmailBody(String email, String verificationUrl) {
        return String.format("""
            Hello,
            
            Thank you for registering with MCA (Multi-Caster Assistant)!
            
            Please click the link below to verify your email address:
            %s
            
            This link will expire in 24 hours.
            
            If you did not create an account, please ignore this email.
            
            Best regards,
            The MCA Team
            """, verificationUrl);
    }
    
    private String buildPasswordResetEmailBody(String email, String resetUrl) {
        return String.format("""
            Hello,
            
            We received a request to reset your MCA account password.
            
            Please click the link below to reset your password:
            %s
            
            This link will expire in 24 hours.
            
            If you did not request a password reset, please ignore this email.
            
            Best regards,
            The MCA Team
            """, resetUrl);
    }
    
    private String buildActivationSuccessEmailBody(String email, String subscriptionType) {
        return String.format("""
            Hello,
            
            Congratulations! Your MCA Premium subscription has been activated successfully.
            
            Subscription Type: %s
            
            You now have access to all premium features including:
            - Unlimited live stream connections
            - Advanced analytics and reporting
            - Priority support
            - Custom widgets and themes
            
            Thank you for choosing MCA!
            
            Best regards,
            The MCA Team
            """, subscriptionType);
    }
}
