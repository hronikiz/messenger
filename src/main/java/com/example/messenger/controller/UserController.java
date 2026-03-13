package com.example.messenger.controller;

import com.example.messenger.dto.UserDTO;
import com.example.messenger.model.User;
import com.example.messenger.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public User createUser(@Valid @RequestBody UserDTO userDTO) {

        User user = new User(
                null,
                userDTO.getUsername(),
                userDTO.getNickname(),
                userDTO.getEmail(),
                userDTO.getPassword()
        );

        return userRepository.save(user);
    }

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAllUsers();
    }

}