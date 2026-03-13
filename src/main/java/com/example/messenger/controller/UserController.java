package com.example.messenger.controller;

import com.example.messenger.model.User;
import com.example.messenger.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PostMapping("/create")
    public User createusUser(@RequestParam String username,
                             @RequestParam String email,
                             @RequestParam String password) {
        User user = new User(null, username, email, password);
        return userRepository.save(user);
    }
    
    @GetMapping("/all")
    public List<User> getAllUsers(){
        return userRepository.findAllUsers();
    }

}
