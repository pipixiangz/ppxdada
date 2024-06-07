package com.ppx.ppxdada.controller;

import com.ppx.ppxdada.model.dto.question.AiGenerateQuestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuestionControllerTest {

    @Resource
    private QuestionController questionController;

    @Test
    void aiGenerateQuestionSSETest() throws InterruptedException {
        AiGenerateQuestionRequest request = new AiGenerateQuestionRequest();
        request.setAppId(3L);
        request.setQuestionNumber(10);
        request.setOptionNumber(2);

        questionController.aiGenerateQuestionSSETest(request, false);
        questionController.aiGenerateQuestionSSETest(request, false);
        questionController.aiGenerateQuestionSSETest(request, true);

        // 模拟主线程一直启动
        Thread.sleep(1000000L);
    }
}