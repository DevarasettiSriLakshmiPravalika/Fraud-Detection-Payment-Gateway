package com.fdpg.auth.service;

import com.fdpg.account.service.AccountService;
import com.fdpg.auth.dto.LoginRequest;
import com.fdpg.auth.dto.LoginResponse;
import com.fdpg.auth.dto.RegisterRequest;
import com.fdpg.auth.dto.UserResponse;
import com.fdpg.auth.entity.Role;
import com.fdpg.auth.entity.User;
import com.fdpg.auth.repository.UserRepository;
import com.fdpg.auth.security.JwtUtil;
import com.fdpg.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AccountService accountService;

    @Override
    public ApiResponse<Void> register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ApiResponse.error("Registration failed", List.of("Username is already taken"));
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("Registration failed", List.of("Email is already registered"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        // Automatically create a virtual account with ₹5000 for the new user
        accountService.createAccountForUser(savedUser);

        return ApiResponse.success("User registered successfully", null);
    }

    @Override
    public ApiResponse<LoginResponse> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String role = userDetails.getAuthorities().iterator().next().getAuthority();

            String jwt = jwtUtil.generateToken(userDetails, role);

            // Fetch full user to include email in response
            User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .email(user.getEmail())
                    .role(role)
                    .build();

            return ApiResponse.success("Login successful", loginResponse);
        } catch (Exception e) {
            return ApiResponse.error("Login failed", List.of("Invalid username or password"));
        }
    }

    @Override
    public ApiResponse<UserResponse> getCurrentUser(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found", List.of("Invalid user"));
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ApiResponse.success("User retrieved successfully", userResponse);
    }
}
