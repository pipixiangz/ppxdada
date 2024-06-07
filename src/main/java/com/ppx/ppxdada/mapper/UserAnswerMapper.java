package com.ppx.ppxdada.mapper;

import com.ppx.ppxdada.model.dto.statistics.AppAnswerCountDTO;
import com.ppx.ppxdada.model.dto.statistics.AppAnswerResultCountDTO;
import com.ppx.ppxdada.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author ppx
 * @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
 * @createDate 2024-05-25 21:54:32
 * @Entity com.ppx.ppxdada.model.entity.UserAnswer
 */
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {
    /**
     * 统计每个应用的答题数
     * @return
     */
    @Select("select appId, count(userId) as answerCount from user_answer " +
            "group by appId order by answerCount desc")
    List<AppAnswerCountDTO> doAppAnswerCount();


    /**
     * 统计每个应用的答题结果
     * @param appId
     * @return
     */
    @Select("select resultName, count(resultName) as resultCount from user_answer " +
            "where appId = #{appId} group by resultName order by resultCount desc")
    List<AppAnswerResultCountDTO> doAppAnswerResultCount(Long appId);
}




