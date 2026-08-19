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
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 观看历史接口（需登录）：
 * - GET    /api/history            我的历史（含番剧信息）
 * - POST   /api/history            记录/更新进度 {animeId, ep, progress}
 * - DELETE /api/history/{animeId}  删除一条
 */
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final JdbcTemplate jdbcTemplate;

    public HistoryController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** GET /api/history —— 按最近观看排序，返回 [{animeId, ep, progress, updatedAt, title, cover, category, episodes}] */
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestAttribute Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT h.anime_id AS animeId, h.ep_no AS ep, h.progress, h.updated_at AS updatedAt, " +
                        "a.title, a.cover, a.category, a.episodes " +
                        "FROM t_history h JOIN t_anime a ON a.id = h.anime_id " +
                        "WHERE h.user_id = ? ORDER BY h.updated_at DESC LIMIT 50", userId);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            if (r.get("progress") instanceof Number n) r.put("progress", n.doubleValue());
            list.add(r);
        }
        return Result.success(list);
    }

    /** POST /api/history —— body {animeId, ep, progress}，同番剧 upsert 置顶 */
    @PostMapping
    public Result<Void> record(@RequestBody Map<String, Object> body, @RequestAttribute Long userId) {
        Long animeId = body.get("animeId") == null ? null : ((Number) body.get("animeId")).longValue();
        if (animeId == null) return Result.error("animeId 不能为空");
        int ep = body.get("ep") == null ? 1 : ((Number) body.get("ep")).intValue();
        double progress = body.get("progress") == null ? 0 : ((Number) body.get("progress")).doubleValue();

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_history WHERE user_id = ? AND anime_id = ?",
                Integer.class, userId, animeId);
        if (exists != null && exists > 0) {
            jdbcTemplate.update(
                    "UPDATE t_history SET ep_no = ?, progress = ?, updated_at = NOW() WHERE user_id = ? AND anime_id = ?",
                    ep, progress, userId, animeId);
        } else {
            jdbcTemplate.update(
                    "INSERT INTO t_history(user_id, anime_id, ep_no, progress) VALUES (?, ?, ?, ?)",
                    userId, animeId, ep, progress);
        }
        return Result.success();
    }

    /** DELETE /api/history/{animeId} */
    @DeleteMapping("/{animeId}")
    public Result<Void> remove(@PathVariable Long animeId, @RequestAttribute Long userId) {
        jdbcTemplate.update("DELETE FROM t_history WHERE user_id = ? AND anime_id = ?", userId, animeId);
        return Result.success();
    }

    /** DELETE /api/history —— 清空当前用户全部历史 */
    @DeleteMapping
    public Result<Void> clear(@RequestAttribute Long userId) {
        jdbcTemplate.update("DELETE FROM t_history WHERE user_id = ?", userId);
        return Result.success();
    }
}
