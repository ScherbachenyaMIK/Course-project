package edu.service;

import edu.model.db.entity.User;
import edu.model.db.repository.UsersRepository;
import edu.model.web.request.RegisterRequest;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsersService {
    static private final String NONE_ROLE = "NONE";
    static private final String NOT_CONFIRMED = "NOT_CONFIRMED";
    static private final String USER_ROLE = "USER";

    @Autowired
    private UsersRepository repository;

    @Transactional
    public boolean registerNewUser(RegisterRequest request) {
        User newUser = new User();
        bindingUserFields(newUser, request);
        if (repository.existsByUsername(request.username())
                || repository.existsByEmail(request.email())) {
            return false;
        }
        try {
            repository.save(newUser);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String checkAuthAndRoleByEmail(String email, String password) {
        User entry = repository.findUserByEmail(email);
        if (entry == null) {
            return NONE_ROLE;
        }
        if (password.equals(entry.getPasswordHash())) {
            return entry.getUserRole();
        }
        return NONE_ROLE;
    }

    public String checkAuthAndRoleByUsername(String username, String password) {
        User entry = repository.findUserByUsername(username);
        if (entry == null) {
            return NONE_ROLE;
        }
        if (password.equals(entry.getPasswordHash())) {
            return entry.getUserRole();
        }
        return NONE_ROLE;
    }

    public User findUserById(Long id) {
        return repository.findUserById(id);
    }

    public User findUserByUsername(String username) {
        return repository.findUserByUsername(username);
    }

    @Transactional
    public boolean confirmEmail(String username) {
        User user = repository.findUserByUsername(username);
        if (user == null) {
            return false;
        }
        if (!NOT_CONFIRMED.equals(user.getUserRole())) {
            return true;
        }
        user.setUserRole(USER_ROLE);
        repository.save(user);
        return true;
    }

    public boolean isExistsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    public boolean isExistsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    private void bindingUserFields(User newUser, RegisterRequest request) {
        newUser.setName(request.name());
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPasswordHash(request.passwordHash());
        newUser.setBirthDate(request.date() != null ? LocalDateTime.of(
                request.date(), LocalTime.MIN) : null
        );
        newUser.setSex(request.sex());
        newUser.setUserRole(NOT_CONFIRMED);
        newUser.setDescription("");
    }
}
