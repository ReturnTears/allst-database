package com.allst.dynamic.scenario.rw;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.DataSources;
import com.allst.dynamic.scenario.common.RouteProbe;
import com.allst.dynamic.scenario.common.RouteView;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final RouteProbe routeProbe;

    public ArticleServiceImpl(ArticleMapper articleMapper, RouteProbe routeProbe) {
        this.articleMapper = articleMapper;
        this.routeProbe = routeProbe;
    }

    @Override
    @WriteRoute
    public RouteView<Article> create(ArticleCommand command) {
        Article article = new Article();
        article.setTitle(Checks.requireText(command == null ? null : command.getTitle(), "title", 128));
        article.setAuthor(Checks.requireText(command == null ? null : command.getAuthor(), "author", 64));
        article.setCreatedAt(LocalDateTime.now());
        articleMapper.insert(article);
        return snapshot(article, "写入只进入 rwMaster，不写从库");
    }

    @Override
    @ReadRoute
    public RouteView<List<Article>> list() {
        String key = DynamicDataSourceContextHolder.peek();
        String reason = DataSourceKeys.RW_MASTER.equals(key)
                ? "本请求已写过主库，或带了 X-Force-Master，读请求留在主库"
                : "读请求在 rwSlave 组内选定 " + key + "，同一次调用不再重新轮询";
        return snapshot(loadAll(), reason);
    }

    @Override
    public RouteView<List<Article>> listFromMaster() {
        return DataSources.call(DataSourceKeys.RW_MASTER, () -> snapshot(loadAll(), "显式读主库，用来和从库结果对照"));
    }

    @Override
    public List<String> seed() {
        List<String> keys = List.of(DataSourceKeys.RW_MASTER, DataSourceKeys.RW_SLAVE_1, DataSourceKeys.RW_SLAVE_2);
        for (String key : keys) {
            DataSources.run(key, () -> {
                articleMapper.delete(new QueryWrapper<Article>().isNotNull("id"));
                insertSeed(1L, "动态数据源入门", "规范");
                insertSeed(2L, "从库只读", "规范");
            });
        }
        return keys;
    }

    private List<Article> loadAll() {
        return articleMapper.selectList(new QueryWrapper<Article>().orderByAsc("id"));
    }

    private void insertSeed(long id, String title, String author) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        articleMapper.insert(article);
    }

    private <T> RouteView<T> snapshot(T records, String reason) {
        return RouteView.of(DynamicDataSourceContextHolder.peek(), routeProbe.database(), "t_article", reason, records);
    }
}
