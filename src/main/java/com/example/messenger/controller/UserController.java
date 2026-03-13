package com.example.messenger.controller;

import com.example.messenger.dto.UserCreateDTO;
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
    public UserDTO createUser(@Valid @RequestBody UserCreateDTO dto) {

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email уже используется");
        }
        if (userRepository.findByNickname(dto.getNickname()).isPresent()) {
            throw new RuntimeException("Nickname уже используется");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); 

        User saved = userRepository.save(user);

        return new UserDTO(saved.getId(), saved.getUsername(), saved.getNickname(), saved.getEmail());
    }

    @GetMapping("/all")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getUsername(), u.getNickname(), u.getEmail()))
                .toList();
    }
}