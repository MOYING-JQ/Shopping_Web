# 电商商城系统 - 后端API接口文档

> 前端已就绪，本文档列出所有前端调用的接口，请按此实现后端。
> 
> - 前端请求 baseURL: `/api`（通过 vite proxy 转发到 `http://localhost:8080`）
> - 后端建议 context-path: `/api`
> - 所有接口统一返回格式: `{ "code": 200, "message": "操作成功", "data": T }`
> - 分页返回格式: `{ "code": 200, "message": "...", "data": { "records": [], "total": 0, "current": 1, "size": 10 } }`
> - 需要登录的接口，前端在请求头携带: `Authorization: Bearer {accessToken}`
> - **测试账号默认明文密码：`123456`**（数据库使用 BCrypt 加密存储，如 `$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi`）

---

## 数据库表结构

### t_user（用户表）

| 字段            | 类型             | 说明                    |
| ------------- | -------------- | --------------------- |
| id            | bigint PK AUTO | 用户ID                  |
| username      | varchar(50)    | 用户名                   |
| password      | varchar(100)   | 密码（BCrypt加密）明文：123456 |
| phone         | varchar(20)    | 手机号                   |
| email         | varchar(100)   | 邮箱                    |
| gender        | tinyint        | 0-未知 1-男 2-女          |
| role          | tinyint        | **0-买家 1-卖家**         |
| avatar        | varchar(255)   | 头像URL                 |
| nickname      | varchar(50)    | 昵称                    |
| level         | int            | 会员等级                  |
| status        | tinyint        | 0-禁用 1-启用             |
| register_time | datetime       | 注册时间                  |
| create_time   | datetime       | 创建时间                  |
| update_time   | datetime       | 更新时间                  |
| deleted       | tinyint        | 逻辑删除 0-未删 1-已删        |

### t_category（商品分类）

| 字段          | 类型             | 说明           |
| ----------- | -------------- | ------------ |
| id          | bigint PK AUTO | 分类ID         |
| name        | varchar(50)    | 分类名称         |
| parent_id   | bigint         | 父分类ID，0为一级分类 |
| sort        | int            | 排序号          |
| status      | tinyint        | 0-禁用 1-启用    |
| create_time | datetime       | 创建时间         |
| update_time | datetime       | 更新时间         |

### t_product（商品）

| 字段             | 类型             | 说明                                  |
| -------------- | -------------- | ----------------------------------- |
| id             | bigint PK AUTO | 商品ID                                |
| name           | varchar(100)   | 商品名称                                |
| category_id    | bigint         | 分类ID                                |
| price          | decimal(10,2)  | 价格                                  |
| original_price | decimal(10,2)  | 原价                                  |
| stock          | int            | 库存                                  |
| sales          | int            | 销量                                  |
| image          | varchar(255)   | 商品主图URL（**可选**，为空时前端根据商品名+分类自动生成图片） |
| description    | text           | 商品描述                                |
| status         | tinyint        | 0-下架 1-上架                           |
| create_time    | datetime       | 创建时间                                |
| update_time    | datetime       | 更新时间                                |
| deleted        | tinyint        | 逻辑删除 0-未删 1-已删                      |

### t_cart（购物车）

| 字段          | 类型             | 说明         |
| ----------- | -------------- | ---------- |
| id          | bigint PK AUTO | 购物车ID      |
| customer_id | bigint         | 用户ID       |
| product_id  | bigint         | 商品ID       |
| quantity    | int            | 数量         |
| selected    | tinyint        | 0-未选中 1-选中 |
| create_time | datetime       | 创建时间       |
| update_time | datetime       | 更新时间       |

### t_order（订单）

> 单笔订单对应一个商品，所有商品信息直接存 t_order 表，不再使用 t_order_item。

| 字段               | 类型             | 说明                            |
| ---------------- | -------------- | ----------------------------- |
| id               | bigint PK AUTO | 订单ID                          |
| order_no         | varchar(32)    | 订单编号                          |
| customer_id      | bigint         | 用户ID                          |
| seller_id        | bigint         | 卖家ID                          |
| product_id       | bigint         | 商品ID                          |
| product_name     | varchar(100)   | 商品名称(冗余)                      |
| product_image    | varchar(500)   | 商品图片(冗余)                      |
| price            | decimal(10,2)  | 成交单价                          |
| quantity         | int            | 购买数量                          |
| total_amount     | decimal(10,2)  | 订单总金额（= price × quantity）     |
| total_quantity   | int            | 商品总数量（= quantity）             |
| status           | tinyint        | 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消 |
| remark           | varchar(255)   | 订单备注                          |
| receiver_name    | varchar(50)    | 收货人                           |
| receiver_phone   | varchar(20)    | 收货电话                          |
| receiver_address | varchar(500)   | 收货地址                          |
| create_time      | datetime       | 创建时间                          |
| pay_time         | datetime       | 支付时间                          |
| ship_time        | datetime       | 发货时间                          |
| update_time      | datetime       | 更新时间                          |
| deleted          | tinyint        | 逻辑删除 0-未删 1-已删                |

---

## 接口列表

### 1. 认证模块（双Token认证）

#### 1.1 用户登录

- **POST** `/auth/login`

- **Headers**: `skipAuth: true`（前端标记跳过Token）

- **Body**:
  
  ```json
  { "username": "buyer", "password": "123456" }
  ```

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzI0NjQwMDAwLCJleHAiOjE3MjQ2NDE4MDB9.xxxxxxxxxxxxxxxxxxxx",
    "refreshToken": "a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4"
  }
  }
  ```

- **实现建议**:
  
  1. 验证用户名密码（BCrypt）
  2. 生成 accessToken（JWT，有效期30分钟）
  3. 生成 refreshToken（UUID或JWT，有效期7天）
  4. refreshToken 存入 Redis 或数据库
  5. 返回双Token

#### 1.2 用户注册

- **POST** `/auth/register`

- **Headers**: `skipAuth: true`

- **Body**:
  
  ```json
  {
  "username": "newuser",
  "password": "123456",
  "phone": "13800138000",
  "email": "user@example.com",
  "role": 0
  }
  ```

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "注册成功",
  "data": true
  }
  ```

- 密码用 BCrypt 加密后存储

#### 1.3 获取当前用户信息

- **GET** `/auth/info`

- **Headers**: `Authorization: Bearer {accessToken}`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "buyer",
    "phone": "13800138000",
    "email": "buyer@example.com",
    "gender": 1,
    "role": 0,
    "avatar": null,
    "nickname": "买家小明",
    "level": 1,
    "status": 1,
    "registerTime": "2024-08-01 10:00:00",
    "createTime": "2024-08-01 10:00:00",
    "updateTime": "2024-08-15 14:30:00"
  }
  }
  ```

#### 1.4 刷新Token

- **POST** `/auth/refresh`

- **Headers**: `skipAuth: true`

- **Body**: `{ "refreshToken": "a1b2c3d4..." }`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzI0NjQxMDAwLCJleHAiOjE3MjQ2NDI4MDB9.yyyyyyyyyyyyyyyyyyyy",
    "refreshToken": "z1y2x3w4v5u6t7s8r9q0p1o2n3m4l5k6j7i8h9g0f1e2d3c4"
  }
  }
  ```

- 验证 refreshToken 有效性，签发新的双Token，旧 refreshToken 失效

#### 1.5 退出登录

- **POST** `/auth/logout`

- **Body**: `{ "refreshToken": "a1b2c3d4..." }`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "退出成功",
  "data": true
  }
  ```

- 使 refreshToken 失效

#### 1.6 修改个人信息

- **PUT** `/auth/profile`

- **Headers**: `Authorization: Bearer {accessToken}`

- **Body**:
  
  ```json
  {
    "nickname": "买家小明",
    "phone": "13800138000",
    "email": "buyer@example.com",
    "gender": 1,
    "avatar": "https://example.com/avatar.jpg"
  }
  ```

- **字段说明**:
  
  | 字段       | 类型      | 必填  | 说明              |
  | -------- | ------- | --- | --------------- |
  | nickname | String  | 否   | 昵称，最长20字符       |
  | phone    | String  | 否   | 手机号（11位，正则校验）   |
  | email    | String  | 否   | 邮箱（格式校验）        |
  | gender   | Integer | 否   | 性别：0-未知 1-男 2-女 |
  | avatar   | String  | 否   | 头像URL           |

- **响应结果示例**:
  
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": {
      "id": 1,
      "username": "buyer",
      "phone": "13800138000",
      "email": "buyer@example.com",
      "gender": 1,
      "role": 0,
      "avatar": "https://example.com/avatar.jpg",
      "nickname": "买家小明",
      "level": 1,
      "status": 1,
      "registerTime": "2024-08-01 10:00:00",
      "updateTime": "2024-09-06 10:30:00"
    }
  }
  ```

- **实现建议**:
  
  1. 从 JWT 解析当前用户 ID，不接受前端传 userId
  2. 只更新传入的非空字段（nickname/phone/email/gender/avatar）
  3. username、role、level、status 等字段不允许通过此接口修改
  4. 返回更新后的完整用户信息，前端同步更新 Pinia store

#### 1.7 修改密码

- **PUT** `/auth/password`

- **Headers**: `Authorization: Bearer {accessToken}`

- **Body**:
  
  ```json
  {
    "oldPassword": "123456",
    "newPassword": "654321"
  }
  ```

- **字段说明**:
  
  | 字段          | 类型     | 必填  | 说明                           |
  | ----------- | ------ | --- | ---------------------------- |
  | oldPassword | String | 是   | 原密码（明文，后端 BCrypt 校验）         |
  | newPassword | String | 是   | 新密码（6-20位，明文，后端 BCrypt 加密存储） |

- **响应结果示例**:
  
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": true
  }
  ```

- **实现建议**:
  
  1. 从 JWT 解析当前用户 ID
  2. 用 BCrypt 校验 oldPassword 是否正确，不正确返回 400 + "原密码错误"
  3. newPassword 用 BCrypt 加密后更新
  4. 密码修改成功后，建议使当前 refreshToken 失效，强制重新登录
  5. 前端收到成功响应后自动清除本地 Token 并跳转登录页

---

### 2. 购物车模块（需要登录）

#### 2.1 获取购物车列表

- **GET** `/cart/list`

- **Headers**: `Authorization: Bearer {accessToken}`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "productId": 5,
      "productName": "MacBook Pro 14 M3",
      "productImage": "https://example.com/macbook.jpg",
      "price": 14999.00,
      "stock": 100,
      "quantity": 2,
      "selected": 1
    },
    {
      "id": 2,
      "productId": 8,
      "productName": "iPhone 15 Pro",
      "productImage": "https://example.com/iphone15.jpg",
      "price": 8999.00,
      "stock": 200,
      "quantity": 1,
      "selected": 0
    }
  ]
  }
  ```

- **SQL建议**:
  
  ```sql
  SELECT c.id, c.product_id, p.name as productName, p.image as productImage,
       p.price, p.stock, c.quantity, c.selected
  FROM t_cart c
  JOIN t_product p ON c.product_id = p.id
  WHERE c.customer_id = #{userId} AND p.deleted = 0
  ```

#### 2.2 获取购物车数量

- **GET** `/cart/count`

- **Headers**: `Authorization: Bearer {accessToken}`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": 3
  }
  ```

#### 2.3 添加商品到购物车

- **POST** `/cart/add`

- **Headers**: `Authorization: Bearer {accessToken}`

- **Body**: `{ "productId": 5, "quantity": 1 }`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "添加成功",
  "data": true
  }
  ```

- **实现建议**: 先查是否已有该商品，有则累加数量，无则新增

#### 2.4 修改数量

- **PUT** `/cart/quantity/{id}/{quantity}`

- **Headers**: `Authorization: Bearer {accessToken}`

- **示例**: `PUT /cart/quantity/1/3`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "修改成功",
  "data": true
  }
  ```

#### 2.5 修改选中状态

- **PUT** `/cart/select/{id}/{selected}`

- **Headers**: `Authorization: Bearer {accessToken}`

- selected: 0-取消 1-选中

- **示例**: `PUT /cart/select/1/0`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "修改成功",
  "data": true
  }
  ```

#### 2.6 全选/全不选

- **PUT** `/cart/select-all/{selected}`

- **Headers**: `Authorization: Bearer {accessToken}`

- **示例**: `PUT /cart/select-all/1`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "修改成功",
  "data": true
  }
  ```

#### 2.7 删除购物车商品

- **DELETE** `/cart/{id}`

- **Headers**: `Authorization: Bearer {accessToken}`

- **示例**: `DELETE /cart/1`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "删除成功",
  "data": true
  }
  ```

#### 2.8 批量删除

- **DELETE** `/cart/batch`

- **Headers**: `Authorization: Bearer {accessToken}`

- **Body**: `{ "ids": [1, 2, 3] }`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "批量删除成功",
  "data": true
  }
  ```

---

### 3. 商品模块

#### 3.1 商品分页查询（商城+后台共用）

- **GET** `/product/page`
- **Query参数**:

| 参数         | 类型         | 说明                            |
| ---------- | ---------- | ----------------------------- |
| name       | String     | 商品名称（模糊）                      |
| categoryId | Long       | 分类ID                          |
| minPrice   | BigDecimal | 最低价格                          |
| maxPrice   | BigDecimal | 最高价格                          |
| minStock   | Integer    | 最小库存                          |
| maxStock   | Integer    | 最大库存                          |
| minSales   | Integer    | 最小销量                          |
| maxSales   | Integer    | 最大销量                          |
| status     | Integer    | 0-下架 1-上架                     |
| sortField  | String     | price/stock/sales/create_time |
| sortOrder  | String     | asc/desc                      |
| current    | Long       | 当前页                           |
| size       | Long       | 每页条数                          |

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 5,
        "name": "MacBook Pro 14 M3",
        "categoryId": 2,
        "categoryName": "笔记本电脑",
        "price": 14999.00,
        "originalPrice": 16999.00,
        "stock": 100,
        "sales": 256,
        "image": "https://example.com/macbook.jpg",
        "description": "苹果M3芯片，18GB内存，512GB存储",
        "status": 1,
        "createTime": "2024-07-20 10:00:00",
        "updateTime": "2024-08-20 12:00:00"
      },
      {
        "id": 8,
        "name": "iPhone 15 Pro 256GB",
        "categoryId": 1,
        "categoryName": "手机通讯",
        "price": 8999.00,
        "originalPrice": 9999.00,
        "stock": 200,
        "sales": 512,
        "image": "https://example.com/iphone15.jpg",
        "description": "钛金属边框，A17 Pro芯片",
        "status": 1,
        "createTime": "2024-07-15 10:00:00",
        "updateTime": "2024-08-22 09:30:00"
      }
    ],
    "total": 58,
    "current": 1,
    "size": 10,
    "pages": 6
  }
  }
  ```

- **MyBatis-Plus建议**:
  
  ```java
  QueryWrapper<Product> wrapper = new QueryWrapper<>();
  wrapper.like(name != null, "name", name);
  wrapper.eq(categoryId != null, "category_id", categoryId);
  wrapper.ge(minPrice != null, "price", minPrice);
  wrapper.le(maxPrice != null, "price", maxPrice);
  wrapper.ge(minStock != null, "stock", minStock);
  wrapper.le(maxStock != null, "stock", maxStock);
  wrapper.eq(status != null, "status", status);
  if (sortField != null) {
    wrapper.orderBy(true, "asc".equals(sortOrder), sortField);
  }
  ```

#### 3.2 商品详情

- **GET** `/product/{id}`

- **示例**: `GET /product/5`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 5,
    "name": "MacBook Pro 14 M3",
    "categoryId": 2,
    "categoryName": "笔记本电脑",
    "price": 14999.00,
    "originalPrice": 16999.00,
    "stock": 100,
    "sales": 256,
    "image": "https://example.com/macbook.jpg",
    "description": "苹果M3芯片，18GB内存，512GB固态硬盘，14.2英寸Liquid Retina XDR显示屏，配备ProMotion自适应刷新率技术，支持Thunderbolt 4接口，1080p FaceTime高清摄像头，MagSafe 3充电接口，最长可达18小时电池续航。",
    "status": 1,
    "createTime": "2024-07-20 10:00:00",
    "updateTime": "2024-08-20 12:00:00"
  }
  }
  ```

#### 3.3 新增商品（卖家）

- **POST** `/product`
- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色 role=1）
- **Content-Type**: `multipart/form-data`
- **FormData 字段**（商品信息 + 图片文件同一次提交）：

| 字段            | 类型      | 必填  | 说明                                      |
| ------------- | ------- | --- | --------------------------------------- |
| name          | String  | 是   | 商品名称                                    |
| categoryId    | Long    | 是   | 分类 ID                                   |
| price         | Decimal | 是   | 售价                                      |
| originalPrice | Decimal | 否   | 原价                                      |
| stock         | Integer | 是   | 库存                                      |
| description   | String  | 否   | 商品描述                                    |
| status        | Integer | 否   | 0 下架 / 1 上架（默认 1）                       |
| image         | String  | 否   | 历史/已有图片 URL（未选择新文件时保留原 URL；留空则前端展示占位图）  |
| file          | 文件      | 否   | 本次新上传的商品主图（JPG / PNG / WEBP / GIF，≤5MB） |

- **示例请求报文（概念）**：
  
  ```
  POST /api/product HTTP/1.1
  Authorization: Bearer <accessToken>
  Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryXXX
  
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="name"
  
  iPad Pro 11 M4
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="categoryId"
  
  3
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="price"
  
  8999.00
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="originalPrice"
  
  9999.00
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="stock"
  
  150
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="status"
  
  1
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="description"
  
  M4芯片，256GB存储，11英寸
  ------WebKitFormBoundaryXXX
  Content-Disposition: form-data; name="file"; filename="ipad.jpg"
  Content-Type: image/jpeg
  
  <binary data...>
  ------WebKitFormBoundaryXXX--
  ```

- **后端实现建议**：
  
  1. Controller 方法签名示例：
     
     ```java
     @PostMapping("/product")
     public Result<Boolean> add(@ModelAttribute ProductDTO dto,
                                @RequestParam(value = "file", required = false) MultipartFile file) {
         // 1. 校验 dto 必填
         // 2. 如果 file != null → 校验类型/大小 → UUID 重命名 → 落盘 → 生成访问 URL 填到 dto.image
         // 3. dto.image 有值且 file == null → 沿用已有 URL
         // 4. 保存 dto 到 t_product
     }
     ```
  
  2. 文件校验规则：
     
     - 允许 `image/jpeg, image/png, image/webp, image/gif`
     - 大小 `<= 5 * 1024 * 1024`
  
  3. 文件名：`UUID` + 原扩展名，按 `/upload/product/{yyyyMM}/{uuid}.{ext}` 保存
  
  4. 返回可访问 URL，通过 Spring `ResourceHandlerRegistry.addResourceHandler("/upload/**").addResourceLocations("file:{upload-root}/")` 暴露

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "新增成功",
  "data": true
  }
  ```

#### 3.4 修改商品（卖家）

- **PUT** `/product`
- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）
- **Content-Type**: `multipart/form-data`
- **FormData 字段**：

| 字段            | 类型      | 必填  | 说明                                                     |
| ------------- | ------- | --- | ------------------------------------------------------ |
| id            | Long    | 是   | 商品 ID                                                  |
| name          | String  | 是   | 商品名称                                                   |
| categoryId    | Long    | 是   | 分类 ID                                                  |
| price         | Decimal | 是   | 售价                                                     |
| originalPrice | Decimal | 否   | 原价                                                     |
| stock         | Integer | 是   | 库存                                                     |
| description   | String  | 否   | 商品描述                                                   |
| status        | Integer | 否   | 0 下架 / 1 上架                                            |
| image         | String  | 否   | 已有图片 URL（**未传新 file 时保留该值**；传了新 file 则以后端保存返回的 URL 覆盖） |
| file          | 文件      | 否   | 本次替换的新图片文件                                             |

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "修改成功",
  "data": true
  }
  ```

- **后端提示**：若 Spring 对 `multipart/form-data + PUT` 兼容差，可切换 `StandardServletMultipartResolver` 或改为 `POST /product` 并在 DTO 中区分是否带 `id`（也可使用 `HiddenHttpMethodFilter` 加 `_method=PUT`）。当前前端默认走 `PUT`，遇到解析失败请在后端按上述方式兜底。

#### 3.5 删除商品（卖家）

- **DELETE** `/product/{id}`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **示例**: `DELETE /product/5`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "删除成功",
  "data": true
  }
  ```

#### 3.6 上下架（卖家）

- **PUT** `/product/status/{id}/{status}`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **示例**: `PUT /product/status/5/0`（下架）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "状态更新成功",
  "data": true
  }
  ```

#### 3.7 销量排行榜

- **GET** `/product/top-sales?limit=10`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 10,
      "name": "AirPods Pro 2",
      "categoryId": 4,
      "categoryName": "数码配件",
      "price": 1899.00,
      "originalPrice": 1999.00,
      "stock": 500,
      "sales": 1280,
      "image": "https://example.com/airpods.jpg",
      "description": "主动降噪，空间音频",
      "status": 1
    },
    {
      "id": 8,
      "name": "iPhone 15 Pro 256GB",
      "categoryId": 1,
      "categoryName": "手机通讯",
      "price": 8999.00,
      "originalPrice": 9999.00,
      "stock": 200,
      "sales": 512,
      "image": "https://example.com/iphone15.jpg",
      "description": "钛金属边框，A17 Pro芯片",
      "status": 1
    }
  ]
  }
  ```

---

### 4. 订单模块

#### 4.1 买家订单分页查询（我的订单）

- **GET** `/order/page`
- **Headers**: `Authorization: Bearer {accessToken}`
- **Query参数**:

| 参数         | 类型      | 说明                                  |
| ---------- | ------- | ----------------------------------- |
| customerId | Long    | **必填** 买家用户ID，只查自己的订单               |
| orderNo    | String  | 订单编号（模糊）                            |
| status     | Integer | 订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消 |
| current    | Long    | 当前页，默认1                             |
| size       | Long    | 每页条数，默认10                           |

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1001,
        "orderNo": "202408251000320001",
        "customerId": 1,
        "customerName": "买家小明",
        "customerPhone": "13800138000",
        "sellerId": 3,
        "sellerName": "数码旗舰店",
        "productId": 5,
        "productName": "MacBook Pro 14 M3",
        "productImage": "https://example.com/macbook.jpg",
        "price": 14999.00,
        "quantity": 1,
        "totalAmount": 14999.00,
        "status": 1,
        "remark": "请尽快发货",
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "receiverAddress": "北京市朝阳区xxx街道xxx小区1号楼101室",
        "createTime": "2024-08-25 10:00:32",
        "payTime": "2024-08-25 10:05:18",
        "shipTime": null,
        "finishTime": null,
        "updateTime": "2024-08-25 10:05:18"
      }
    ],
    "total": 36,
    "current": 1,
    "size": 10,
    "pages": 4
  }
  }
  ```

- **Mapper.xml建议**:
  
  ```xml
  <select id="selectOrderPage" resultType="...Order">
  SELECT o.*, su.username as sellerName
  FROM t_order o
  LEFT JOIN t_user su ON o.seller_id = su.id
  <where>
    o.deleted = 0
    AND o.customer_id = #{customerId}
    <if test="orderNo != null and orderNo != ''">AND o.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>
    <if test="status != null">AND o.status = #{status}</if>
  </where>
  ORDER BY o.create_time DESC
  </select>
  ```

#### 4.2 卖家订单分页查询（卖家中心订单管理）

- **GET** `/order/seller/page`
- **Headers**: `Authorization: Bearer {accessToken}`
- **Query参数**:

| 参数           | 类型         | 说明                                  |
| ------------ | ---------- | ----------------------------------- |
| sellerId     | Long       | **必填** 卖家用户ID，只查自己店铺的订单             |
| orderNo      | String     | 订单编号（模糊）                            |
| customerName | String     | 买家用户名（模糊，需关联t_user表）                |
| status       | Integer    | 订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消 |
| minAmount    | BigDecimal | 最小金额                                |
| maxAmount    | BigDecimal | 最大金额                                |
| startTime    | DateTime   | 起始时间                                |
| endTime      | DateTime   | 截止时间                                |
| sortField    | String     | total_amount / create_time          |
| sortOrder    | String     | asc / desc                          |
| current      | Long       | 当前页，默认1                             |
| size         | Long       | 每页条数，默认10                           |

- **响应结果示例**: 同 4.1，但只返回该卖家的订单

- **Mapper.xml建议**:
  
  ```xml
  <select id="selectSellerOrderPage" resultType="...Order">
  SELECT o.*, cu.username as customerName, cu.phone as customerPhone
  FROM t_order o
  LEFT JOIN t_user cu ON o.customer_id = cu.id
  <where>
    o.deleted = 0
    AND o.seller_id = #{sellerId}
    <if test="orderNo != null and orderNo != ''">AND o.order_no LIKE CONCAT('%', #{orderNo}, '%')</if>
    <if test="customerName != null and customerName != ''">AND cu.username LIKE CONCAT('%', #{customerName}, '%')</if>
    <if test="status != null">AND o.status = #{status}</if>
    <if test="minAmount != null">AND o.total_amount >= #{minAmount}</if>
    <if test="maxAmount != null">AND o.total_amount <= #{maxAmount}</if>
    <if test="startTime != null">AND o.create_time >= #{startTime}</if>
    <if test="endTime != null">AND o.create_time <= #{endTime}</if>
  </where>
  ORDER BY
  <choose>
    <when test="sortField != null and sortField != ''">${sortField} ${sortOrder}</when>
    <otherwise>o.create_time DESC</otherwise>
  </choose>
  </select>
  ```

#### 4.3 订单详情

- **GET** `/order/{id}`

- **Headers**: `Authorization: Bearer {accessToken}`

- **示例**: `GET /order/1001`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "orderNo": "202408251000320001",
    "customerId": 1,
    "customerName": "买家小明",
    "customerPhone": "13800138000",
    "sellerId": 3,
    "sellerName": "数码旗舰店",
    "productId": 5,
    "productName": "MacBook Pro 14 M3",
    "productImage": "https://example.com/macbook.jpg",
    "price": 14999.00,
    "quantity": 1,
    "totalAmount": 14999.00,
    "status": 2,
    "remark": "请尽快发货",
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区xxx街道xxx小区1号楼101室",
    "createTime": "2024-08-25 10:00:32",
    "payTime": "2024-08-25 10:05:18",
    "shipTime": "2024-08-26 09:30:00",
    "finishTime": null,
    "updateTime": "2024-08-26 09:30:00"
  }
  }
  ```

#### 4.4 创建订单（买家下单）

- **POST** `/order`

- **Headers**: `Authorization: Bearer {accessToken}`

- **Body**:
  
  ```json
  {
  "customerId": 1,
  "sellerId": 3,
  "productId": 5,
  "productName": "iPad Pro 11",
  "price": 8999.00,
  "quantity": 2,
  "remark": "备注",
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "receiverAddress": "北京市朝阳区xxx"
  }
  ```

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "下单成功",
  "data": 1001
  }
  ```
  
  > `data` 返回新创建订单的主键 ID（Long），前端据此跳转到支付页。

- **实现建议**:
  
  1. 生成订单编号
  2. 根据 price × quantity 计算 total_amount
  3. 直接插入 t_order（含 seller_id、product_id、product_name、price、quantity、total_amount）
  4. 扣减库存、增加销量
  5. 清空购物车中已下单的商品
  6. 加 `@Transactional`
  7. **返回新建订单的主键 id**（insert 后回传到 data）

- **前端调用约定**:
  
  - 购物车一次结算多种商品时，前端按商品逐个调用本接口
  - 每笔订单只含一个商品，并携带该商品所属卖家的 sellerId
  - 所有商品信息直接存入 t_order，不再使用 t_order_item
  - 返回的订单 ID 收集后跳转 `/payment?orderIds=id1,id2`

#### 4.5 修改订单状态

- **PUT** `/order/status/{id}/{status}`
- **Headers**: `Authorization: Bearer {accessToken}`
- 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消
- **按状态自动写入对应时间字段**:

| 状态值 | 含义  | 写入时间字段                |
| --- | --- | --------------------- |
| 1   | 已付款 | `pay_time = now()`    |
| 2   | 已发货 | `ship_time = now()`   |
| 3   | 已完成 | `finish_time = now()` |
| 0/4 | 其他  | 不设置时间字段               |

> `update_time` 由 MyBatis-Plus 自动填充（MyMetaObjectHandler）

- **示例**: `PUT /order/status/1001/2`（标记为已发货）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "状态更新成功",
  "data": true
  }
  ```

#### 4.6 删除订单

- **DELETE** `/order/{id}`

- **Headers**: `Authorization: Bearer {accessToken}`

- **示例**: `DELETE /order/1001`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "删除成功",
  "data": true
  }
  ```

#### 4.7 最近订单

- **GET** `/order/recent?limit=5`

- **Headers**: `Authorization: Bearer {accessToken}`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1003,
      "orderNo": "202408261420150003",
      "customerId": 1,
      "customerName": "买家小明",
      "sellerId": 3,
      "productId": 5,
      "productName": "MacBook Pro 14 M3",
      "price": 14999.00,
      "quantity": 1,
      "totalAmount": 14999.00,
      "status": 0,
      "createTime": "2024-08-26 14:20:15",
      "payTime": null
    },
    {
      "id": 1002,
      "orderNo": "202408260915220002",
      "customerId": 1,
      "customerName": "买家小明",
      "sellerId": 3,
      "productId": 5,
      "productName": "MacBook Pro 14 M3",
      "price": 8999.00,
      "quantity": 1,
      "totalAmount": 8999.00,
      "status": 3,
      "createTime": "2024-08-26 09:15:22",
      "payTime": "2024-08-26 09:18:00",
      "finishTime": "2024-08-27 10:00:00"
    }
  ]
  }
  ```

---

### 5. 我的客户（卖家后台）

> 卖家只能查看**买过自己商品**的客户列表，不允许新增、修改、删除客户。

#### 5.1 客户分页查询

- **GET** `/customer/page`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色 role=1）

- **Query参数**:
  
  | 参数        | 类型      | 必填  | 说明                         |
  | --------- | ------- | --- | -------------------------- |
  | sellerId  | Long    | 是   | 卖家用户ID（从登录态获取，前端传入）        |
  | username  | String  | 否   | 用户名模糊搜索                    |
  | phone     | String  | 否   | 手机号模糊搜索                    |
  | gender    | Integer | 否   | 性别：0-未知 1-男 2-女            |
  | level     | Integer | 否   | 会员等级：1-普通 2-银卡 3-金卡        |
  | status    | Integer | 否   | 账户状态：1-启用 0-禁用             |
  | startTime | String  | 否   | 注册时间起（yyyy-MM-dd HH:mm:ss） |
  | endTime   | String  | 否   | 注册时间止（yyyy-MM-dd HH:mm:ss） |
  | current   | Integer | 否   | 当前页，默认1                    |
  | size      | Integer | 否   | 每页条数，默认10                  |

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "buyer",
        "phone": "13800138000",
        "email": "buyer@example.com",
        "gender": 1,
        "role": 0,
        "avatar": null,
        "nickname": "买家小明",
        "level": 1,
        "status": 1,
        "registerTime": "2024-08-01 10:00:00",
        "orderCount": 5,
        "totalAmount": 1299.00,
        "lastOrderTime": "2024-09-01 15:30:00"
      }
    ],
    "total": 12,
    "current": 1,
    "size": 10,
    "pages": 2
  }
  }
  ```

- **实现建议**:
  
  1. 必须传 `sellerId`，否则返回 400
  2. 通过 `t_order` 表关联查询：先查 `SELECT DISTINCT customer_id FROM t_order WHERE seller_id = ?` 拿到该卖家的客户 ID 集合
  3. 再关联 `t_user` 查客户基础信息
  4. 子查询统计每个客户对该卖家的 `orderCount`（订单数）、`totalAmount`（累计消费）、`lastOrderTime`（最近下单时间）
  5. **不提供**新增、修改、删除客户的接口

#### 5.2 客户详情

- **GET** `/customer/{id}`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **示例**: `GET /customer/1`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "buyer",
    "phone": "13800138000",
    "email": "buyer@example.com",
    "gender": 1,
    "role": 0,
    "avatar": null,
    "nickname": "买家小明",
    "level": 1,
    "status": 1,
    "registerTime": "2024-08-01 10:00:00",
    "orderCount": 5,
    "totalAmount": 1299.00,
    "lastOrderTime": "2024-09-01 15:30:00"
  }
  }
  ```

> ⚠️ 本模块为**只读**，无新增、修改、删除接口。卖家如需管理自身账户信息，请使用「个人中心」（1.6 / 1.7）。

---

### 6. 分类模块

#### 6.1 分类树

- **GET** `/category/tree`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "手机数码",
      "parentId": 0,
      "sort": 1,
      "status": 1,
      "children": [
        {
          "id": 11,
          "name": "手机通讯",
          "parentId": 1,
          "sort": 1,
          "status": 1,
          "children": []
        },
        {
          "id": 12,
          "name": "笔记本电脑",
          "parentId": 1,
          "sort": 2,
          "status": 1,
          "children": []
        },
        {
          "id": 13,
          "name": "平板电脑",
          "parentId": 1,
          "sort": 3,
          "status": 1,
          "children": []
        },
        {
          "id": 14,
          "name": "数码配件",
          "parentId": 1,
          "sort": 4,
          "status": 1,
          "children": []
        }
      ]
    },
    {
      "id": 2,
      "name": "家用电器",
      "parentId": 0,
      "sort": 2,
      "status": 1,
      "children": [
        {
          "id": 21,
          "name": "电视",
          "parentId": 2,
          "sort": 1,
          "status": 1,
          "children": []
        },
        {
          "id": 22,
          "name": "冰箱",
          "parentId": 2,
          "sort": 2,
          "status": 1,
          "children": []
        }
      ]
    }
  ]
  }
  ```

#### 6.2 分类列表

- **GET** `/category/list`

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    { "id": 1, "name": "手机数码", "parentId": 0, "sort": 1, "status": 1 },
    { "id": 2, "name": "家用电器", "parentId": 0, "sort": 2, "status": 1 },
    { "id": 11, "name": "手机通讯", "parentId": 1, "sort": 1, "status": 1 },
    { "id": 12, "name": "笔记本电脑", "parentId": 1, "sort": 2, "status": 1 },
    { "id": 13, "name": "平板电脑", "parentId": 1, "sort": 3, "status": 1 },
    { "id": 14, "name": "数码配件", "parentId": 1, "sort": 4, "status": 1 },
    { "id": 21, "name": "电视", "parentId": 2, "sort": 1, "status": 1 },
    { "id": 22, "name": "冰箱", "parentId": 2, "sort": 2, "status": 0 }
  ]
  }
  ```

---

### 7. 仪表盘统计模块

#### 7.1 总览统计

- **GET** `/dashboard/overview`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalSales": 1286500.00,
    "totalOrders": 368,
    "totalCustomers": 128,
    "totalProducts": 58
  }
  }
  ```

- **实现建议**:
  
  1. totalSales: SELECT SUM(total_amount) FROM t_order WHERE status = 3 (已完成)
  2. totalOrders: SELECT COUNT(*) FROM t_order
  3. totalCustomers: SELECT COUNT(*) FROM t_customer WHERE deleted = 0
  4. totalProducts: SELECT COUNT(*) FROM t_product WHERE deleted = 0

#### 7.2 近7天销售趋势

- **GET** `/dashboard/sales-trend`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    { "date": "2024-08-20", "amount": 45997.00, "orderCount": 8 },
    { "date": "2024-08-21", "amount": 32800.00, "orderCount": 5 },
    { "date": "2024-08-22", "amount": 68500.00, "orderCount": 12 },
    { "date": "2024-08-23", "amount": 51200.00, "orderCount": 9 },
    { "date": "2024-08-24", "amount": 89900.00, "orderCount": 15 },
    { "date": "2024-08-25", "amount": 125600.00, "orderCount": 22 },
    { "date": "2024-08-26", "amount": 98700.00, "orderCount": 18 }
  ]
  }
  ```
  
  ```sql
  SELECT DATE(create_time) as date, SUM(total_amount) as amount, COUNT(*) as orderCount
  FROM t_order WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) AND deleted = 0
  GROUP BY DATE(create_time) ORDER BY date
  ```

#### 7.3 各分类销售额（四表关联）

- **GET** `/dashboard/category-sales`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    { "categoryName": "手机通讯", "totalAmount": 528600.00, "orderCount": 86, "productCount": 12 },
    { "categoryName": "笔记本电脑", "totalAmount": 425500.00, "orderCount": 45, "productCount": 8 },
    { "categoryName": "数码配件", "totalAmount": 158400.00, "orderCount": 132, "productCount": 20 },
    { "categoryName": "平板电脑", "totalAmount": 98000.00, "orderCount": 28, "productCount": 6 },
    { "categoryName": "电视", "totalAmount": 48000.00, "orderCount": 12, "productCount": 5 },
    { "categoryName": "冰箱", "totalAmount": 28000.00, "orderCount": 8, "productCount": 4 }
  ]
  }
  ```
  
  ```sql
  SELECT c.name as categoryName, SUM(o.total_amount) as totalAmount,
       COUNT(DISTINCT o.id) as orderCount, COUNT(DISTINCT p.id) as productCount
  FROM t_order o
  JOIN t_product p ON o.product_id = p.id
  JOIN t_category c ON p.category_id = c.id
  WHERE o.status != 4 AND o.deleted = 0
  GROUP BY c.id, c.name ORDER BY totalAmount DESC
  ```

#### 7.4 订单状态分布

- **GET** `/dashboard/order-status`

- **Headers**: `Authorization: Bearer {accessToken}`（需要卖家角色）

- **响应结果示例**:
  
  ```json
  {
  "code": 200,
  "message": "操作成功",
  "data": [
    { "name": "待付款", "value": 36 },
    { "name": "已付款", "value": 28 },
    { "name": "已发货", "value": 42 },
    { "name": "已完成", "value": 238 },
    { "name": "已取消", "value": 24 }
  ]
  }
  ```

- **实现建议**:
  
  ```sql
  SELECT status, COUNT(*) as value FROM t_order
  WHERE deleted = 0 GROUP BY status
  ```
  
  再在Java中映射状态码到中文名称: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消

---

## 技术栈建议

| 层    | 技术                                      |
| ---- | --------------------------------------- |
| 框架   | Spring Boot 2.7.x                       |
| ORM  | MyBatis-Plus 3.5.x                      |
| 数据库  | MySQL 8.0+                              |
| 连接池  | Druid                                   |
| 认证   | JWT (双Token) + Spring Security 或 拦截器    |
| 密码加密 | BCrypt                                  |
| 分页   | MyBatis-Plus PaginationInnerInterceptor |
| 逻辑删除 | @TableLogic                             |
| 自动填充 | MetaObjectHandler                       |
| 跨域   | CorsConfig                              |

## 双Token认证流程

```
1. 登录 → 返回 accessToken(短期30min) + refreshToken(长期7天)
2. 请求 → Header: Authorization: Bearer {accessToken}
3. accessToken过期 → 401
4. 前端自动用 refreshToken 调 /auth/refresh
5. 刷新成功 → 获取新双Token，重试原请求
6. 刷新失败 → 跳转登录页
```

## 统一返回格式

```java
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
}
```

## 当前登录用户获取建议

在拦截器/过滤器中解析 JWT，将 userId 放入 ThreadLocal 或 HttpServletRequest：

```java
public class UserContext {
    private static ThreadLocal<Long> userId = new ThreadLocal<>();
    public static Long getUserId() { return userId.get(); }
    public static void setUserId(Long id) { userId.set(id); }
}
```

## 初始化测试数据建议（INSERT SQL）

```sql
-- 2个测试账号，密码明文都是 123456（BCrypt加密后相同）
INSERT INTO t_user (id, username, password, phone, email, gender, role, avatar, nickname, level, status, register_time, create_time, update_time, deleted) VALUES
(1, 'buyer',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'buyer@example.com',  1, 0, NULL, '买家小明', 1, 1, NOW(), NOW(), NOW(), 0),
(2, 'seller', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13900139000', 'seller@example.com', 2, 1, NULL, '卖家小红', 3, 1, NOW(), NOW(), NOW(), 0);
```
