package edu.service;

import edu.PostgreIntegrationTest;
import edu.cofiguration.NoKafkaConfig;
import edu.model.db.entity.User;
import edu.model.web.request.RegisterRequest;
import java.sql.Timestamp;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(NoKafkaConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsersServiceTest extends PostgreIntegrationTest {
    @Autowired
    private UsersService service;
    private final User user = User.builder()
            .username("testUsername")
            .name("testName")
            .email("test@mail.by")
            .passwordHash("VerySecretPassword")
            .userRole("USER")
            .sex('M')
            .birthDate(new Timestamp(954882000))
            .build();

    private final RegisterRequest request = new RegisterRequest(
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getPasswordHash(),
            user.getSex(),
            user.getBirthDate().toLocalDateTime().toLocalDate()
    );

    @Test
    @Order(1)
    void registerNewUser() {
        assertThat(service.registerNewUser(request)).isTrue();
    }

    @Test
    @Order(2)
    void registerNewUserTwice() {
        assertThat(service.registerNewUser(request)).isFalse();
    }

    @Test
    @Order(3)
    void checkAuthAndRoleByEmail() {
        assertThat(service.checkAuthAndRoleByEmail(
                "test@mail.by",
                "VerySecretPassword"
        )).isEqualTo("NOT_CONFIRMED");
    }

    @Test
    @Order(4)
    void checkAuthAndRoleByEmailNotExists() {
        assertThat(service.checkAuthAndRoleByEmail(
                "test",
                "VerySecretPassword"
        )).isEqualTo("NONE");
    }

    @Test
    @Order(5)
    void checkAuthAndRoleByEmailIncorrectPassword() {
        assertThat(service.checkAuthAndRoleByEmail(
                "test@mail.by",
                "IncorrectPassword"
        )).isEqualTo("NONE");
    }

    @Test
    @Order(6)
    void checkAuthAndRoleByUsername() {
        assertThat(service.checkAuthAndRoleByUsername(
                "testUsername",
                "VerySecretPassword"
        )).isEqualTo("NOT_CONFIRMED");
    }

    @Test
    @Order(7)
    void checkAuthAndRoleByUsernameNotExists() {
        assertThat(service.checkAuthAndRoleByUsername(
                "test@mail.ru",
                "VerySecretPassword"
        )).isEqualTo("NONE");
    }

    @Test
    @Order(8)
    void checkAuthAndRoleByUsernameIncorrectPassword() {
        assertThat(service.checkAuthAndRoleByUsername(
                "testUsername",
                "IncorrectPassword"
        )).isEqualTo("NONE");
    }

    @Test
    @Order(9)
    void isExistsByUsername() {
        assertThat(service.isExistsByUsername("testUsername")).isTrue();
    }

    @Test
    @Order(10)
    void isExistsByUsernameNotExists() {
        assertThat(service.isExistsByUsername("test@mail.by")).isFalse();
    }

    @Test
    @Order(11)
    void isExistsByEmail() {
        assertThat(service.isExistsByEmail("test@mail.by")).isTrue();
    }

    @Test
    @Order(12)
    void isExistsByEmailNotExists() {
        assertThat(service.isExistsByEmail("testUsername")).isFalse();
    }
}