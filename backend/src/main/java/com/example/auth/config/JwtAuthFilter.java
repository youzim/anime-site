package com.example.auth.config;

import com.example.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 鉴权过滤器：
 * - 放行 登录/注册 和 CORS 预检（OPTIONS）
 * - 其他 /api/** 请求必须带 Authorization: Bearer &lt;token&gt;，否则 401
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 登录/注册不用 token，其余 /api/auth/** 也一并放行
        if (path.startsWith("/api/auth/")) {
            return true;
        }
        // 公开浏览接口：番剧列表/详情/排行/轮播、弹幕查询、评论查询、帖子查询 无需登录
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/anime/")) return true;
            if (path.equals("/api/danmaku")) return true;
            if (path.equals("/api/comment")) return true;
            if (path.startsWith("/api/post")) return true;
        }
        // 其余 /api/**（历史/收藏/写操作/用户信息等）仍需 token
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // CORS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            Claims claims = jwtUtil.parseToken(auth.substring(7));
            if (claims != null) {
                // 把用户信息放进 request 属性，接口里用 @RequestAttribute 取
                request.setAttribute("userId", jwtUtil.getUid(claims));
                request.setAttribute("username", claims.getSubject());
                chain.doFilter(request, response);
                return;
            }
        }

        // 没带 token 或 token 无效 → 401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"未登录或登录已过期\",\"data\":null}");
    }
}
