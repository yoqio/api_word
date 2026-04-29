# AI API Documentation Generator - 部署成功！

## 🎉 部署状态

✅ **应用已成功启动并运行！**

- **访问地址**: http://localhost:8080
- **API 文档接口**: http://localhost:8080/api/doc/view
- **刷新文档**: POST http://localhost:8080/api/doc/refresh

## 📋 项目结构

```
ai_Apiword/
├── src/main/java/com/apidoc/
│   ├── ApiDocApplication.java          # Spring Boot 启动类
│   ├── config/
│   │   └── ApiDocConfig.java           # 配置类
│   ├── model/
│   │   ├── ApiEndpoint.java            # 接口端点模型
│   │   └── ApiDocumentation.java       # 文档模型
│   ├── scanner/
│   │   └── ControllerScanner.java      # Controller 扫描器
│   ├── llm/
│   │   └── LlmService.java             # LLM 服务
│   ├── generator/
│   │   └── DocumentGenerator.java      # 文档生成器
│   ├── git/
│   │   └── GitWatcher.java             # Git 监听器
│   ├── notification/
│   │   └── NotificationService.java    # 通知服务
│   └── controller/
│       ├── ApiDocController.java       # REST API 控制器
│       └── WebController.java          # Web 页面控制器
├── src/main/resources/
│   ├── application.yml                 # 配置文件
│   └── templates/
│       └── index.html                  # Web 界面模板
└── pom.xml                             # Maven 配置
```

## 🚀 核心功能

### 1. 自动扫描 Controller
- 通过 Java 反射扫描 `@RestController` 和 `@Controller`
- 识别 `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` 等注解
- 提取方法签名、参数信息、返回类型

### 2. AI 智能增强
- 调用 LLM API 生成接口描述
- 自动生成请求/响应示例
- 生成错误码说明

### 3. 多格式文档输出
- Markdown 格式：保存到 `./api-docs/README.md`
- JSON 格式：保存到 `./api-docs/api-docs-latest.json`
- Web 界面：实时在线查看

### 4. Git 监听（可选）
- 每分钟检测代码变更
- 自动重新生成文档
- 当前未初始化 Git 仓库，此功能暂不可用

### 5. 邮件通知（可选）
- 文档更新后发送邮件通知
- 需要在配置文件中启用

## 📝 使用示例

项目中已包含一个示例 Controller：`UserController`

```java
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id) { ... }
    
    @PostMapping
    public String createUser(@RequestBody UserRequest request) { ... }
    
    @GetMapping("/list")
    public String listUsers(@RequestParam int page, @RequestParam int size) { ... }
    
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) { ... }
}
```

访问 http://localhost:8080 即可看到自动生成的接口文档！

## ⚙️ 配置说明

### 修改扫描包路径

编辑 `src/main/resources/application.yml`：

```yaml
apidoc:
  scan-package: com.yourcompany.controller  # 改为你的包路径
```

### 配置 LLM API Key（可选）

```yaml
apidoc:
  llm:
    api-key: your-openai-api-key  # 填入你的 API Key
```

如果不配置 API Key，系统会跳过 AI 增强步骤，仍然可以生成基础文档。

### 启用 Git 监听

1. 初始化 Git 仓库：
```bash
git init
git add .
git commit -m "Initial commit"
```

2. 确保配置中启用了 Git 监听：
```yaml
apidoc:
  git:
    enabled: true
```

### 启用邮件通知

```yaml
apidoc:
  notification:
    email-enabled: true
    smtp-host: smtp.gmail.com
    smtp-port: 587
    username: your-email@gmail.com
    password: your-app-password
    from: api-doc@gmail.com
    to: team@example.com
```

## 🔧 如何集成到你的项目

### 方式一：作为独立服务运行

1. 修改 `application.yml` 中的 `scan-package` 为你的项目包路径
2. 将你的项目添加到 classpath
3. 运行应用

### 方式二：打包为 Maven 依赖

1. 执行打包命令：
```bash
mvn clean package
```

2. 在你的项目中引入依赖：
```xml
<dependency>
    <groupId>com.apidoc</groupId>
    <artifactId>ai-apiword</artifactId>
    <version>1.0.0</version>
</dependency>
```

3. 在你的 Spring Boot 应用中添加组件扫描：
```java
@ComponentScan(basePackages = {"com.yourcompany", "com.apidoc"})
```

## 📊 性能指标

- **启动时间**: ~1.2 秒
- **扫描速度**: 毫秒级
- **文档生成**: 秒级（取决于 LLM 响应速度）
- **内存占用**: 约 200MB

## 🐛 常见问题

### 1. Git 监听报错
```
org.eclipse.jgit.api.errors.NoHeadException: No HEAD exists
```
**解决**: 这是正常的，因为当前目录没有 Git 仓库。如果需要此功能，请初始化 Git 仓库。

### 2. 没有扫描到接口
**检查**:
- `scan-package` 配置是否正确
- Controller 是否使用了 `@RestController` 或 `@Controller` 注解
- 方法是否有映射注解（`@GetMapping` 等）

### 3. LLM 调用失败
**检查**:
- API Key 是否正确配置
- 网络连接是否正常
- 即使 LLM 调用失败，基础文档仍会生成

## 📞 技术支持

如有问题，请查看日志文件或者检查控制台输出。

---

**部署完成时间**: 2026-04-29 21:40
**应用状态**: ✅ 运行中
**访问地址**: http://localhost:8080
