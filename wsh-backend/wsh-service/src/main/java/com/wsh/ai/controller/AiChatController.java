package com.wsh.ai.controller;

import com.wsh.admin.dto.PageQueryRequest;
import com.wsh.common.core.result.PageResult;
import com.wsh.common.core.result.R;
import com.wsh.common.security.util.SecurityUtil;
import com.wsh.ai.dto.*;
import com.wsh.ai.service.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 创建新对话
     */
    @PostMapping("/conversations")
    public R<ConversationResponse> createConversation() {
        Long userId = SecurityUtil.getUserId();
        return R.ok(aiChatService.createConversation(userId));
    }

    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public R<PageResult<ConversationResponse>> listConversations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getUserId();
        PageQueryRequest pageQuery = new PageQueryRequest();
        pageQuery.setPage(page);
        pageQuery.setSize(size);
        return R.ok(aiChatService.listConversations(userId, pageQuery));
    }

    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{id}")
    public R<ConversationDetailResponse> getConversationDetail(@PathVariable("id") Long conversationId) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(aiChatService.getConversationDetail(userId, conversationId));
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{id}")
    public R<Void> deleteConversation(@PathVariable("id") Long conversationId) {
        Long userId = SecurityUtil.getUserId();
        aiChatService.deleteConversation(userId, conversationId);
        return R.ok();
    }

    /**
     * 发送消息
     */
    @PostMapping("/conversations/{id}/messages")
    public R<ChatMessageResponse> sendMessage(
            @PathVariable("id") Long conversationId,
            @Valid @RequestBody ChatMessageRequest request) {
        Long userId = SecurityUtil.getUserId();
        return R.ok(aiChatService.sendMessage(userId, conversationId, request));
    }
}
