package com.example.auth.controller;

import com.example.auth.common.Result;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 番剧接口（公开，无需登录）：
 * - GET /api/anime/list   分页列表，支持 category/keyword/sort 筛选
 * - GET /api/anime/{id}   详情
 * - GET /api/anime/rank   排行榜（views / rating）
 * - GET /api/anime/banner 首页轮播
 * - GET /api/anime/related/{id} 相关推荐（同分类）
 */
@RestController
@RequestMapping("/api/anime")
public class AnimeController {

    private final JdbcTemplate jdbcTemplate;

    public AnimeController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 行 → 前端字段（tags 拆成数组，views 为数字） */
    private Map<String, Object> toAnime(Map<String, Object> row) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("id", row.get("id"));
        a.put("title", row.get("title"));
        a.put("author", row.get("author"));
        a.put("category", row.get("category"));
        a.put("tags", toTags(String.valueOf(row.get("tags"))));
        a.put("desc", row.get("description"));
        a.put("cover", row.get("cover"));
        a.put("rating", row.get("rating"));
        a.put("year", row.get("year"));
        a.put("views", row.get("views"));
        a.put("episodes", row.get("episodes"));
        a.put("biliSeason", row.get("bili_season"));
        a.put("bvid", row.get("bvid"));
        a.put("videoUrl", row.get("video_url"));
        return a;
    }

    private List<String> toTags(String tags) {
        List<String> list = new ArrayList<>();
        if (tags != null && !tags.isBlank()) {
            for (String t : tags.split(",")) {
                String s = t.trim();
                if (!s.isEmpty()) list.add(s);
            }
        }
        return list;
    }

    /**
     * GET /api/anime/list?page=1&size=12&category=&keyword=&sort=views|rating|newest
     * 返回 { list, total, page, size }
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "views") String sort) {

        // 安全处理分页
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 12;

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (category != null && !category.isBlank() && !"全部".equals(category)) {
            where.append(" AND category = ?");
            args.add(category);
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.trim() + "%";
            where.append(" AND (title LIKE ? OR author LIKE ? OR tags LIKE ?)");
            args.add(kw);
            args.add(kw);
            args.add(kw);
        }

        // 排序白名单
        String orderBy = switch (sort == null ? "" : sort) {
            case "rating" -> "rating DESC, views DESC";
            case "newest" -> "year DESC, updated_at DESC";
            default -> "views DESC";
        };

        Integer total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM t_anime" + where, Integer.class, args.toArray());
        if (total == null) total = 0;

        List<Object> pageArgs = new ArrayList<>(args);
        pageArgs.add(size);
        pageArgs.add((page - 1) * size);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_anime" + where + " ORDER BY " + orderBy + " LIMIT ? OFFSET ?",
                pageArgs.toArray());

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) list.add(toAnime(row));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return Result.success(data);
    }

    /** GET /api/anime/{id} —— 详情 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_anime WHERE id = ?", id);
        if (rows.isEmpty()) return Result.error("番剧不存在");
        return Result.success(toAnime(rows.get(0)));
    }

    /** GET /api/anime/rank?sort=views|rating —— 排行榜前 20 */
    @GetMapping("/rank")
    public Result<List<Map<String, Object>>> rank(
            @RequestParam(defaultValue = "rating") String sort,
            @RequestParam(defaultValue = "20") int limit) {
        if (limit < 1 || limit > 50) limit = 20;
        String orderBy = "views".equals(sort) ? "views DESC" : "rating DESC";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_anime ORDER BY " + orderBy + " LIMIT ?", limit);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) list.add(toAnime(row));
        return Result.success(list);
    }

    /** GET /api/anime/banner —— 首页轮播（is_banner=1） */
    @GetMapping("/banner")
    public Result<List<Map<String, Object>>> banner() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_anime WHERE is_banner = 1 ORDER BY views DESC LIMIT 4");
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) list.add(toAnime(row));
        return Result.success(list);
    }

    /** GET /api/anime/related/{id} —— 相关推荐（同分类，最多 4 部） */
    @GetMapping("/related/{id}")
    public Result<List<Map<String, Object>>> related(@PathVariable Long id) {
        List<Map<String, Object>> self = jdbcTemplate.queryForList(
                "SELECT category FROM t_anime WHERE id = ?", id);
        if (self.isEmpty()) return Result.success(new ArrayList<>());
        String category = String.valueOf(self.get(0).get("category"));
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM t_anime WHERE category = ? AND id <> ? ORDER BY rating DESC LIMIT 4",
                category, id);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map<String, Object> row : rows) list.add(toAnime(row));
        return Result.success(list);
    }
}
