package com.rice.mianshi.sentinel;

import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;

/**
 * sentinel规则管理器
 * @author ricejson
 */
@Component
public class SentinelRulesManager {
    @PostConstruct
    public void init() {
        initFlowRules();
        initDegradeRules();
    }

    /**
     * 初始化流控规则
     */
    public void initFlowRules() {
        ParamFlowRule rule = new ParamFlowRule("listQuestionVOByPageEs");
        // 对第0个参数进行限流（ip）
        rule.setParamIdx(0);
        // 最多60次
        rule.setCount(60);
        // 统计周期为60s
        rule.setDurationInSec(60);
        ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    /**
     * 初始化降级规则
     */
    public void initDegradeRules() {
        DegradeRule slowCallRule = new DegradeRule("listQuestionVOByPageEs");
        slowCallRule.setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType());
        // 慢调用阈值
        slowCallRule.setSlowRatioThreshold(3);
        // 慢调用率
        slowCallRule.setCount(0.2);
        // 统计频率
        slowCallRule.setStatIntervalMs(30 * 1000);
        // 最小请求数
        slowCallRule.setMinRequestAmount(10);
        // 降级时长
        slowCallRule.setTimeWindow(60);
        DegradeRule errorRule = new DegradeRule("listQuestionVOByPageEs");
        errorRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        // 错误率
        errorRule.setCount(0.1);
        // 最小请求数
        errorRule.setMinRequestAmount(10);
        // 降级时长
        errorRule.setTimeWindow(60);
        // 统计频率
        errorRule.setStatIntervalMs(30 * 1000);
        DegradeRuleManager.loadRules(Arrays.asList(slowCallRule, errorRule));


    }
}
