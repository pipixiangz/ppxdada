package com.ppx.ppxdada.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ppx.ppxdada.common.ErrorCode;
import com.ppx.ppxdada.constant.CommonConstant;
import com.ppx.ppxdada.exception.ThrowUtils;
import com.ppx.ppxdada.mapper.AppMapper;
import com.ppx.ppxdada.model.dto.app.AppQueryRequest;
import com.ppx.ppxdada.model.entity.App;
import com.ppx.ppxdada.model.entity.User;
import com.ppx.ppxdada.model.enums.AppScoringStrategyEnum;
import com.ppx.ppxdada.model.enums.AppTypeEnum;
import com.ppx.ppxdada.model.enums.ReviewStatusEnum;
import com.ppx.ppxdada.model.vo.AppVO;
import com.ppx.ppxdada.model.vo.UserVO;
import com.ppx.ppxdada.service.AppService;
import com.ppx.ppxdada.service.UserService;
import com.ppx.ppxdada.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 *
 *
 *
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param app
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Integer reviewStatus = app.getReviewStatus();


        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
            ThrowUtils.throwIf(StringUtils.isBlank(appDesc), ErrorCode.PARAMS_ERROR, "应用描述不能为空");

            AppTypeEnum appTypeEnum = AppTypeEnum.getEnumByValue(appType);
            ThrowUtils.throwIf(appTypeEnum == null, ErrorCode.PARAMS_ERROR, "应用类别非法");

            AppScoringStrategyEnum scoringStrategyEnum = AppScoringStrategyEnum.getEnumByValue(scoringStrategy);
            ThrowUtils.throwIf(scoringStrategyEnum == null, ErrorCode.PARAMS_ERROR, "应用评分策略非法");
        }
        // 修改数据时，有参数则校验
        // 补充校验规则
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length()>80, ErrorCode.PARAMS_ERROR, "应用名称要小于80");
        }
        // 用户填写了审核状态
        if (reviewStatus != null) {
            ReviewStatusEnum reviewStatusEnum = ReviewStatusEnum.getEnumByValue(reviewStatus);
            ThrowUtils.throwIf(reviewStatusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态非法");
        }

    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        String appIcon = appQueryRequest.getAppIcon();
        Integer appType = appQueryRequest.getAppType();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        Long notId = appQueryRequest.getNotId();
        String searchText = appQueryRequest.getSearchText();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件, 例如：appName like '%searchText%' or appDesc like '%searchText%'
            queryWrapper.and(qw -> qw.like("appName", appName).or().like("appDesc", appDesc));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);

        // 精确查询
        queryWrapper.eq(StringUtils.isNotBlank(appIcon), "appIcon", appIcon);
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);

        // endregion

        return appVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        // 从分页结果中获取记录列表
        List<App> appList = appPage.getRecords();

        // 创建一个新的 Page 对象，用于存储 AppVO 对象的分页结果
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());

        // 如果 appList 为空，则直接返回空的 appVOPage
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }

        // 将 App 对象列表转换为 AppVO 对象列表
        List<AppVO> appVOList = appList.stream().map(AppVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选部分: 关联查询用户信息

        // 从 appList 中提取所有的 userId，并存储在一个 Set<Long> 中，确保每个 userId 是唯一的
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());

        // 根据 userIdSet 查询用户信息，并存储在一个 Map<Long, List<User>> 中，key 是 userId，value 是对应的 User 对象列表
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 为每个 AppVO 对象填充用户信息
        appVOList.forEach(appVO -> {
            Long userId = appVO.getUserId();
            User user = null;

            // 如果 userIdUserListMap 中包含当前 appVO 的 userId，则获取对应的用户信息
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }

            // 将获取到的用户信息转换为 UserVO 对象，并设置到 appVO 中
            appVO.setUser(userService.getUserVO(user));
        });
        // endregion 可选部分结束

        // 将转换后的 AppVO 对象列表设置到分页结果中
        appVOPage.setRecords(appVOList);

        // 返回包含 AppVO 对象的分页结果
        return appVOPage;
    }


}
