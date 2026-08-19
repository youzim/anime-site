package com.example.auth.controller;

import com.example.auth.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏接口（需登录）：
 * - GET  /api/favorite           我的收藏（含番剧信息）
 * - POST /api/favorite/{animeId} 收藏/取消（切换）
 * - GET  /api/favorite/status/{animeId} 是否已收藏
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    private final JdbcTemplate jdbcTemplate;

    public FavoriteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** GET /api/favorite */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestAttribute Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT f.anime_id AS animeId, f.created_at AS createdAt, " +
                        "a.title, a.cover, a.category, a.rating, a.episodes, a.author " +
                        "FROM t_favorite f JOIN t_anime a ON a.id = f.anime_id " +
                        "WHERE f.user_id = ? ORDER BY f.created_at DESC", userId);
        return Result.success(rows);
    }

    /** POST /api/favorite/{animeId} —— 返回 {favorited: true/false} */
    @PostMapping("/{animeId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long animeId, @RequestAttribute Long userId) {
        List<Map<String, Object>> exists = jdbcTemplate.queryForList(
                "SELECT id FROM t_favorite WHERE user_id = ? AND anime_id = ?", userId, animeId);
        boolean favorited;
        if (exists.isEmpty()) {
            jdbcTemplate.update("INSERT INTO t_favorite(user_id, anime_id) VALUES (?, ?)", userId, animeId);
            favorited = true;
        } else {
            jdbcTemplate.update("DELETE FROM t_favorite WHERE user_id = ? AND anime_id = ?", userId, animeId);
            favorited = false;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("favorited", favorited);
        return Result.success(data, favorited ? "收藏成功" : "已取消收藏");
    }

    /** GET /api/favorite/status/{animeId} —— 返回 {favorited: true/false} */
    @GetMapping("/status/{animeId}")
    public Result<Map<String, Object>> status(@PathVariable Long animeId, @RequestAttribute Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_favorite WHERE user_id = ? AND anime_id = ?",
                Integer.class, userId, animeId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("favorited", count != null && count > 0);
        return Result.success(data);
    }
}
