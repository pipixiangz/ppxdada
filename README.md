## ppxdada_backend

### 项目描述
开发了一个基于 Spring Boot、Redis、ChatGLM、RxJava、SSE、Vue3、Arco Design 和 Pinia 的 AI 答题应用平台。用户可以通过 AI 快速生成题目并制作应用，管理员审核后，用户可以在线答题，并根据多种评分算法或 AI 得到回答总结。管理员还可以集中管理全站内容并进行统计分析。

### 库表设计

根据业务需求设计了用户、应用、题目、评分结果、用户答题等表结构。题目表采用 JSON 格式存储复杂嵌套题目和选项，便于维护和扩展，通过添加 appId 索引提高检索性能。

### 评分模块

采用策略模式实现了多种用户回答评分算法（如统计得分、AI 评分等），全局执行器会扫描策略类上的自定义注解并选取相应策略，提升系统的可扩展性。

### 通用 AI 服务

封装了基于 ChatGLM 的通用 AI 服务，通过配置类自动读取密钥配置，初始化 AI 客户端 Bean，便于全局使用。

### 文件管理服务

封装了基于腾讯云 COS 的通用文件管理服务，通过配置类自动读取密钥配置，初始化 COS 客户端 Bean，便于全局使用。

### AI 生成题目

通过优化 Prompt（如系统预设、少样本学习、任务拆解等）使 AI 返回 JSON 格式的题目内容，便于后端处理。由于 AI 生成题目较慢，选用 ChatGLM 的流式 API，并通过 SSE 实时推送单道题目给前端，提高用户体验。

### 异步数据流处理

基于 RxJava 的操作符链式调用处理 AI 异步数据流，通过 map 获取并处理字符串、filter 过滤空值、flatMap 映射字符串为单个字符，再通过括号平衡算法拼接出单道题目，使逻辑清晰简洁。

### 防重复提交

采用雪花算法为每次答题分配唯一 id，并通过数据库主键实现幂等设计，防止用户多次提交重复答案，避免生成重复数据。

### 缓存优化

使用 Caffeine 本地缓存答案 Hash 对应的 AI 评分结果，提高评分性能（从 10s 到 5ms），并通过 Redisson 分布式锁解决缓存击穿问题。

### 分表设计

为应对用户答题数增长，基于 Sharding JDBC 配置取模分片算法，根据应用 id 对用户答题记录进行分表，提高单表查询性能和可扩展性。

### 线程池优化

由于服务器资源有限，为会员创建核心线程数更大的隔离线程池，确保普通用户不会影响会员使用 AI 的体验。

### 统计分析

后端基于 MyBatis 注解自定义 SQL 实现热门应用和回答分布的统计，前端通过 ECharts 实现数据可视化。

### 接口文档

使用 Knife4j 和 Swagger 自动生成后端接口文档，并通过编写 ApiOperation 等注解补充接口说明，避免了手动编写和维护文档的麻烦。


![image](https://github.com/pipixiangz/ppxdada/blob/main/img/homepage.jpg)

![image](https://github.com/pipixiangz/ppxdada/blob/main/img/UserManagement.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/AnswerManagement.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/answer.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/createQuestions.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/createApps.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/MyAnswers.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/questionManagement.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/scoringManagement.jpg)
![image](https://github.com/pipixiangz/ppxdada/blob/main/img/Statistics.jpg)
