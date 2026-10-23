package com.allst.dynamic.scenario.rw;

import com.allst.dynamic.scenario.common.RouteView;
import lombok.Data;

import java.util.List;

@Data
public class ReadAfterWriteView {
    private RouteView<Article> written;
    private RouteView<List<Article>> read;
}
