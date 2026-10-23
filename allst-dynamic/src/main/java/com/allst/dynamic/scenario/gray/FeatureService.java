package com.allst.dynamic.scenario.gray;

import com.allst.dynamic.scenario.common.DataSourceKeys;
import com.allst.dynamic.scenario.common.DataSources;
import com.allst.dynamic.scenario.common.RouteProbe;
import com.allst.dynamic.scenario.common.RouteView;
import com.allst.dynamic.scenario.common.ScenarioException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FeatureService {

    private final FeatureFlagMapper featureFlagMapper;
    private final GrayPolicy grayPolicy;
    private final RouteProbe routeProbe;

    public FeatureService(FeatureFlagMapper featureFlagMapper, GrayPolicy grayPolicy, RouteProbe routeProbe) {
        this.featureFlagMapper = featureFlagMapper;
        this.grayPolicy = grayPolicy;
        this.routeProbe = routeProbe;
    }

    public String decide(String versionHeader, String userId) {
        return GrayDecider.decide(grayPolicy, versionHeader, userId);
    }

    public RouteView<List<FeatureFlag>> current(String versionHeader, String userId) {
        String key = decide(versionHeader, userId);
        return DataSources.call(key, () -> {
            List<FeatureFlag> rows = featureFlagMapper.selectList(new QueryWrapper<FeatureFlag>().orderByAsc("id"));
            if (rows.isEmpty()) {
                throw new ScenarioException(404, "当前库还没有特性配置，请先调用 /scenario/gray/seed");
            }
            return RouteView.of(key, routeProbe.database(), "t_feature",
                    GrayDecider.explain(key, versionHeader, userId), rows);
        });
    }

    public List<String> seed() {
        write(DataSourceKeys.RELEASE_STABLE, 1L, "checkout.button", "立即购买", "stable");
        write(DataSourceKeys.RELEASE_GRAY, 1L, "checkout.button", "立即购买-灰度", "gray");
        return List.of(DataSourceKeys.RELEASE_STABLE, DataSourceKeys.RELEASE_GRAY);
    }

    public GrayPolicy updatePolicy(GrayPolicyCommand command) {
        if (command == null || command.getPercent() == null) {
            throw new ScenarioException(400, "percent 不能为空");
        }
        int percent = command.getPercent();
        if (percent < 0 || percent > 100) {
            throw new ScenarioException(400, "percent 必须在 0 到 100 之间");
        }
        List<String> users = new ArrayList<>();
        if (command.getAllowUsers() != null) {
            if (command.getAllowUsers().size() > 100) {
                throw new ScenarioException(400, "白名单最多 100 个用户");
            }
            for (String user : command.getAllowUsers()) {
                users.add(com.allst.dynamic.scenario.common.Checks.requireText(user, "allowUsers", 64));
            }
        }
        grayPolicy.replace(percent, users);
        return grayPolicy;
    }

    public GrayPolicy policy() {
        return grayPolicy;
    }

    private void write(String key, long id, String featureKey, String featureValue, String versionTag) {
        DataSources.run(key, () -> {
            featureFlagMapper.delete(new QueryWrapper<FeatureFlag>().isNotNull("id"));
            FeatureFlag flag = new FeatureFlag();
            flag.setId(id);
            flag.setFeatureKey(featureKey);
            flag.setFeatureValue(featureValue);
            flag.setVersionTag(versionTag);
            featureFlagMapper.insert(flag);
        });
    }
}
