package com.erokin.strove.controller;

import com.erokin.strove.dto.ApiResponse;
import com.erokin.strove.dto.AuthResponse;
import com.erokin.strove.dto.LoginRequest;
import com.erokin.strove.dto.RegisterRequest;
import com.erokin.strove.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        System.out.println("========== Register Request ==========");
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        try {
            AuthResponse response = authService.register(request);
            System.out.println("Registration successful for user: " + response.getUsername());
            return ApiResponse.success("注册成功", response);
        } catch (Exception e) {
            System.out.println("Registration failed with error: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("========== Login Request ==========");
        System.out.println("Login Name: " + request.getLoginName());
        System.out.println("Password Length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));
        try {
            AuthResponse response = authService.login(request);
            System.out.println("Login successful for user: " + response.getUsername());
            System.out.println("Token generated: " + (response.getToken() != null ? "Yes" : "No"));
            ApiResponse<AuthResponse> apiResponse = ApiResponse.success("登录成功", response);
            System.out.println("Returning success response");
            return apiResponse;
        } catch (Exception e) {
            System.out.println("Login failed with error: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }
}
