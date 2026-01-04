package com.erokin.strove.service;

import com.erokin.strove.dto.LoginRequest;
import com.erokin.strove.dto.RegisterRequest;
import com.erokin.strove.dto.AuthResponse;
import com.erokin.strove.entity.User;
import com.erokin.strove.repository.UserRepository;
import com.erokin.strove.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        // 生成JWT Token
        String token = tokenProvider.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        System.out.println("AuthService.login called with loginName: " + request.getLoginName());
        System.out.println("Password received: [" + request.getPassword() + "]");
        System.out.println("Password length: " + request.getPassword().length());
        
        // 查找用户（支持用户名或邮箱登录）
        User user = userRepository.findByUsername(request.getLoginName())
                .or(() -> {
                    System.out.println("User not found by username, trying email...");
                    return userRepository.findByEmail(request.getLoginName());
                })
                .orElseThrow(() -> {
                    System.out.println("User not found by username or email");
                    return new RuntimeException("用户名或密码错误");
                });

        System.out.println("User found: " + user.getUsername() + " (ID: " + user.getId() + ")");
        System.out.println("Stored password hash: " + user.getPassword());

        // 验证密码
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password matches: " + passwordMatches);
        
        // Test with hardcoded password
        System.out.println("Testing with hardcoded '123456': " + passwordEncoder.matches("123456", user.getPassword()));
        
        if (!passwordMatches) {
            System.out.println("Password verification failed");
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成JWT Token
        String token = tokenProvider.generateToken(user.getId(), user.getUsername());
        System.out.println("JWT Token generated successfully");

        AuthResponse response = new AuthResponse(token, user.getId(), user.getUsername(), user.getEmail());
        System.out.println("Returning AuthResponse for user: " + response.getUsername());
        return response;
    }
}
