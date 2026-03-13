package com.example.messenger.repository;

import com.example.messenger.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class UserRepository {
    private final List<User> users = new ArrayList<>();
    private Long nextId = 1L;

    public User save(User user) {
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    public List<User> findAllUsers(){
        return users;
    }
}
