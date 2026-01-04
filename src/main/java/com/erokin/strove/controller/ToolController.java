package com.erokin.strove.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 临时工具控制器 - 用于生成密码哈希
 * TODO: 生产环境中应删除此控制器
 */
@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final PasswordEncoder passwordEncoder;

    public ToolController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 生成BCrypt密码哈希
     * 访问: http://localhost:8123/api/tools/hash-password?password=123456
     */
    @GetMapping("/hash-password")
    public String hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        System.out.println("=================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("=================================");
        return "Password: " + password + "<br>BCrypt Hash: " + hash;
    }
}
