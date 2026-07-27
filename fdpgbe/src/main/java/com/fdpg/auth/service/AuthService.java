package com.fdpg.auth.service;

import com.fdpg.auth.dto.LoginRequest;
import com.fdpg.auth.dto.LoginResponse;
import com.fdpg.auth.dto.RegisterRequest;
import com.fdpg.auth.dto.UserResponse;
import com.fdpg.common.response.ApiResponse;

public interface AuthService {
    ApiResponse<Void> register(RegisterRequest request);
    ApiResponse<LoginResponse> login(LoginRequest request);
    ApiResponse<UserResponse> getCurrentUser(String username);
}
