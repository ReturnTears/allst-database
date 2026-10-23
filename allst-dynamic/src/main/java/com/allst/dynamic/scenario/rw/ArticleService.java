package com.allst.dynamic.scenario.rw;

import com.allst.dynamic.scenario.common.RouteView;

import java.util.List;

public interface ArticleService {

    RouteView<Article> create(ArticleCommand command);

    RouteView<List<Article>> list();

    RouteView<List<Article>> listFromMaster();

    List<String> seed();
}
