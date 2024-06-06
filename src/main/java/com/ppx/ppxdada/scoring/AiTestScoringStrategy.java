package com.ppx.ppxdada.scoring;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ppx.ppxdada.manager.AiManager;
import com.ppx.ppxdada.model.dto.question.QuestionAnswerDTO;
import com.ppx.ppxdada.model.dto.question.QuestionContentDTO;
import com.ppx.ppxdada.model.entity.App;
import com.ppx.ppxdada.model.entity.Question;
import com.ppx.ppxdada.model.entity.ScoringResult;
import com.ppx.ppxdada.model.entity.UserAnswer;
import com.ppx.ppxdada.model.vo.QuestionVO;
import com.ppx.ppxdada.service.QuestionService;
import com.ppx.ppxdada.service.ScoringResultService;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI 测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private AiManager aiManager;

    @Resource
    private RedissonClient redissonClient;

    // 分布式锁的key
    // 每个业务对应一把锁， 所有的线程抢一把锁
    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";


    /**
     * AI 评分结果本地缓存 （只属于这个类）
     */
    private final Cache<String, String> answerCacheMap =
            Caffeine.newBuilder()
                    // 设置初始容量为 1024 个条目
                    .initialCapacity(1024)
                    // 配置缓存项在最后一次访问后 5 分钟移除
                    .expireAfterAccess(5L, TimeUnit.MINUTES)
                    // 构建缓存实例
                    .build();

    /**
     * AI 评分系统消息
     */
    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象";

    /**
     * AI评分用户消息封装
     *
     * @param app
     * @param questionContentDTOList
     * @param choices
     * @return
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTOList, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < questionContentDTOList.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTOList.get(i).getTitle());
            questionAnswerDTO.setUserAnswer(choices.get(i));
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        userMessage.append(JSONUtil.toJsonStr(questionAnswerDTOList));
        return userMessage.toString();
    }

    /**
     * 评分
     *
     * @param choices
     * @param app
     * @return
     * @throws Exception
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) throws Exception {

        Long appId = app.getId();

        // 0. 从缓存中获取评分结果
        String jsonStr = JSONUtil.toJsonStr(choices);
        String cacheKey = buildCacheKey(appId, jsonStr);
        String answerJson = answerCacheMap.getIfPresent(cacheKey);
            // 命中缓存则直接返回结果
        if (StrUtil.isNotBlank(answerJson)) {
            UserAnswer userAnswer = JSONUtil.toBean(answerJson, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;
        }

        // 分布式锁
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + cacheKey);
        try {
            // 竞争分布式锁，等待 3 秒，15 秒自动释放
            boolean res = lock.tryLock(3, 15, TimeUnit.SECONDS);
            if (!res){

                return null;
            }
            // 抢到锁的业务才能执行 AI 调用
            // 1. 根据 id 查询到题目
            // 这段代码在功能上与 SQL 查询是类似的，只不过它通过 MyBatis-Plus 提供的查询构建器来实现，而不是直接编写 SQL 语句
            // SELECT * FROM Question WHERE app_id = {appId} LIMIT 1;
            Question question = questionService.getOne(
                    Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
            );
            QuestionVO questionVO = QuestionVO.objToVo(question);
            List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();

            // 2. 调用 AI 获取结果
            // 封装 Prompt
            String userMessage = getAiTestScoringUserMessage(app, questionContent, choices);
            // AI 生成
            String result = aiManager.doSyncStableRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage);
            // 截取结果处理
            int start = result.indexOf("{");
            int end = result.lastIndexOf("}");
            String json = result.substring(start, end + 1);

            // 3. 缓存结果
            answerCacheMap.put(cacheKey, json);

            // 4. 构造返回值，填充答案对象的属性
            UserAnswer userAnswer = JSONUtil.toBean(json, UserAnswer.class);
            userAnswer.setAppId(appId);
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(jsonStr);
            return userAnswer;

        } finally {
            // 释放锁
            if (lock != null && lock.isLocked()) {
                // 判断是否是当前线程持有锁（只有本人能释放）
                if(lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }


    }

    /**
     * 构建缓存key
     *
     * @param appId
     * @param choices
     * @return
     */
    private String buildCacheKey(Long appId, String choices) {
        return DigestUtils.md5Hex(appId + ":" + choices);
    }
}
