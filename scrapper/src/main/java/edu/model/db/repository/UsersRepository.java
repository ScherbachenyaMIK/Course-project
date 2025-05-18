package edu.model.db.repository;

import edu.model.db.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsersRepository extends JpaRepository<User, Long> {
    @NotNull
    User save(@NotNull User user);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User findUserByEmail(String email);

    User findUserByUsername(String username);

    User findUserById(Long id);
}
