package edu.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import edu.configuration.ResendProperties;
import edu.security.JwtProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {
    @Autowired
    private ResendProperties resendProperties;

    @Autowired
    private JwtProvider jwtProvider;

    private Resend resend;

    @PostConstruct
    void init() {
        this.resend = new Resend(resendProperties.apiKey());
    }

    public void sendConfirmationEmail(String username, String email) {
        String token = jwtProvider.generateEmailConfirmationToken(
                username,
                resendProperties.confirmation().expiration().toMillis()
        );
        String link = resendProperties.confirmation().baseUrl() + "/confirm?token=" + token;
        String html = "<p>Hi " + escape(username) + ",</p>"
                + "<p>Please confirm your email by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">Confirm email</a></p>"
                + "<p>If you did not create an account, you can ignore this message.</p>";

        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(resendProperties.from())
                .to(email)
                .subject("Confirm your account")
                .html(html)
                .build();

        try {
            resend.emails().send(request);
            log.info("Confirmation email sent to {}", email);
        } catch (ResendException e) {
            log.error("Failed to send confirmation email to {}: {}", email, e.getMessage());
        }
    }

    private String escape(String raw) {
        return raw == null ? "" : raw
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
