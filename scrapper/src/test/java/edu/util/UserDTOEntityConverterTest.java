package edu.util;

import edu.model.db.entity.User;
import edu.model.web.dto.UserDTO;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDTOEntityConverterTest {

    @Test
    void convertsUserWithBirthDate() {
        User user = User.builder()
                .id(1L)
                .username("alice")
                .name("Alice")
                .email("a@b.com")
                .sex('F')
                .birthDate(LocalDateTime.of(1990, 1, 2, 0, 0))
                .registrationDate(LocalDateTime.of(2026, 1, 1, 0, 0))
                .userRole("USER")
                .description("bio")
                .articles(Collections.emptyList())
                .build();

        UserDTO dto = UserDTOEntityConverter.convert(user);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.username()).isEqualTo("alice");
        assertThat(dto.nativeName()).isEqualTo("Alice");
        assertThat(dto.email()).isEqualTo("a@b.com");
        assertThat(dto.sex()).isEqualTo('F');
        assertThat(dto.birthDate()).isNotNull();
        assertThat(dto.registrationDate()).isNotNull();
        assertThat(dto.role()).isEqualTo("USER");
        assertThat(dto.description()).isEqualTo("bio");
        assertThat(dto.articles()).isEmpty();
    }

    @Test
    void convertsUserWithoutBirthDate() {
        User user = User.builder()
                .id(2L)
                .username("bob")
                .name("Bob")
                .email("b@c.com")
                .registrationDate(LocalDateTime.of(2026, 1, 1, 0, 0))
                .articles(Collections.emptyList())
                .build();

        UserDTO dto = UserDTOEntityConverter.convert(user);

        assertThat(dto.birthDate()).isNull();
        assertThat(dto.username()).isEqualTo("bob");
    }

    @Test
    void emptyDTOHasNullFields() {
        UserDTO dto = UserDTOEntityConverter.emptyDTO();

        assertThat(dto.id()).isNull();
        assertThat(dto.username()).isNull();
    }
}
