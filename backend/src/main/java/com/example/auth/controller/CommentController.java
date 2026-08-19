package com.example.auth.controller;

import com.example.auth.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评论接口：
 * - GET    /api/comment?animeId=1       评论列表（公开）
 * - POST   /api/comment                 发表评论（需登录）
 * - DELETE /api/comment/{id}            删除（需登录，本人可删）
 * - POST   /api/comment/{id}/like       点赞（需登录）
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final JdbcTemplate jdbcTemplate;

    public CommentController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** GET /api/comment?animeId= —— 按时间正序（最早在前，像讨论串） */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam Long animeId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, anime_id AS animeId, user_id AS userId, username, content, likes, created_at AS createdAt " +
                        "FROM t_comment WHERE anime_id = ? ORDER BY id ASC", animeId);
        return Result.success(rows);
    }

    /** POST /api/comment —— body {animeId, content} */
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body,
                                           @RequestAttribute Long userId,
                                           @RequestAttribute String username) {
        Long animeId = body.get("animeId") == null ? null : ((Number) body.get("animeId")).longValue();
        String content = body.get("content") == null ? "" : String.valueOf(body.get("content")).trim();
        if (animeId == null || content.isEmpty()) return Result.error("参数不完整");
        if (content.length() > 500) content = content.substring(0, 500);

        jdbcTemplate.update(
                "INSERT INTO t_comment(anime_id, user_id, username, content) VALUES (?, ?, ?, ?)",
                animeId, userId, username, content);

        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id", id);
        data.put("animeId", animeId);
        data.put("userId", userId);
        data.put("username", username);
        data.put("content", content);
        data.put("likes", 0);
        return Result.success(data, "评论成功");
    }

    /** DELETE /api/comment/{id} —— 仅本人可删 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, @RequestAttribute Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id FROM t_comment WHERE id = ? AND user_id = ?", id, userId);
        if (rows.isEmpty()) return Result.error("只能删除自己的评论");
        jdbcTemplate.update("DELETE FROM t_comment WHERE id = ?", id);
        return Result.success();
    }

    /** POST /api/comment/{id}/like —— 点赞 +1 */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        int updated = jdbcTemplate.update("UPDATE t_comment SET likes = likes + 1 WHERE id = ?", id);
        if (updated == 0) return Result.error("评论不存在");
        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM t_comment WHERE id = ?", Integer.class, id);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("likes", likes);
        return Result.success(data);
    }
}
