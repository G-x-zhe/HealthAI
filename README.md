# HealthAI · 智慧健康助手

基于大语言模型（LLM）与专业医药知识库的智能健康问答平台。支持 AI 多轮连续对话、医药知识检索、知识收藏、学习提醒等功能，采用前后端分离架构。

> ⚠️ 本项目内容仅供健康科普与学习参考，不能替代专业医生的诊断和治疗建议。

---

## 一、功能特性

- **用户认证**：注册 / 登录 / 登出 / 信息查询与修改，基于 JWT 无状态鉴权，登出 Token 加入 Redis 黑名单。
- **AI 智能问答**：接入本地 Ollama 大模型（DeepSeek），支持多轮上下文对话。
  - **双层会话记忆**：Redis 保存最近 10 轮短期上下文（TTL 2 小时），过期后自动从 MySQL 回捞历史重建，隔天续聊也不"失忆"。
  - **答案缓存**：Redis 缓存高频问题答案，配合布隆过滤器预判，降低大模型调用成本。
  - **并发与保护**：Redisson 分布式锁防重复提交、固定窗口限流（60 秒 10 次）、敏感词过滤。
- **医药知识库**：关键词 / 分类多条件分页检索、知识详情（浏览量自增）、分类列表；启动时预热热门知识到 Redis。
- **知识收藏**：收藏、取消、收藏状态检查、我的收藏列表（批量查询避免 N+1）。
- **学习提醒**：创建 / 查看 / 取消学习提醒，服务端强制归属校验防越权。
- **操作日志**：基于 Spring AOP + 自定义注解 `@OperationLog` 自动记录接口操作。
- **统一规范**：全局异常处理、统一响应结构 `Result<T>`、参数校验（`@Valid`）、全局日期格式（`yyyy-MM-dd HH:mm:ss`）。

---

## 二、技术栈

### 后端

| 分类 | 技术 | 版本 |
|---|---|---|
| 语言 / 框架 | Java / Spring Boot | 21 / 3.2.5 |
| 安全框架 | Spring Security + JWT (jjwt) | 0.12.5 |
| ORM | MyBatis + PageHelper | 3.0.3 / 2.1.0 |
| 数据库 | MySQL | 8.x |
| 缓存 / 锁 | Redis + Redisson | 3.27.0 |
| 大模型 | Spring AI + Ollama（deepseek-r1:1.5b） | Spring AI 1.0.0-M5 |
| 工具库 | Hutool、Lombok | 5.8.26 |

### 前端

| 分类 | 技术 |
|---|---|
| 框架 / 构建 | Vue 3.5（Composition API）+ Vite 6 |
| 路由 / 请求 | Vue Router 4 + Axios |
| UI 组件库 | Element Plus 2.9（中文本地化、医疗青绿主题） |

---

## 三、项目结构

```
HealthAi/
├── src/main/java/com/healthai/
│   ├── HealthAiApplication.java        # 启动类
│   ├── config/                         # 安全、JWT 过滤器、Redis、Jackson、跨域配置
│   ├── controller/                     # 5 个控制器（auth/chat/knowledge/collect/remind）
│   ├── service/                        # 业务接口与实现
│   ├── mapper/                         # MyBatis Mapper 接口
│   ├── entity/                         # 实体类
│   ├── dto/                            # 请求 / 展示对象
│   ├── common/                         # JWT、Result、布隆过滤器、敏感词、密码工具等
│   ├── aspect/                         # AOP 操作日志切面
│   └── exception/                      # 全局异常处理
├── src/main/resources/
│   ├── application.yaml                # 后端配置
│   └── mapper/                         # MyBatis XML 映射文件
└── frontend/                           # Vue3 前端工程
    └── src/
        ├── api/                        # 按模块封装的接口
        ├── router/                     # 路由与登录守卫
        ├── layouts/                    # 主布局（顶栏导航）
        ├── utils/                      # Axios 实例、工具函数
        └── views/                      # 6 个页面
```

---

## 四、快速开始

### 环境要求

- JDK 21、Maven 3.9+
- Node.js 18+（推荐 20+）
- MySQL 8.x、Redis（本地默认端口即可）
- [Ollama](https://ollama.com/) 并拉取模型：`ollama pull deepseek-r1:1.5b`

### 1. 初始化数据库

创建数据库（表结构请按开发文档中的建表脚本初始化，核心表如下）：

```sql
CREATE DATABASE healthai_db DEFAULT CHARACTER SET utf8mb4;
```

| 表名 | 用途 |
|---|---|
| `ai_user` | 用户账号 |
| `medical_knowledge` | 医药知识 |
| `ai_chat_history` | AI 对话历史（按 session_id 聚合会话） |
| `user_collect` | 用户知识收藏 |
| `study_remind` | 学习提醒 |

### 2. 启动后端

修改 `src/main/resources/application.yaml` 中的数据库密码与（生产环境）JWT 密钥：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/healthai_db?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的MySQL密码
```

启动：

```bash
# 确保 MySQL、Redis、Ollama 已运行
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`，已配置代理将 `/api` 转发到后端 `8080`，无需处理跨域。后端若改用其他端口，修改 `frontend/vite.config.js` 中的 `proxy.target` 即可。

---

## 五、接口一览（共 20 个）

统一响应结构：`{ "code": 200, "message": "成功", "data": ... }`；除知识库接口外均需在请求头携带 `Authorization: <JWT>`。

### 用户认证 `/api/auth`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/register` | 用户注册（公开） |
| POST | `/login` | 登录，返回 JWT（公开） |
| GET | `/info` | 获取当前用户信息 |
| POST | `/logout` | 登出（Token 加黑名单） |
| PUT | `/update` | 修改用户信息 |

### 医药知识 `/api/knowledge`（全部公开）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/search` | 关键词 / 分类分页搜索（`keyword,category,pageNum,pageSize`） |
| GET | `/detail/{knowledgeId}` | 知识详情（浏览量 +1） |
| GET | `/categories` | 分类列表 |

### AI 问答 `/api/chat`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/ask` | 智能提问，入参 `{question, sessionId}`，返回答案、模型、耗时、sessionId |
| GET | `/history` | 分页查询我的全部对话历史 |
| GET | `/history/session/{sessionId}` | 按会话查询对话记录（时间正序） |
| GET | `/history/detail/{chatId}` | 查询单条对话详情 |
| DELETE | `/history/session/{sessionId}` | 删除整个会话（同步清除 Redis 上下文） |

### 知识收藏 `/api/collect`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/add?knowledgeId=` | 收藏知识 |
| DELETE | `/remove?knowledgeId=` | 取消收藏 |
| GET | `/my` | 我的收藏列表 |
| GET | `/check?knowledgeId=` | 是否已收藏（`{collected: true/false}`） |

### 学习提醒 `/api/remind`

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/create` | 创建提醒（`title,content,remindTime,remindType`） |
| GET | `/my` | 我的提醒列表 |
| PUT | `/cancel/{remindId}` | 取消提醒（校验归属） |

---

## 六、前端页面

| 页面 | 路由 | 说明 |
|---|---|---|
| 登录 / 注册 | `/login`、`/register` | 品牌分栏设计，表单校验 |
| 知识广场 | `/knowledge` | 搜索、分类筛选、卡片网格、分页 |
| 知识详情 | `/knowledge/:id` | 正文、关键词、浏览量、收藏 |
| AI 问答 | `/chat` | 会话侧栏 + 气泡对话；sessionId 同步 URL，刷新自动恢复会话 |
| 个人中心 | `/profile` | 用户信息、我的收藏、学习提醒 |

---

## 七、核心设计说明

- **双层会话记忆**
  - 短期：Redis List 以 `RPUSH + LTRIM` 保存每会话最近 10 轮，时间严格正序，TTL 2 小时。
  - 长期：每轮问答异步落库 `ai_chat_history`；Redis 未命中时从 MySQL 取最近 10 轮重建上下文并回填，实现跨天 / 重启后的记忆延续。
- **答案缓存与布隆过滤器**：先用布隆过滤器判断问题"可能存在"再查 Redis，避免大量无效缓存查询；缓存命中的问答同样写入会话上下文，保证多轮记忆不断档。
- **鉴权链路**：`JwtAuthenticationFilter` 校验 Token（兼容裸 Token 与 `Bearer ` 前缀）并注入 `userId`，Spring Security 负责路径级放行与 401/403 JSON 响应。
- **前端请求层**：Axios 拦截器自动注入 Token、解包 `Result.data`、401/403 自动跳转登录；路由守卫拦截需登录页面并支持登录后回跳。

---

## 八、默认端口

| 服务 | 端口 |
|---|---|
| 后端 Spring Boot | 8080 |
| 前端 Vite | 5173 |
| MySQL | 3306 |
| Redis | 6379 |
| Ollama | 11434 |

---

## 九、免责声明

本项目仅用于技术学习与健康知识科普，AI 生成内容可能存在错误，不能作为疾病诊断或用药依据。如有身体不适，请及时前往正规医疗机构就诊。
