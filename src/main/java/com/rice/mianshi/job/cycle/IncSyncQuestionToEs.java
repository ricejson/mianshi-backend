package com.rice.mianshi.job.cycle;

import com.rice.mianshi.esdao.QuestionEsDao;
import com.rice.mianshi.mapper.QuestionMapper;
import com.rice.mianshi.model.dto.question.QuestionEsDTO;
import com.rice.mianshi.model.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 增量同步题目数据到ES中
 * @author ricejson
 */
@Component
@Slf4j
public class IncSyncQuestionToEs {

    @Resource
    private QuestionEsDao questionEsDao;

    @Resource
    private QuestionMapper questionMapper;

    private static final long FIVE_MINUTES = 5 * 60 * 1000L;

    @Scheduled(fixedRate = 60 * 1000) // 每分钟执行一次
    public void run() {
        // 需不需要同步已经被删除的数据
        Date fiveBeforeDate = new Date(System.currentTimeMillis() - FIVE_MINUTES);
        List<Question> questions = questionMapper.getQuestionListWithDelete(fiveBeforeDate);
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
        log.info("IncSyncQuestionToEs start, total {}", total);
        for (int i = 0; i < total; i += pageSize) {
            int end = Math.min(i + pageSize, total);
            log.info("sync from {} to {}", i, end);
            questionEsDao.saveAll(questionEsDTOS.subList(i, end));
        }
        log.info("IncSyncQuestionToEs end, total {}", total);
    }
}
