# Vibe Music 🎶

一个基于 **Spring Boot 3 + Vue 3** 的前后端分离音乐网站。本仓库将原开源项目的三个子项目整合在一起，并附带数据库脚本，方便一次性克隆、部署和运行。

本项目参照以下三个上游仓库（致谢原作者(https://github.com/Alex-LiSun/vibe-music-server/commits?author=Alex-LiSun)），功能与结构上与其保持一致：

- [Vibe Music Server](https://github.com/Alex-LiSun/vibe-music-server) — 后端 API 服务
- [Vibe Music Admin](https://github.com/Alex-LiSun/vibe-music-admin) — 后台管理系统
- [Vibe Music Client](https://github.com/Alex-LiSun/vibe-music-client) — Web 音乐播放器

---

## 目录

- [项目组成](#项目组成-)
- [技术栈](#技术栈-)
- [系统需求](#系统需求-)
- [目录结构](#目录结构)
- [安装与启动](#安装与启动-)
- [项目脚本](#项目脚本-)
- [演示与截图](#演示与截图-)
- [常见问题](#常见问题-faq-)
- [免责声明](#免责声明-)
- [许可证与贡献](#许可证与贡献-)

---

## 项目组成 ✨

### 1. Vibe Music Server（后端，`music-server/`）

后端 API 服务，基于 **Spring Boot 3** 构建，采用 Java 17、Maven、MyBatis-Plus、MySQL、Redis 和 MinIO，为客户端和管理端提供数据支持与业务逻辑处理。

- **用户认证与管理**：注册、登录、信息修改、头像上传、注销；管理员可查询、禁用/启用用户。
- **内容管理**：歌手、歌曲、歌单的增删改查；歌曲文件上传；首页轮播图管理。
- **用户互动**：歌曲/歌单评论的发表与删除；歌曲/歌单收藏与取消收藏；用户反馈提交与处理。
- **文件服务**：使用 MinIO 存储音乐文件、头像、封面等静态资源。
- **权限控制**：基于 JWT 与角色的 API 访问控制。
- **数据缓存**：利用 Redis 缓存热点数据。
- **邮件服务**：支持发送验证码等邮件通知（可选，需自行配置 SMTP）。

### 2. Vibe Music Admin（后台管理，`music-admin/`）

基于 [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) 模板开发的后台管理系统。管理员可通过本系统：

- 用户管理：查看用户列表与详情、禁用或启用账号。
- 歌手管理：添加、编辑、删除歌手，维护歌手资料。
- 歌曲管理：添加、编辑、删除歌曲，管理歌曲资源。
- 歌单管理：创建、编辑、删除歌单，管理推荐/用户歌单。
- 反馈管理：查看、处理或删除用户反馈。
- 轮播图管理：添加、编辑、删除首页轮播图。

### 3. Vibe Music Client（Web 播放器，`music-client/`）

现代化 Web 音乐播放器，界面设计借鉴了 [GlassMusicPlayer](https://github.com/XiangZi7/GlassMusicPlayer)。

- **游客**：浏览歌曲/歌手/歌单，搜索与播放音乐，暗黑模式切换。
- **注册用户**：登录/注销、编辑资料与头像、个性化推荐、评论点赞、收藏歌曲/歌单、歌曲下载。

---

## 技术栈 🛠️

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3、Java 17、Maven、MyBatis-Plus、MySQL 8、Redis、Druid、JWT (java-jwt)、MinIO、Lombok、Spring Boot Validation、Java Mail |
| 管理端 | Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、Tailwind CSS、ECharts |
| 用户端 | Vue 3、Vite、TypeScript、Pinia、Element Plus、Tailwind CSS、Artplayer |
| 包管理器 | pnpm |

---

## 系统需求 ⚙️

| 软件 | 版本要求 |
|---|---|
| JDK | 17 或更高版本 |
| Maven | 3.6 或更高版本 |
| Node.js | `^18.18.0 \|\| ^20.9.0 \|\| >=22.0.0`（管理端）；`>=18.12.0`（用户端）。仓库 `music-admin/.nvmrc` 给出了推荐版本 |
| pnpm | `>=9` |
| MySQL | 8.0 或更高版本 |
| Redis | 推荐 6.0 或更高版本 |
| MinIO | 最新稳定版 |

---

## 目录结构

```
.
├── music-server/    # 后端 Spring Boot 服务（默认端口 8080）
├── music-admin/     # 后台管理系统（默认端口 8089）
├── music-client/    # Web 音乐播放器（默认端口 8090）
└── music.sql        # 数据库建表脚本 + 种子数据
```

> MinIO 桶数据（`vibe-music-data`）、依赖目录（`node_modules`）、构建产物（`target`/`dist`）未纳入版本控制，需自行准备。

---

## 安装与启动 🚀

以下步骤为通用流程，路径、端口与凭据请按你自己的环境调整。

### 1. 克隆仓库

```bash
git clone <本仓库地址>
cd <仓库目录>
```

### 2. 准备基础设施

- 安装并启动 **MySQL 8.0+**，创建数据库：
  ```sql
  CREATE DATABASE vibe_music CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  ```
  导入本仓库的 `music.sql`（含建表与种子数据）。
- 安装并启动 **Redis**（默认端口 6379）。
- 安装并启动 **MinIO**，创建名为 `vibe-music-data` 的 Bucket，并设置允许匿名读取（前端直接通过 URL 加载图片/音频）。
- 音乐与图片资源文件需自行放入 MinIO 桶；原项目提供的下载方式见[原仓库说明](https://github.com/Alex-LiSun/vibe-music-server#文件下载-)。

### 3. 配置后端

修改 `music-server/src/main/resources/application.yml`，将下列占位项改为你自己的环境：

```yaml
spring:
  datasource:
    url: jdbc:mysql://YOUR_MYSQL_HOST:3306/vibe_music?useUnicode=true&characterEncoding=utf-8&useSSL=false
    username: YOUR_MYSQL_USER
    password: YOUR_MYSQL_PASSWORD
  data:
    redis:
      host: YOUR_REDIS_HOST
      port: 6379
      password: YOUR_REDIS_PASSWORD   # 无密码可留空
      database: 1
  mail:                                # 可选：需要验证码功能时配置
    host: smtp.example.com
    username: your-email@example.com
    password: YOUR_EMAIL_APP_PASSWORD

minio:
  endpoint: http://YOUR_MINIO_HOST:9000
  accessKey: YOUR_MINIO_ACCESS_KEY
  secretKey: YOUR_MINIO_SECRET_KEY
  bucket: vibe-music-data
```

> 请勿将生产环境的真实密码提交到代码库，建议使用环境变量或本地未跟踪的配置文件管理。

### 4. 启动后端（端口 8080）

```bash
cd music-server
mvn clean package -DskipTests
java -jar target/vibe-music-server-*.jar
```

或使用 IDEA 打开 `music-server`，将 Project SDK 与 Maven Runner JRE 设置为 JDK 17，运行 `cn.edu.seig.vibemusic.VibeMusicServerApplication`。

### 5. 配置并启动前端

两个前端的 API 地址默认指向后端服务。若后端地址不同，修改对应配置：

- 管理端：`music-admin/src/utils/http/index.ts` 中的 `baseURL`
- 用户端：`music-client/src/utils/http.ts` 中的 `baseURL`（也可参考 `.env.development` 中的 `VITE_APP_BASE_API`）

```bash
# 后台管理 8089
cd music-admin
pnpm install
pnpm dev

# Web 播放器 8090
cd ../music-client
pnpm install
pnpm dev
```

> 提示：若 `pnpm dev` 在 PowerShell 报"禁止运行脚本"，可将当前用户执行策略设为 `RemoteSigned` 后重开终端。

### 6. 访问

| 服务 | 默认地址 |
|---|---|
| Web 播放器 | http://localhost:8090 |
| 后台管理 | http://localhost:8089 |
| 后端 API | http://localhost:8080 |
| MinIO 控制台 | http://localhost:9001 |

管理端可使用数据库种子中的管理员账号登录（账号密码见 `music.sql` 的 `tb_admin` 表；种子密码仅供演示，部署到公网前请务必更换）。

---

## 项目脚本 📜

**后端（Maven）**

- `mvn clean`：清理构建产物
- `mvn compile`：编译
- `mvn test`：运行测试
- `mvn package`：打包为可执行 JAR
- `mvn spring-boot:run`：开发模式启动
- `java -jar target/*.jar`：运行打包后的 JAR

**前端（pnpm）**

- `pnpm dev`：启动开发服务器
- `pnpm build`：构建生产版本
- `pnpm preview`：预览生产构建
- `pnpm lint` / `pnpm format`：代码检查与格式化

---

## 演示与截图 📺

- 演示视频：[Bilibili](https://www.bilibili.com/video/BV1tKJ8z8E6z/)
- 原项目截图：[管理端](https://github.com/Alex-LiSun/vibe-music-admin#项目截图-) · [用户端](https://github.com/Alex-LiSun/vibe-music-client#项目截图-)

---

## 常见问题 (FAQ) ❓

- **后端启动失败，提示数据库连接错误？**
  检查 `application.yml` 中 `spring.datasource` 的 URL、用户名、密码；确认 MySQL 已运行、数据库已创建且字符集正确、用户有访问权限。
- **后端启动失败，提示 Redis 连接错误？**
  检查 `spring.data.redis` 的 host、port、password；确认 Redis 已运行且可达。
- **文件上传失败或图片无法访问？**
  检查 `minio` 配置；确认 MinIO 已运行、`vibe-music-data` Bucket 已创建、Bucket 策略允许匿名读取/应用有读写权限；检查 `spring.servlet.multipart` 的文件大小限制。
- **端口冲突？**
  修改 `application.yml` 的 `server.port`，或结束占用 8080/8089/8090 端口的进程。
- **前端 API 无法访问？**
  确认后端已启动；检查前端 `baseURL` / `VITE_APP_BASE_API` 与后端地址一致；用浏览器开发者工具 Network 面板查看请求与 CORS 错误；检查防火墙/代理设置。
- **Java 版本错误（class file version 不匹配）？**
  请使用 JDK 17 编译与运行，并确保 `pom.xml` 中 `java.version` 为 17。

---

## 免责声明 ⚠️

本项目仅供学习和技术研究使用。应用展示的音乐、用户数据等均由你自行部署的服务承载，请在遵守所在地法律法规与版权政策的前提下使用：

- **请勿用于任何商业用途。**
- 项目作者不对使用本项目产生的任何直接或间接问题、数据安全风险、版权纠纷或经济损失承担责任。
- 使用者需自行确保数据来源合法合规，以及依赖服务的安全稳定运行。

---

## 许可证与贡献 📄❤️

本项目采用 MIT 许可证。欢迎提交 Issue、Pull Request 或提出建议。
