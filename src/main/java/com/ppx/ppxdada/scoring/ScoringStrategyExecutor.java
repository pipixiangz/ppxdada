package com.ppx.ppxdada.scoring;

import com.ppx.ppxdada.common.ErrorCode;
import com.ppx.ppxdada.exception.BusinessException;
import com.ppx.ppxdada.model.entity.App;
import com.ppx.ppxdada.model.entity.UserAnswer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 评分策略执行器
 */
@Service
public class ScoringStrategyExecutor {

    // 策略列表
    @Resource
    private List<ScoringStrategy> scoringStrategyList;

    /**
     * 评分
     *
     * @param choiceList
     * @param app
     * @return
     * @throws Exception
     */
    public UserAnswer doScore(List<String> choiceList, App app) throws Exception {
        Integer appType = app.getAppType();
        Integer appScoringStrategy = app.getScoringStrategy();
        if (appType == null || appScoringStrategy == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
        }
        // 根据注解获取策略 （核心）
        for (ScoringStrategy strategy : scoringStrategyList) {
            // 检查当前策略类是否有 ScoringStrategyConfig 注解
            if (strategy.getClass().isAnnotationPresent(ScoringStrategyConfig.class)) {
                // 获取策略类上的 ScoringStrategyConfig 注解
                ScoringStrategyConfig scoringStrategyConfig = strategy.getClass().getAnnotation(ScoringStrategyConfig.class);
                // 判断注解中的 appType 和 scoringStrategy 是否匹配指定的 appType 和 appScoringStrategy
                if (scoringStrategyConfig.appType() == appType && scoringStrategyConfig.scoringStrategy() == appScoringStrategy) {
                    // 如果匹配，调用该策略的 doScore 方法，并返回结果
                    return strategy.doScore(choiceList, app);

                }
            }
        }

        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用配置有误，未找到匹配的策略");
    }
}
