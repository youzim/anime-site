-- 自动建表脚本（Spring Boot sql.init 启动时执行，幂等：全部 IF NOT EXISTS）
-- 表结构与 db/init_anime.sql、init_post.sql 一致

CREATE TABLE IF NOT EXISTS t_user (
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    username   VARCHAR(50) NOT NULL COMMENT '用户名',
    password   VARCHAR(100) NOT NULL COMMENT '密码',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

CREATE TABLE IF NOT EXISTS t_anime (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    author      VARCHAR(100) NOT NULL DEFAULT '' COMMENT '制作方',
    category    VARCHAR(20)  NOT NULL COMMENT '分类：国漫/日漫/欧美动漫/电影',
    tags        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '标签，逗号分隔',
    description VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '简介',
    cover       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '封面图',
    rating      DECIMAL(3,1) NOT NULL DEFAULT 0 COMMENT '评分',
    year        INT          NOT NULL DEFAULT 0 COMMENT '年份',
    views       BIGINT       NOT NULL DEFAULT 0 COMMENT '播放量',
    episodes    INT          NOT NULL DEFAULT 1 COMMENT '总集数',
    is_banner   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否轮播推荐',
    bili_season VARCHAR(30)  NOT NULL DEFAULT '' COMMENT 'B站番剧season_id（保留字段）',
    bvid        VARCHAR(50)  NOT NULL DEFAULT '' COMMENT 'B站视频BV号（保留字段）',
    video_url   VARCHAR(500) NOT NULL DEFAULT '' COMMENT '直链视频地址',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_category (category),
    KEY idx_views (views),
    KEY idx_rating (rating)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='番剧表';

CREATE TABLE IF NOT EXISTS t_danmaku (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    anime_id   BIGINT        NOT NULL COMMENT '番剧id',
    ep_no      INT           NOT NULL DEFAULT 1 COMMENT '集数',
    time       DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '弹幕时间点（秒）',
    text       VARCHAR(200)  NOT NULL COMMENT '弹幕内容',
    color      VARCHAR(20)   NOT NULL DEFAULT '#ffffff' COMMENT '弹幕颜色',
    username   VARCHAR(50)   NOT NULL DEFAULT '游客' COMMENT '发送人',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_anime_ep (anime_id, ep_no)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='弹幕表';

CREATE TABLE IF NOT EXISTS t_history (
    id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT        NOT NULL COMMENT '用户id',
    anime_id   BIGINT        NOT NULL COMMENT '番剧id',
    ep_no      INT           NOT NULL DEFAULT 1 COMMENT '看到第几集',
    progress   DECIMAL(10,3) NOT NULL DEFAULT 0 COMMENT '播放进度（秒）',
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_anime (user_id, anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='观看历史表';

CREATE TABLE IF NOT EXISTS t_favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT   NOT NULL COMMENT '用户id',
    anime_id   BIGINT   NOT NULL COMMENT '番剧id',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_anime (user_id, anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

CREATE TABLE IF NOT EXISTS t_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    anime_id   BIGINT       NOT NULL COMMENT '番剧id',
    user_id    BIGINT       NOT NULL COMMENT '用户id',
    username   VARCHAR(50)  NOT NULL COMMENT '用户名',
    content    VARCHAR(500) NOT NULL COMMENT '评论内容',
    likes      INT          NOT NULL DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_anime (anime_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';

CREATE TABLE IF NOT EXISTS t_post (
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

CREATE TABLE IF NOT EXISTS t_post_comment (
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
