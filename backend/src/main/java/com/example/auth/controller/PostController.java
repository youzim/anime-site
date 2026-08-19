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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区留言板接口：
 * - GET    /api/post?page=&size=          帖子列表（公开，含评论数）
 * - GET    /api/post/{id}                 帖子详情（含全部评论，公开）
 * - POST   /api/post                      发帖（需登录）
 * - DELETE /api/post/{id}                 删帖（需登录，本人）
 * - POST   /api/post/{id}/like            帖子点赞（需登录）
 * - POST   /api/post/{id}/comment         帖子评论（需登录）
 * - DELETE /api/post/comment/{id}         删评论（需登录，本人）
 * - POST   /api/post/comment/{id}/like    评论点赞（需登录）
 */
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final JdbcTemplate jdbcTemplate;

    public PostController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Map<String, Object> toPost(Map<String, Object> row, int commentCount) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", row.get("id"));
        p.put("userId", row.get("user_id"));
        p.put("username", row.get("username"));
        p.put("title", row.get("title"));
        p.put("content", row.get("content"));
        p.put("likes", row.get("likes"));
        p.put("createdAt", row.get("created_at"));
        p.put("commentCount", commentCount);
        return p;
    }

    /** GET /api/post?page=&size= —— 返回 {list, total, page, size} */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 20;

        Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_post", Integer.class);
        if (total == null) total = 0;

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_post ORDER BY id DESC LIMIT ? OFFSET ?", size, (page - 1) * size);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM t_post_comment WHERE post_id = ?", Integer.class, r.get("id"));
            list.add(toPost(r, cnt == null ? 0 : cnt));
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /** GET /api/post/{id} —— 帖子 + 评论列表 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_post WHERE id = ?", id);
        if (rows.isEmpty()) return Result.error("帖子不存在");

        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_post_comment WHERE post_id = ?", Integer.class, id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("post", toPost(rows.get(0), cnt == null ? 0 : cnt));

        List<Map<String, Object>> comments = jdbcTemplate.queryForList(
                "SELECT id, user_id AS userId, username, content, likes, created_at AS createdAt " +
                        "FROM t_post_comment WHERE post_id = ? ORDER BY id ASC", id);
        data.put("comments", comments);
        return Result.success(data);
    }

    /** POST /api/post —— body {title, content} */
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body,
                                           @RequestAttribute Long userId,
                                           @RequestAttribute String username) {
        String title = body.get("title") == null ? "" : String.valueOf(body.get("title")).trim();
        String content = body.get("content") == null ? "" : String.valueOf(body.get("content")).trim();
        if (content.isEmpty()) return Result.error("内容不能为空");
        if (title.length() > 100) title = title.substring(0, 100);
        if (content.length() > 2000) content = content.substring(0, 2000);

        jdbcTemplate.update(
                "INSERT INTO t_post(user_id, username, title, content) VALUES (?, ?, ?, ?)",
                userId, username, title, content);
        Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", id);
        return Result.success(data, "发表成功");
    }

    /** DELETE /api/post/{id} —— 仅本人可删 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id, @RequestAttribute Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id FROM t_post WHERE id = ? AND user_id = ?", id, userId);
        if (rows.isEmpty()) return Result.error("只能删除自己的帖子");
        jdbcTemplate.update("DELETE FROM t_post_comment WHERE post_id = ?", id);
        jdbcTemplate.update("DELETE FROM t_post WHERE id = ?", id);
        return Result.success();
    }

    /** POST /api/post/{id}/like */
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> like(@PathVariable Long id) {
        int updated = jdbcTemplate.update("UPDATE t_post SET likes = likes + 1 WHERE id = ?", id);
        if (updated == 0) return Result.error("帖子不存在");
        Integer likes = jdbcTemplate.queryForObject("SELECT likes FROM t_post WHERE id = ?", Integer.class, id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("likes", likes);
        return Result.success(data);
    }

    /** POST /api/post/{id}/comment —— body {content} */
    @PostMapping("/{id}/comment")
    public Result<Map<String, Object>> addComment(@PathVariable Long id,
                                                  @RequestBody Map<String, Object> body,
                                                  @RequestAttribute Long userId,
                                                  @RequestAttribute String username) {
        String content = body.get("content") == null ? "" : String.valueOf(body.get("content")).trim();
        if (content.isEmpty()) return Result.error("评论内容不能为空");
        if (content.length() > 500) content = content.substring(0, 500);

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_post WHERE id = ?", Integer.class, id);
        if (exists == null || exists == 0) return Result.error("帖子不存在");

        jdbcTemplate.update(
                "INSERT INTO t_post_comment(post_id, user_id, username, content) VALUES (?, ?, ?, ?)",
                id, userId, username, content);
        Long cid = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", cid);
        return Result.success(data, "评论成功");
    }

    /** DELETE /api/post/comment/{id} —— 仅本人可删 */
    @DeleteMapping("/comment/{id}")
    public Result<Void> removeComment(@PathVariable Long id, @RequestAttribute Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id FROM t_post_comment WHERE id = ? AND user_id = ?", id, userId);
        if (rows.isEmpty()) return Result.error("只能删除自己的评论");
        jdbcTemplate.update("DELETE FROM t_post_comment WHERE id = ?", id);
        return Result.success();
    }

    /** POST /api/post/comment/{id}/like */
    @PostMapping("/comment/{id}/like")
    public Result<Map<String, Object>> likeComment(@PathVariable Long id) {
        int updated = jdbcTemplate.update("UPDATE t_post_comment SET likes = likes + 1 WHERE id = ?", id);
        if (updated == 0) return Result.error("评论不存在");
        Integer likes = jdbcTemplate.queryForObject(
                "SELECT likes FROM t_post_comment WHERE id = ?", Integer.class, id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("likes", likes);
        return Result.success(data);
    }
}
