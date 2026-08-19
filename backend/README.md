# auth-backend —— 登录 / 注册练习后端

Spring Boot 3 + JdbcTemplate 的简单后端，提供 **注册** 和 **登录** 两个接口。
数据库只有一张表 `t_user`，存用户名和密码。

> 📖 **接口文档（Word）**：见 [`接口文档.docx`](接口文档.docx)，含登录/注册的接口地址、请求方法、接收参数与返回示例。

## 项目结构

```
auth-backend
├── pom.xml
├── db/init.sql                     # 数据库初始化脚本（已执行过）
└── src/main
    ├── java/com/example/auth
    │   ├── AuthApplication.java    # 启动类
    │   ├── common/Result.java      # 统一返回体 {code, msg, data}
    │   ├── config/CorsConfig.java  # 跨域配置
    │   ├── config/JwtAuthFilter.java   # JWT 鉴权过滤器（除 login/register 外都验 token）
    │   ├── util/JwtUtil.java       # JWT 签发 / 校验工具
    │   ├── controller/AuthController.java  # 登录（返回 token）/ 注册接口
    │   └── controller/UserController.java  # 需要 token 的示例接口 /api/user/me
    └── resources/application.yml   # 端口 8080，数据库 exam_auth，JWT 配置
```

## 数据库

- 库名：`exam_auth`
- 表：`t_user(id, username, password)`，username 唯一
- 账号：root，密码 `123456`（与本机 MySQL 一致）
- 已插入测试账号：**admin / 123456**

## 在 IDEA 中运行

1. `File → Open` 选择 `auth-backend` 文件夹，等待 Maven 下载依赖。
2. 运行 `AuthApplication` 的 `main` 方法。
3. 控制台出现 `Tomcat started on port 8080` 即启动成功。

> JDK 建议选 17 或 21（本机已装）。若只有 JDK 26，编译目标也是 17，可正常使用。

### 关于 JDK 26 的启动警告

如果用 JDK 26 运行，启动时可能打印：
```
WARNING: A restricted method in java.lang.System has been called
WARNING: java.lang.System::load has been called by org.apache.tomcat.jni.Library ...
```
这**只是警告，不影响运行**。原因是 JDK 24 起限制调用 `System.load` 这类原生方法，Tomcat 加载底层原生库时会触发提示。

项目已通过以下方式消除：
- `pom.xml` 的 `spring-boot-maven-plugin` 已配 `--enable-native-access=ALL-UNNAMED`（`mvn spring-boot:run` 时生效）；
- 新增 IDEA 运行配置 `.idea/runConfigurations/AuthApplication.xml`，已带 VM 参数 `--enable-native-access=ALL-UNNAMED`（IDEA 直接点 Run 时生效）。

如果改完配置后需要让 IDEA 重新加载：**完全退出 IDEA 再重新打开项目**。若出现两个 `AuthApplication` 运行配置，右键删除旧的即可。
换用 JDK 21 运行则不会出现这条警告。

## 接口说明

### 1. 注册

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{ "username": "zhangsan", "password": "abc123" }
```

成功返回：`{ "code": 200, "msg": "success", "data": null }`
用户名重复返回：`{ "code": 500, "msg": "用户名已存在", "data": null }`

### 2. 登录

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "123456" }
```

成功返回（多了 token，24 小时有效）：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiI..."
  }
}
```
密码错误返回：`{ "code": 500, "msg": "用户名或密码错误", "data": null }`

### 3. 需要登录的接口（带 token 访问）

除登录/注册外，所有 `/api/**` 接口都要在请求头带 token，否则返回 `401`：

```
GET http://localhost:8080/api/user/me
Authorization: Bearer <登录返回的token>
```

成功返回：
```json
{ "code": 200, "msg": "success", "data": { "id": 1, "username": "admin" } }
```

不带 token 访问：
```json
{ "code": 401, "msg": "未登录或登录已过期", "data": null }
```

## JWT 配置（application.yml）

```yaml
jwt:
  secret: auth-backend-demo-secret-key-2026-change-me   # 至少 32 字符，正式项目请改掉
  expire-hours: 24                                       # token 有效期（小时）
```

## 说明

- 练习项目密码明文存储，真实项目请改用 BCrypt 等加密方式。
- 已配置跨域（`CorsConfig`），前端 Vue / uniapp 可直接调用。
