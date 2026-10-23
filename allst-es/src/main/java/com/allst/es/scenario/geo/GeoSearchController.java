package com.allst.es.scenario.geo;

import com.allst.es.scenario.common.ScenarioResponse;
import com.allst.es.scenario.common.SearchPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/scenario/geo")
public class GeoSearchController {
    private final GeoSearchService service;

    public GeoSearchController(GeoSearchService service) {
        this.service = service;
    }

    @PostMapping("/index/init")
    public ScenarioResponse<Boolean> init() {
        return ScenarioResponse.ok(service.initIndex());
    }

    @PostMapping("/seed")
    public ScenarioResponse<Integer> seed() {
        int count = service.seed();
        return ScenarioResponse.ok(count, count);
    }

    @PostMapping("/places")
    public ScenarioResponse<PlaceDocument> save(@RequestBody PlaceCommand command) {
        return ScenarioResponse.ok(service.save(command));
    }

    @GetMapping("/nearby")
    public ScenarioResponse<SearchPage<PlaceDocument>> nearby(@RequestParam double lat,
                                                                @RequestParam double lon,
                                                                @RequestParam(defaultValue = "3") double distanceKm,
                                                                @RequestParam(required = false) String category,
                                                                @RequestParam(required = false) String q,
                                                                @RequestParam(required = false) Integer size) {
        SearchPage<PlaceDocument> page = service.nearby(lat, lon, distanceKm, category, q, size);
        return ScenarioResponse.ok(page, page.getTotal());
    }

    @GetMapping("/bbox")
    public ScenarioResponse<SearchPage<PlaceDocument>> bbox(@RequestParam double topLat,
                                                              @RequestParam double leftLon,
                                                              @RequestParam double bottomLat,
                                                              @RequestParam double rightLon,
                                                              @RequestParam(required = false) String category,
                                                              @RequestParam(required = false) Integer size) {
        SearchPage<PlaceDocument> page = service.boundingBox(topLat, leftLon, bottomLat, rightLon, category, size);
        return ScenarioResponse.ok(page, page.getTotal());
    }

    @GetMapping("/heatmap")
    public ScenarioResponse<List<HeatCell>> heatmap(@RequestParam(required = false) String category,
                                                      @RequestParam(required = false) Integer precision) {
        List<HeatCell> cells = service.heatmap(category, precision);
        return ScenarioResponse.ok(cells, cells.size());
    }
}
