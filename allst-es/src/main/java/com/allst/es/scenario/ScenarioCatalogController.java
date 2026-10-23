package com.allst.es.scenario;

import com.allst.es.scenario.analytics.OrderEventDocument;
import com.allst.es.scenario.common.ScenarioMeta;
import com.allst.es.scenario.common.ScenarioResponse;
import com.allst.es.scenario.fulltext.ArticleDocument;
import com.allst.es.scenario.geo.PlaceDocument;
import com.allst.es.scenario.log.AppLogDocument;
import com.allst.es.scenario.product.ProductDocument;
import com.allst.es.scenario.siem.SecurityEventDocument;
import com.allst.es.scenario.suggest.SuggestTermDocument;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/scenario")
public class ScenarioCatalogController {
    @GetMapping
    public ScenarioResponse<List<ScenarioMeta>> list() {
        List<ScenarioMeta> scenarios = Arrays.asList(
                new ScenarioMeta("fulltext", "全文搜索", ArticleDocument.INDEX, "/scenario/fulltext", "docs/es-scenarios/01-全文搜索.md"),
                new ScenarioMeta("analytics", "实时分析", OrderEventDocument.INDEX, "/scenario/analytics", "docs/es-scenarios/02-实时分析.md"),
                new ScenarioMeta("geo", "地理数据应用", PlaceDocument.INDEX, "/scenario/geo", "docs/es-scenarios/03-地理数据应用.md"),
                new ScenarioMeta("log", "日志和事件数据分析", AppLogDocument.INDEX, "/scenario/log", "docs/es-scenarios/04-日志与事件数据分析.md"),
                new ScenarioMeta("siem", "安全信息和事件管理", SecurityEventDocument.INDEX, "/scenario/siem", "docs/es-scenarios/05-安全信息与事件管理.md"),
                new ScenarioMeta("suggest", "搜索建议", SuggestTermDocument.INDEX, "/scenario/suggest", "docs/es-scenarios/06-搜索建议.md"),
                new ScenarioMeta("product", "电商分面检索", ProductDocument.INDEX, "/scenario/product", "docs/es-scenarios/07-电商分面检索.md")
        );
        return ScenarioResponse.ok(scenarios, scenarios.size());
    }
}
