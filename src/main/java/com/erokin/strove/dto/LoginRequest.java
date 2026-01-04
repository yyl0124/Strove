package com.erokin.strove.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "登录名不能为空")
    private String loginName; // 可以是email或username

    @NotBlank(message = "密码不能为空")
    private String password;

    // Getters and Setters
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
