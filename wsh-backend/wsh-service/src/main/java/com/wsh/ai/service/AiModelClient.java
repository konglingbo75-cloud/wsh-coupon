package com.wsh.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wsh.domain.entity.AiMessage;
import com.wsh.domain.entity.AiModelConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * AI模型调用客户端
 * 支持OpenAI、Claude、通义千问、DeepSeek等兼容OpenAI API格式的模型
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiModelClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class ChatResult {
        private String content;
        private int inputTokens;
        private int outputTokens;
    }

    /**
     * 调用AI模型进行对话
     */
    public ChatResult chat(AiModelConfig model, String systemPrompt, List<AiMessage> history, String userMessage) {
        try {
            String endpoint = getEndpoint(model);
            HttpHeaders headers = buildHeaders(model);
            String requestBody = buildRequestBody(model, systemPrompt, history, userMessage);
            
            log.debug("调用AI模型[{}], endpoint={}", model.getModelName(), endpoint);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);
            
            return parseResponse(model.getProviderCode(), response.getBody());
        } catch (Exception e) {
            log.error("AI模型调用失败: {}", e.getMessage(), e);
            
            // 返回错误信息作为回复
            ChatResult result = new ChatResult();
            result.setContent("抱歉，AI服务暂时不可用，请稍后再试。错误信息：" + e.getMessage());
            result.setInputTokens(0);
            result.setOutputTokens(0);
            return result;
        }
    }

    private String getEndpoint(AiModelConfig model) {
        if (model.getApiEndpoint() != null && !model.getApiEndpoint().isBlank()) {
            return model.getApiEndpoint();
        }
        
        // 默认端点
        return switch (model.getProviderCode()) {
            case "openai" -> "https://api.openai.com/v1/chat/completions";
            case "claude" -> "https://api.anthropic.com/v1/messages";
            case "qwen" -> "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            case "deepseek" -> "https://api.deepseek.com/v1/chat/completions";
            default -> "https://api.openai.com/v1/chat/completions";
        };
    }

    private HttpHeaders buildHeaders(AiModelConfig model) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String apiKey = model.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("API Key未配置");
        }
        
        if ("claude".equals(model.getProviderCode())) {
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");
        } else {
            headers.set("Authorization", "Bearer " + apiKey);
        }
        
        return headers;
    }

    private String buildRequestBody(AiModelConfig model, String systemPrompt, List<AiMessage> history, String userMessage) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        
        if ("claude".equals(model.getProviderCode())) {
            // Anthropic Claude 格式
            root.put("model", model.getModelName());
            root.put("max_tokens", model.getMaxTokens() != null ? model.getMaxTokens() : 4096);
            root.put("system", systemPrompt);
            
            ArrayNode messages = root.putArray("messages");
            for (AiMessage msg : history) {
                if (!"system".equals(msg.getRole())) {
                    ObjectNode msgNode = messages.addObject();
                    msgNode.put("role", msg.getRole());
                    msgNode.put("content", msg.getContent());
                }
            }
            ObjectNode userNode = messages.addObject();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
        } else {
            // OpenAI 兼容格式 (OpenAI, 通义千问, DeepSeek等)
            root.put("model", model.getModelName());
            root.put("max_tokens", model.getMaxTokens() != null ? model.getMaxTokens() : 4096);
            if (model.getTemperature() != null) {
                root.put("temperature", model.getTemperature().doubleValue());
            }
            
            ArrayNode messages = root.putArray("messages");
            
            // 系统提示词
            ObjectNode systemNode = messages.addObject();
            systemNode.put("role", "system");
            systemNode.put("content", systemPrompt);
            
            // 历史消息
            for (AiMessage msg : history) {
                if (!"system".equals(msg.getRole())) {
                    ObjectNode msgNode = messages.addObject();
                    msgNode.put("role", msg.getRole());
                    msgNode.put("content", msg.getContent());
                }
            }
            
            // 当前用户消息
            ObjectNode userNode = messages.addObject();
            userNode.put("role", "user");
            userNode.put("content", userMessage);
        }
        
        return objectMapper.writeValueAsString(root);
    }

    private ChatResult parseResponse(String providerCode, String responseBody) throws Exception {
        ChatResult result = new ChatResult();
        JsonNode root = objectMapper.readTree(responseBody);
        
        if ("claude".equals(providerCode)) {
            // Claude 响应格式
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                result.setContent(content.get(0).path("text").asText(""));
            }
            JsonNode usage = root.path("usage");
            result.setInputTokens(usage.path("input_tokens").asInt(0));
            result.setOutputTokens(usage.path("output_tokens").asInt(0));
        } else {
            // OpenAI 兼容格式
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                result.setContent(choices.get(0).path("message").path("content").asText(""));
            }
            JsonNode usage = root.path("usage");
            result.setInputTokens(usage.path("prompt_tokens").asInt(0));
            result.setOutputTokens(usage.path("completion_tokens").asInt(0));
        }
        
        return result;
    }
}
