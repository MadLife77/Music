# Copilot 改动后故障修复报告

时间：2026-11-24 17:08。背景：Copilot 修改了项目后，用户报告三个问题：① VS Code 中 `pnpm dev` 无法启动前端；② 后端通过 IDEA 2022 能启动；③ MySQL 无法启动。

## 一、现象复现与根因

| 问题 | 实测结果 | 根因 |
|---|---|---|
| 前端 pnpm dev 无法启动（VS Code） | 命令行实测两端 `pnpm dev` 均能正常启动；VS Code 中失败 | PowerShell 执行策略 `Restricted` 阻止 `.ps1`（npm/pnpm 的启动脚本）。上一轮已改为 CurrentUser=`RemoteSigned`，**需重开 VS Code 终端生效**。此外 client 暴露出 `tailwind.config.js` 的 ESM/CJS 问题（见下） |
| 后端 IDEA 能启动，命令行启动失败 | 报错 `UnsupportedClassVersionError: class file version 69.0`，当前 JVM 只支持 61.0 | Copilot 把 `pom.xml` 改成 `java.version=25` + Spring Boot 3.5.16，IDEA 使用了 Java 25 所以能启动；但项目要求/用户指定的 `JAVA_HOME=C:\JAVA\JAVA17` 是 Java 17，jar 无法运行 |
| MySQL 无法启动 | 服务 `MySQL` 为 `Stopped/Disabled`；用 `mysqld --defaults-file=C:\MySQL\my.ini` 直启完全正常（8.0.45 ready） | 服务被禁用，服务管理器/IDE 中启动会被拒；直启方式可用；启用服务需管理员权限（当前 `sc config` 返回 Access denied） |
| 用户端页面空白（附带发现） | `ReferenceError: module is not defined` in `tailwind.config.js:2` | client 的 `package.json` 为 `"type":"module"`，但 `tailwind.config.js` 用 CommonJS `module.exports`；Node 22 严格按 ESM 解析导致失败（Node 20 未暴露） |

## 二、修改点

1. `music-server/pom.xml`：`java.version` 25 → **17**；Spring Boot 3.5.16 → **3.3.7**（原稳定版本，保留 Copilot 加的 lombok 版本与 compiler 插件、我此前加的 spring-security-crypto 依赖）。原文件备份为 `pom.xml.copilot-backup`。
2. `music-client/tailwind.config.js`：`module.exports = {` → `export default {`（ESM 语法，与 `"type":"module"` 一致）。
3. 服务恢复（非文件改动）：以 `mysqld --defaults-file=C:\MySQL\my.ini` 直启 MySQL；启动 Redis、MinIO、后端 jar、admin/client dev server。
4. 后端已用 JDK17 重新 `mvn clean package`，新 jar 生成于 17:05:24。

## 三、验证结果（全部通过）

- 后端：用 `C:\JAVA\JAVA17\bin\java.exe -jar` 启动成功，`Started VibeMusicServerApplication in 2.816s`；`/banner/getBannerList` code=0，`/admin/login` code=0（token 195 字符）。
- 前端：admin `pnpm dev` → VITE v6.4.6 ready，8089 监听；client `pnpm dev` → ready，8090 监听，HTTP 200。
- 用户端浏览器：33 张图片、0 碎图，页面正常渲染。
- MinIO 直链：图片 HEAD 200 / image/png。
- 全端口：3306、6379、9000、8080、8089、8090 全部 UP。

## 四、仍需你处理 / 注意事项

1. **VS Code 终端**：请**关闭并重新打开** VS Code（或其集成终端），使 RemoteSigned 执行策略与新 PATH 生效；否则 `pnpm dev` 仍可能被旧终端会话的执行策略拦截。
2. **MySQL 服务启用需管理员权限**：当前服务仍是 `Disabled`。若希望像正常服务一样启动/停止，请在**管理员 PowerShell** 执行：
   ```powershell
   sc.exe config MySQL start= auto
   Start-Service MySQL
   ```
   在此之前请用直启命令：`Start-Process 'C:\MySQL\MySQL Server 8.0\bin\mysqld.exe' -ArgumentList '--defaults-file=C:\MySQL\my.ini' -WindowStyle Hidden`。
3. **IDEA 中运行后端**：IDEA 若使用 Java 25，则可运行 Copilot 编译的 jar；但当前 pom 已恢复为 Java 17，请在 IDEA 中把 Project SDK 设为 `C:\JAVA\JAVA17`（与你设定的 JAVA_HOME 一致），否则 IDEA 编译时可能再次生成 Java 25 字节码。
4. **Copilot 后续改动**：若 Copilot 再次修改 pom 的 java.version 或 package.json 的 type，需按本报告恢复。
