-- Initial data for local testing

-- Onboarding fee plans
INSERT INTO tb_onboarding_fee_plan (plan_id, plan_name, plan_type, fee_amount, duration_months, description, status) VALUES
(1, '单店年费', 1, 2000.00, 12, '适用于单门店商户，按门店计费，每年2000元/店', 1),
(2, '品牌基础年费', 2, 10000.00, 12, '适用于品牌商户（3家门店以内），每年10000元', 1),
(3, '品牌标准年费', 2, 30000.00, 12, '适用于品牌商户（10家门店以内），每年30000元', 1),
(4, '品牌旗舰年费', 2, 50000.00, 12, '适用于品牌商户（不限门店），每年50000元', 1);

-- Test user
INSERT INTO tb_user (user_id, openid, nickname, phone, status) VALUES
(100001, 'test_openid_001', '测试用户A', '13800138001', 0);

-- Test merchant 1 (active, with location for nearby search)
INSERT INTO tb_merchant (merchant_id, merchant_code, merchant_name, contact_name, contact_phone, address, city, longitude, latitude, business_category, status, integration_type, profit_sharing_rate) VALUES
(200001, 'M202602220001', '老王烤鱼', '王老板', '13900139001', '深圳市南山区科技园路1号', '深圳', 113.944580, 22.531160, '中餐', 1, 3, 0.0200);

-- Test merchant 2 (for same city test)
INSERT INTO tb_merchant (merchant_id, merchant_code, merchant_name, contact_name, contact_phone, address, city, longitude, latitude, business_category, status, integration_type, profit_sharing_rate) VALUES
(200002, 'M202602220002', '小李奶茶', '李老板', '13900139002', '深圳市南山区深南大道2号', '深圳', 113.942100, 22.532800, '奶茶', 1, 1, 0.0150);

-- Test branch
INSERT INTO tb_merchant_branch (branch_id, merchant_id, branch_name, address, status) VALUES
(300001, 200001, '老王烤鱼(科技园店)', '深圳市南山区科技园路1号', 1),
(300002, 200002, '小李奶茶(深南店)', '深圳市南山区深南大道2号', 1);

-- Test employee (admin)
INSERT INTO tb_merchant_employee (employee_id, merchant_id, branch_id, name, phone, openid, role, status) VALUES
(400001, 200001, 300001, '王老板', '13900139001', 'test_openid_001', 1, 1);

-- Test member snapshot (user is member at merchant 1) with expiring points
INSERT INTO tb_merchant_member_snapshot (snapshot_id, user_id, merchant_id, source_member_id, member_level_name, points, points_value, points_expire_date, balance, consume_count, total_consume_amount, dormancy_level, sync_time, sync_status) VALUES
(500001, 100001, 200001, 'EXT_M001', '金卡会员', 2500, 25.00, '2026-02-25', 388.50, 15, 3280.00, 0, CURRENT_TIMESTAMP, 1);

-- Test member snapshot (user is dormant member at merchant 2)
INSERT INTO tb_merchant_member_snapshot (snapshot_id, user_id, merchant_id, source_member_id, member_level_name, points, points_value, points_expire_date, balance, consume_count, total_consume_amount, dormancy_level, sync_time, sync_status) VALUES
(500002, 100001, 200002, 'EXT_M002', '普通会员', 500, 5.00, '2026-03-15', 0.00, 2, 68.00, 2, CURRENT_TIMESTAMP, 1);

-- Test activities for merchant 1 (老王烤鱼)
INSERT INTO tb_activity (activity_id, merchant_id, activity_type, activity_name, activity_desc, status, config, stock, sold_count, target_member_type, is_public, sync_source, sort_order, start_time, end_time) VALUES
(600001, 200001, 3, '夏日烤鱼8折券', '原价100元代金券，仅售80元', 1, '{"voucher_value":100,"selling_price":80,"min_consume":200,"valid_days":30}', 100, 15, 0, 1, 'manual', 1, '2026-02-01 00:00:00', '2026-03-31 23:59:59'),
(600002, 200001, 1, '2500积分换咖啡', '消费积分兑换精品咖啡一杯', 1, '{"points_required":2500,"product_name":"精品美式咖啡"}', -1, 8, 1, 0, 'manual', 2, '2026-02-01 00:00:00', '2026-12-31 23:59:59'),
(600003, 200001, 3, '沉睡会员专属5折券', '老会员回归专属优惠', 1, '{"voucher_value":50,"selling_price":25,"target_dormancy":[2,3],"valid_days":14}', 50, 3, 2, 0, 'manual', 3, '2026-02-01 00:00:00', '2026-06-30 23:59:59'),
(600004, 200001, 2, '充500送80', '储值优惠活动，充值即返现', 1, '{"recharge_amount":500,"gift_amount":80}', -1, 12, 0, 1, 'manual', 4, '2026-02-01 00:00:00', '2026-03-31 23:59:59');

-- Test activities for merchant 2 (小李奶茶)
INSERT INTO tb_activity (activity_id, merchant_id, activity_type, activity_name, activity_desc, status, config, stock, sold_count, target_member_type, is_public, sync_source, sort_order, start_time, end_time) VALUES
(600005, 200002, 3, '奶茶买一送一', '任意奶茶买一杯送一杯', 1, '{"voucher_value":20,"selling_price":15,"min_consume":0,"valid_days":7}', 200, 88, 0, 1, 'api', 1, '2026-02-20 00:00:00', '2026-02-28 23:59:59'),
(600006, 200002, 4, '团购套餐', '双人下午茶套餐原价88元团购价58元', 1, '{"original_price":88,"group_price":58,"source":"manual","valid_days":30}', 50, 22, 0, 1, 'manual', 2, '2026-02-01 00:00:00', '2026-03-31 23:59:59'),
(600007, 200002, 3, '老顾客回归礼', '60天未消费会员专属半价券', 1, '{"voucher_value":30,"selling_price":15,"target_dormancy":[2,3],"valid_days":14}', 100, 5, 2, 0, 'manual', 3, '2026-02-01 00:00:00', '2026-06-30 23:59:59');

-- Test voucher expiring in 5 days
INSERT INTO tb_voucher (voucher_id, voucher_code, order_id, user_id, merchant_id, activity_id, voucher_type, voucher_value, status, valid_start_time, valid_end_time) VALUES
(700001, 'V20260222001', 800001, 100001, 200001, 600001, 1, 100.00, 0, CURRENT_TIMESTAMP, '2026-02-27 23:59:59');

-- Test order (paid)
INSERT INTO tb_order (order_id, order_no, user_id, merchant_id, activity_id, order_type, order_amount, pay_amount, status, pay_time, transaction_id, is_dormancy_awake) VALUES
(800001, '20260222100001000001', 100001, 200001, 600001, 3, 80.00, 80.00, 1, '2026-02-22 10:00:00', 'WX_TXN_001', 0);

-- Test profit sharing record (pending verification)
INSERT INTO tb_profit_sharing (sharing_id, order_no, voucher_id, merchant_id, transaction_id, total_amount, service_fee_rate, service_fee, merchant_amount, status, retry_count) VALUES
(850001, '20260222100001000001', 700001, 200001, 'WX_TXN_001', 80.00, 0.0200, 1.60, 78.40, 0, 0);

-- Test notification setting
INSERT INTO tb_user_notification_setting (setting_id, user_id, points_expire_notify, balance_notify, voucher_expire_notify, activity_notify) VALUES
(900001, 100001, 1, 1, 1, 1);

-- Test equity reminder (pending)
INSERT INTO tb_equity_reminder (reminder_id, user_id, merchant_id, reminder_type, equity_type, equity_value, expire_date, remind_status) VALUES
(1000001, 100001, 200001, 1, 'points', 12.00, '2026-02-25', 0);

-- NOTE: tb_admin_user default admin account is created by DataInitializer with BCrypt password

-- Test merchant audit log
INSERT INTO tb_merchant_audit_log (log_id, merchant_id, admin_id, action, prev_status, new_status, reason, created_at) VALUES
(1100001, 200001, 1, 'APPROVE', 0, 1, '资料齐全，审核通过', '2026-02-20 09:00:00'),
(1100002, 200002, 1, 'APPROVE', 0, 1, '品牌资质确认，审核通过', '2026-02-21 10:30:00');

-- Test admin operation log
INSERT INTO tb_admin_operation_log (log_id, admin_id, module, action, target_id, detail, ip, created_at) VALUES
(1200001, 1, 'merchant', 'AUDIT_APPROVE', '200001', '审核通过商户：老王烤鱼', '127.0.0.1', '2026-02-20 09:00:00'),
(1200002, 1, 'merchant', 'AUDIT_APPROVE', '200002', '审核通过商户：小李奶茶', '127.0.0.1', '2026-02-21 10:30:00');
