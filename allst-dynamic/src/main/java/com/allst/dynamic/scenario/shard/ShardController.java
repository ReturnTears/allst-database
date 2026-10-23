package com.allst.dynamic.scenario.shard;

import com.allst.dynamic.scenario.common.ScenarioResponse;
import com.allst.dynamic.scenario.runtime.RegisterDataSourceCommand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/shard")
public class ShardController {

    private final ShardEventService shardEventService;

    public ShardController(ShardEventService shardEventService) {
        this.shardEventService = shardEventService;
    }

    @GetMapping("/locate")
    public ScenarioResponse<?> locate(@RequestParam("shardKey") String shardKey) {
        return ScenarioResponse.ok(shardEventService.locate(shardKey));
    }

    @GetMapping("/directory")
    public ScenarioResponse<?> directory() {
        return ScenarioResponse.ok(shardEventService.directory());
    }

    @PostMapping("/events")
    public ScenarioResponse<?> create(@RequestBody EventCommand command) {
        return ScenarioResponse.ok(shardEventService.create(command));
    }

    @GetMapping("/events")
    public ScenarioResponse<?> list(@RequestParam("shardKey") String shardKey) {
        return ScenarioResponse.ok(shardEventService.list(shardKey));
    }

    @PostMapping("/datasources")
    public ScenarioResponse<String> register(@RequestBody RegisterDataSourceCommand command) {
        return ScenarioResponse.ok(shardEventService.register(command));
    }

    @PostMapping("/slots")
    public ScenarioResponse<?> move(@RequestBody MoveSlotsCommand command) {
        return ScenarioResponse.ok(shardEventService.move(command));
    }
}
