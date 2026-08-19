-- 登录/注册练习 —— 数据库初始化脚本
-- 用法：mysql -uroot -p123456 < init.sql   （已执行过，可重复执行）

CREATE DATABASE IF NOT EXISTS exam_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE exam_auth;

CREATE TABLE IF NOT EXISTS t_user (
    id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50)  NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 测试账号：admin / 123456
INSERT INTO t_user (username, password)
SELECT 'admin', '123456'
WHERE NOT EXISTS (SELECT 1 FROM t_user WHERE username = 'admin');
