package com.habsida.store.service;

import com.habsida.store.entity.User;
import com.habsida.store.repository.UserRepository;
import com.habsida.store.repository.UserRoleRepository;
import com.habsida.store.security.AuthUser;
import com.habsida.store.security.JwtService;
import com.habsida.store.security.dto.AuthResponse;
import com.habsida.store.security.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Build auth response with access and refresh tokens for a known authenticated user (e.g. after login). */
    public AuthResponse buildAuthResponse(AuthUser authUser) {
        String accessToken = jwtService.generateToken(authUser.getEmail(), authUser.getId());
        String refreshToken = jwtService.generateRefreshToken(authUser.getEmail(), authUser.getId());
        List<String> roles = authUser.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(authUser.getId())
                .email(authUser.getEmail())
                .roles(roles)
                .build();
    }

    /** Register a new user. Returns empty if email already exists or constraint violation. */
    @Transactional
    public Optional<AuthResponse> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Optional.empty();
        }
        try {
            User user = User.builder()
                    .email(request.getEmail())
                    .passwordHash(passwordEncoder.encode(request.getPassword()))
                    .build();
            user = userRepository.save(user);
            return Optional.of(buildAuthResponse(user));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    /** Build auth response for a persisted user (e.g. after register or refresh). */
    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user.getEmail(), user.getId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getId());
        List<String> roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getName() : null)
                .filter(name -> name != null)
                .collect(Collectors.toList());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    /** Exchange a valid refresh token for new access and refresh tokens. */
    public Optional<AuthResponse> refresh(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            return Optional.empty();
        }
        String email = jwtService.extractEmail(refreshToken);
        Long userId = jwtService.extractUserId(refreshToken);
        return userRepository.findByEmail(email)
                .filter(u -> u.getId().equals(userId))
                .map(this::buildAuthResponse);
    }
}
