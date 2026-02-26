-- Initial data for local testing

-- Onboarding fee plans
INSERT INTO tb_onboarding_fee_plan (plan_id, plan_name, plan_type, fee_amount, duration_months, description, status) VALUES
(1, '单店年费', 1, 2000.00, 12, '适用于单门店商户，按门店计费，每年2000元/店', 1),
(2, '品牌基础年费', 2, 10000.00, 12, '适用于品牌商户（3家门店以内），每年10000元', 1),
(3, '品牌标准年费', 2, 30000.00, 12, '适用于品牌商户（10家门店以内），每年30000元', 1),
(4, '品牌旗舰年费', 2, 50000.00, 12, '适用于品牌商户（不限门店），每年50000元', 1);

-- Open cities
INSERT INTO tb_open_city (city_id, city_code, city_name, province_name, pinyin, level, longitude, latitude, status, sort_order, open_date) VALUES
(1, '440300', '深圳', '广东省', 'S', 1, 114.057868, 22.543099, 1, 100, CURRENT_DATE),
(2, '440100', '广州', '广东省', 'G', 1, 113.264385, 23.129112, 1, 99, CURRENT_DATE),
(3, '110000', '北京', '北京市', 'B', 1, 116.405285, 39.904989, 1, 98, CURRENT_DATE),
(4, '310000', '上海', '上海市', 'S', 1, 121.472644, 31.231706, 1, 97, CURRENT_DATE),
(5, '330100', '杭州', '浙江省', 'H', 2, 120.153576, 30.287459, 1, 90, CURRENT_DATE);

-- Test users
INSERT INTO tb_user (user_id, openid, nickname, phone, status) VALUES
(100001, 'test_openid_001', '测试用户A', '13800138001', 0),
(100002, 'test_openid_002', '测试用户B', '13800138002', 0),
(100003, 'test_openid_003', '测试用户C', '13800138003', 0);

-- Test merchant 1 (active, with location for nearby search)
INSERT INTO tb_merchant (merchant_id, merchant_code, merchant_name, contact_name, contact_phone, address, city, longitude, latitude, business_category, status, integration_type, profit_sharing_rate) VALUES
(200001, 'M202602220001', '老王烤鱼', '王老板', '13900139001', '深圳市南山区科技园路1号', '深圳', 113.944580, 22.531160, '中餐', 1, 1, 0.0200);

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

-- User 100002 is dormant member at merchant 200001
INSERT INTO tb_merchant_member_snapshot (snapshot_id, user_id, merchant_id, source_member_id, member_level_name, points, points_value, points_expire_date, balance, consume_count, total_consume_amount, dormancy_level, sync_time, sync_status) VALUES
(500003, 100002, 200001, 'EXT_M003', '银卡会员', 800, 8.00, '2026-03-10', 0.00, 5, 580.00, 2, CURRENT_TIMESTAMP, 1);

-- User 100003 is lightly dormant at merchant 200002
INSERT INTO tb_merchant_member_snapshot (snapshot_id, user_id, merchant_id, source_member_id, member_level_name, points, points_value, points_expire_date, balance, consume_count, total_consume_amount, dormancy_level, sync_time, sync_status) VALUES
(500004, 100003, 200002, 'EXT_M004', '普通会员', 200, 2.00, '2026-04-30', 50.00, 3, 150.00, 1, CURRENT_TIMESTAMP, 1);

-- Test activities for merchant 1 (老王烤鱼)
INSERT INTO tb_activity (activity_id, merchant_id, activity_type, activity_name, activity_desc, status, config, stock, sold_count, target_member_type, is_public, sync_source, sort_order, start_time, end_time) VALUES
(600001, 200001, 3, '夏日烤鱼8折券', '原价100元代金券，仅售80元', 1, '{"voucher_value":100,"selling_price":80,"min_consume":200,"valid_days":30}', 100, 15, 0, 1, 'manual', 1, '2026-02-01 00:00:00', '2026-03-31 23:59:59'),
(600002, 200001, 1, '2500积分换咖啡', '消费积分兑换精品咖啡一杯', 1, '{"points_required":2500,"product_name":"精品美式咖啡"}', -1, 8, 1, 0, 'manual', 2, '2026-02-01 00:00:00', '2026-12-31 23:59:59'),
(600003, 200001, 3, '沉睡会员专属5折券', '老会员回归专属优惠', 1, '{"voucher_value":50,"selling_price":25,"target_dormancy":[2,3],"valid_days":14}', 50, 3, 2, 0, 'manual', 3, '2026-02-01 00:00:00', '2026-06-30 23:59:59'),
(600004, 200001, 2, '充500送80', '储值优惠活动，充值即返现', 1, '{"recharge_amount":500,"gift_amount":80}', -1, 12, 0, 1, 'manual', 4, '2026-02-01 00:00:00', '2026-03-31 23:59:59');

-- Test activities for merchant 2 (小李奶茶) - including group buy activity
INSERT INTO tb_activity (activity_id, merchant_id, activity_type, activity_name, activity_desc, status, config, stock, sold_count, target_member_type, is_public, sync_source, sort_order, start_time, end_time) VALUES
(600005, 200002, 3, '奶茶买一送一', '任意奶茶买一杯送一杯', 1, '{"voucher_value":20,"selling_price":15,"min_consume":0,"valid_days":7}', 200, 88, 0, 1, 'api', 1, '2026-02-20 00:00:00', '2026-02-28 23:59:59'),
(600006, 200002, 4, '团购套餐', '双人下午茶套餐原价88元团购价58元', 1, '{"original_price":88,"group_price":58,"source":"manual","valid_days":30}', 50, 22, 0, 1, 'manual', 2, '2026-02-01 00:00:00', '2026-03-31 23:59:59'),
(600007, 200002, 3, '老顾客回归礼', '60天未消费会员专属半价券', 1, '{"voucher_value":30,"selling_price":15,"target_dormancy":[2,3],"valid_days":14}', 100, 5, 2, 0, 'manual', 3, '2026-02-01 00:00:00', '2026-06-30 23:59:59'),
(600008, 200002, 4, '三人拼团奶茶券', '三人成团，每人仅需10元', 1, '{"original_price":25,"group_price":10,"source":"manual","valid_days":7}', 100, 30, 0, 1, 'manual', 4, '2026-02-01 00:00:00', '2026-03-31 23:59:59');

-- Group buy config for activity 600008
INSERT INTO tb_group_buy_config (config_id, activity_id, min_members, max_members, expire_hours, auto_refund, allow_self_buy) VALUES
(1, 600008, 3, 5, 24, 1, 0);

-- Test group orders
INSERT INTO tb_group_order (group_order_id, group_no, activity_id, initiator_user_id, required_members, current_members, status, expire_time, complete_time, created_at) VALUES
(1, 'GP20260225001', 600008, 100001, 3, 3, 1, '2026-02-26 12:00:00', '2026-02-25 10:30:00', '2026-02-25 08:00:00'),
(2, 'GP20260225002', 600008, 100002, 3, 2, 0, '2026-02-26 18:00:00', NULL, '2026-02-25 09:00:00'),
(3, 'GP20260225003', 600008, 100003, 3, 1, 2, '2026-02-24 12:00:00', NULL, '2026-02-24 08:00:00');

-- Test group participants
INSERT INTO tb_group_participant (participant_id, group_order_id, user_id, order_id, is_initiator, join_time) VALUES
(1, 1, 100001, 800002, 1, '2026-02-25 08:00:00'),
(2, 1, 100002, 800003, 0, '2026-02-25 09:00:00'),
(3, 1, 100003, 800004, 0, '2026-02-25 10:00:00'),
(4, 2, 100002, 800005, 1, '2026-02-25 09:00:00'),
(5, 2, 100001, 800006, 0, '2026-02-25 10:00:00'),
(6, 3, 100003, 800007, 1, '2026-02-24 08:00:00');

-- Test voucher expiring in 5 days
INSERT INTO tb_voucher (voucher_id, voucher_code, order_id, user_id, merchant_id, activity_id, voucher_type, voucher_value, status, valid_start_time, valid_end_time) VALUES
(700001, 'V20260222001', 800001, 100001, 200001, 600001, 1, 100.00, 0, CURRENT_TIMESTAMP, '2026-02-27 23:59:59');

-- Additional orders for voucher testing
INSERT INTO tb_order (order_id, order_no, user_id, merchant_id, activity_id, order_type, order_amount, pay_amount, status, pay_time, transaction_id, is_dormancy_awake) VALUES
(800008, '20260223100001000008', 100001, 200002, 600005, 3, 15.00, 15.00, 1, '2026-02-23 11:00:00', 'WX_TXN_008', 0),
(800009, '20260224100002000009', 100002, 200002, 600007, 3, 15.00, 15.00, 1, '2026-02-24 12:00:00', 'WX_TXN_009', 1),
(800010, '20260220100001000010', 100001, 200001, 600003, 3, 25.00, 25.00, 1, '2026-02-20 15:00:00', 'WX_TXN_010', 1);

-- Additional vouchers for equity testing
INSERT INTO tb_voucher (voucher_id, voucher_code, order_id, user_id, merchant_id, activity_id, voucher_type, voucher_value, status, valid_start_time, valid_end_time) VALUES
(700002, 'V20260223001', 800008, 100001, 200002, 600005, 1, 20.00, 0, CURRENT_TIMESTAMP, '2026-02-28 23:59:59'),
(700003, 'V20260224001', 800009, 100002, 200002, 600007, 1, 30.00, 0, CURRENT_TIMESTAMP, '2026-03-05 23:59:59'),
(700004, 'V20260220001', 800010, 100001, 200001, 600003, 4, 50.00, 0, CURRENT_TIMESTAMP, '2026-03-10 23:59:59');

-- Test orders
INSERT INTO tb_order (order_id, order_no, user_id, merchant_id, activity_id, order_type, order_amount, pay_amount, status, pay_time, transaction_id, is_dormancy_awake) VALUES
(800001, '20260222100001000001', 100001, 200001, 600001, 3, 80.00, 80.00, 1, '2026-02-22 10:00:00', 'WX_TXN_001', 0),
(800002, '20260225100001000002', 100001, 200002, 600008, 4, 10.00, 10.00, 1, '2026-02-25 08:05:00', 'WX_TXN_002', 0),
(800003, '20260225100002000003', 100002, 200002, 600008, 4, 10.00, 10.00, 1, '2026-02-25 09:05:00', 'WX_TXN_003', 0),
(800004, '20260225100003000004', 100003, 200002, 600008, 4, 10.00, 10.00, 1, '2026-02-25 10:05:00', 'WX_TXN_004', 0),
(800005, '20260225100002000005', 100002, 200002, 600008, 4, 10.00, 10.00, 1, '2026-02-25 09:10:00', 'WX_TXN_005', 0),
(800006, '20260225100001000006', 100001, 200002, 600008, 4, 10.00, 10.00, 1, '2026-02-25 10:15:00', 'WX_TXN_006', 0),
(800007, '20260224100003000007', 100003, 200002, 600008, 4, 10.00, 10.00, 0, NULL, NULL, 0);

-- Test profit sharing record (pending verification)
INSERT INTO tb_profit_sharing (sharing_id, order_no, voucher_id, merchant_id, transaction_id, total_amount, service_fee_rate, service_fee, merchant_amount, status, retry_count) VALUES
(850001, '20260222100001000001', 700001, 200001, 'WX_TXN_001', 80.00, 0.0200, 1.60, 78.40, 0, 0);

-- Test notification setting
INSERT INTO tb_user_notification_setting (setting_id, user_id, points_expire_notify, balance_notify, voucher_expire_notify, activity_notify) VALUES
(900001, 100001, 1, 1, 1, 1),
(900002, 100002, 1, 0, 1, 1);

-- Test equity reminders (various types and statuses)
INSERT INTO tb_equity_reminder (reminder_id, user_id, merchant_id, reminder_type, equity_type, equity_value, expire_date, remind_status) VALUES
(1000001, 100001, 200001, 1, 'points', 25.00, '2026-02-25', 0),
(1000002, 100001, 200001, 3, 'voucher', 100.00, '2026-02-27', 0),
(1000003, 100001, 200002, 3, 'voucher', 20.00, '2026-02-28', 0),
(1000004, 100002, 200001, 5, 'dormancy', 0.00, NULL, 1);

-- User equity summary (pre-computed cache)
INSERT INTO tb_user_equity_summary (summary_id, user_id, total_points_value, total_balance, total_voucher_value, expiring_points_value, expiring_voucher_count, merchant_count, last_updated) VALUES
(1, 100001, 30.00, 388.50, 170.00, 25.00, 2, 2, CURRENT_TIMESTAMP),
(2, 100002, 8.00, 0.00, 30.00, 0.00, 0, 1, CURRENT_TIMESTAMP),
(3, 100003, 2.00, 50.00, 0.00, 0.00, 0, 1, CURRENT_TIMESTAMP);

-- Consume records (historical)
INSERT INTO tb_merchant_consume_record (record_id, user_id, merchant_id, branch_id, consume_time, consume_amount, invoice_status, source_order_no, sync_time) VALUES
(600001, 100001, 200001, 300001, '2026-02-20 12:30:00', 128.00, 1, 'EXT_ORDER_001', CURRENT_TIMESTAMP),
(600002, 100001, 200001, 300001, '2026-02-15 18:00:00', 98.00, 0, 'EXT_ORDER_002', CURRENT_TIMESTAMP),
(600003, 100001, 200002, 300002, '2026-02-10 11:30:00', 35.00, 0, 'EXT_ORDER_003', CURRENT_TIMESTAMP),
(600004, 100002, 200001, 300001, '2025-12-01 19:00:00', 256.00, 1, 'EXT_ORDER_004', CURRENT_TIMESTAMP),
(600005, 100003, 200002, 300002, '2026-01-15 14:00:00', 42.00, 0, 'EXT_ORDER_005', CURRENT_TIMESTAMP);

-- Verification records (historical)
INSERT INTO tb_verification_record (record_id, voucher_id, voucher_code, user_id, merchant_id, branch_id, employee_id, verify_time, is_dormancy_awake, sync_status) VALUES
(1300001, 700001, 'V20260222001', 100001, 200001, 300001, 400001, '2026-02-23 14:30:00', 0, 1);

-- Service fee records
INSERT INTO tb_service_fee_record (record_id, merchant_id, order_no, voucher_id, verify_record_id, order_amount, service_fee_rate, service_fee, merchant_amount, sharing_status) VALUES
(1400001, 200001, '20260222100001000001', 700001, 1300001, 80.00, 0.0200, 1.60, 78.40, 2);

-- Merchant balance accounts
INSERT INTO tb_merchant_balance (balance_id, merchant_id, balance, total_recharge, total_consume) VALUES
(1, 200001, 1000.00, 1000.00, 1.60),
(2, 200002, 500.00, 500.00, 0.00);

-- Balance logs
INSERT INTO tb_merchant_balance_log (log_id, merchant_id, change_type, amount, balance_before, balance_after, related_order_no, remark) VALUES
(1500001, 200001, 1, 1000.00, 0.00, 1000.00, NULL, '初始充值'),
(1500002, 200001, 2, 1.60, 1000.00, 998.40, '20260222100001000001', '服务费扣除-核销V20260222001'),
(1500003, 200002, 1, 500.00, 0.00, 500.00, NULL, '初始充值');

-- Monthly service fee summary
INSERT INTO tb_monthly_service_fee (summary_id, merchant_id, year_month, order_count, total_amount, service_fee, deduct_status, deduct_time) VALUES
(1, 200001, '2026-02', 1, 80.00, 1.60, 1, '2026-02-23 15:00:00');

-- NOTE: tb_admin_user default admin account is created by DataInitializer with BCrypt password

-- Test merchant audit log
INSERT INTO tb_merchant_audit_log (log_id, merchant_id, admin_id, action, prev_status, new_status, reason, created_at) VALUES
(1100001, 200001, 1, 'APPROVE', 0, 1, '资料齐全，审核通过', '2026-02-20 09:00:00'),
(1100002, 200002, 1, 'APPROVE', 0, 1, '品牌资质确认，审核通过', '2026-02-21 10:30:00');

-- Test admin operation log
INSERT INTO tb_admin_operation_log (log_id, admin_id, module, action, target_id, detail, ip, created_at) VALUES
(1200001, 1, 'merchant', 'AUDIT_APPROVE', '200001', '审核通过商户：老王烤鱼', '127.0.0.1', '2026-02-20 09:00:00'),
(1200002, 1, 'merchant', 'AUDIT_APPROVE', '200002', '审核通过商户：小李奶茶', '127.0.0.1', '2026-02-21 10:30:00');

-- Platform member data
INSERT INTO tb_platform_member (member_id, user_id, member_level, total_consume_amount, total_consume_count, merchant_count) VALUES
(1, 100001, 1, 3280.00, 15, 2),
(2, 100002, 0, 68.00, 2, 1),
(3, 100003, 0, 30.00, 1, 1);

-- AI model config
INSERT INTO tb_ai_model_config (config_id, provider_code, provider_name, model_name, api_endpoint, is_default, status, input_price, output_price, max_tokens, temperature) VALUES
(1, 'openai', 'OpenAI', 'gpt-4o-mini', 'https://api.openai.com/v1/chat/completions', 1, 1, 0.000150, 0.000600, 4096, 0.70);
