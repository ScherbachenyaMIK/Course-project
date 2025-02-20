package edu.service;

import edu.PostgreIntegrationTest;
import edu.cofiguration.NoKafkaConfig;
import edu.model.db.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

//TODO fill with tests
@SpringBootTest
@Import(NoKafkaConfig.class)
class UsersServiceTest extends PostgreIntegrationTest {
    @Autowired
    private UsersService service;
    private final User user;
    {
        user = User.builder()
                .username("testUsername")
                .name("testName")
                .email("test@mail.by")
                .passwordHash("VerySecretPassword")
                .userRole("USER")
                .sex('M')
                .build();
    }

    @Test
    void checkAuthAndRoleByEmail() {
    }

    @Test
    void checkAuthAndRoleByUsername() {
    }
}