package edu.service;

import edu.model.db.entity.User;
import edu.model.db.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsersService {
    static private final String NONE_ROLE = "NONE";

    @Autowired
    private UsersRepository repository;

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
}
