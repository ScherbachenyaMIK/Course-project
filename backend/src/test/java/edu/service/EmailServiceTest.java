package edu.service;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() throws Exception {
        emailService = new EmailService();
    }

    @Test
    void escapeHandlesNull() throws Exception {
        Method escape = EmailService.class.getDeclaredMethod("escape", String.class);
        escape.setAccessible(true);

        String result = (String) escape.invoke(emailService, (Object) null);

        assertThat(result).isEmpty();
    }

    @Test
    void escapeHandlesHtmlChars() throws Exception {
        Method escape = EmailService.class.getDeclaredMethod("escape", String.class);
        escape.setAccessible(true);

        String result = (String) escape.invoke(emailService, "<script>alert('xss')&</script>");

        assertThat(result).doesNotContain("<");
        assertThat(result).doesNotContain(">");
        assertThat(result).contains("&amp;");
        assertThat(result).contains("&lt;");
        assertThat(result).contains("&gt;");
    }

    @Test
    void escapeHandlesPlainText() throws Exception {
        Method escape = EmailService.class.getDeclaredMethod("escape", String.class);
        escape.setAccessible(true);

        String result = (String) escape.invoke(emailService, "hello world");

        assertThat(result).isEqualTo("hello world");
    }
}
