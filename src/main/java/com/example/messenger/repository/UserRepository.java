package com.example.messenger.repository;

import com.example.messenger.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private final List<User> users = new ArrayList<>();
    private Long nextId = 1L;

    public User save(User user) {
        for (User u : users) {
            if (u.getEmail().equals(user.getEmail())){
                throw new RuntimeException("Email уже используется");
            }
            if (u.getNickname().equals(user.getNickname())){
                throw new RuntimeException("Nickname уже используется");
            }
        }
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    public List<User> findAllUsers() {
        return users;
    }

}