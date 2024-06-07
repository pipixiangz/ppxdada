package com.ppx.ppxdada.model.dto.statistics;

import lombok.Data;

@Data
/**
 * App用户提交答案数
 */
public class AppAnswerCountDTO {

    private Long appId;
    /**
     * 用户提交答案数
     */
    private Long answerCount;
}
