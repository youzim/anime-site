package com.example.auth.controller;

import com.example.auth.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 弹幕接口：
 * - GET  /api/danmaku?animeId=1&ep=1   取某集弹幕（公开）
 * - POST /api/danmaku                  发弹幕（需登录）
 */
@RestController
@RequestMapping("/api/danmaku")
public class DanmakuController {

    private final JdbcTemplate jdbcTemplate;

    public DanmakuController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** GET /api/danmaku?animeId=&ep= —— 返回 [{id, time, text, color, username, createdAt}] */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestParam Long animeId,
                                                  @RequestParam(defaultValue = "1") int ep) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, time, text, color, username, created_at AS createdAt " +
                        "FROM t_danmaku WHERE anime_id = ? AND ep_no = ? ORDER BY time ASC, id ASC",
                animeId, ep);
        for (Map<String, Object> r : rows) {
            if (r.get("time") instanceof Number n) r.put("time", n.doubleValue());
        }
        return Result.success(rows);
    }

    /** POST /api/danmaku —— body {animeId, ep, time, text, color}，需登录 */
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body,
                                           @RequestAttribute Long userId,
                                           @RequestAttribute String username) {
        Long animeId = body.get("animeId") == null ? null : ((Number) body.get("animeId")).longValue();
        int ep = body.get("ep") == null ? 1 : ((Number) body.get("ep")).intValue();
        double time = body.get("time") == null ? 0 : ((Number) body.get("time")).doubleValue();
        String text = body.get("text") == null ? "" : String.valueOf(body.get("text")).trim();
        String color = body.get("color") == null ? "#ffffff" : String.valueOf(body.get("color"));

        if (animeId == null || text.isEmpty()) return Result.error("参数不完整");
        if (text.length() > 50) text = text.substring(0, 50);

        jdbcTemplate.update(
                "INSERT INTO t_danmaku(anime_id, ep_no, time, text, color, username) VALUES (?, ?, ?, ?, ?, ?)",
                animeId, ep, time, text, color, username);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class));
        data.put("animeId", animeId);
        data.put("ep", ep);
        data.put("time", time);
        data.put("text", text);
        data.put("color", color);
        data.put("username", username);
        return Result.success(data, "弹幕发送成功");
    }
}
