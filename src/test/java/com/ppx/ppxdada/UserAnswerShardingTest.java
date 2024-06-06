package com.ppx.ppxdada;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ppx.ppxdada.model.entity.UserAnswer;
import com.ppx.ppxdada.service.UserAnswerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class UserAnswerShardingTest {
    @Resource
    private UserAnswerService userAnswerService;

    @Test
    void test() {

        UserAnswer userAnswer1 = new UserAnswer();

        userAnswer1.setAppId(8L);
        userAnswer1.setUserId(8L);
        userAnswer1.setChoices("8");
        userAnswerService.save(userAnswer1);

        UserAnswer userAnswer2 = new UserAnswer();
        userAnswer2.setAppId(9L);
        userAnswer2.setUserId(9L);
        userAnswer2.setChoices("9");
        userAnswerService.save(userAnswer2);

        UserAnswer userAnswerOne = userAnswerService.getOne(Wrappers.lambdaQuery(UserAnswer.class).eq(UserAnswer::getAppId, 8L));
        System.out.println(JSONUtil.toJsonStr(userAnswerOne));

        UserAnswer userAnswerTwo = userAnswerService.getOne(Wrappers.lambdaQuery(UserAnswer.class).eq(UserAnswer::getAppId, 9L));
        System.out.println(JSONUtil.toJsonStr(userAnswerTwo));
    }
}
