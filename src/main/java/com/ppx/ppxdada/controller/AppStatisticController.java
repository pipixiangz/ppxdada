package com.ppx.ppxdada.controller;

import cn.hutool.core.io.FileUtil;
import com.ppx.ppxdada.common.BaseResponse;
import com.ppx.ppxdada.common.ErrorCode;
import com.ppx.ppxdada.common.ResultUtils;
import com.ppx.ppxdada.constant.FileConstant;
import com.ppx.ppxdada.exception.BusinessException;
import com.ppx.ppxdada.exception.ThrowUtils;
import com.ppx.ppxdada.manager.CosManager;
import com.ppx.ppxdada.mapper.UserAnswerMapper;
import com.ppx.ppxdada.model.dto.file.UploadFileRequest;
import com.ppx.ppxdada.model.dto.statistics.AppAnswerCountDTO;
import com.ppx.ppxdada.model.dto.statistics.AppAnswerResultCountDTO;
import com.ppx.ppxdada.model.entity.User;
import com.ppx.ppxdada.model.enums.FileUploadBizEnum;
import com.ppx.ppxdada.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 统计接口
 *
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;

    /**
     * 热门应用回答数统计
     * @return
     */
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountDTO>> getAppAnswerCount() {
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    /**
     * 某应用答题结果分布统计（Top10）
     * @param appId
     * @return
     */
    @GetMapping("/answer_result_count")
    public BaseResponse<List<AppAnswerResultCountDTO>> getAppAnswerResultCount(Long appId) {
        ThrowUtils.throwIf(appId == null || appId<=0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }
}
