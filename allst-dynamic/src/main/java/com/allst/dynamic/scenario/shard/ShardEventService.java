package com.allst.dynamic.scenario.shard;

import com.allst.dynamic.scenario.common.Checks;
import com.allst.dynamic.scenario.common.DataSources;
import com.allst.dynamic.scenario.common.RouteProbe;
import com.allst.dynamic.scenario.common.RouteView;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.allst.dynamic.scenario.runtime.RegisterDataSourceCommand;
import com.allst.dynamic.scenario.runtime.RuntimeDataSourceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShardEventService {

    private final ShardEventMapper shardEventMapper;
    private final ShardDirectory shardDirectory;
    private final RuntimeDataSourceService runtimeDataSourceService;
    private final RouteProbe routeProbe;

    public ShardEventService(ShardEventMapper shardEventMapper, ShardDirectory shardDirectory,
                             RuntimeDataSourceService runtimeDataSourceService, RouteProbe routeProbe) {
        this.shardEventMapper = shardEventMapper;
        this.shardDirectory = shardDirectory;
        this.runtimeDataSourceService = runtimeDataSourceService;
        this.routeProbe = routeProbe;
    }

    public ShardDirectory.ShardLocation locate(String shardKey) {
        return shardDirectory.locate(Checks.requireText(shardKey, "shardKey", 64));
    }

    public List<ShardDirectory.ShardSlot> directory() {
        return shardDirectory.snapshot();
    }

    public RouteView<ShardEvent> create(EventCommand command) {
        if (command == null) {
            throw new ScenarioException(400, "请求体不能为空");
        }
        String shardKey = Checks.requireText(command.getShardKey(), "shardKey", 64);
        String eventType = Checks.requireText(command.getEventType(), "eventType", 32);
        String payload = command.getPayload() == null ? "" : command.getPayload().trim();
        if (payload.length() > 256) {
            throw new ScenarioException(400, "payload长度不能超过256");
        }
        ShardDirectory.ShardLocation location = shardDirectory.locate(shardKey);
        ShardEvent event = new ShardEvent();
        event.setShardKey(shardKey);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setCreatedAt(LocalDateTime.now());
        return DataSources.call(location.getDataSource(), () -> {
            shardEventMapper.insert(event);
            return RouteView.of(location.getDataSource(), routeProbe.database(), "t_event",
                    "分片键 " + shardKey + " 落在槽 " + location.getSlot(), event);
        });
    }

    public RouteView<List<ShardEvent>> list(String shardKey) {
        String normalized = Checks.requireText(shardKey, "shardKey", 64);
        ShardDirectory.ShardLocation location = shardDirectory.locate(normalized);
        return DataSources.call(location.getDataSource(), () -> {
            List<ShardEvent> events = shardEventMapper.selectList(new QueryWrapper<ShardEvent>()
                    .eq("shard_key", normalized)
                    .orderByDesc("id"));
            return RouteView.of(location.getDataSource(), routeProbe.database(), "t_event",
                    "按分片键回到槽 " + location.getSlot(), events);
        });
    }

    public String register(RegisterDataSourceCommand command) {
        if (command == null) {
            throw new ScenarioException(400, "请求体不能为空");
        }
        String name = Checks.requireText(command.getName(), "name", 16);
        if (!name.matches("^[a-z][a-z0-9]{0,15}$")) {
            throw new ScenarioException(400, "name 只能是小写字母开头，后接小写字母或数字");
        }
        String key = "rtShard" + name;
        runtimeDataSourceService.register(key, command.getUrl(), command.getUsername(), command.getPassword(),
                command.getDriverClassName());
        return key;
    }

    public List<ShardDirectory.ShardSlot> move(MoveSlotsCommand command) {
        if (command == null) {
            throw new ScenarioException(400, "请求体不能为空");
        }
        String dataSource = Checks.requireText(command.getDataSource(), "dataSource", 64);
        if (!runtimeDataSourceService.exists(dataSource)) {
            throw new ScenarioException(404, "数据源 " + dataSource + " 不存在");
        }
        shardDirectory.move(dataSource, command.getSlots());
        return shardDirectory.snapshot();
    }
}
