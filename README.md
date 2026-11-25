# Vibe Music 🎶

一个基于 **Spring Boot 3 + Vue 3** 的前后端分离音乐网站（毕业设计项目），包含管理端、用户端和后端 API 服务。

本项目为本地复刻部署版本，由三个子项目 + 数据库脚本组成，可在本机完整运行：歌曲/歌手/歌单/轮播管理、用户注册登录、收藏评论、在线播放（MinIO 对象存储）等功能。

---

## 一、技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.3.7、MyBatis-Plus 3.5.9、MySQL 8、Redis、Druid、JWT (Auth0 java-jwt)、MinIO 8.5.9、Lombok、Spring Security Crypto |
| 管理端 | Vue 3.5、Vite 6、TypeScript、Element Plus、Pinia、Vue Router 4、ECharts、Tailwind CSS、@pureadmin 组件库 |
| 用户端 | Vue 3.5、Vite 6、TypeScript、Element Plus、Pinia（持久化）、Artplayer、unplugin-auto-import、Tailwind CSS |
| 包管理 | pnpm 9.15.2 |
| 对象存储 | MinIO（桶 `vibe-music-data`）|

原项目参考（致谢）：
- [vibe-music-server](https://github.com/Alex-LiSun/vibe-music-server)
- [vibe-music-admin](https://github.com/Alex-LiSun/vibe-music-admin)
- [vibe-music-client](https://github.com/Alex-LiSun/vibe-music-client)

---

## 二、目录结构

```
.
├── music-admin/        # 管理端（Vue3 + Vite，端口 8089）
├── music-client/       # 用户端（Vue3 + Vite，端口 8090）
├── music-server/       # 后端（Spring Boot，端口 8080）
└── music.sql           # 数据库建表 + 种子数据（11 张表）
```

> 说明：MinIO 桶数据（`vibe-music-data/`）、本地工具链（`_reference/`）、`node_modules`、`target` 等大文件/本地数据**未纳入版本控制**，请自行准备。文件类资源需自行准备 MinIO 桶 `vibe-music-data` 并设置匿名只读策略。

---

## 三、环境要求

| 软件 | 版本 | 说明 |
|---|---|---|
| JDK | 17（`JAVA_HOME=C:\JAVA\JAVA17`） | 后端编译与运行 |
| Maven | 3.6+ | 已配置阿里云镜像 |
| Node.js | 22.12.0（见 `.nvmrc`） | 前端 |
| pnpm | 9.15.2 | 前端包管理 |
| MySQL | 8.0+ | 数据库 `vibe_music` |
| Redis | 5+/6+ | JWT token 缓存（db1） |
| MinIO | 自编译稳定版 | 桶 `vibe-music-data`，公开读 |

---

## 四、快速启动

### 1. 初始化数据库

```powershell
$MYSQL='C:\MySQL\MySQL Server 8.0\bin\mysql.exe'
& $MYSQL -uroot -p123456 -e "CREATE DATABASE IF NOT EXISTS vibe_music DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
Get-Content 'music.sql' -Encoding UTF8 | & $MYSQL -uroot -p123456 --default-character-set=utf8mb4 vibe_music
```

导入后应有 11 张表（用户 64、歌曲 502、歌手 124、歌单 18 等种子数据）。

### 2. 启动基础设施（MySQL / Redis / MinIO）

```powershell
# MySQL（服务被禁用时用直启）
Start-Process 'C:\MySQL\MySQL Server 8.0\bin\mysqld.exe' `
  -ArgumentList @('--defaults-file=C:\MySQL\my.ini') -WindowStyle Hidden

# Redis
$rd='C:\Users\Madlife\Documents\Redis-x64-5.0.14.1'
Start-Process "$rd\redis-server.exe" -ArgumentList "$rd\redis.windows.conf" -WindowStyle Hidden

# MinIO
$env:MINIO_ROOT_USER='minioadmin'; $env:MINIO_ROOT_PASSWORD='minioadmin'
Start-Process 'C:\Users\Madlife\Documents\vibe-tools\minio\bin\minio.exe' `
  -ArgumentList @('server','<桶数据目录>','--address',':9000','--console-address',':9001') -WindowStyle Hidden
```

### 3. 启动后端（端口 8080）

用 IDEA 打开 `music-server`，**Project SDK 设为 JDK 17**，运行 `cn.edu.seig.vibemusic.VibeMusicServerApplication`。
或命令行：

```powershell
mvn -f music-server/pom.xml clean package -DskipTests
java -jar music-server/target/vibe-music-server-0.0.1-SNAPSHOT.jar
```

### 4. 启动前端（端口 8089 / 8090）

```powershell
cd music-admin; pnpm install --no-frozen-lockfile; pnpm dev   # 管理端 8089
cd music-client; pnpm install --no-frozen-lockfile; pnpm dev  # 用户端 8090
```

---

## 五、访问地址与账号

| 服务 | 地址 |
|---|---|
| 用户端 | http://localhost:8090 |
| 管理端 | http://localhost:8089 |
| 后端 API | http://localhost:8080 |
| MinIO 控制台 | http://localhost:9001（minioadmin / minioadmin）|

**管理端账号**：`admin_172` / `123456abc`（另有 `admin_888` 等 79 个同密码账号）

**用户端**：使用种子用户邮箱 + 密码登录（发验证码功能因无真实 SMTP 不可用，密码登录正常）

---

## 六、配置文件说明

- `music-server/src/main/resources/application.yml`：数据库连接（root/123456）、Redis（127.0.0.1:6379 db1）、MinIO（127.0.0.1:9000 / minioadmin / vibe-music-data）、邮件占位配置。
- `music-admin/.env*`、`music-client/.env*`：前端端口、路由模式、API 地址（两端 API baseURL 均硬编码 `http://localhost:8080`）。
- **注意**：以上均为本地开发占位凭据，部署到公网前务必修改。

---

## 七、常见问题速查

| 现象 | 处理 |
|---|---|
| `pnpm dev` 报"禁止运行脚本" | 确认 PowerShell 执行策略 `RemoteSigned`，重开终端 |
| 后端报 `UnsupportedClassVersionError: 69.0` | pom 被改成 Java 25；改回 `<java.version>17</java.version>` + Boot 3.3.7，用 JDK17 重新编译 |
| 图片碎图 / 9000 连接拒绝 | 启动 MinIO；桶需设置匿名只读策略 |
| 头像上传弹窗一直转圈 | Redis 业务缓存残留坏链；清缓存 + 浏览器 localStorage（`AudioStore`）|
| MySQL 服务面板无法启动 | 服务为 Disabled，用 `mysqld --defaults-file=C:\MySQL\my.ini` 直启，或管理员执行 `sc config MySQL start= auto` |

---

## 八、启动顺序速查（IDE 方式）

```
MySQL(3306) → Redis(6379) → MinIO(9000) → 后端 IDEA Run(8080) → 前端 pnpm dev(8089/8090)
```

- VS Code 打开 `music-admin` / `music-client`，终端执行 `pnpm dev`（执行策略需为 `RemoteSigned`，改完环境变量请重开 VS Code）。
- IDEA 打开 `music-server`，Project SDK 与 Maven Runner 均设为 JDK 17，运行 `VibeMusicServerApplication`。
- 注意：pom 应为 `java.version=17` + Spring Boot `3.3.7`；若被改为 Java 25，IDEA 编译的 jar 在 JDK17 上将报 `UnsupportedClassVersionError`，需改回并用 JDK17 重新编译。
