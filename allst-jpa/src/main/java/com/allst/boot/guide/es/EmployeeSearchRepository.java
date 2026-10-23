package com.allst.boot.guide.es;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 和 EmployeeRepository 同时存在：一个走关系库，一个走搜索索引。
 */
public interface EmployeeSearchRepository extends ElasticsearchRepository<EmployeeDocument, String> {

    List<EmployeeDocument> findByName(String name);
}
