package com.example.auth.controller;

import com.example.auth.common.Result;
import com.example.auth.util.JwtUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 登录 / 注册接口（练习版）：单表 t_user 存用户名密码
 * 说明：练习项目密码为明文存储，真实项目请使用 BCrypt 加密
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JdbcTemplate jdbcTemplate;
    private final JwtUtil jwtUtil;

    public AuthController(JdbcTemplate jdbcTemplate, JwtUtil jwtUtil) {
        this.jdbcTemplate = jdbcTemplate;
        this.jwtUtil = jwtUtil;
    }

    /** POST /api/auth/register —— 注册，body 传 {username, password} */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, Object> body) {
        String username = String.valueOf(body.get("username") == null ? "" : body.get("username")).trim();
        String password = body.get("password") == null ? "" : String.valueOf(body.get("password"));

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error("用户名和密码不能为空");
        }
        // 用户名不能重复
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_user WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            return Result.error("用户名已存在");
        }
        jdbcTemplate.update("INSERT INTO t_user(username, password) VALUES (?, ?)", username, password);
        return Result.success();
    }

    /** POST /api/auth/login —— 登录，body 传 {username, password} */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String username = String.valueOf(body.get("username") == null ? "" : body.get("username")).trim();
        String password = body.get("password") == null ? "" : String.valueOf(body.get("password"));

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return Result.error("用户名和密码不能为空");
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, username, password FROM t_user WHERE username = ?", username);
        if (rows.isEmpty() || !password.equals(String.valueOf(rows.get(0).get("password")))) {
            return Result.error("用户名或密码错误");
        }
        Long id = ((Number) rows.get(0).get("id")).longValue();
        String name = String.valueOf(rows.get(0).get("username"));
        String token = jwtUtil.generateToken(id, name);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        data.put("username", name);
        data.put("token", token);
        return Result.success(data);
    }
}
