package com.rice.mianshi.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rice.mianshi.annotation.AuthCheck;
import com.rice.mianshi.common.BaseResponse;
import com.rice.mianshi.common.DeleteRequest;
import com.rice.mianshi.common.ErrorCode;
import com.rice.mianshi.common.ResultUtils;
import com.rice.mianshi.constant.UserConstant;
import com.rice.mianshi.exception.BusinessException;
import com.rice.mianshi.exception.ThrowUtils;
import com.rice.mianshi.model.dto.question.QuestionAddRequest;
import com.rice.mianshi.model.dto.question.QuestionQueryRequest;
import com.rice.mianshi.model.dto.question.QuestionUpdateRequest;
import com.rice.mianshi.model.entity.Question;
import com.rice.mianshi.model.entity.QuestionBankQuestion;
import com.rice.mianshi.model.entity.User;
import com.rice.mianshi.model.vo.QuestionVO;
import com.rice.mianshi.service.QuestionBankQuestionService;
import com.rice.mianshi.service.QuestionBankService;
import com.rice.mianshi.service.QuestionService;
import com.rice.mianshi.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目接口
 *
 * @author <a href="https://github.com/ricejson">程序员米饭</a>
 * @from <a href="https://www.code-nav.cn">编程导航学习圈</a>
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionBankQuestionService questionBankQuestionService;
    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        // 处理标签
        String tags = questionAddRequest.getTags();
        List<String> tagList = JSONUtil.toList(tags, String.class);
        if (CollUtil.isNotEmpty(tagList)) {
            String tagStr = JSONUtil.toJsonStr(tagList);
            question.setTags(tagStr);
        }
        // 数据校验
        questionService.validQuestion(question, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        // 数据校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        QuestionVO questionVO = questionService.getQuestionVO(question, request);
        if (loginUser != null) {
            // 爬虫检测
            questionService.crawlerDetect(loginUser.getId());
        } else {
            // 不设置推荐答案
            questionVO.setAnswer("");
        }
        // 获取封装类
        return ResultUtils.success(questionVO);
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 200, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);
        // 获取questionBankId
        Long questionBankId = questionQueryRequest.getQuestionBankId();
        if (questionBankId != null && questionBankId > 0) {
            // 关联查询题目题库关联表
            LambdaQueryWrapper<QuestionBankQuestion> lambdaQueryWrapper = Wrappers.lambdaQuery(QuestionBankQuestion.class)
                    .select(QuestionBankQuestion::getQuestionId)
                    .eq(QuestionBankQuestion::getQuestionBankId, questionBankId);
            Set<Long> questionIdSet = questionBankQuestionService.list(lambdaQueryWrapper).stream()
                    .map(QuestionBankQuestion::getQuestionId).collect(Collectors.toSet());
            if (CollUtil.isNotEmpty(questionIdSet)) {
                queryWrapper.in("id", questionIdSet);
            }
        }
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                queryWrapper);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（es）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo/es")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPageEs(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        Entry entry = null;
        String ip = request.getRemoteAddr();
        // 获取封装类
        try {
            entry = SphU.entry("listQuestionVOByPageEs", EntryType.IN, 1, ip);
            return ResultUtils.success(questionService.getQuestionVOPage(questionService.searchFormEs(questionQueryRequest, request), request));
        } catch (Throwable th) {
            // 上报业务异常
            if (!BlockException.isBlockException(th)) {
                Tracer.trace(th);
                // 处理降级
                return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误！");
            }
            if (th instanceof DegradeException) {
                // 处理降级
                return ResultUtils.success(null);
            }
            // 处理限流
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "访问过于频繁，请稍后再试！");

        } finally {
            if (entry != null) {
                entry.exit(1, ip);
            }
        }
    }
    // endregion
}
