package com.allst.es;

import com.allst.es.entity.VideoDTO;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootTest
class AllstEsApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    /**
     * 判断索引是否存在
     */
    @Test
    void contextLoads() {
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(VideoDTO.class);
        boolean exists = indexOperations.exists();
        System.out.println(exists);
    }

    /**
     * 创建索引
     */
    @Test
    void contextLoads2() {
        // spring data es所有索引操作都在这个接口
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(VideoDTO.class);
        // 是否存在，存在则删除
        if (indexOperations.exists()) {
            indexOperations.delete();
        }
        // 创建索引
        indexOperations.create();
        log.info("测试--索引创建成功");
        // 设置映射: 在正式开发中，几乎不会使用框架创建索引或设置映射，这是架构或者管理员的工作，不适合使用代码实现
        elasticsearchTemplate.indexOps(VideoDTO.class).putMapping();
    }

    /**
     * 删除索引
     */
    @Test
    public void deleteIndex() {
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(VideoDTO.class);
        boolean delete = indexOperations.delete();
        System.out.println(delete);
    }

    /**
     * 索引下插入文档数据
     */
    @Test
    public void insert() {
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(1L);
        videoDTO.setTitle("高级微服务研发课 Spring Cloud");
        videoDTO.setCreateTime(LocalDateTime.now());
        videoDTO.setDuration(100);
        videoDTO.setCategory("后端");
        videoDTO.setDescription("这个是综合大型课程，包括了JVM，Redis，新版Spring Boot 3.x + ，架构，监控，性能优化，算法，高并发等多方面内容");
        VideoDTO saved = elasticsearchTemplate.save(videoDTO);
        System.out.println(saved);
    }

    /**
     * 向索引下已存在的文档插入数据时，则更新
     */
    @Test
    public void update() {
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(1L);
        videoDTO.setTitle("高级微服务研发课 Spring Cloud V2");
        videoDTO.setCreateTime(LocalDateTime.now());
        videoDTO.setDuration(102);
        videoDTO.setCategory("后端");
        videoDTO.setDescription("这个是综合大型课程，包括了JVM，Redis，新版Spring Boot 3.x +，Linux,架构，监控，性能优化，容器，算法，高并发等多方面内容");
        VideoDTO saved = elasticsearchTemplate.save(videoDTO);
        System.out.println(saved);
    }

    /**
     * 批量插入文档数据
     */
    @Test
    public void batchInsert() {
        List<VideoDTO> list = Lists.newArrayList();
        list.add(new VideoDTO(2L, "康康的前端设计课程", "前端设计，PS，制图，Vision", 12301, "前端"));
        list.add(new VideoDTO(3L, "俊俊的前端性能优化", "前端高手系列", 100042, "前端"));
        list.add(new VideoDTO(4L, "海量数据项目大课", "强哥哥的后端+大数据综合课程", 5432345, "大数据"));
        list.add(new VideoDTO(5L, "IT课堂永久会员", "可以看海量专题课程，IT技术持续充电平台", 6542, "后端"));
        list.add(new VideoDTO(6L, "百度-前端低代码平台", "高效开发底层基础平台，效能平台案例", 53422, "前端"));
        list.add(new VideoDTO(7L, "自动化测试平台大课", "微服务架构下的spring cloud架构大课，包括jvm,效能平台", 6542, "后端"));
        Iterable<VideoDTO> result = elasticsearchTemplate.save(list);
        System.out.println(result);
    }

    /**
     * 根据主键id查询文档数据
     */
    @Test
    public void searchById() {
        VideoDTO videoDTO = elasticsearchTemplate.get("3", VideoDTO.class);
        assert videoDTO != null;
        System.out.println(videoDTO);
    }

    /**
     * 根据主键id删除文档数据
     */
    @Test
    public void deleteById() {
        String delete = elasticsearchTemplate.delete("2", VideoDTO.class);
        System.out.println(delete);
    }

    /**
     * 查询所有文档数据
     * 新版的搜索语法，查询采用新版的lambda表达式语法
     */
    @Test
    public void searchAll() {
        SearchHits<VideoDTO> search = elasticsearchTemplate.search(Query.findAll(), VideoDTO.class);
        List<SearchHit<VideoDTO>> searchHits = search.getSearchHits();
        // 获得searchHits,进行遍历得到content
        List<VideoDTO> videoDTOS = Lists.newArrayList();
        searchHits.forEach(hit -> videoDTOS.add(hit.getContent()));
        System.out.println(videoDTOS);
    }

    /**
     * 匹配查询
     */
    @Test
    public void matchQuery() {
        // 新版的匹配查询
        /*Query query = NativeQuery.builder().withQuery(q -> q
                .match(m -> m
                        .field("description") // 字段
                        .query("spring") // 值 （匹配的值）
                )).build();*/
        TermQueryBuilder queryBuilder = QueryBuilders.termQuery("description", "spring");
        Query query = new NativeSearchQueryBuilder().withQuery(queryBuilder)
                .build();
        SearchHits<VideoDTO> searchHits = elasticsearchTemplate.search(query, VideoDTO.class);
        // 获得searchHits,进行遍历得到content
        List<VideoDTO> videoDTOS = Lists.newArrayList();
        searchHits.forEach(hit -> videoDTOS.add(hit.getContent()));
        System.out.println(videoDTOS);
    }

    /**
     * 分页查询
     */
    @Test
    public void pageSearch() {
        /*Query query = NativeQuery.builder().withQuery(Query.findAll())
                .withPageable(Pageable.ofSize(3).withPage(0)).build();*/
        QueryBuilder builder = QueryBuilders.matchAllQuery();
        Query query = new NativeSearchQueryBuilder().withQuery(builder).withPageable(Pageable.ofSize(3).withPage(0))
                .build();
        SearchHits<VideoDTO> searchHits = elasticsearchTemplate.search(query, VideoDTO.class);
        // 获得searchHits,进行遍历得到content
        List<VideoDTO> videoDTOS = Lists.newArrayList();
        searchHits.forEach(hit -> videoDTOS.add(hit.getContent()));
        System.out.println(videoDTOS);
    }

    /**
     * 排序查询，根据时长降序排列
     * 新版本中使用
     * ascending()：默认的，正序
     * descending()：倒叙
     */
    @Test
    public void sortSearch() {
        /*Query query = NativeQuery.builder().withQuery(Query.findAll())
                .withPageable(Pageable.ofSize(10).withPage(0))
                .withSort(Sort.by("duration").descending()).build();*/
        Query query = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchAllQuery())
                .withPageable(Pageable.ofSize(10).withPage(0)).
                withSort(SortBuilders.fieldSort("duration").order(SortOrder.DESC)).build();
        SearchHits<VideoDTO> searchHits = elasticsearchTemplate.search(query, VideoDTO.class);
        // 获得searchHits,进行遍历得到content
        List<VideoDTO> videoDTOS = Lists.newArrayList();
        searchHits.forEach(hit -> videoDTOS.add(hit.getContent()));
        System.out.println(videoDTOS);
    }
}
