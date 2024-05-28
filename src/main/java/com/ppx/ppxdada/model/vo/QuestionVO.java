package com.ppx.ppxdada.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.ppx.ppxdada.model.dto.question.QuestionContentDTO;
import com.ppx.ppxdada.model.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目视图
 *
 *
 *
 */
@Data
public class QuestionVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 题目内容（json格式）
     */
    private List<QuestionContentDTO> questionContent;

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param questionVO
     * @return
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        // 从 questionVO 对象中获取 QuestionContent 属性，返回 QuestionContentDTO 对象
        List<QuestionContentDTO> questionDTO = questionVO.getQuestionContent();
        // 将 QuestionContentDTO 对象转换为 JSON 字符串
        String jsonString = JSONUtil.toJsonStr(questionDTO);
        // 将转换后的 JSON 字符串设置给 question 对象的 QuestionContent 属性
        question.setQuestionContent(jsonString);

        return question;
    }

    /**
     * 对象转封装类
     *
     * @param question
     * @return
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        // 从 question 对象中获取 QuestionContent 属性，返回 JSON 字符串
        String questionContent = question.getQuestionContent();
        if(questionContent!=null){
            // 将 JSON 字符串转换为 QuestionContentDTO 对象
            List<QuestionContentDTO> questionDTO = JSONUtil.toList(questionContent, QuestionContentDTO.class);
            // 将转换后的 QuestionContentDTO 对象设置给 questionVO 对象的 QuestionContent 属性
            questionVO.setQuestionContent(questionDTO);
        }

        return questionVO;
    }
}
