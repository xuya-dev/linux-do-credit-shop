-- ============================================================
-- LDC Shop 数据库初始化脚本 / LDC Shop Database Init Script
-- 数据库: ldc_shop / Database: ldc_shop
-- 字符集: utf8mb4 / Charset: utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS `ldc_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ldc_shop`;

-- ============================================================
-- 1. 用户表 / User Table
-- 存储 LINUX DO OAuth 同步的用户信息
-- ============================================================
DROP TABLE IF EXISTS `ldc_user`;
CREATE TABLE `ldc_user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID / User ID',
    `ldc_uid`       VARCHAR(64)  NOT NULL COMMENT 'LINUX DO 用户ID / LINUX DO User ID',
    `username`      VARCHAR(100) NOT NULL COMMENT '用户名 / Username',
    `nickname`      VARCHAR(100) DEFAULT NULL COMMENT '昵称 / Nickname',
    `avatar`        VARCHAR(500) DEFAULT NULL COMMENT '头像URL / Avatar URL',
    `email`         VARCHAR(200) DEFAULT NULL COMMENT '邮箱 / Email',
    `trust_level`   TINYINT      DEFAULT 0 COMMENT '信任等级 / Trust Level',
    `role`          VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '角色: admin/user / Role: admin/user',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=正常 / Status: 0=disabled 1=enabled',
    `last_login_at` DATETIME     DEFAULT NULL COMMENT '最后登录时间 / Last Login Time',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除 / Logical Delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ldc_user_ldc_uid` (`ldc_uid`),
    KEY `idx_ldc_user_username` (`username`),
    KEY `idx_ldc_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表 / User Table';

-- ============================================================
-- 2. 商品分类表 / Category Table
-- ============================================================
DROP TABLE IF EXISTS `ldc_category`;
CREATE TABLE `ldc_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID / Category ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '分类名称 / Category Name',
    `icon`        VARCHAR(500) DEFAULT NULL COMMENT '分类图标 / Category Icon',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '排序值 / Sort Order',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0=禁用 1=启用 / Status: 0=disabled 1=enabled',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 / Logical Delete',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表 / Category Table';

-- ============================================================
-- 3. 商品表 / Product Table
-- 支持虚拟商品(卡密)和实物商品
-- ============================================================
DROP TABLE IF EXISTS `ldc_product`;
CREATE TABLE `ldc_product` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '商品ID / Product ID',
    `category_id`   BIGINT        DEFAULT NULL COMMENT '分类ID / Category ID',
    `name`          VARCHAR(200)  NOT NULL COMMENT '商品名称 / Product Name',
    `description`   TEXT          DEFAULT NULL COMMENT '商品描述 / Product Description',
    `cover_image`   VARCHAR(500)  DEFAULT NULL COMMENT '封面图片 / Cover Image',
    `images`        JSON          DEFAULT NULL COMMENT '商品图片列表(JSON) / Product Images(JSON)',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '积分价格 / Credit Price',
    `product_type`  TINYINT       NOT NULL DEFAULT 1 COMMENT '商品类型: 1=虚拟(卡密) 2=实物 / Type: 1=virtual 2=physical',
    `stock`         INT           NOT NULL DEFAULT 0 COMMENT '库存数量 / Stock Count',
    `sold_count`    INT           NOT NULL DEFAULT 0 COMMENT '已售数量 / Sold Count',
    `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0=下架 1=上架 / Status: 0=off 1=on',
    `sort_order`    INT           NOT NULL DEFAULT 0 COMMENT '排序值 / Sort Order',
    `created_by`    BIGINT        DEFAULT NULL COMMENT '创建人ID / Created By',
    `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`       TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 / Logical Delete',
    PRIMARY KEY (`id`),
    KEY `idx_ldc_product_category_id` (`category_id`),
    KEY `idx_ldc_product_status` (`status`),
    KEY `idx_ldc_product_type` (`product_type`),
    KEY `idx_ldc_product_category_status` (`category_id`, `status`),
    CONSTRAINT `fk_ldc_product_category` FOREIGN KEY (`category_id`) REFERENCES `ldc_category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表 / Product Table';

-- ============================================================
-- 4. 卡密表 / Product Card Table
-- 虚拟商品的卡密/激活码池
-- ============================================================
DROP TABLE IF EXISTS `ldc_product_card`;
CREATE TABLE `ldc_product_card` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '卡密ID / Card ID',
    `product_id`  BIGINT   NOT NULL COMMENT '商品ID / Product ID',
    `card_content` TEXT    NOT NULL COMMENT '卡密内容 / Card Content',
    `status`      TINYINT  NOT NULL DEFAULT 0 COMMENT '状态: 0=可用 1=已售 2=已禁用 / Status: 0=available 1=sold 2=disabled',
    `order_id`    BIGINT   DEFAULT NULL COMMENT '关联订单ID / Related Order ID',
    `sold_at`     DATETIME DEFAULT NULL COMMENT '售出时间 / Sold Time',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    PRIMARY KEY (`id`),
    KEY `idx_ldc_product_card_product_id` (`product_id`),
    KEY `idx_ldc_product_card_status` (`status`),
    KEY `idx_ldc_product_card_order_id` (`order_id`),
    KEY `idx_ldc_product_card_product_status` (`product_id`, `status`),
    CONSTRAINT `fk_ldc_product_card_product` FOREIGN KEY (`product_id`) REFERENCES `ldc_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡密表 / Product Card Table';

-- ============================================================
-- 5. 订单表 / Order Table
-- ============================================================
DROP TABLE IF EXISTS `ldc_order`;
CREATE TABLE `ldc_order` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID / Order ID',
    `order_no`         VARCHAR(64)   NOT NULL COMMENT '订单编号 / Order Number',
    `user_id`          BIGINT        NOT NULL COMMENT '买家用户ID / Buyer User ID',
    `product_id`       BIGINT        NOT NULL COMMENT '商品ID / Product ID',
    `product_name`     VARCHAR(200)  NOT NULL COMMENT '商品名称(快照) / Product Name(Snapshot)',
    `product_type`     TINYINT       NOT NULL COMMENT '商品类型: 1=虚拟 2=实物 / Product Type',
    `quantity`         INT           NOT NULL DEFAULT 1 COMMENT '购买数量 / Quantity',
    `unit_price`       DECIMAL(10,2) NOT NULL COMMENT '单价(积分) / Unit Price(Credits)',
    `total_amount`     DECIMAL(10,2) NOT NULL COMMENT '总金额(积分) / Total Amount(Credits)',
    `ldc_trade_no`     VARCHAR(100)  DEFAULT NULL COMMENT 'LDC平台交易号 / LDC Trade Number',
    `ldc_out_trade_no` VARCHAR(100)  DEFAULT NULL COMMENT '商户业务单号 / Merchant Out Trade Number',
    `payment_status`   TINYINT       NOT NULL DEFAULT 0 COMMENT '支付状态: 0=待支付 1=已支付 2=已退款 3=已取消 / Payment Status',
    `delivery_status`  TINYINT       NOT NULL DEFAULT 0 COMMENT '发货状态: 0=待发货 1=已发货 2=已完成 / Delivery Status',
    `delivery_info`    JSON          DEFAULT NULL COMMENT '配送信息(JSON) / Delivery Info(JSON)',
    `contact_info`     VARCHAR(500)  DEFAULT NULL COMMENT '联系方式 / Contact Info',
    `remark`           VARCHAR(500)  DEFAULT NULL COMMENT '买家备注 / Buyer Remark',
    `admin_remark`     VARCHAR(500)  DEFAULT NULL COMMENT '管理员备注 / Admin Remark',
    `paid_at`          DATETIME      DEFAULT NULL COMMENT '支付时间 / Paid Time',
    `delivered_at`     DATETIME      DEFAULT NULL COMMENT '发货时间 / Delivered Time',
    `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`          TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除 / Logical Delete',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ldc_order_order_no` (`order_no`),
    KEY `idx_ldc_order_user_id` (`user_id`),
    KEY `idx_ldc_order_product_id` (`product_id`),
    KEY `idx_ldc_order_payment_status` (`payment_status`),
    KEY `idx_ldc_order_ldc_trade_no` (`ldc_trade_no`),
    KEY `idx_ldc_order_user_payment` (`user_id`, `payment_status`),
    KEY `idx_ldc_order_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表 / Order Table';

-- ============================================================
-- 6. 订单卡密关联表 / Order Card Relation Table
-- 记录订单分配的卡密
-- ============================================================
DROP TABLE IF EXISTS `ldc_order_card`;
CREATE TABLE `ldc_order_card` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `order_id`   BIGINT NOT NULL COMMENT '订单ID / Order ID',
    `card_id`    BIGINT NOT NULL COMMENT '卡密ID / Card ID',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    PRIMARY KEY (`id`),
    KEY `idx_ldc_order_card_order_id` (`order_id`),
    KEY `idx_ldc_order_card_card_id` (`card_id`),
    CONSTRAINT `fk_ldc_order_card_order` FOREIGN KEY (`order_id`) REFERENCES `ldc_order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ldc_order_card_card` FOREIGN KEY (`card_id`) REFERENCES `ldc_product_card` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单卡密关联表 / Order Card Relation Table';

-- ============================================================
-- 7. 争议表 / Dispute Table
-- ============================================================
DROP TABLE IF EXISTS `ldc_dispute`;
CREATE TABLE `ldc_dispute` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '争议ID / Dispute ID',
    `order_id`    BIGINT       NOT NULL COMMENT '订单ID / Order ID',
    `user_id`     BIGINT       NOT NULL COMMENT '发起用户ID / Initiator User ID',
    `reason`      TEXT         NOT NULL COMMENT '争议原因 / Dispute Reason',
    `evidence`    JSON         DEFAULT NULL COMMENT '证据(图片URL列表) / Evidence(Image URLs)',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=待处理 1=已同意(退款) 2=已拒绝 3=平台介入 / Status',
    `admin_note`  TEXT         DEFAULT NULL COMMENT '管理员处理备注 / Admin Processing Note',
    `handled_by`  BIGINT       DEFAULT NULL COMMENT '处理人ID / Handler ID',
    `handled_at`  DATETIME     DEFAULT NULL COMMENT '处理时间 / Handled Time',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 / Logical Delete',
    PRIMARY KEY (`id`),
    KEY `idx_ldc_dispute_order_id` (`order_id`),
    KEY `idx_ldc_dispute_user_id` (`user_id`),
    KEY `idx_ldc_dispute_status` (`status`),
    KEY `idx_ldc_dispute_user_status` (`user_id`, `status`),
    CONSTRAINT `fk_ldc_dispute_order` FOREIGN KEY (`order_id`) REFERENCES `ldc_order` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ldc_dispute_user` FOREIGN KEY (`user_id`) REFERENCES `ldc_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='争议表 / Dispute Table';

-- ============================================================
-- 8. 公告表 / Announcement Table
-- ============================================================
DROP TABLE IF EXISTS `ldc_announcement`;
CREATE TABLE `ldc_announcement` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '公告ID / Announcement ID',
    `title`        VARCHAR(200) NOT NULL COMMENT '公告标题 / Announcement Title',
    `content`      LONGTEXT     DEFAULT NULL COMMENT '公告正文(Markdown) / Content(Markdown)',
    `type`         TINYINT      NOT NULL DEFAULT 1 COMMENT '类型: 1=通知 2=活动 3=更新 / Type: 1=notice 2=activity 3=update',
    `is_top`       TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶: 0=否 1=是 / Is Pinned',
    `status`       TINYINT      NOT NULL DEFAULT 0 COMMENT '状态: 0=草稿 1=已发布 / Status: 0=draft 1=published',
    `cover_image`  VARCHAR(500) DEFAULT NULL COMMENT '封面图片 / Cover Image',
    `published_at` DATETIME     DEFAULT NULL COMMENT '发布时间 / Published Time',
    `created_by`   BIGINT       DEFAULT NULL COMMENT '创建人ID / Created By',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created Time',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    `deleted`      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 / Logical Delete',
    PRIMARY KEY (`id`),
    KEY `idx_ldc_announcement_type` (`type`),
    KEY `idx_ldc_announcement_status` (`status`),
    KEY `idx_ldc_announcement_is_top` (`is_top`),
    CONSTRAINT `fk_ldc_announcement_user` FOREIGN KEY (`created_by`) REFERENCES `ldc_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表 / Announcement Table';

-- ============================================================
-- 9. 系统配置表 / Config Table
-- ============================================================
DROP TABLE IF EXISTS `ldc_setting`;
CREATE TABLE `ldc_setting` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置ID / Config ID',
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键 / Config Key',
    `config_value` TEXT         DEFAULT NULL COMMENT '配置值 / Config Value',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '配置描述 / Description',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated Time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ldc_setting_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表 / Config Table';

-- ============================================================
-- 初始化数据 / Initial Data
-- ============================================================

-- 默认系统配置 / Default System Settings
INSERT INTO `ldc_setting` (`config_key`, `config_value`, `description`) VALUES
('shop_name', 'LDC Shop', '商店名称 / Shop Name'),
('shop_description', 'LINUX DO Credit 积分商城', '商店描述 / Shop Description'),
('shop_logo', '', '商店Logo / Shop Logo'),
('shop_notice', '', '店铺公告 / Shop Notice'),
('ldc_payment_client_id', '', 'LDC支付Client ID / LDC Payment Client ID'),
('ldc_payment_client_secret', '', 'LDC支付Client Secret / LDC Payment Client Secret'),
('ldc_payment_private_key', '', 'Ed25519商户私钥(Base64) / Ed25519 Merchant Private Key(Base64)'),
('ldc_payment_public_key', '', 'LDC平台公钥(Base64) / LDC Platform Public Key(Base64)'),
('ldc_payment_gateway_url', 'https://credit.linux.do/epay', 'LDC网关地址 / LDC Gateway URL'),
('ldc_payment_notify_url', '', 'LDC异步通知地址 / LDC Notify URL'),
('ldc_payment_return_url', '', 'LDC同步跳转地址 / LDC Return URL'),
('ldc_oauth_client_id', '', 'OAuth Client ID'),
('ldc_oauth_client_secret', '', 'OAuth Client Secret'),
('ldc_oauth_redirect_uri', '', 'OAuth 回调地址 / OAuth Redirect URI'),
('ldc_oauth_authorize_url', 'https://connect.linux.do/oauth2/authorize', 'OAuth 授权地址 / OAuth Authorize URL'),
('ldc_oauth_token_url', 'https://connect.linux.do/oauth2/token', 'OAuth 令牌地址 / OAuth Token URL'),
('ldc_oauth_user_info_url', 'https://connect.linux.do/api/user', 'OAuth 用户信息地址 / OAuth User Info URL'),
('ldc_admin_usernames', 'admin', '管理员用户名(逗号分隔) / Admin Usernames(comma separated)');
