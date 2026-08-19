-- ============================================================
-- 社区留言板（帖子 + 帖子评论）—— 数据库初始化脚本
-- 用法：mysql -uroot -p123456 < init_post.sql
-- 可重复执行（DROP 后重建，不影响 t_anime 等表）
-- ============================================================

USE exam_auth;

DROP TABLE IF EXISTS t_post_comment;
DROP TABLE IF EXISTS t_post;

-- ---------- 帖子表 ----------
CREATE TABLE t_post (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '发帖用户id',
    username   VARCHAR(50)  NOT NULL COMMENT '发帖用户名',
    title      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '标题',
    content    VARCHAR(2000) NOT NULL COMMENT '内容',
    likes      INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='社区帖子表';

-- ---------- 帖子评论表 ----------
CREATE TABLE t_post_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    post_id    BIGINT       NOT NULL COMMENT '帖子id',
    user_id    BIGINT       NOT NULL COMMENT '评论用户id',
    username   VARCHAR(50)  NOT NULL COMMENT '评论用户名',
    content    VARCHAR(500) NOT NULL COMMENT '评论内容',
    likes      INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='帖子评论表';

-- 演示帖子
INSERT INTO t_post (user_id, username, title, content, likes) VALUES
(1, 'admin', '欢迎来到星空动漫留言板！', '在这里可以分享你正在追的番、安利冷门佳作，或者对网站提建议～', 3),
(1, 'admin', '大家最近在看什么？', '我先来：在补《罗小黑战记》，治愈系 yyds！', 1);

-- 演示帖子评论
INSERT INTO t_post_comment (post_id, user_id, username, content, likes) VALUES
(1, 1, 'admin', '支持！希望网站越做越好', 2);
