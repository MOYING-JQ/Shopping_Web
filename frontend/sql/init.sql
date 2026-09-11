-- ============================================
-- 电商商城系统 数据库初始化脚本
-- MySQL 8.0+
-- 仿淘宝：买家购物 + 卖家管理 + 双Token认证
-- ============================================

CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ecommerce;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表（买家+卖家）
-- ============================================
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      varchar(50)  NOT NULL COMMENT '用户名',
  `password`      varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `phone`         varchar(20)  DEFAULT NULL COMMENT '手机号',
  `email`         varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender`        tinyint      NOT NULL DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
  `role`          tinyint      NOT NULL DEFAULT 0 COMMENT '角色: 0-买家 1-卖家',
  `avatar`        varchar(255) DEFAULT NULL COMMENT '头像URL',
  `nickname`      varchar(50)  DEFAULT NULL COMMENT '昵称',
  `level`         int          NOT NULL DEFAULT 1 COMMENT '会员等级: 1-普通 2-银卡 3-金卡',
  `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `register_time` datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `create_time`   datetime     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   datetime     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       tinyint      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 商品分类表（树形结构）
-- ============================================
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name`        varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id`   bigint      NOT NULL DEFAULT 0 COMMENT '父分类ID, 0为一级分类',
  `sort`        int         NOT NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  `status`      tinyint     NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
  `create_time` datetime    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- ============================================
-- 3. 商品表
-- ============================================
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
  `id`             bigint        NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `seller_id`      bigint        NOT NULL COMMENT '卖家用户ID（关联t_user.id）',
  `name`           varchar(100)  NOT NULL COMMENT '商品名称',
  `category_id`    bigint        NOT NULL COMMENT '分类ID',
  `price`          decimal(10,2) NOT NULL COMMENT '销售价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价（划线价）',
  `stock`          int           NOT NULL DEFAULT 0 COMMENT '库存数量',
  `sales`          int           NOT NULL DEFAULT 0 COMMENT '销量',
  `image`          varchar(255)  DEFAULT NULL COMMENT '商品主图URL',
  `description`    text          COMMENT '商品描述',
  `status`         tinyint       NOT NULL DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `create_time`    datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        tinyint       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sales` (`sales`),
  KEY `idx_price` (`price`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_product_seller` FOREIGN KEY (`seller_id`) REFERENCES `t_user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ============================================
-- 4. 购物车表
-- ============================================
DROP TABLE IF EXISTS `t_cart`;
CREATE TABLE `t_cart` (
  `id`          bigint  NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `customer_id` bigint  NOT NULL COMMENT '用户ID',
  `product_id`  bigint  NOT NULL COMMENT '商品ID',
  `quantity`   int     NOT NULL DEFAULT 1 COMMENT '数量',
  `selected`   tinyint NOT NULL DEFAULT 1 COMMENT '是否选中: 0-未选中 1-选中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_product` (`customer_id`, `product_id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ============================================
-- 5. 订单表
-- ============================================
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id`               bigint        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no`         varchar(32)   NOT NULL COMMENT '订单编号（如 ORD20240115001）',
  `customer_id`      bigint        NOT NULL COMMENT '用户ID',
  `total_amount`     decimal(10,2) NOT NULL COMMENT '订单总金额',
  `total_quantity`   int           NOT NULL DEFAULT 0 COMMENT '商品总数量',
  `status`           tinyint       NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待付款 1-已付款 2-已发货 3-已完成 4-已取消',
  `remark`           varchar(255)  DEFAULT NULL COMMENT '订单备注',
  `receiver_name`    varchar(50)   DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone`   varchar(20)   DEFAULT NULL COMMENT '收货人电话',
  `receiver_address` varchar(500)  DEFAULT NULL COMMENT '收货地址',
  `create_time`      datetime      DEFAULT CURRENT_TIMESTAMP COMMENT '创建（下单）时间',
  `pay_time`         datetime      DEFAULT NULL COMMENT '支付时间',
  `ship_time`        datetime      DEFAULT NULL COMMENT '发货时间',
  `finish_time`      datetime      DEFAULT NULL COMMENT '完成时间',
  `update_time`      datetime      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          tinyint       NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删 1-已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 6. 订单明细表
-- ============================================
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `id`           bigint        NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id`     bigint        NOT NULL COMMENT '订单ID',
  `product_id`   bigint        NOT NULL COMMENT '商品ID',
  `product_name` varchar(100)  NOT NULL COMMENT '商品名称（冗余，下单时快照）',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片（冗余）',
  `quantity`     int           NOT NULL COMMENT '购买数量',
  `price`        decimal(10,2) NOT NULL COMMENT '成交单价（下单时快照）',
  `subtotal`    decimal(10,2) NOT NULL COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 初始化数据
-- ============================================

-- ---------- 分类数据（二级树形） ----------
INSERT INTO `t_category` (`name`, `parent_id`, `sort`, `status`) VALUES
('手机数码',  0, 1, 1),
('电脑办公',  0, 2, 1),
('家用电器',  0, 3, 1),
('服饰鞋包',  0, 4, 1),
('食品生鲜',  0, 5, 1),
('美妆护肤',  0, 6, 1),
('手机',      1, 1, 1),
('平板',      1, 2, 1),
('智能穿戴',  1, 3, 1),
('笔记本',    2, 1, 1),
('台式机',    2, 2, 1),
('外设配件',  2, 3, 1),
('大家电',    3, 1, 1),
('小家电',    3, 2, 1),
('男装',      4, 1, 1),
('女装',      4, 2, 1),
('鞋靴',      4, 3, 1),
('箱包',      4, 4, 1),
('零食',      5, 1, 1),
('饮料',      5, 2, 1),
('护肤',      6, 1, 1),
('彩妆',      6, 2, 1);

-- ---------- 用户数据 ----------
-- 密码均为 123456 的 BCrypt 加密
INSERT INTO `t_user` (`username`, `password`, `phone`, `email`, `gender`, `role`, `nickname`, `level`, `status`, `register_time`) VALUES
('buyer',     '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138001', 'buyer@example.com',    1, 0, '购物达人',   3, 1, '2024-01-15 09:30:00'),
('buyer2',    '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138002', 'buyer2@example.com',   2, 0, '爱买的小李', 2, 1, '2024-02-20 14:00:00'),
('buyer3',    '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138003', 'buyer3@example.com',   1, 0, '剁手党',     1, 1, '2024-03-10 10:15:00'),
('seller',    '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138004', 'seller@example.com',   1, 1, '旗舰店',     3, 1, '2024-01-01 08:00:00'),
('seller2',   '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138005', 'seller2@example.com',  2, 1, '官方自营',   2, 1, '2024-01-05 09:00:00'),
('admin',     '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jee7m3vJ7K8Y8S', '13800138006', 'admin@example.com',    1, 1, '管理员',     3, 1, '2024-01-01 00:00:00');

-- ---------- 商品数据（seller_id: 4=seller, 5=seller2, 6=admin） ----------
INSERT INTO `t_product` (`seller_id`, `name`, `category_id`, `price`, `original_price`, `stock`, `sales`, `description`, `status`) VALUES
-- seller(4) - 手机数码
(4, 'iPhone 15 Pro 256GB',       7,  8999.00, 9999.00,  500, 1280, '苹果旗舰手机，A17 Pro芯片，钛金属机身，4800万像素主摄', 1),
(4, '华为 Mate 60 Pro',          7,  6999.00, 7999.00,  300,  980, '华为旗舰，卫星通信，麒麟9000S芯片，昆仑玻璃', 1),
(4, '小米14 Ultra',              7,  6499.00, 6999.00,  200,  650, '徕卡光学镜头，骁龙8 Gen3，影像旗舰', 1),
(4, '三星 Galaxy S24 Ultra',     7,  5999.00, 6499.00,  150,  420, '三星AI旗舰，2亿像素，S Pen书写', 1),
(4, 'OPPO Find X7',              7,  4999.00, 5499.00,  250,  380, '哈苏影像，天玑9300，5000mAh电池', 1),
-- seller(4) - 平板
(4, 'iPad Pro 12.9 M2',          8,  8499.00, 8999.00,  120,  310, '苹果平板，M2芯片，Liquid Retina XDR屏', 1),
(4, '华为 MatePad Pro 13.2',     8,  5199.00, 5699.00,   90,  180, '华为平板，柔光屏，星闪技术', 1),
-- seller(4) - 智能穿戴
(4, 'Apple Watch S9',            9,  2999.00, 3299.00,  300,  560, '苹果手表，S9芯片，双指互触功能', 1),
(4, '华为 Watch GT4',            9,  1488.00, 1688.00,  400,  720, '华为手表，14天续航，健康监测', 1),
-- seller2(5) - 电脑办公
(5, 'MacBook Pro 14 M3',         10, 14999.00, 15999.00, 100, 320, '苹果笔记本，M3芯片，专业级性能，Liquid Retina XDR', 1),
(5, '联想 ThinkPad X1 Carbon',   10, 12999.00, 13999.00,  80, 210, '商务旗舰笔记本，碳纤维机身，键盘手感一流', 1),
(5, 'Dell XPS 15',               10, 11999.00, 12999.00,  60, 180, '戴尔高端创作本，4K OLED屏，RTX 4060', 1),
(5, '华硕 灵耀14',               10, 6999.00, 7999.00,  150, 290, '轻薄本，OLED屏，RTX 4050', 1),
-- seller2(5) - 外设配件
(5, '罗技 MX Master 3S',         12, 699.00,  799.00,   800, 1500, '高端无线鼠标，静音点击，多设备切换', 1),
(5, '雷蛇 BlackWidow V4',         12, 1299.00, 1499.00,  300,  420, '机械键盘，绿轴，RGB背光', 1),
(5, '显示器 戴尔 U2723QE',       12, 3999.00, 4499.00,  150,  260, '4K USB-C显示器，IPS Black面板', 1),
-- seller2(5) - 家用电器
(5, '海尔冰箱 470L',             13, 3299.00, 3799.00,  200,  380, '法式四门，风冷无霜，变频压缩机', 1),
(5, '美的空调 1.5匹',            13, 2499.00, 2999.00,  350,  680, '新一级能效，变频冷暖，自清洁', 1),
(5, '戴森吹风机 HD15',           14, 2999.00, 3299.00,  200,  890, '戴森高速吹风机，智能温控', 1),
(5, '小米 智能台灯 Pro',         14, 399.00,  499.00,   500, 680, 'LED护眼台灯，APP控制，色温调节', 1),
-- admin(6) - 服饰鞋包
(6, '优衣库 男士衬衫',           15, 199.00,  249.00,   600, 1200, '简约纯棉商务衬衫，透气舒适', 1),
(6, 'Nike Sportswear T恤',       15, 299.00,  349.00,   400,  680, 'Nike经典Logo T恤，Dri-FIT面料', 1),
(6, 'ZARA 女士连衣裙',           16, 399.00,  499.00,   200,  560, '时尚印花连衣裙，优雅收腰设计', 1),
(6, 'ONLY 女士大衣',             16, 899.00,  1199.00,  120,  340, '秋冬羊毛混纺大衣，经典翻领', 0),
(6, 'Nike Air Force 1',          17, 799.00,  899.00,   400,  890, '经典空军一号板鞋，百搭潮流', 1),
(6, 'Adidas Ultraboost',         17, 1299.00, 1499.00,   250,  670, '阿迪达斯跑鞋，Boost中底，Primeknit鞋面', 1),
(6, '新秀丽 双肩包',             18, 899.00,  1099.00,  180,  320, '商务双肩包，防泼水，多隔层', 1),
-- admin(6) - 食品生鲜
(6, '三只松鼠坚果礼盒',          19, 128.00,  168.00,  1000, 2300, '每日坚果混合装30袋，健康零食', 1),
(6, '良品铺子肉脯',              19, 59.90,   79.90,    800, 1800, '原味猪肉脯，独立包装，口感细腻', 1),
(6, '百草味糕点礼盒',            19, 88.00,  118.00,   600,  980, '传统糕点8种口味，年节送礼', 1),
(6, '可口可乐 24罐装',           20, 59.00,   72.00,    500, 3200, '经典可乐330ml*24罐，冰镇更佳', 1),
(6, '农夫山泉 24瓶装',           20, 48.00,   60.00,    600, 2800, '天然矿泉水550ml*24，日常必备', 1),
(6, '星巴克咖啡豆',              20, 89.00,  108.00,    300,  760, '中度烘焙，哥伦比亚产区，250g', 1),
-- admin(6) - 美妆护肤
(6, '兰蔻小黑瓶精华',            21, 1080.00, 1280.00, 200,  560, '兰蔻精华肌底液50ml，修护肌肤', 1),
(6, '雅诗兰黛红石榴水',          21, 580.00,  680.00,  300,  420, '红石榴鲜活亮采爽肤水200ml', 1),
(6, 'YSL口红小金条',             22, 350.00,  420.00,  400,  890, 'YSL方管口红，显色持久，滋润不拔干', 1),
(6, '阿玛尼权利粉底液',          22, 580.00,  660.00,  250,  340, '阿玛尼设计师粉底液30ml，持久遮瑕', 1);

-- ---------- 购物车数据 ----------
INSERT INTO `t_cart` (`customer_id`, `product_id`, `quantity`, `selected`) VALUES
(1, 1,  1, 1),
(1, 14, 2, 1),
(1, 27, 1, 0),
(2, 3,  1, 1),
(2, 22, 1, 1),
(3, 10, 1, 1),
(3, 18, 1, 1);

-- ---------- 订单数据 ----------
INSERT INTO `t_order` (`order_no`, `customer_id`, `total_amount`, `total_quantity`, `status`, `remark`, `receiver_name`, `receiver_phone`, `receiver_address`, `create_time`, `pay_time`, `ship_time`, `finish_time`) VALUES
('ORD20240115001', 1, 8999.00,  1, 3, '快递放门口',         '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-01-15 10:00:00', '2024-01-15 10:05:00', '2024-01-16 09:00:00', '2024-01-18 15:00:00'),
('ORD20240120002', 2, 2197.00,  3, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-01-20 14:30:00', '2024-01-20 14:35:00', '2024-01-21 10:00:00', '2024-01-23 16:00:00'),
('ORD20240205003', 1, 699.00,   1, 2, '尽快发货',           '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-02-05 09:20:00', '2024-02-05 09:25:00', '2024-02-06 08:00:00', NULL),
('ORD20240218004', 3, 15698.00, 2, 3, NULL,                '王强', '13800138003', '广州市天河区天河路200号',  '2024-02-18 16:00:00', '2024-02-18 16:10:00', '2024-02-19 14:00:00', '2024-02-21 10:30:00'),
('ORD20240301005', 2, 399.00,   1, 3, '送人',               '李娜', '13800138002', '上海市浦东新区张江路100号','2024-03-01 11:45:00', '2024-03-01 11:50:00', '2024-03-02 09:00:00', '2024-03-04 14:00:00'),
('ORD20240312006', 1, 458.00,   2, 1, NULL,                '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-03-12 13:15:00', '2024-03-12 13:20:00', NULL,                  NULL),
('ORD20240320007', 3, 6999.00,  1, 2, NULL,                '王强', '13800138003', '广州市天河区天河路200号',  '2024-03-20 15:30:00', '2024-03-20 15:35:00', '2024-03-21 11:00:00', NULL),
('ORD20240405008', 2, 128.00,   1, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-04-05 17:00:00', '2024-04-05 17:05:00', '2024-04-06 09:30:00', '2024-04-08 12:00:00'),
('ORD20240415009', 1, 2198.00,  2, 0, NULL,                '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-04-15 10:30:00', NULL,                  NULL,                  NULL),
('ORD20240501010', 3, 14999.00, 1, 3, NULL,                '王强', '13800138003', '广州市天河区天河路200号',  '2024-05-01 08:00:00', '2024-05-01 08:05:00', '2024-05-02 10:00:00', '2024-05-04 15:00:00'),
('ORD20240518011', 2, 59.90,    1, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-05-18 14:20:00', '2024-05-18 14:25:00', '2024-05-19 09:00:00', '2024-05-21 11:00:00'),
('ORD20240610012', 1, 8198.00,  2, 3, NULL,                '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-06-10 09:00:00', '2024-06-10 09:10:00', '2024-06-11 08:30:00', '2024-06-13 16:00:00'),
('ORD20240625013', 3, 399.00,   1, 4, '不想要了',           '王强', '13800138003', '广州市天河区天河路200号',  '2024-06-25 16:40:00', NULL,                  NULL,                  NULL),
('ORD20240708014', 2, 59.00,    1, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-07-08 11:10:00', '2024-07-08 11:15:00', '2024-07-09 10:00:00', '2024-07-11 14:00:00'),
('ORD20240720015', 1, 1299.00,  1, 2, NULL,                '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-07-20 13:50:00', '2024-07-20 13:55:00', '2024-07-21 09:00:00', NULL),
('ORD20240801016', 3, 107.00,   2, 1, NULL,                '王强', '13800138003', '广州市天河区天河路200号',  '2024-08-01 10:25:00', '2024-08-01 10:30:00', NULL,                  NULL),
('ORD20240815017', 2, 8999.00,  1, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-08-15 15:00:00', '2024-08-15 15:05:00', '2024-08-16 10:00:00', '2024-08-18 13:00:00'),
('ORD20240901018', 1, 6499.00,  1, 3, NULL,                '张伟', '13800138001', '北京市朝阳区建国路88号',  '2024-09-01 09:45:00', '2024-09-01 09:50:00', '2024-09-02 08:00:00', '2024-09-04 15:00:00'),
('ORD20240910019', 3, 798.00,   2, 0, NULL,                '王强', '13800138003', '广州市天河区天河路200号',  '2024-09-10 14:00:00', NULL,                  NULL,                  NULL),
('ORD20241005021', 2, 2198.00,  2, 3, NULL,                '李娜', '13800138002', '上海市浦东新区张江路100号','2024-10-05 11:20:00', '2024-10-05 11:25:00', '2024-10-06 09:30:00', '2024-10-08 16:00:00');

-- ---------- 订单明细数据 ----------
INSERT INTO `t_order_item` (`order_id`, `product_id`, `product_name`, `quantity`, `price`, `subtotal`) VALUES
(1,  1,  'iPhone 15 Pro 256GB',    1, 8999.00, 8999.00),
(2,  21, '优衣库 男士衬衫',         1, 199.00,  199.00),
(2,  24, 'Nike Air Force 1',        1, 799.00,  799.00),
(2,  27, '三只松鼠坚果礼盒',       1, 128.00,  128.00),
(2,  21, '优衣库 男士衬衫',         1, 199.00,  199.00),
(3,  14, '罗技 MX Master 3S',      1, 699.00,  699.00),
(4,  10, 'MacBook Pro 14 M3',      1, 14999.00, 14999.00),
(4,  14, '罗技 MX Master 3S',      1, 699.00,  699.00),
(5,  19, '小米 智能台灯 Pro',       1, 399.00,  399.00),
(6,  19, '小米 智能台灯 Pro',       1, 399.00,  399.00),
(6,  27, '三只松鼠坚果礼盒',       1, 128.00,  128.00),
(7,  2,  '华为 Mate 60 Pro',       1, 6999.00, 6999.00),
(8,  27, '三只松鼠坚果礼盒',       1, 128.00,  128.00),
(9,  24, 'Nike Air Force 1',       2, 1099.00, 2198.00),
(10, 10, 'MacBook Pro 14 M3',      1, 14999.00, 14999.00),
(11, 28, '良品铺子肉脯',           1, 59.90,   59.90),
(12, 1,  'iPhone 15 Pro 256GB',    1, 8999.00, 8999.00),
(13, 3,  '小米14 Ultra',           1, 6499.00, 6499.00),
(14, 22, 'ZARA 女士连衣裙',        1, 399.00,  399.00),
(15, 27, '三只松鼠坚果礼盒',       1, 128.00,  128.00),
(16, 24, 'Nike Air Force 1',       1, 1299.00, 1299.00),
(17, 27, '三只松鼠坚果礼盒',       1, 128.00,  128.00),
(17, 28, '良品铺子肉脯',           1, 59.90,   59.90),
(18, 1,  'iPhone 15 Pro 256GB',    1, 8999.00, 8999.00),
(19, 3,  '小米14 Ultra',           1, 6499.00, 6499.00),
(20, 24, 'Nike Air Force 1',       2, 399.00,  798.00),
(21, 33, '星巴克咖啡豆',           2, 1099.00, 2198.00);
