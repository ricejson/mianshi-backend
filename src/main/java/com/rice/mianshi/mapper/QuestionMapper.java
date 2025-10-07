package com.rice.mianshi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rice.mianshi.model.entity.Question;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
* @author 57017
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2025-08-31 17:59:38
* @Entity generator.domain.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    @Select("select * from question where updateTime >= #{minUpdateTime}")
    List<Question> getQuestionListWithDelete(Date minUpdateTime);

}




