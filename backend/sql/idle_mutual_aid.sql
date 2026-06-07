SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS idle_mutual_aid DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE idle_mutual_aid;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像',
    role TINYINT DEFAULT 0 COMMENT '角色：0-普通用户 1-管理员',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    credit_score DECIMAL(10,2) DEFAULT 100.00 COMMENT '信用分数',
    credit_level VARCHAR(20) DEFAULT '良好' COMMENT '信用等级',
    exchange_count INT DEFAULT 0 COMMENT '互换次数',
    release_count INT DEFAULT 0 COMMENT '发布次数',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS idle_item;
CREATE TABLE idle_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '物品ID',
    item_name VARCHAR(100) NOT NULL COMMENT '物品名称',
    item_desc TEXT COMMENT '物品描述',
    category VARCHAR(50) NOT NULL COMMENT '物品分类',
    item_images TEXT COMMENT '物品图片，逗号分隔',
    quantity INT NOT NULL DEFAULT 1 COMMENT '物品数量',
    original_value DECIMAL(10,2) COMMENT '原价',
    condition_level TINYINT NOT NULL COMMENT '成色：1-全新 2-九成新 3-八成新 4-七成新及以下',
    pickup_point_id BIGINT NOT NULL COMMENT '自提点ID',
    pickup_point_name VARCHAR(100) COMMENT '自提点名称',
    user_id BIGINT NOT NULL COMMENT '发布人ID',
    username VARCHAR(50) COMMENT '发布人用户名',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已下架 1-上架中 2-互换中 3-已归档',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    exchange_count INT DEFAULT 0 COMMENT '互换次数',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    offline_time DATETIME COMMENT '下架时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_pickup_point_id (pickup_point_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='闲置物品表';

DROP TABLE IF EXISTS exchange_apply;
CREATE TABLE exchange_apply (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    apply_no VARCHAR(32) NOT NULL COMMENT '申请编号',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_name VARCHAR(100) COMMENT '物品名称',
    item_images TEXT COMMENT '物品图片',
    item_owner_id BIGINT NOT NULL COMMENT '物品所有人ID',
    item_owner_name VARCHAR(50) COMMENT '物品所有人',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(50) COMMENT '申请人',
    applicant_phone VARCHAR(20) COMMENT '申请人电话',
    apply_quantity INT NOT NULL DEFAULT 1 COMMENT '申请数量',
    apply_reason VARCHAR(500) COMMENT '申请原因',
    exchange_item_desc TEXT COMMENT '互换物品描述',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-审核通过 2-审核拒绝 3-已完成 4-已取消',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_user_name VARCHAR(50) COMMENT '审核人',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_apply_no (apply_no),
    KEY idx_item_id (item_id),
    KEY idx_applicant_id (applicant_id),
    KEY idx_item_owner_id (item_owner_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='互换申请表';

DROP TABLE IF EXISTS pickup_point;
CREATE TABLE pickup_point (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自提点ID',
    point_name VARCHAR(100) NOT NULL COMMENT '自提点名称',
    address VARCHAR(255) NOT NULL COMMENT '地址',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    business_hours VARCHAR(100) COMMENT '营业时间',
    point_desc VARCHAR(500) COMMENT '自提点描述',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    max_capacity INT DEFAULT 100 COMMENT '最大容量',
    current_stock INT DEFAULT 0 COMMENT '当前库存',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用 1-启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='自提点表';

DROP TABLE IF EXISTS claim_record;
CREATE TABLE claim_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    record_no VARCHAR(32) NOT NULL COMMENT '领用单号',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_name VARCHAR(100) COMMENT '物品名称',
    item_images TEXT COMMENT '物品图片',
    item_owner_id BIGINT NOT NULL COMMENT '物品所有人ID',
    item_owner_name VARCHAR(50) COMMENT '物品所有人',
    claim_user_id BIGINT NOT NULL COMMENT '领用人ID',
    claim_user_name VARCHAR(50) COMMENT '领用人',
    claim_user_phone VARCHAR(20) COMMENT '领用人电话',
    claim_quantity INT NOT NULL DEFAULT 1 COMMENT '领用数量',
    exchange_apply_id BIGINT COMMENT '关联互换申请ID',
    pickup_point_id BIGINT NOT NULL COMMENT '自提点ID',
    pickup_point_name VARCHAR(100) COMMENT '自提点名称',
    claim_time DATETIME COMMENT '申请领用时间',
    pickup_time DATETIME COMMENT '实际领取时间',
    pickup_status TINYINT DEFAULT 0 COMMENT '领取状态：0-待领取 1-已领取 2-已取消',
    pickup_code VARCHAR(20) COMMENT '取件码',
    operator_name VARCHAR(50) COMMENT '操作人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_record_no (record_no),
    KEY idx_item_id (item_id),
    KEY idx_claim_user_id (claim_user_id),
    KEY idx_pickup_status (pickup_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品领用记录表';

DROP TABLE IF EXISTS credit_rating;
CREATE TABLE credit_rating (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    score DECIMAL(10,2) NOT NULL COMMENT '变更后分数',
    level VARCHAR(20) COMMENT '变更后等级',
    change_type VARCHAR(20) NOT NULL COMMENT '变更类型：ADD-加分 SUBTRACT-扣分',
    change_value DECIMAL(10,2) NOT NULL COMMENT '变更分值',
    change_reason VARCHAR(200) NOT NULL COMMENT '变更原因',
    related_id BIGINT COMMENT '关联ID',
    related_type VARCHAR(50) COMMENT '关联类型',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信用评级表';

DROP TABLE IF EXISTS item_archive;
CREATE TABLE item_archive (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '归档ID',
    archive_no VARCHAR(32) NOT NULL COMMENT '归档编号',
    item_id BIGINT NOT NULL COMMENT '物品ID',
    item_name VARCHAR(100) COMMENT '物品名称',
    item_desc TEXT COMMENT '物品描述',
    category VARCHAR(50) COMMENT '物品分类',
    item_images TEXT COMMENT '物品图片',
    quantity INT COMMENT '物品数量',
    original_value DECIMAL(10,2) COMMENT '原价',
    condition_level TINYINT COMMENT '成色',
    pickup_point_id BIGINT COMMENT '自提点ID',
    pickup_point_name VARCHAR(100) COMMENT '自提点名称',
    user_id BIGINT COMMENT '发布人ID',
    username VARCHAR(50) COMMENT '发布人',
    online_days INT COMMENT '上线天数',
    view_count INT COMMENT '浏览次数',
    exchange_count INT COMMENT '互换次数',
    archive_reason VARCHAR(500) COMMENT '归档原因',
    archive_type VARCHAR(20) COMMENT '归档类型：MANUAL-手动 AUTO-自动',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_archive_no (archive_no),
    KEY idx_item_id (item_id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品归档表';

DROP TABLE IF EXISTS monthly_statistics;
CREATE TABLE monthly_statistics (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '统计ID',
    statistics_month VARCHAR(7) NOT NULL COMMENT '统计月份 yyyy-MM',
    new_user_count INT DEFAULT 0 COMMENT '新增用户数',
    active_user_count INT DEFAULT 0 COMMENT '活跃用户数',
    release_item_count INT DEFAULT 0 COMMENT '发布物品数',
    exchange_apply_count INT DEFAULT 0 COMMENT '互换申请数',
    exchange_success_count INT DEFAULT 0 COMMENT '互换成功数',
    exchange_success_rate DECIMAL(5,2) DEFAULT 0 COMMENT '互换成功率%',
    claim_count INT DEFAULT 0 COMMENT '领用数',
    archive_item_count INT DEFAULT 0 COMMENT '归档物品数',
    total_value DECIMAL(12,2) DEFAULT 0 COMMENT '总价值',
    credit_upgrade_count INT DEFAULT 0 COMMENT '信用升级数',
    credit_downgrade_count INT DEFAULT 0 COMMENT '信用降级数',
    pickup_point_count INT DEFAULT 0 COMMENT '自提点总数',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_statistics_month (statistics_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月度互助数据表';

INSERT INTO sys_user (username, password, nickname, phone, role, status, credit_score, credit_level) VALUES
('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', '13800138000', 1, 1, 100.00, '优秀'),
('zhangsan', 'e10adc3949ba59abbe56e057f20f883e', '张三', '13900139000', 0, 1, 95.50, '良好'),
('lisi', 'e10adc3949ba59abbe56e057f20f883e', '李四', '13700137000', 0, 1, 88.00, '良好'),
('wangwu', 'e10adc3949ba59abbe56e057f20f883e', '王五', '13600136000', 0, 1, 102.50, '优秀'),
('zhaoliu', 'e10adc3949ba59abbe56e057f20f883e', '赵六', '13500135000', 0, 1, 75.00, '一般');

INSERT INTO pickup_point (point_name, address, contact_person, contact_phone, business_hours, point_desc, max_capacity, current_stock, status) VALUES
('社区服务中心自提点', '阳光社区服务中心1楼', '张姐', '13800000001', '周一至周日 08:00-20:00', '社区服务中心内，24小时监控', 200, 45, 1),
('东区物业自提点', '阳光花园东区物业办公室', '李哥', '13800000002', '周一至周日 09:00-18:00', '靠近东门，交通便利', 150, 32, 1),
('西区门岗自提点', '阳光花园西大门门岗', '王师傅', '13800000003', '全天24小时', '24小时有人值班', 100, 18, 1),
('南区便民自提点', '阳光花园南区3号楼架空层', '赵阿姨', '13800000004', '周一至周日 07:00-21:00', '便民服务点，位置优越', 120, 25, 1);

INSERT INTO idle_item (item_name, item_desc, category, item_images, quantity, original_value, condition_level, pickup_point_id, pickup_point_name, user_id, username, status, view_count, exchange_count) VALUES
('九成新儿童绘本套装', '包含20本经典儿童绘本，适合3-6岁儿童阅读，无涂画无破损。', '图书文具', '/images/book1.jpg,/images/book2.jpg', 1, 200.00, 2, 1, '社区服务中心自提点', 2, 'zhangsan', 1, 156, 2),
('小米空气净化器2S', '使用半年，滤芯8成新，静音效果好，适合小房间使用。', '家用电器', '/images/air1.jpg', 1, 699.00, 2, 2, '东区物业自提点', 3, 'lisi', 1, 234, 3),
('折叠婴儿推车', '宝宝长大了用不上，可折叠轻便，携带方便，送雨罩。', '母婴用品', '/images/stroller1.jpg,/images/stroller2.jpg', 1, 450.00, 3, 1, '社区服务中心自提点', 4, 'wangwu', 2, 189, 1),
('羽毛球拍套装', '两支尤尼克斯羽毛球拍，送6个羽毛球，几乎全新。', '运动户外', '/images/badminton1.jpg', 1, 320.00, 2, 3, '西区门岗自提点', 2, 'zhangsan', 1, 87, 0),
('整套茶具套装', '紫砂茶具整套，带茶盘，朋友送的一直没用。', '家居用品', '/images/tea1.jpg,/images/tea2.jpg', 1, 580.00, 1, 4, '南区便民自提点', 5, 'zhaoliu', 1, 112, 1),
('儿童自行车12寸', '粉色儿童自行车，辅助轮齐全，适合3-5岁小朋友。', '母婴用品', '/images/bike1.jpg', 1, 380.00, 3, 2, '东区物业自提点', 3, 'lisi', 3, 256, 2),
('办公桌椅套装', '简约现代办公桌椅，椅子是人体工学椅，搬家转让。', '家具家装', '/images/desk1.jpg,/images/desk2.jpg', 1, 1200.00, 2, 1, '社区服务中心自提点', 4, 'wangwu', 1, 312, 4),
('Kindle电子书阅读器', 'Kindle Paperwhite 4代，8G版本，屏幕完好带保护套。', '数码电子', '/images/kindle1.jpg', 1, 558.00, 2, 3, '西区门岗自提点', 2, 'zhangsan', 1, 178, 2),
('电烤箱家用小型', '小熊电烤箱，12L容量，可烤蛋糕面包，使用次数少。', '家用电器', '/images/oven1.jpg', 1, 199.00, 2, 4, '南区便民自提点', 5, 'zhaoliu', 1, 95, 0),
('乐高积木城市系列', '乐高城市警察局套装，零件齐全带说明书，已拼好可拆。', '玩具游戏', '/images/lego1.jpg,/images/lego2.jpg', 1, 420.00, 2, 1, '社区服务中心自提点', 4, 'wangwu', 2, 203, 1);

INSERT INTO exchange_apply (apply_no, item_id, item_name, item_images, item_owner_id, item_owner_name, applicant_id, applicant_name, applicant_phone, apply_quantity, apply_reason, exchange_item_desc, status, audit_user_id, audit_user_name, audit_time, audit_remark) VALUES
('EX202401001', 1, '九成新儿童绘本套装', '/images/book1.jpg,/images/book2.jpg', 2, 'zhangsan', 3, 'lisi', '13700137000', 1, '孩子正好需要这类绘本', '家里有闲置玩具车可以交换', 1, 1, 'admin', '2024-01-15 10:30:00', '审核通过，双方信用良好'),
('EX202401002', 2, '小米空气净化器2S', '/images/air1.jpg', 3, 'lisi', 4, 'wangwu', '13600136000', 1, '新家需要净化空气', '可以用闲置的微波炉交换', 3, 1, 'admin', '2024-01-16 14:20:00', '已完成领用'),
('EX202401003', 3, '折叠婴儿推车', '/images/stroller1.jpg,/images/stroller2.jpg', 4, 'wangwu', 5, 'zhaoliu', '13500135000', 1, '宝宝刚出生需要', '暂时没有可交换的物品，申请直接领用', 2, 1, 'admin', '2024-01-17 09:15:00', '用户信用等级一般，建议有交换物品再申请'),
('EX202401004', 5, '整套茶具套装', '/images/tea1.jpg,/images/tea2.jpg', 5, 'zhaoliu', 2, 'zhangsan', '13900139000', 1, '家里正好缺一套茶具', '有一套咖啡具可以交换', 0, NULL, NULL, NULL, NULL),
('EX202401005', 6, '儿童自行车12寸', '/images/bike1.jpg', 3, 'lisi', 4, 'wangwu', '13600136000', 1, '女儿生日想要一辆自行车', '有儿童安全座椅可以交换', 1, 1, 'admin', '2024-01-18 11:00:00', '审核通过'),
('EX202401006', 10, '乐高积木城市系列', '/images/lego1.jpg,/images/lego2.jpg', 4, 'wangwu', 2, 'zhangsan', '13900139000', 1, '儿子是乐高迷', '有遥控飞机可以交换', 0, NULL, NULL, NULL, NULL);

INSERT INTO claim_record (record_no, item_id, item_name, item_images, item_owner_id, item_owner_name, claim_user_id, claim_user_name, claim_user_phone, claim_quantity, exchange_apply_id, pickup_point_id, pickup_point_name, claim_time, pickup_time, pickup_status, pickup_code, operator_name) VALUES
('CR202401001', 2, '小米空气净化器2S', '/images/air1.jpg', 3, 'lisi', 4, 'wangwu', '13600136000', 1, 2, 2, '东区物业自提点', '2024-01-16 14:30:00', '2024-01-17 10:15:00', 1, 'A1001', '李哥'),
('CR202401002', 7, '办公桌椅套装', '/images/desk1.jpg,/images/desk2.jpg', 4, 'wangwu', 5, 'zhaoliu', '13500135000', 1, NULL, 1, '社区服务中心自提点', '2024-01-19 09:00:00', NULL, 0, 'A1002', '张姐'),
('CR202401003', 8, 'Kindle电子书阅读器', '/images/kindle1.jpg', 2, 'zhangsan', 3, 'lisi', '13700137000', 1, NULL, 3, '西区门岗自提点', '2024-01-20 15:30:00', '2024-01-20 17:45:00', 1, 'A1003', '王师傅'),
('CR202401004', 1, '九成新儿童绘本套装', '/images/book1.jpg,/images/book2.jpg', 2, 'zhangsan', 3, 'lisi', '13700137000', 1, 1, 1, '社区服务中心自提点', '2024-01-15 11:00:00', NULL, 0, 'A1004', '张姐');

INSERT INTO credit_rating (user_id, username, score, level, change_type, change_value, change_reason, related_id, related_type, operator_id, operator_name) VALUES
(2, 'zhangsan', 95.50, '良好', 'ADD', 2.50, '发布物品获得好评', 1, 'IDLE_ITEM', 1, 'admin'),
(2, 'zhangsan', 98.00, '良好', 'ADD', 2.50, '成功完成互换', 1, 'EXCHANGE_APPLY', 1, 'admin'),
(3, 'lisi', 88.00, '良好', 'ADD', 3.00, '成功完成互换', 2, 'EXCHANGE_APPLY', 1, 'admin'),
(4, 'wangwu', 102.50, '优秀', 'ADD', 5.00, '月度互助之星', NULL, 'MONTHLY_AWARD', 1, 'admin'),
(5, 'zhaoliu', 75.00, '一般', 'SUBTRACT', 5.00, '申请互换后爽约', 3, 'EXCHANGE_APPLY', 1, 'admin'),
(2, 'zhangsan', 95.50, '良好', 'ADD', 2.00, '物品被领用好评', 8, 'CLAIM_RECORD', 1, 'admin');

INSERT INTO item_archive (archive_no, item_id, item_name, item_desc, category, item_images, quantity, original_value, condition_level, pickup_point_id, pickup_point_name, user_id, username, online_days, view_count, exchange_count, archive_reason, archive_type, operator_id, operator_name) VALUES
('AR202401001', 6, '儿童自行车12寸', '粉色儿童自行车，辅助轮齐全，适合3-5岁小朋友。', '母婴用品', '/images/bike1.jpg', 1, 380.00, 3, 2, '东区物业自提点', 3, 'lisi', 30, 256, 2, '物品已互换完成', 'AUTO', 1, 'admin'),
('AR202401002', 4, '羽毛球拍套装', '两支尤尼克斯羽毛球拍，送6个羽毛球，几乎全新。', '运动户外', '/images/badminton1.jpg', 1, 320.00, 2, 3, '西区门岗自提点', 2, 'zhangsan', 15, 87, 0, '用户主动下架', 'MANUAL', 2, 'zhangsan');

INSERT INTO monthly_statistics (statistics_month, new_user_count, active_user_count, release_item_count, exchange_apply_count, exchange_success_count, exchange_success_rate, claim_count, archive_item_count, total_value, credit_upgrade_count, credit_downgrade_count, pickup_point_count, remark) VALUES
('2024-01', 12, 45, 28, 18, 12, 66.67, 15, 5, 12580.00, 8, 2, 4, '一月份运营数据'),
('2023-12', 8, 38, 22, 15, 9, 60.00, 11, 3, 9860.00, 5, 1, 4, '十二月份运营数据'),
('2023-11', 15, 52, 35, 22, 16, 72.73, 18, 7, 15680.00, 10, 3, 3, '十一月份运营数据');

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_desc VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    data_scope TINYINT DEFAULT 1 COMMENT '数据权限范围：1-全部 2-本部门及以下 3-本部门 4-本人 5-自定义',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_type TINYINT NOT NULL COMMENT '权限类型：1-菜单 2-按钮 3-接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(200) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏 1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    api_method VARCHAR(10) COMMENT '请求方式：GET/POST/PUT/DELETE',
    api_path VARCHAR(200) COMMENT '接口路径',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_parent_id (parent_id),
    KEY idx_permission_type (permission_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

DROP TABLE IF EXISTS sys_data_permission;
CREATE TABLE sys_data_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型：PICKUP_POINT-自提点 USER-用户',
    business_id BIGINT NOT NULL COMMENT '业务ID',
    create_time DATETIME COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_business (role_id, business_type, business_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据权限表';

ALTER TABLE sys_user DROP COLUMN role;
ALTER TABLE sys_user ADD COLUMN dept_id BIGINT COMMENT '部门ID' AFTER avatar;

INSERT INTO sys_role (role_code, role_name, role_desc, status, data_scope, sort_order) VALUES
('SUPER_ADMIN', '超级管理员', '拥有系统所有权限', 1, 1, 1),
('OPERATOR', '运营专员', '负责日常运营管理', 1, 1, 2),
('AUDITOR', '审核专员', '负责互换申请审核', 1, 1, 3),
('PICKUP_ADMIN', '自提点管理员', '负责自提点管理', 1, 5, 4),
('NORMAL_USER', '普通用户', '普通注册用户', 1, 4, 5);

INSERT INTO sys_permission (permission_code, permission_name, permission_type, parent_id, path, component, icon, sort_order, visible, status, api_method, api_path) VALUES
('dashboard', '仪表盘', 1, 0, 'dashboard', 'dashboard/index', 'Odometer', 1, 1, 1, NULL, NULL),
('idle_item', '闲置物品', 1, 0, 'idle-item', 'idle-item/index', 'Goods', 2, 1, 1, NULL, NULL),
('idle_item_list', '物品列表', 2, 2, NULL, NULL, NULL, 1, 1, 1, 'GET', '/idle-item/page'),
('idle_item_add', '发布物品', 2, 2, NULL, NULL, 'Plus', 2, 1, 1, 'POST', '/idle-item'),
('idle_item_edit', '编辑物品', 2, 2, NULL, NULL, 'Edit', 3, 1, 1, 'PUT', '/idle-item'),
('idle_item_delete', '删除物品', 2, 2, NULL, NULL, 'Delete', 4, 1, 1, 'DELETE', '/idle-item/*'),
('exchange_apply', '互换申请', 1, 0, 'exchange-apply', 'exchange-apply/index', 'RefreshRight', 3, 1, 1, NULL, NULL),
('exchange_apply_list', '申请列表', 2, 7, NULL, NULL, NULL, 1, 1, 1, 'GET', '/exchange-apply/page'),
('exchange_apply_audit', '审核申请', 2, 7, NULL, NULL, 'Check', 2, 1, 1, 'POST', '/exchange-apply/*/audit'),
('pickup_point', '自提点管理', 1, 0, 'pickup-point', 'pickup-point/index', 'Location', 4, 1, 1, NULL, NULL),
('pickup_point_list', '自提点列表', 2, 10, NULL, NULL, NULL, 1, 1, 1, 'GET', '/pickup-point/page'),
('pickup_point_add', '新增自提点', 2, 10, NULL, NULL, 'Plus', 2, 1, 1, 'POST', '/pickup-point'),
('pickup_point_edit', '编辑自提点', 2, 10, NULL, NULL, 'Edit', 3, 1, 1, 'PUT', '/pickup-point'),
('pickup_point_delete', '删除自提点', 2, 10, NULL, NULL, 'Delete', 4, 1, 1, 'DELETE', '/pickup-point/*'),
('claim_record', '领用记录', 1, 0, 'claim-record', 'claim-record/index', 'Document', 5, 1, 1, NULL, NULL),
('claim_record_list', '领用列表', 2, 15, NULL, NULL, NULL, 1, 1, 1, 'GET', '/claim-record/page'),
('claim_record_confirm', '确认领取', 2, 15, NULL, NULL, 'Check', 2, 1, 1, 'POST', '/claim-record/*/confirm'),
('credit_rating', '信用评级', 1, 0, 'credit-rating', 'credit-rating/index', 'Medal', 6, 1, 1, NULL, NULL),
('credit_rating_list', '评级记录', 2, 18, NULL, NULL, NULL, 1, 1, 1, 'GET', '/credit-rating/page'),
('credit_rating_adjust', '信用调整', 2, 18, NULL, NULL, 'Edit', 2, 1, 1, 'POST', '/credit-rating/adjust'),
('item_archive', '归档管理', 1, 0, 'item-archive', 'item-archive/index', 'Folder', 7, 1, 1, NULL, NULL),
('item_archive_list', '归档列表', 2, 21, NULL, NULL, NULL, 1, 1, 1, 'GET', '/item-archive/page'),
('statistics', '数据统计', 1, 0, 'statistics', 'statistics/index', 'DataLine', 8, 1, 1, NULL, NULL),
('statistics_view', '查看统计', 2, 23, NULL, NULL, NULL, 1, 1, 1, 'GET', '/statistics/**'),
('system', '系统管理', 1, 0, 'system', NULL, 'Setting', 9, 1, 1, NULL, NULL),
('system_user', '用户管理', 1, 25, 'user', 'system/user/index', 'User', 1, 1, 1, NULL, NULL),
('system_user_list', '用户列表', 2, 26, NULL, NULL, NULL, 1, 1, 1, 'GET', '/auth/users'),
('system_user_edit', '编辑用户', 2, 26, NULL, NULL, 'Edit', 2, 1, 1, 'PUT', '/auth/user/*'),
('system_user_assign_role', '分配角色', 2, 26, NULL, NULL, 'UserFilled', 3, 1, 1, 'POST', '/auth/user/*/roles'),
('system_role', '角色管理', 1, 25, 'role', 'system/role/index', 'Avatar', 2, 1, 1, NULL, NULL),
('system_role_list', '角色列表', 2, 29, NULL, NULL, NULL, 1, 1, 1, 'GET', '/system/role/page'),
('system_role_add', '新增角色', 2, 29, NULL, NULL, 'Plus', 2, 1, 1, 'POST', '/system/role'),
('system_role_edit', '编辑角色', 2, 29, NULL, NULL, 'Edit', 3, 1, 1, 'PUT', '/system/role'),
('system_role_delete', '删除角色', 2, 29, NULL, NULL, 'Delete', 4, 1, 1, 'DELETE', '/system/role/*'),
('system_role_assign_permission', '分配权限', 2, 29, NULL, NULL, 'Key', 5, 1, 1, 'POST', '/system/role/*/permissions'),
('system_permission', '权限管理', 1, 25, 'permission', 'system/permission/index', 'Key', 3, 1, 1, NULL, NULL),
('system_permission_list', '权限列表', 2, 34, NULL, NULL, NULL, 1, 1, 1, 'GET', '/system/permission/list'),
('system_permission_add', '新增权限', 2, 34, NULL, NULL, 'Plus', 2, 1, 1, 'POST', '/system/permission'),
('system_permission_edit', '编辑权限', 2, 34, NULL, NULL, 'Edit', 3, 1, 1, 'PUT', '/system/permission'),
('system_permission_delete', '删除权限', 2, 34, NULL, NULL, 'Delete', 4, 1, 1, 'DELETE', '/system/permission/*');

INSERT INTO sys_user_role (user_id, role_id, create_time) VALUES
(1, 1, NOW()),
(2, 5, NOW()),
(3, 5, NOW()),
(4, 5, NOW()),
(5, 5, NOW());

INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(1, 1, NOW()), (1, 2, NOW()), (1, 3, NOW()), (1, 4, NOW()), (1, 5, NOW()), (1, 6, NOW()),
(1, 7, NOW()), (1, 8, NOW()), (1, 9, NOW()), (1, 10, NOW()), (1, 11, NOW()), (1, 12, NOW()),
(1, 13, NOW()), (1, 14, NOW()), (1, 15, NOW()), (1, 16, NOW()), (1, 17, NOW()), (1, 18, NOW()),
(1, 19, NOW()), (1, 20, NOW()), (1, 21, NOW()), (1, 22, NOW()), (1, 23, NOW()), (1, 24, NOW()),
(1, 25, NOW()), (1, 26, NOW()), (1, 27, NOW()), (1, 28, NOW()), (1, 29, NOW()), (1, 30, NOW()),
(1, 31, NOW()), (1, 32, NOW()), (1, 33, NOW()), (1, 34, NOW()), (1, 35, NOW()), (1, 36, NOW()),
(1, 37, NOW()), (1, 38, NOW()),
(2, 1, NOW()), (2, 2, NOW()), (2, 3, NOW()), (2, 4, NOW()), (2, 5, NOW()), (2, 6, NOW()),
(2, 7, NOW()), (2, 8, NOW()), (2, 9, NOW()), (2, 10, NOW()), (2, 11, NOW()), (2, 12, NOW()),
(2, 13, NOW()), (2, 14, NOW()), (2, 15, NOW()), (2, 16, NOW()), (2, 18, NOW()), (2, 19, NOW()),
(2, 21, NOW()), (2, 22, NOW()), (2, 23, NOW()), (2, 24, NOW()),
(3, 1, NOW()), (3, 7, NOW()), (3, 8, NOW()), (3, 9, NOW()), (3, 15, NOW()), (3, 16, NOW()),
(3, 17, NOW()), (3, 18, NOW()), (3, 19, NOW()),
(4, 1, NOW()), (4, 10, NOW()), (4, 11, NOW()), (4, 13, NOW()), (4, 15, NOW()), (4, 16, NOW()),
(4, 17, NOW()),
(5, 1, NOW()), (5, 2, NOW()), (5, 3, NOW()), (5, 4, NOW()), (5, 7, NOW()), (5, 8, NOW()),
(5, 10, NOW()), (5, 11, NOW()), (5, 15, NOW()), (5, 16, NOW()), (5, 18, NOW()), (5, 19, NOW());

INSERT INTO sys_data_permission (role_id, business_type, business_id, create_time) VALUES
(4, 'PICKUP_POINT', 1, NOW()),
(4, 'PICKUP_POINT', 2, NOW());

UPDATE sys_user SET dept_id = 1 WHERE id = 1;
UPDATE sys_user SET dept_id = 2 WHERE id = 2;
UPDATE sys_user SET dept_id = 2 WHERE id = 3;
UPDATE sys_user SET dept_id = 3 WHERE id = 4;
UPDATE sys_user SET dept_id = 3 WHERE id = 5;
