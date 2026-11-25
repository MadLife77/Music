# VS Code 启动前端 + IDEA 启动后端 操作指南

> 适用环境：`C:\Users\Madlife\Desktop\毕业设计`，JDK 17（`C:\JAVA\JAVA17`）、Node 22.12.0、pnpm 9.15.2、MinIO（自编译）。
> 配套文档：[启动使用手册.md](启动使用手册.md)（命令行方式）、[Copilot改动修复报告.md](Copilot改动修复报告.md)。

---

## 0. 启动前的前提（务必先确认）

1. **基础设施已启动**：MySQL(3306)、Redis(6379)、MinIO(9000)。若未启动，见本文第 4 节。
2. **VS Code / IDEA 必须重新打开过**：环境变量（PATH、`JAVA_HOME`、执行策略 `RemoteSigned`）是在本会话中设置的，**已经打开的老终端不会自动生效**。改完环境变量后请完全退出 VS Code / IDEA 再重新打开。
3. **后端 pom 必须是 Java 17**：`music-server/pom.xml` 中 `<java.version>17</java.version>`、父版本 `3.3.7`。若被改成 Java 25（Copilot 曾做过），IDEA 编译出的 jar 将无法在 `C:\JAVA\JAVA17` 运行，报 `UnsupportedClassVersionError: class file version 69.0`。
4. **PowerShell 执行策略应为 RemoteSigned**（否则 npm/pnpm 的 `.ps1` 被拦截）：
   ```powershell
   Get-ExecutionPolicy -Scope CurrentUser    # 期望 RemoteSigned
   ```

---

## 1. VS Code 启动前端（管理端 + 用户端）

### 1.1 打开项目

- 管理端：用 VS Code 打开 `C:\Users\Madlife\Desktop\毕业设计\music-admin`
- 用户端：另开一个 VS Code 窗口打开 `C:\Users\Madlife\Desktop\毕业设计\music-client`
  （两个项目各占一个窗口/终端，便于分别看日志）

### 1.2 配置 VS Code 终端

1. `Ctrl + ` ` 打开集成终端，确认是 **PowerShell** 或 **CMD**（两者均已配置好）。
2. 验证工具链可调用：
   ```powershell
   node -v      # v22.12.0
   pnpm -v      # 9.15.2
   ```
   若提示"无法识别"或"禁止运行脚本"：关闭整个 VS Code 重新打开（前提 2、4）。

### 1.3 安装依赖（仅首次或依赖变更时）

在对应项目根目录执行：
```powershell
cd C:\Users\Madlife\Desktop\毕业设计\music-admin
pnpm install --no-frozen-lockfile
```
用户端同理（把路径换成 `music-client`）。两端 `.npmrc` 已配置国内镜像与 `node-linker=hoisted`，正常约十几秒完成。

### 1.4 启动 dev server

终端里执行（**在各自项目根目录**）：
```powershell
# 管理端
cd C:\Users\Madlife\Desktop\毕业设计\music-admin
pnpm dev
```
看到 `VITE v6.4.6 ready` 与 `Local: http://localhost:8089/` 即成功。

```powershell
# 用户端（另一个终端）
cd C:\Users\Madlife\Desktop\毕业设计\music-client
pnpm dev
```
看到 `Local: http://localhost:8090/` 即成功。

### 1.5 VS Code 的"运行和调试"方式（可选）

若更习惯点界面按钮，可建 `.vscode/launch.json`（每个项目各建一份）：

管理端 `music-admin/.vscode/launch.json`：
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "node",
      "request": "launch",
      "name": "Vite: admin",
      "cwd": "${workspaceFolder}",
      "runtimeExecutable": "pnpm",
      "runtimeArgs": ["dev"],
      "console": "integratedTerminal",
      "skipFiles": ["<node_internals>/**"]
    }
  ]
}
```
用户端把 `"name"` 改为 `Vite: client` 即可。然后按 `F5` 启动。若 runtimeExecutable 解析不到，改用 `${env:APPDATA}\..\Local\...` 的 pnpm 绝对路径，或直接用终端命令（更稳妥）。

### 1.6 停止

在对应终端按 `Ctrl + C`。

---

## 2. IDEA 启动后端（Spring Boot）

### 2.1 打开项目

IDEA 打开 `C:\Users\Madlife\Desktop\毕业设计\music-server`（Maven 项目，等待依赖下载完成）。

### 2.2 关键：设置 Project SDK 为 Java 17

> 这一步最重要——Copilot 曾把项目改成 Java 25，若 IDEA 用 Java 25 编译，生成的 jar 在 `C:\JAVA\JAVA17` 上跑不起来。

1. `File → Project Structure → Project`（或 `Ctrl+Alt+Shift+S`）
2. **SDK** 选择 `C:\JAVA\JAVA17`（若列表没有，点 `Add JDK...` 选该目录）
3. **Language level** 选 `17`
4. `Modules → Sources` 中 Language level 也设为 17
5. `Settings → Build, Execution, Deployment → Compiler → Java Compiler`，把 **Target bytecode version** 设为 17
6. 确认 `pom.xml` 中 `<java.version>17</java.version>`、`<version>3.3.7</version>`（父版本）

### 2.3 Maven 配置

`Settings → Build, Execution, Deployment → Build Tools → Maven`：
- **Maven home**：`C:\Users\Madlife\Documents\apache-maven-3.6.3`
- **Runner → JRE**：选 `C:\JAVA\JAVA17`（与 Project SDK 一致）
- 若依赖下载慢，`settings.xml` 已配置阿里云镜像（`C:\Users\Madlife\Documents\apache-maven-3.6.3\conf\settings.xml`），无需额外设置。

### 2.4 启动

1. 打开启动类：`src/main/java/cn/edu/seig/vibemusic/VibeMusicServerApplication.java`
2. 右键 → `Run 'VibeMusicServerApplication'`（或点类旁绿色三角）
3. 控制台出现以下内容即成功：
   ```
   Tomcat started on port 8080 (http) with context path '/'
   Started VibeMusicServerApplication in ~3s
   ```

### 2.5 验证后端

浏览器或 PowerShell 访问：
```powershell
Invoke-RestMethod 'http://localhost:8080/banner/getBannerList' | Select-Object code   # 期望 0
```
或浏览器打开 `http://localhost:8080/banner/getBannerList`，应返回 `"code": 0` 和轮播数据。

---

## 3. 完整启动顺序（日常）

```
1. MySQL(3306) → 2. Redis(6379) → 3. MinIO(9000) → 4. IDEA 后端(8080) → 5. VS Code 两个前端(8089/8090)
```

| 服务 | 端口 | 启动方式 |
|---|---|---|
| MySQL | 3306 | 直启命令（服务为 Disabled，见第 4 节）|
| Redis | 6379 | 直启命令（见第 4 节）|
| MinIO | 9000 | 直启命令（见第 4 节）|
| 后端 | 8080 | IDEA Run |
| 管理端 | 8089 | VS Code `pnpm dev` |
| 用户端 | 8090 | VS Code `pnpm dev` |

访问地址：用户端 `http://localhost:8090`、管理端 `http://localhost:8089`（账号 `admin_172` / `123456abc`）、MinIO 控制台 `http://localhost:9001`（minioadmin/minioadmin）。

---

## 4. 前置服务启动命令（PowerShell）

```powershell
# MySQL（服务 Disabled，用直启；以管理员 PowerShell 执行 sc config 可永久启用）
Start-Process 'C:\MySQL\MySQL Server 8.0\bin\mysqld.exe' `
  -ArgumentList @('--defaults-file=C:\MySQL\my.ini') -WindowStyle Hidden

# Redis
$rd='C:\Users\Madlife\Documents\Redis-x64-5.0.14.1'
Start-Process "$rd\redis-server.exe" -ArgumentList "$rd\redis.windows.conf" -WindowStyle Hidden

# MinIO
$env:MINIO_ROOT_USER='minioadmin'; $env:MINIO_ROOT_PASSWORD='minioadmin'
Start-Process 'C:\Users\Madlife\Documents\vibe-tools\minio\bin\minio.exe' `
  -ArgumentList @('server','C:\Users\Madlife\Desktop\毕业设计\_reference\minio-data',
                  '--address',':9000','--console-address',':9001') -WindowStyle Hidden
```

一键检查是否都起来：
```powershell
foreach($p in 3306,6379,9000,8080,8089,8090){
  "{0}={1}" -f $p, $(if(Get-NetTCPConnection -State Listen -LocalPort $p -ErrorAction SilentlyContinue){'UP'}else{'DOWN'})
}
```

---

## 5. 常见问题（IDE 场景）

| 现象 | 原因 | 处理 |
|---|---|---|
| VS Code 中 `pnpm` 报"无法识别" / "禁止运行脚本" | 老终端没感知新 PATH / 执行策略 | 完全关闭 VS Code 重新打开；确认 `Get-ExecutionPolicy -Scope CurrentUser` = RemoteSigned |
| VS Code 终端 `node -v` 不是 v22.12.0 | 终端在 PATH 修改前打开 | 重开终端/VS Code |
| client 启动报 `ReferenceError: module is not defined` (tailwind.config.js) | 配置用了 CommonJS 但项目 `"type":"module"` | 已改为 `export default`，若再次出现检查该文件首行 |
| IDEA 启动后端报 `UnsupportedClassVersionError: 69.0` | 用 Java 25 编译了 | 按 2.2 把 Project SDK / Maven Runner JRE 都设为 `C:\JAVA\JAVA17`，确认 pom 是 java 17 + Boot 3.3.7，然后 `mvn clean` 重编 |
| IDEA 能跑但命令行 jar 跑不了 | IDEA 配了别的 JDK | 统一为 `C:\JAVA\JAVA17`，与本项目 pom 一致 |
| MySQL 在服务面板无法启动 | 服务 Disabled 且权限不足 | 用第 4 节直启命令；要永久启用需管理员执行 `sc config MySQL start= auto` |
| 后端起来但接口报数据库连接失败 | MySQL 未启动 | 先启动 MySQL（第 4 节）|
| 前端页面能开但图片碎图 / 9000 拒绝 | MinIO 未启动 | 先启动 MinIO（第 4 节）|
| 登录 401 | Redis 未启动或 token 过期 | 启动 Redis；重新登录 |

---

## 6. 与"命令行启动方式"的区别

- **命令行方式**（启动使用手册.md）：`java -jar xxx.jar`、`pnpm dev` 全部在 PowerShell 中执行，适合快速验证/部署。
- **IDE 方式**（本文档）：后端用 IDEA 的 Run 配置（可打断点调试），前端用 VS Code 终端 `pnpm dev`（可配合浏览器调试）。
- 两者**共用同一份代码、数据库和 MinIO**，不要同时用两种方式启动同一个端口（会端口冲突）。
