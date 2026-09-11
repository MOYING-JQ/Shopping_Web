# project_test —— 墨莹（moying）电商网站前后端

> 基于 **SpringBoot 4 + MyBatis-Plus + MySQL** 构建的个人全栈项目，覆盖用户认证、商品分类、购物车、订单等电商核心业务，采用 **JWT 双 Token** 认证与 **AOP** 切面日志。

## 一、技术栈

| 分类     | 技术                           | 版本     |
| ------ | ---------------------------- | ------ |
| 开发语言   | Java                         | 21     |
| 核心框架   | SpringBoot                   | 4.1.1  |
| ORM 框架 | MyBatis-Plus                 | 3.5.17 |
| 数据库    | MySQL                        | 8.x    |
| 认证方案   | JWT（jjwt）                    | 0.12.6 |
| 安全框架   | Spring Security（BCrypt 密码哈希） | -      |
| 参数校验   | Bean Validation              | 3.1    |
| 切面编程   | Spring AOP（spring-aspects）   | 7.0.9  |
| 构建工具   | Maven                        | -      |

## 二、环境要求

| 依赖    | 要求                        |
| ----- | ------------------------- |
| JDK   | 21 及以上（SpringBoot 4 强制要求） |
| Maven | 3.6+（或使用项目自带 `mvnw`）      |
| MySQL | 8.0+                      |

## 三、快速启动

1. **克隆仓库**

```bash
git clone <仓库地址>
```

2. **修改数据库配置**

编辑 `src/main/resources/application.yaml`，填入你的 MySQL 账号密码：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/project_test?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

3. **启动项目**

```bash
# 方式一：使用 Maven Wrapper
./mvnw spring-boot:run

# 方式二：使用 IDEA 直接运行主启动类
```

4. **验证启动**

浏览器访问 `http://localhost:8080`，看到项目首页即启动成功。

## 四、项目结构

```
project_test
├── src/main/java/com/moying/project_test
│   ├── aspect        # AOP 切面（日志、权限等）
│   ├── common        # 通用返回结果、常量
│   ├── config        # 配置类（MyBatis-Plus、JWT、CORS 等）
│   ├── controller    # 控制层（接收请求）
│   ├── dto           # 数据传输对象（接收参数）
│   ├── entity        # 实体类（对应数据库表）
│   ├── exception     # 全局异常处理
│   ├── Filter        # 过滤器（JWT 拦截等）
│   ├── interception  # 拦截器
│   ├── mapper        # MyBatis-Plus Mapper 接口
│   ├── service       # 业务层接口 + impl 实现
│   ├── util          # 工具类
│   └── vo            # 视图对象（返回给前端）
├── src/main/resources
│   ├── mapper        # MyBatis XML 映射文件
│   ├── static        # 静态资源
│   └── templates     # 模板文件
└── pom.xml           # Maven 依赖配置
```

## 五、功能模块

| 模块   | 控制器                   | 功能说明                  |
| ---- | --------------------- | --------------------- |
| 用户认证 | `AuthController`      | 登录、注册、JWT Token 签发与刷新 |
| 用户管理 | `CustomerController`  | 用户信息查询与维护             |
| 商品分类 | `CategoryController`  | 分类的增删改查               |
| 商品管理 | `ProductController`   | 商品列表、详情、上下架           |
| 购物车  | `CartController`      | 加购、改数量、删除、清空          |
| 订单   | `OrderController`     | 下单、订单列表、订单状态管理        |
| 数据看板 | `DashboardController` | 后台统计概览数据              |

## 六、认证说明

- 登录成功返回 **Access Token + Refresh Token** 双 Token；
- 请求受保护接口时在请求头携带 Token：

```text
Authorization: Bearer <access_token>
```

- 密码使用 **BCrypt** 加密存储，数据库不保存明文。

## 七、注意事项

1. `application.yaml` 中的数据库密码等敏感信息**不要提交到 Git**，环境相关配置建议拆分到 `application-dev.yaml` 并通过 `.gitignore` 忽略；
2. 首次启动请先创建数据库 `project_test`，表结构可基于 `entity` 实体与 MyBatis-Plus 自动建表或导入 SQL 脚本；
3. 依赖下载使用阿里云公共仓库（已在 `pom.xml` 配置），国内环境拉取更快。

## 八、更新日志

| 版本              | 日期  | 说明             |
| --------------- | --- | -------------- |
| v0.0.1-SNAPSHOT | 开发中 | 基础框架搭建，核心模块开发中 |



## 九、实例展示

#### 配置后端ymal配置文件

##### 可以跟换jwt密钥自己定义（一定要大于32位）

![jwt](./实例图片/屏幕截图%202026-09-11%20150928.png)



配置图片存放的路径

path 更改图片存放位置

![图片](./实例图片/屏幕截图%202026-09-11%20152258.png)



#### 后端启动类启动

![启动类](./实例图片/屏幕截图%202026-09-11%20145235.png)



#### 前端项目启动

![cmd启动](file:///C:/Users/HUAWEI/Pictures/%E6%88%AA%E5%9B%BE/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202026-09-11%20150422.png)



#### 项目效果

![效果](./实例图片/屏幕截图%202026-09-11%20152828.png)

默认账号密码buyer   123456

管理员 卖家  admin  123123



![xiaoguo](./实例图片/屏幕截图%202026-09-11%20153236.png)



![qqq](./实例图片/屏幕截图%202026-09-11%20154348.png)





卖家中心![qq](./实例图片/屏幕截图%202026-09-11%20154443.png)

![drg](./实例图片/屏幕截图%202026-09-11%20154447.png)



![dsa](./实例图片/屏幕截图%202026-09-11%20154607.png)





![qw](./实例图片/屏幕截图%202026-09-11%20154724.png)





![wq](./实例图片/屏幕截图%202026-09-11%20155155.png)





支付系统目前还尚未开发（余额可支付）

![sda](./实例图片/屏幕截图%202026-09-11%20155259.png)





### 十。可扩展区域

        1. 支付系统

        2.购物车系统

        3. 接口高并发

#### 

© 2026 墨莹（moying）


