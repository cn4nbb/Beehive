# 🐝 Beehive

> 像蜂群一样协作，让线程池"活"起来。

轻量级动态线程池框架 —— 运行时调参、健康监控、自动告警，开箱即用。

---

## ✨ 特性

- 🎛️ **动态调参** — 运行时修改核心线程数、最大线程数、队列容量，无需重启
- 📦 **多种队列** — 内置 `LinkedBlockingQueue`、`ArrayBlockingQueue`、`SynchronousQueue` 等 7 种队列
- 🔢 **拒绝计数** — 透明包装 JDK 四种拒绝策略，自动统计拒绝次数，拒绝率一目了然
- 🔧 **弹性队列** — `ResizableCapacityLinkedBlockingQueue` 支持运行时动态调整容量，不再被 `final` 束缚
- 🩺 **健康监控** — 队列使用率、活跃线程数实时采集，超阈值自动触发告警
- 📢 **告警通知** — 支持 DingTalk 等通知渠道，自带频率限制，避免告警风暴
- ☁️ **配置中心** — 支持 Nacos、Apollo，配置变更秒级生效
- 🕸️ **Web 容器** — Tomcat、Jetty、Undertow 线程池一键纳入管理
- 🖥️ **可视化控制台** — 在线查看线程池状态、动态修改参数

---

## 🚀 快速开始

### 📋 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Spring Boot | 3.0.7+ |
| Maven | 3.6+ |

### 📥 引入依赖

```xml
<dependency>
    <groupId>edu.cnan.beehive</groupId>
    <artifactId>beehive-common-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### ⚙️ 配置文件

```yaml
beehive:
  executors:
    - threadPoolId: order-pool
      corePoolSize: 10
      maximumPoolSize: 20
      queueCapacity: 1024
      workQueue: LinkedBlockingQueue
      rejectedHandler: AbortPolicy
      keepAliveTime: 30
      alarm:
        enable: true
        queueThreshold: 80       # 队列使用率超过 80% 告警
        activeThreshold: 80      # 活跃线程超过 80% 告警
      notify:
        receives: "user1,user2"  # 通知接收人
        interval: 5              # 告警最小间隔（分钟）
```

### 💉 注入使用

```java
@Resource
private ThreadPoolExecutor orderPool;

public void process() {
    orderPool.execute(() -> doSomething());
}
```

就这么简单 🎉

---

## 🧱 模块结构

```
Beehive
├── core                          🔩 核心模块
│   ├── executor                  → BeehiveExecutor 增强线程池 + 注册中心
│   └── support                   → 队列枚举、拒绝策略枚举、弹性队列
├── spring-base                   🌱 Spring 集成（Bean 声明、后处理器）
├── starter
│   ├── common-spring-boot-starter    ⚙️ 公共自动配置
│   ├── apollo-spring-boot-starter    🚀 Apollo 配置刷新
│   ├── nacos-cloud-spring-boot-starter ☁️ Nacos 配置刷新
│   ├── dashboard-dev-spring-boot-starter 📊 控制台 API
│   └── adapter
│       └── web-spring-boot-starter   🕸️ Web 容器适配（Tomcat / Jetty / Undertow）
├── dashboard-dev                 🖥️ 可视化控制台
└── example
    ├── apollo-example            👀 Apollo 集成示例
    └── nacos-cloud-example       👀 Nacos 集成示例
```

---

## 🎯 核心设计

```
    ┌─────────────────────────────┐
    │  yml / Nacos / Apollo       │  外部配置
    └─────────────┬───────────────┘
                  ▼
    ┌─────────────────────────────┐
    │  BeehiveExecutorProperties  │  配置模型（字符串形式）
    └─────────────┬───────────────┘
                  ▼
    ┌─────────────────────────────┐
    │  ThreadPoolExecutorBuilder  │  构建器（名字 → 实例）
    └─────────────┬───────────────┘
                  ▼
    ┌─────────────────────────────┐
    │  BeehiveExecutor            │  增强线程池（计数 + 优雅关闭）
    └─────────────┬───────────────┘
                  ▼
    ┌─────────────────────────────┐
    │  BeehiveExecutorRegistry    │  全局注册中心（ConcurrentHashMap）
    └─────────────────────────────┘
```

> 配置层存字符串、构造层转实例、运行层做增强、注册层管全局 —— 各司其职，边界清晰。

---

## 📈 进度

`0.0.1-SNAPSHOT` · 施工中 🔧

- [x] ✅ `core` — 核心线程池与支撑类型
- [ ] 🔜 `spring-base` — Spring 集成
- [ ] 🔜 `starter` — 自动配置
- [ ] 🔜 `dashboard-dev` — 控制台
- [ ] 🔜 `example` — 示例

---

## 📄 许可

本项目仅供学习参考。
