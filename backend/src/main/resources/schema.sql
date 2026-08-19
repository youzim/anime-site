-- 自动建表脚本（Spring Boot sql.init 启动时执行，幂等：全部 IF NOT EXISTS）
-- 语法兼容 MySQL 与 H2（MODE=MySQL）

CREATE TABLE IF NOT EXISTS t_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS t_anime (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    title       VARCHAR(100)  NOT NULL,
    author      VARCHAR(100)  NOT NULL DEFAULT '',
    category    VARCHAR(20)   NOT NULL,
    tags        VARCHAR(200)  NOT NULL DEFAULT '',
    description VARCHAR(1000) NOT NULL DEFAULT '',
    cover       VARCHAR(255)  NOT NULL DEFAULT '',
    rating      DECIMAL(3,1)  NOT NULL DEFAULT 0,
    `year`      INT           NOT NULL DEFAULT 0,
    views       BIGINT        NOT NULL DEFAULT 0,
    episodes    INT           NOT NULL DEFAULT 1,
    is_banner   TINYINT       NOT NULL DEFAULT 0,
    bili_season VARCHAR(30)   NOT NULL DEFAULT '',
    bvid        VARCHAR(50)   NOT NULL DEFAULT '',
    video_url   VARCHAR(500)  NOT NULL DEFAULT '',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_views (views),
    INDEX idx_rating (rating)
);

CREATE TABLE IF NOT EXISTS t_danmaku (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    anime_id   BIGINT        NOT NULL,
    ep_no      INT           NOT NULL DEFAULT 1,
    time       DECIMAL(10,3) NOT NULL DEFAULT 0,
    text       VARCHAR(200)  NOT NULL,
    color      VARCHAR(20)   NOT NULL DEFAULT '#ffffff',
    username   VARCHAR(50)   NOT NULL DEFAULT '游客',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_anime_ep (anime_id, ep_no)
);

CREATE TABLE IF NOT EXISTS t_history (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    user_id    BIGINT        NOT NULL,
    anime_id   BIGINT        NOT NULL,
    ep_no      INT           NOT NULL DEFAULT 1,
    progress   DECIMAL(10,3) NOT NULL DEFAULT 0,
    updated_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (user_id, anime_id)
);

CREATE TABLE IF NOT EXISTS t_favorite (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL,
    anime_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE (user_id, anime_id)
);

CREATE TABLE IF NOT EXISTS t_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    anime_id   BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    content    VARCHAR(500) NOT NULL,
    likes      INT          NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_anime (anime_id)
);

CREATE TABLE IF NOT EXISTS t_post (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    user_id    BIGINT        NOT NULL,
    username   VARCHAR(50)   NOT NULL,
    title      VARCHAR(100)  NOT NULL DEFAULT '',
    content    VARCHAR(2000) NOT NULL,
    likes      INT           NOT NULL DEFAULT 0,
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_created (created_at)
);

CREATE TABLE IF NOT EXISTS t_post_comment (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    post_id    BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    username   VARCHAR(50)  NOT NULL,
    content    VARCHAR(500) NOT NULL,
    likes      INT          NOT NULL DEFAULT 0,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_post (post_id)
);
