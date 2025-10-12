package com.rice.mianshi.job.once;

import com.rice.mianshi.esdao.QuestionEsDao;
import com.rice.mianshi.model.dto.question.QuestionEsDTO;
import com.rice.mianshi.model.entity.Question;
import com.rice.mianshi.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全量同步题目数据到es中
 * @author ricejson
 */
//@Component
@Slf4j
public class FullSyncQuestionToEs implements CommandLineRunner {
    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionEsDao questionEsDao;

    @Override
    public void run(String... args) throws Exception {
        // 1. 查询 MySQL 全部题目数据
        List<Question> questions = questionService.list();
        if (questions.isEmpty()) {
            return ;
        }
        // 2. 映射到 EsDTO
        List<QuestionEsDTO> questionEsDTOS = questions.stream().
                map(QuestionEsDTO::objToDto).
                collect(Collectors.toList());
        // 3. 插入到 es 中
        final int pageSize = 500;
        int total = questionEsDTOS.size();
        log.info("FullSyncQuestionToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOS.subList(i, end));
        }
        log.info("FullSyncQuestionToEs end, total {}", total);
    }
}
