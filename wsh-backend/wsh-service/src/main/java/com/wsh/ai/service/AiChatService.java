package com.wsh.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.domain.entity.*;
import com.wsh.domain.mapper.*;
import com.wsh.ai.dto.*;
import com.wsh.equity.service.EquitySummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;
    private final AiModelConfigMapper modelConfigMapper;
    private final AiUsageDailyMapper usageDailyMapper;
    private final EquitySummaryService equitySummaryService;
    private final AiModelClient aiModelClient;

    /**
     * 创建新对话
     */
    @Transactional
    public ConversationResponse createConversation(Long userId) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        conversation.setMessageCount(0);
        conversation.setTotalTokens(0);
        conversation.setTotalCost(BigDecimal.ZERO);
        conversation.setStatus(1);
        
        // 获取默认模型
        AiModelConfig defaultModel = modelConfigMapper.selectOne(
                new LambdaQueryWrapper<AiModelConfig>()
                        .eq(AiModelConfig::getIsDefault, 1)
                        .eq(AiModelConfig::getStatus, 1)
        );
        if (defaultModel != null) {
            conversation.setModelConfigId(defaultModel.getConfigId());
        }
        
        conversationMapper.insert(conversation);
        log.info("用户[{}]创建新对话, conversationId={}", userId, conversation.getConversationId());
        
        return toConversationResponse(conversation);
    }

    /**
     * 获取用户对话列表
     */
    public PageResult<ConversationResponse> listConversations(Long userId, PageQueryRequest pageQuery) {
        Page<AiConversation> page = new Page<>(pageQuery.getPage(), pageQuery.getSize());
        
        IPage<AiConversation> result = conversationMapper.selectPage(page,
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getStatus, 1)
                        .orderByDesc(AiConversation::getUpdatedAt)
        );
        
        List<ConversationResponse> list = result.getRecords().stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(list, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 获取对话详情（含消息）
     */
    public ConversationDetailResponse getConversationDetail(Long userId, Long conversationId) {
        AiConversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getConversationId, conversationId)
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getStatus, 1)
        );
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        
        List<AiMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreatedAt)
        );
        
        ConversationDetailResponse resp = new ConversationDetailResponse();
        BeanUtils.copyProperties(conversation, resp);
        resp.setMessages(messages.stream().map(m -> {
            ConversationDetailResponse.MessageItem item = new ConversationDetailResponse.MessageItem();
            item.setMessageId(m.getMessageId());
            item.setRole(m.getRole());
            item.setContent(m.getContent());
            item.setInputTokens(m.getInputTokens());
            item.setOutputTokens(m.getOutputTokens());
            item.setCreatedAt(m.getCreatedAt());
            return item;
        }).collect(Collectors.toList()));
        
        return resp;
    }

    /**
     * 删除对话
     */
    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        AiConversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getConversationId, conversationId)
                        .eq(AiConversation::getUserId, userId)
        );
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        
        conversation.setStatus(0);
        conversationMapper.updateById(conversation);
        log.info("用户[{}]删除对话, conversationId={}", userId, conversationId);
    }

    /**
     * 发送消息并获取AI回复
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long conversationId, ChatMessageRequest request) {
        // 验证对话存在
        AiConversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getConversationId, conversationId)
                        .eq(AiConversation::getUserId, userId)
                        .eq(AiConversation::getStatus, 1)
        );
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        
        // 获取模型配置
        AiModelConfig model = modelConfigMapper.selectById(conversation.getModelConfigId());
        if (model == null || model.getStatus() != 1) {
            model = modelConfigMapper.selectOne(
                    new LambdaQueryWrapper<AiModelConfig>()
                            .eq(AiModelConfig::getIsDefault, 1)
                            .eq(AiModelConfig::getStatus, 1)
            );
        }
        if (model == null) {
            throw new RuntimeException("没有可用的AI模型");
        }
        
        // 保存用户消息
        AiMessage userMessage = new AiMessage();
        userMessage.setConversationId(conversationId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(request.getContent());
        userMessage.setInputTokens(0);
        userMessage.setOutputTokens(0);
        userMessage.setCost(BigDecimal.ZERO);
        messageMapper.insert(userMessage);
        
        // 获取历史消息构建上下文
        List<AiMessage> historyMessages = messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreatedAt)
                        .last("LIMIT 20")
        );
        
        // 构建系统提示词
        String systemPrompt = buildSystemPrompt(userId);
        
        // 调用AI模型
        AiModelClient.ChatResult result = aiModelClient.chat(model, systemPrompt, historyMessages, request.getContent());
        
        // 计算费用
        BigDecimal inputCost = model.getInputPrice() != null 
                ? model.getInputPrice().multiply(BigDecimal.valueOf(result.getInputTokens())).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal outputCost = model.getOutputPrice() != null
                ? model.getOutputPrice().multiply(BigDecimal.valueOf(result.getOutputTokens())).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalCost = inputCost.add(outputCost);
        
        // 保存AI回复
        AiMessage assistantMessage = new AiMessage();
        assistantMessage.setConversationId(conversationId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(result.getContent());
        assistantMessage.setInputTokens(result.getInputTokens());
        assistantMessage.setOutputTokens(result.getOutputTokens());
        assistantMessage.setCost(totalCost);
        messageMapper.insert(assistantMessage);
        
        // 更新对话统计
        conversation.setMessageCount(conversation.getMessageCount() + 2);
        conversation.setTotalTokens(conversation.getTotalTokens() + result.getInputTokens() + result.getOutputTokens());
        conversation.setTotalCost(conversation.getTotalCost().add(totalCost));
        
        // 更新对话标题（首条消息）
        if (conversation.getMessageCount() == 2) {
            String title = request.getContent();
            if (title.length() > 30) {
                title = title.substring(0, 30) + "...";
            }
            conversation.setTitle(title);
        }
        conversationMapper.updateById(conversation);
        
        // 更新日统计
        updateDailyUsage(model.getConfigId(), result.getInputTokens(), result.getOutputTokens(), totalCost);
        
        log.info("用户[{}]发送消息, conversationId={}, inputTokens={}, outputTokens={}, cost={}",
                userId, conversationId, result.getInputTokens(), result.getOutputTokens(), totalCost);
        
        ChatMessageResponse resp = new ChatMessageResponse();
        resp.setMessageId(assistantMessage.getMessageId());
        resp.setConversationId(conversationId);
        resp.setRole("assistant");
        resp.setContent(result.getContent());
        resp.setInputTokens(result.getInputTokens());
        resp.setOutputTokens(result.getOutputTokens());
        resp.setCost(totalCost);
        resp.setCreatedAt(assistantMessage.getCreatedAt());
        
        return resp;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(Long userId) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是微生活券吧的智能助手，帮助用户管理会员权益、发现优惠活动。\n\n");
        sb.append("你可以帮助用户：\n");
        sb.append("1. 查询权益余额和过期时间\n");
        sb.append("2. 推荐附近优惠活动\n");
        sb.append("3. 查询订单状态\n");
        sb.append("4. 解答平台使用问题\n\n");
        
        // 获取用户权益数据
        try {
            var summary = equitySummaryService.getSummary(userId);
            sb.append("当前用户权益概况：\n");
            sb.append("- 总积分价值：").append(summary.getTotalPointsValue()).append("元\n");
            sb.append("- 总储值余额：").append(summary.getTotalBalance()).append("元\n");
            sb.append("- 总券价值：").append(summary.getTotalVoucherValue()).append("元\n");
            sb.append("- 即将过期积分价值：").append(summary.getExpiringPointsValue()).append("元\n");
            sb.append("- 即将过期券数量：").append(summary.getExpiringVoucherCount()).append("张\n");
            sb.append("- 有权益的商户数：").append(summary.getMerchantCount()).append("家\n\n");
        } catch (Exception e) {
            log.warn("获取用户权益数据失败: {}", e.getMessage());
        }
        
        sb.append("请用友好、专业的语气回答用户问题，回答要简洁明了。");
        sb.append("如涉及具体金额或时间，请提醒用户以实际页面显示为准。");
        
        return sb.toString();
    }

    /**
     * 更新日统计
     */
    private void updateDailyUsage(Long modelConfigId, int inputTokens, int outputTokens, BigDecimal cost) {
        LocalDate today = LocalDate.now();
        
        AiUsageDaily daily = usageDailyMapper.selectOne(
                new LambdaQueryWrapper<AiUsageDaily>()
                        .eq(AiUsageDaily::getStatDate, today)
                        .eq(AiUsageDaily::getModelConfigId, modelConfigId)
        );
        
        if (daily == null) {
            daily = new AiUsageDaily();
            daily.setStatDate(today);
            daily.setModelConfigId(modelConfigId);
            daily.setCallCount(1);
            daily.setTotalInputTokens(inputTokens);
            daily.setTotalOutputTokens(outputTokens);
            daily.setTotalCost(cost);
            usageDailyMapper.insert(daily);
        } else {
            daily.setCallCount(daily.getCallCount() + 1);
            daily.setTotalInputTokens(daily.getTotalInputTokens() + inputTokens);
            daily.setTotalOutputTokens(daily.getTotalOutputTokens() + outputTokens);
            daily.setTotalCost(daily.getTotalCost().add(cost));
            usageDailyMapper.updateById(daily);
        }
    }

    private ConversationResponse toConversationResponse(AiConversation conversation) {
        ConversationResponse resp = new ConversationResponse();
        BeanUtils.copyProperties(conversation, resp);
        return resp;
    }
}
