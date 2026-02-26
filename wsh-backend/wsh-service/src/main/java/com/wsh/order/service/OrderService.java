package com.wsh.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsh.common.core.constant.Constants;
import com.wsh.common.core.exception.BusinessException;
import com.wsh.common.core.util.IdGenerator;
import com.wsh.common.redis.util.RedisUtil;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import com.wsh.integration.wechat.WechatPayService;
import com.wsh.order.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final VoucherMapper voucherMapper;
    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;
    private final ProfitSharingMapper profitSharingMapper;
    private final MerchantMemberSnapshotMapper snapshotMapper;
    private final WechatPayService wechatPayService;
    private final RedisUtil redisUtil;

    private static final String STOCK_KEY_PREFIX = "activity:stock:";

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderCreateRequest request, Long userId) {
        // 1. 查询活动信息
        Activity activity = activityMapper.selectById(request.getActivityId());
        if (activity == null) {
            throw new BusinessException("活动不存在");
        }
        
        // 2. 校验活动状态
        validateActivity(activity);
        
        // 3. 校验会员资格（专属活动）
        validateMemberQualification(activity, userId);
        
        // 4. 扣减库存（原子操作）
        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;
        if (!deductStock(activity.getActivityId(), activity.getStock(), quantity)) {
            throw new BusinessException("活动库存不足");
        }
        
        try {
            // 5. 计算金额
            BigDecimal orderAmount = calculateOrderAmount(activity, quantity);
            BigDecimal payAmount = orderAmount; // 暂不支持优惠券抵扣
            
            // 6. 创建订单
            Order order = new Order();
            order.setOrderId(IdGenerator.nextId());
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setMerchantId(activity.getMerchantId());
            order.setActivityId(activity.getActivityId());
            order.setOrderType(activity.getActivityType());
            order.setOrderAmount(orderAmount);
            order.setPayAmount(payAmount);
            order.setStatus(Constants.ORDER_STATUS_PENDING);
            order.setIsDormancyAwake(activity.getTargetMemberType() == Constants.TARGET_MEMBER_DORMANT ? 1 : 0);
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            
            orderMapper.insert(order);
            log.info("订单创建成功: orderNo={}, userId={}, activityId={}", order.getOrderNo(), userId, activity.getActivityId());
            
            // 7. 构建响应
            Merchant merchant = merchantMapper.selectById(activity.getMerchantId());
            return buildOrderResponse(order, activity, merchant, null);
            
        } catch (Exception e) {
            // 回滚库存
            rollbackStock(activity.getActivityId(), quantity);
            throw e;
        }
    }

    /**
     * 发起支付
     */
    public PaymentResponse requestPayment(Long orderId, Long userId, String openid) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING) {
            throw new BusinessException("订单状态异常，无法支付");
        }
        
        // 检查是否超时
        if (isOrderExpired(order)) {
            closeOrder(order.getOrderId());
            throw new BusinessException("订单已超时关闭");
        }
        
        // 调用微信支付
        Activity activity = activityMapper.selectById(order.getActivityId());
        String description = activity != null ? activity.getActivityName() : "微生活券吧订单";
        
        return wechatPayService.createPayment(order.getOrderNo(), order.getPayAmount(), description, openid);
    }

    /**
     * 支付成功回调处理
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(String orderNo, String transactionId, LocalDateTime payTime) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            log.error("支付回调订单不存在: orderNo={}", orderNo);
            return;
        }
        
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING) {
            log.warn("订单状态已变更，忽略回调: orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }
        
        // 1. 更新订单状态
        int updated = orderMapper.updatePaySuccess(orderNo, payTime, transactionId);
        if (updated == 0) {
            log.warn("订单状态更新失败（可能已处理）: orderNo={}", orderNo);
            return;
        }
        
        // 2. 生成券码
        Voucher voucher = generateVoucher(order);
        voucherMapper.insert(voucher);
        
        // 3. 创建分账记录（待核销时分账）
        createProfitSharingRecord(order, voucher);
        
        // 4. 更新活动已售数量
        activityMapper.incrementSoldCount(order.getActivityId());
        
        log.info("支付成功处理完成: orderNo={}, voucherCode={}", orderNo, voucher.getVoucherCode());
    }

    /**
     * 查询用户订单列表
     */
    public OrderListResponse getUserOrders(Long userId, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreatedAt);
        
        List<Order> allOrders = orderMapper.selectList(wrapper);
        int total = allOrders.size();
        
        // 分页
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        List<Order> pagedOrders = start < total ? allOrders.subList(start, end) : Collections.emptyList();
        
        // 加载关联数据
        List<OrderResponse> responses = pagedOrders.stream()
                .map(this::enrichOrderResponse)
                .collect(Collectors.toList());
        
        return OrderListResponse.builder()
                .orders(responses)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 查询订单详情
     */
    public OrderResponse getOrderDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此订单");
        }
        return enrichOrderResponse(order);
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此订单");
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING) {
            throw new BusinessException("只能取消待支付订单");
        }
        
        closeOrder(order.getOrderId());
        
        // 回滚库存
        rollbackStock(order.getActivityId(), 1);
        
        log.info("订单取消成功: orderId={}, userId={}", orderId, userId);
    }

    /**
     * 查询用户券包
     */
    public VoucherListResponse getUserVouchers(Long userId, Integer status) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getUserId, userId)
                .eq(status != null, Voucher::getStatus, status)
                .orderByDesc(Voucher::getCreatedAt);
        
        List<Voucher> vouchers = voucherMapper.selectList(wrapper);
        
        // 统计各状态数量
        int available = 0, used = 0, expired = 0;
        for (Voucher v : vouchers) {
            if (v.getStatus() == Constants.VOUCHER_STATUS_UNUSED) {
                // 检查是否已过期
                if (v.getValidEndTime() != null && v.getValidEndTime().isBefore(LocalDateTime.now())) {
                    v.setStatus(Constants.VOUCHER_STATUS_EXPIRED);
                    voucherMapper.updateById(v);
                    expired++;
                } else {
                    available++;
                }
            } else if (v.getStatus() == Constants.VOUCHER_STATUS_USED) {
                used++;
            } else if (v.getStatus() == Constants.VOUCHER_STATUS_EXPIRED) {
                expired++;
            }
        }
        
        // 转换响应
        List<VoucherListResponse.VoucherItem> items = vouchers.stream()
                .map(this::convertVoucherItem)
                .collect(Collectors.toList());
        
        return VoucherListResponse.builder()
                .vouchers(items)
                .total(vouchers.size())
                .availableCount(available)
                .usedCount(used)
                .expiredCount(expired)
                .build();
    }

    /**
     * 查询券码详情
     */
    public VoucherListResponse.VoucherItem getVoucherDetail(Long voucherId, Long userId) {
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BusinessException("券码不存在");
        }
        if (!voucher.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此券码");
        }
        return convertVoucherItem(voucher);
    }

    /**
     * 关闭超时订单
     */
    @Transactional(rollbackFor = Exception.class)
    public int closeExpiredOrders() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(Constants.ORDER_TIMEOUT_MINUTES);
        List<Order> expiredOrders = orderMapper.selectExpiredOrders(expireTime);
        
        int closedCount = 0;
        for (Order order : expiredOrders) {
            try {
                closeOrder(order.getOrderId());
                rollbackStock(order.getActivityId(), 1);
                closedCount++;
                log.info("超时订单已关闭: orderNo={}", order.getOrderNo());
            } catch (Exception e) {
                log.error("关闭超时订单失败: orderNo={}", order.getOrderNo(), e);
            }
        }
        
        return closedCount;
    }

    // ==================== 私有方法 ====================

    private void validateActivity(Activity activity) {
        if (activity.getStatus() != 1) {
            throw new BusinessException("活动未开始或已结束");
        }
        LocalDateTime now = LocalDateTime.now();
        if (activity.getStartTime() != null && now.isBefore(activity.getStartTime())) {
            throw new BusinessException("活动未开始");
        }
        if (activity.getEndTime() != null && now.isAfter(activity.getEndTime())) {
            throw new BusinessException("活动已结束");
        }
    }

    private void validateMemberQualification(Activity activity, Long userId) {
        Integer targetType = activity.getTargetMemberType();
        if (targetType == null || targetType == Constants.TARGET_MEMBER_ALL) {
            return; // 全部会员可参与
        }
        
        // 查询用户在该商户的会员状态
        MerchantMemberSnapshot snapshot = snapshotMapper.selectByUserAndMerchant(userId, activity.getMerchantId());
        if (snapshot == null) {
            throw new BusinessException("您还不是该商户会员，无法参与此专属活动");
        }
        
        int dormancyLevel = snapshot.getDormancyLevel() != null ? snapshot.getDormancyLevel() : 0;
        boolean isActive = dormancyLevel == Constants.DORMANCY_ACTIVE;
        
        if (targetType == Constants.TARGET_MEMBER_ACTIVE && !isActive) {
            throw new BusinessException("此活动仅限活跃会员参与");
        }
        if (targetType == Constants.TARGET_MEMBER_DORMANT && isActive) {
            throw new BusinessException("此活动仅限沉睡会员专属");
        }
    }

    private boolean deductStock(Long activityId, Integer totalStock, int quantity) {
        if (totalStock == null || totalStock < 0) {
            return true; // 无限库存
        }
        
        String stockKey = STOCK_KEY_PREFIX + activityId;
        
        // 确保缓存中有库存值
        if (redisUtil.get(stockKey) == null) {
            Activity activity = activityMapper.selectById(activityId);
            int sold = activity.getSoldCount() != null ? activity.getSoldCount() : 0;
            int currentStock = totalStock - sold;
            redisUtil.set(stockKey, currentStock, 3600L, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        // 原子扣减（Lua 脚本，防超卖）
        Long remaining = redisUtil.stockDecrement(stockKey, quantity);
        return remaining != null && remaining >= 0;
    }

    private void rollbackStock(Long activityId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + activityId;
        Object cached = redisUtil.get(stockKey);
        if (cached != null) {
            long currentStock = ((Number) cached).longValue();
            redisUtil.set(stockKey, currentStock + quantity, 3600L, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    private BigDecimal calculateOrderAmount(Activity activity, int quantity) {
        String config = activity.getConfig();
        if (config == null || config.isBlank()) {
            return BigDecimal.ZERO;
        }
        
        try {
            // 简单解析JSON获取价格
            BigDecimal unitPrice = parsePrice(config, activity.getActivityType());
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        } catch (Exception e) {
            log.error("解析活动配置失败: activityId={}", activity.getActivityId(), e);
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parsePrice(String config, Integer activityType) {
        // 简单的JSON解析（生产环境应使用Jackson）
        if (config.contains("selling_price")) {
            return extractDecimal(config, "selling_price");
        }
        if (config.contains("recharge_amount")) {
            return extractDecimal(config, "recharge_amount");
        }
        if (config.contains("group_price")) {
            return extractDecimal(config, "group_price");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal extractDecimal(String json, String key) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex < 0) {
            keyIndex = json.indexOf(key);
        }
        if (keyIndex < 0) return BigDecimal.ZERO;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex < 0) return BigDecimal.ZERO;
        
        StringBuilder sb = new StringBuilder();
        for (int i = colonIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                sb.append(c);
            } else if (sb.length() > 0) {
                break;
            }
        }
        
        return sb.length() > 0 ? new BigDecimal(sb.toString()) : BigDecimal.ZERO;
    }

    private String generateOrderNo() {
        // 格式: yyyyMMddHHmmss + 6位随机数
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        String random = String.format("%06d", new Random().nextInt(1000000));
        return timestamp + random;
    }

    private Voucher generateVoucher(Order order) {
        Activity activity = activityMapper.selectById(order.getActivityId());
        
        Voucher voucher = new Voucher();
        voucher.setVoucherId(IdGenerator.nextId());
        voucher.setVoucherCode(generateVoucherCode());
        voucher.setOrderId(order.getOrderId());
        voucher.setUserId(order.getUserId());
        voucher.setMerchantId(order.getMerchantId());
        voucher.setActivityId(order.getActivityId());
        voucher.setVoucherType(order.getOrderType());
        voucher.setVoucherValue(order.getPayAmount());
        voucher.setStatus(Constants.VOUCHER_STATUS_UNUSED);
        voucher.setValidStartTime(LocalDateTime.now());
        
        // 计算有效期
        int validDays = parseValidDays(activity != null ? activity.getConfig() : null);
        voucher.setValidEndTime(LocalDateTime.now().plusDays(validDays));
        voucher.setCreatedAt(LocalDateTime.now());
        
        return voucher;
    }

    private String generateVoucherCode() {
        // 格式: V + 12位随机字符
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("V");
        Random random = new Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private int parseValidDays(String config) {
        if (config == null) return 30;
        if (config.contains("valid_days")) {
            try {
                return extractDecimal(config, "valid_days").intValue();
            } catch (Exception e) {
                return 30;
            }
        }
        return 30;
    }

    private void createProfitSharingRecord(Order order, Voucher voucher) {
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        BigDecimal feeRate = merchant != null && merchant.getProfitSharingRate() != null 
                ? merchant.getProfitSharingRate() 
                : new BigDecimal("0.02");
        
        BigDecimal serviceFee = order.getPayAmount().multiply(feeRate);
        BigDecimal merchantAmount = order.getPayAmount().subtract(serviceFee);
        
        ProfitSharing sharing = new ProfitSharing();
        sharing.setSharingId(IdGenerator.nextId());
        sharing.setOrderNo(order.getOrderNo());
        sharing.setVoucherId(voucher.getVoucherId());
        sharing.setMerchantId(order.getMerchantId());
        sharing.setTransactionId(order.getTransactionId());
        sharing.setTotalAmount(order.getPayAmount());
        sharing.setServiceFeeRate(feeRate);
        sharing.setServiceFee(serviceFee);
        sharing.setMerchantAmount(merchantAmount);
        sharing.setStatus(Constants.SHARING_STATUS_PENDING);
        sharing.setRetryCount(0);
        sharing.setCreatedAt(LocalDateTime.now());
        sharing.setUpdatedAt(LocalDateTime.now());
        
        profitSharingMapper.insert(sharing);
    }

    private void closeOrder(Long orderId) {
        orderMapper.updateStatus(orderId, Constants.ORDER_STATUS_CLOSED);
    }

    private boolean isOrderExpired(Order order) {
        return order.getCreatedAt()
                .plusMinutes(Constants.ORDER_TIMEOUT_MINUTES)
                .isBefore(LocalDateTime.now());
    }

    private OrderResponse enrichOrderResponse(Order order) {
        Activity activity = activityMapper.selectById(order.getActivityId());
        Merchant merchant = merchantMapper.selectById(order.getMerchantId());
        
        Voucher voucher = null;
        if (order.getStatus() == Constants.ORDER_STATUS_PAID) {
            voucher = voucherMapper.selectByOrderId(order.getOrderId());
        }
        
        return buildOrderResponse(order, activity, merchant, voucher);
    }

    private OrderResponse buildOrderResponse(Order order, Activity activity, Merchant merchant, Voucher voucher) {
        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .merchantId(order.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .merchantLogo(merchant != null ? merchant.getLogoUrl() : null)
                .activityId(order.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .activityCover(activity != null ? activity.getCoverImage() : null)
                .orderType(order.getOrderType())
                .orderTypeName(getOrderTypeName(order.getOrderType()))
                .orderAmount(order.getOrderAmount())
                .payAmount(order.getPayAmount())
                .status(order.getStatus())
                .statusName(getOrderStatusName(order.getStatus()))
                .payTime(order.getPayTime())
                .transactionId(order.getTransactionId())
                .isDormancyAwake(order.getIsDormancyAwake() != null && order.getIsDormancyAwake() == 1)
                .createdAt(order.getCreatedAt());
        
        if (voucher != null) {
            builder.voucherInfo(OrderResponse.VoucherInfo.builder()
                    .voucherId(voucher.getVoucherId())
                    .voucherCode(voucher.getVoucherCode())
                    .voucherType(voucher.getVoucherType())
                    .voucherValue(voucher.getVoucherValue())
                    .status(voucher.getStatus())
                    .validStartTime(voucher.getValidStartTime())
                    .validEndTime(voucher.getValidEndTime())
                    .build());
        }
        
        return builder.build();
    }

    private VoucherListResponse.VoucherItem convertVoucherItem(Voucher voucher) {
        Activity activity = activityMapper.selectById(voucher.getActivityId());
        Merchant merchant = merchantMapper.selectById(voucher.getMerchantId());
        
        LocalDateTime now = LocalDateTime.now();
        boolean expiringSoon = voucher.getValidEndTime() != null 
                && voucher.getValidEndTime().isAfter(now)
                && voucher.getValidEndTime().isBefore(now.plusDays(7));
        
        int remainingDays = 0;
        if (voucher.getValidEndTime() != null && voucher.getValidEndTime().isAfter(now)) {
            remainingDays = (int) ChronoUnit.DAYS.between(now, voucher.getValidEndTime());
        }
        
        return VoucherListResponse.VoucherItem.builder()
                .voucherId(voucher.getVoucherId())
                .voucherCode(voucher.getVoucherCode())
                .orderId(voucher.getOrderId())
                .merchantId(voucher.getMerchantId())
                .merchantName(merchant != null ? merchant.getMerchantName() : null)
                .merchantLogo(merchant != null ? merchant.getLogoUrl() : null)
                .activityId(voucher.getActivityId())
                .activityName(activity != null ? activity.getActivityName() : null)
                .voucherType(voucher.getVoucherType())
                .voucherTypeName(getVoucherTypeName(voucher.getVoucherType()))
                .voucherValue(voucher.getVoucherValue())
                .status(voucher.getStatus())
                .statusName(getVoucherStatusName(voucher.getStatus()))
                .validStartTime(voucher.getValidStartTime())
                .validEndTime(voucher.getValidEndTime())
                .usedTime(voucher.getUsedTime())
                .createdAt(voucher.getCreatedAt())
                .expiringSoon(expiringSoon)
                .remainingDays(remainingDays)
                .build();
    }

    private String getOrderTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case Constants.ACTIVITY_TYPE_VOUCHER -> "代金券";
            case Constants.ACTIVITY_TYPE_DEPOSIT -> "储值充值";
            case Constants.ACTIVITY_TYPE_POINTS -> "积分兑换";
            case Constants.ACTIVITY_TYPE_GROUP -> "团购";
            default -> "未知";
        };
    }

    private String getOrderStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case Constants.ORDER_STATUS_PENDING -> "待支付";
            case Constants.ORDER_STATUS_PAID -> "已支付";
            case Constants.ORDER_STATUS_CLOSED -> "已关闭";
            case Constants.ORDER_STATUS_REFUNDED -> "已退款";
            default -> "未知";
        };
    }

    private String getVoucherTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "代金券";
            case 2 -> "兑换券";
            case 3 -> "储值码";
            case 4 -> "沉睡唤醒券";
            default -> "未知";
        };
    }

    private String getVoucherStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case Constants.VOUCHER_STATUS_UNUSED -> "未使用";
            case Constants.VOUCHER_STATUS_USED -> "已使用";
            case Constants.VOUCHER_STATUS_EXPIRED -> "已过期";
            case 3 -> "已退款";
            default -> "未知";
        };
    }
}
