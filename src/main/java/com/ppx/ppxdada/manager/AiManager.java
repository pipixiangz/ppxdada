package com.ppx.ppxdada.manager;


import com.ppx.ppxdada.common.ErrorCode;
import com.ppx.ppxdada.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用AI调用能力
 */
@Component
public class AiManager {

    @Resource
    private ClientV4 clientV4;

    // 较稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    // 不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.99f;

    /**
     * 同步调用（答案较稳定）
     *
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncStableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, STABLE_TEMPERATURE);
    }

    /**
     * 同步调用（答案较随机）
     *
     * @param systemMessage
     * @param userMessage
     * @return
     */
    public String doSyncUnstableRequest(String systemMessage, String userMessage) {
        return doSyncRequest(systemMessage, userMessage, UNSTABLE_TEMPERATURE);
    }

    /**
     * 通用同步请求
     *
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public String doSyncRequest(String systemMessage, String userMessage, Float temperature) {
        return doRequest(systemMessage, userMessage, Boolean.FALSE, temperature);
    }


    /**
     * 通用请求, 简化消息传递
     *
     * @param systemMessage
     * @param userMessage
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(String systemMessage, String userMessage, Boolean stream, Float temperature) {
        // 构造请求
        List<ChatMessage> messages = new ArrayList<>();
        // 系统消息
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(systemChatMessage);
        // 用户消息
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);

        return doRequest(messages, stream, temperature);
    }

    /**
     * 通用请求
     *
     * @param messages
     * @param stream
     * @param temperature
     * @return
     */
    public String doRequest(List<ChatMessage> messages, Boolean stream, Float temperature) {
        // 构造请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream) // 是否是流式对话，FALSE的意思是等所有回答写完再返回，TRUE的意思是一边写一边返回
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(messages)
                .build();
        try {
            // 调用
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            // 结果
            ChatMessage result = invokeModelApiResp.getData().getChoices().get(0).getMessage();
            return result.getContent().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }


    /**
     * 通用流式请求, 简化消息传递
     *
     * @param systemMessage
     * @param userMessage
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(String systemMessage, String userMessage, Float temperature) {
        List<ChatMessage> messages = new ArrayList<>();
        // 系统消息
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        messages.add(systemChatMessage);
        // 用户消息
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);

        return doStreamRequest(messages, temperature);
    }

    /**
     * 流式请求
     *
     * @param messages
     * @param temperature
     * @return
     */
    public Flowable<ModelData> doStreamRequest(List<ChatMessage> messages, Float temperature) {
        // 构造请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE) // 是否是流式对话，TRUE的意思是一边写一边返回
                .invokeMethod(Constants.invokeMethod)
                .temperature(temperature)
                .messages(messages)
                .build();
        try {
            // 调用
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            // 返回Flowable对象
            return invokeModelApiResp.getFlowable();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }
    }
}
