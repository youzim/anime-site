# 星空动漫 · 全栈动漫网站

Vue3 + Spring Boot 的完整动漫网站（练习项目）：番剧浏览 / 搜索 / 选集播放 / 弹幕 / 追番 / 历史 / 评论 / 社区留言板 / 个人中心。

## 技术栈

| 端 | 技术 |
|---|---|
| 前端 `frontend/` | Vue 3 + Vite + Element Plus + Pinia + Vue Router + dan-player 弹幕播放器 |
| 后端 `backend/` | Spring Boot 3.3 + JdbcTemplate + JWT（jjwt） |
| 数据库 | MySQL 8（库名 `exam_auth`，脚本在 `db/`） |

## 本地运行

### 1. 初始化数据库（MySQL 需已启动，账号密码 root/123456）

```bash
mysql -uroot -p123456 < db/init_anime.sql   # 番剧/弹幕/历史/收藏/评论 表 + 16 部番剧种子数据
mysql -uroot -p123456 < db/init_post.sql    # 社区帖子表
```

> 后端 `backend/src/main/resources/application.yml` 里可改数据库账号密码。

### 2. 启动后端（端口 8080，JDK 17/21）

```bash
cd backend
mvn spring-boot:run
# 或打包后运行
mvn clean package -DskipTests
java -jar target/auth-backend-1.0.0.jar
```

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 ，登录账号 `admin / 123456`。

> 前端默认请求 `http://localhost:8080/api`。部署时通过环境变量覆盖：
> `VITE_API_BASE_URL=https://你的后端域名/api npm run build`

## 主要功能

- 首页：轮播 Banner、分类板块（国漫/日漫/欧美动漫/电影）、列表分页 + 排序 + 搜索
- 详情页：简介 / 评分 / 追番 / 选集 / 评论（发表/点赞/删除）/ 相关推荐
- 播放页：直链长视频播放器 + 本站弹幕 + 选集切换 + 进度上报 + 续播 + 猜你喜欢
- 排行：评分榜 / 热播榜
- 历史 / 收藏 / 个人中心（统计、最近观看、我的评论、追番管理）
- 社区留言板：发帖 / 点赞 / 回复
- JWT 登录注册

## 部署指南（免费）

### 方案 A（最快，零成本）：本机后端 + Cloudflare 免费隧道 + Vercel 前端

1. **后端**：本机跑起来后，开一条免费公网隧道：
   ```bash
   # 下载 cloudflared 后运行（Windows: cloudflared-windows-amd64.exe）
   cloudflared tunnel --url http://localhost:8080
   ```
   会得到一个 `https://xxxx.trycloudflare.com` 公网地址（每次运行会变）。
2. **前端**：把 `frontend/` 推到 GitHub → 在 [Vercel](https://vercel.com) 导入该仓库（Framework=Vite，构建命令 `npm run build`，输出目录 `dist`），并在 Vercel 环境变量里设置 `VITE_API_BASE_URL=https://xxxx.trycloudflare.com/api`。
3. 访问 Vercel 给的 `https://xxx.vercel.app` 即可。

> 优点：不需要信用卡、5 分钟上线。缺点：后端在你电脑上，需保持电脑开机且隧道地址每次运行会变（换地址后去 Vercel 改环境变量重新部署）。

### 方案 B（稳定上云）：Render 免费后端 + 免费 MySQL + Vercel 前端

1. **数据库**：在 [Railway](https://railway.app) 或 [Aiven](https://aiven.io) 创建免费 MySQL，拿到连接串，改 `backend/src/main/resources/application.yml` 的数据库地址并执行 `db/*.sql`。
2. **后端**：把 `backend/` 推到 GitHub → 在 [Render](https://render.com) 建 Free Web Service（构建命令 `mvn clean package -DskipTests`，启动命令 `java -jar target/auth-backend-1.0.0.jar`），免费实例无请求约 15 分钟休眠、再次访问会等待启动。
3. **前端**：同方案 A 的 Vercel 部署，`VITE_API_BASE_URL=https://你的后端域名/api`。

## 接口速览（统一返回 {code, msg, data}）

- 公开 GET：`/api/anime/list`（分页/分类/关键词/排序）、`/api/anime/{id}`、`/api/anime/rank`、`/api/anime/banner`、`/api/anime/related/{id}`、`/api/danmaku`、`/api/comment`、`/api/post`
- 需登录：`/api/user/me`、`/api/user/stats`、`/api/history` 系列、`/api/favorite` 系列、弹幕/评论/帖子写操作
- 认证：`/api/auth/login`、`/api/auth/register`
