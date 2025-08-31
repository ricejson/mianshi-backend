package com.rice.mianshi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rice.mianshi.model.entity.Question;
import com.rice.mianshi.service.QuestionService;
import com.rice.mianshi.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author 57017
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2025-08-31 17:59:38
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




