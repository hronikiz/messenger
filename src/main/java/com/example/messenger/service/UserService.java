package com.example.messenger.service;

import com.example.messenger.dto.*;
import com.example.messenger.entity.User;
import com.example.messenger.exception.ConflictException;
import com.example.messenger.exception.NotFoundException;
import com.example.messenger.repository.UserRepository;
import com.example.messenger.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email уже используется");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new ConflictException("Nickname уже занят");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .build()
        );

        return new AuthResponse(token, toDTO(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        user.setOnline(true);
        userRepository.save(user);

        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .build()
        );

        return new AuthResponse(token, toDTO(user));
    }

    @Transactional
    public void logout(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String email) {
        return userRepository.findByEmail(email)
            .map(this::toDTO)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> searchByNickname(String nickname) {
        return userRepository.findAll().stream()
            .filter(u -> u.getNickname().toLowerCase().contains(nickname.toLowerCase()))
            .map(this::toDTO)
            .toList();
    }

    public UserDTO toDTO(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getEmail(),
            user.isOnline(),
            user.getLastSeen()
        );
    }

    public User getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
