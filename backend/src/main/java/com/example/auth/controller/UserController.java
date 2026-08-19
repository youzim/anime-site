package com.example.auth.controller;

import com.example.auth.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 需要登录（带 token）才能访问的接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * GET /api/user/me —— 通过 token 取当前登录用户
     */
    @GetMapping("/me")
    public Result<Map<String, Object>> me(@RequestAttribute Long userId,
                                          @RequestAttribute String username) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", userId);
        data.put("username", username);
        return Result.success(data);
    }

    /**
     * GET /api/user/stats —— 用户数据统计（个人中心用）
     * 返回：追番数、历史数、评论数、发帖数、弹幕数、注册时间
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(@RequestAttribute Long userId) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("favoriteCount", count("SELECT COUNT(*) FROM t_favorite WHERE user_id = ?", userId));
        data.put("historyCount", count("SELECT COUNT(*) FROM t_history WHERE user_id = ?", userId));
        data.put("commentCount", count("SELECT COUNT(*) FROM t_comment WHERE user_id = ?", userId));
        data.put("postCount", count("SELECT COUNT(*) FROM t_post WHERE user_id = ?", userId));
        data.put("danmakuCount", count("SELECT COUNT(*) FROM t_danmaku WHERE username IN (SELECT username FROM t_user WHERE id = ?)", userId));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT created_at AS createdAt FROM t_user WHERE id = ?", userId);
        if (!rows.isEmpty()) data.put("createdAt", rows.get(0).get("createdAt"));
        return Result.success(data);
    }

    private int count(String sql, Long userId) {
        Integer c = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return c == null ? 0 : c;
    }
}
