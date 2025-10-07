package com.rice.mianshi.esdao;

import com.rice.mianshi.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 操作Question的 es dao层
 * @author ricejson
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

}
