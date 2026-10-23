package com.allst.dynamic.scenario.gray;

import com.allst.dynamic.scenario.common.ScenarioHeaders;
import com.allst.dynamic.scenario.common.ScenarioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scenario/gray")
public class GrayController {

    private final FeatureService featureService;

    public GrayController(FeatureService featureService) {
        this.featureService = featureService;
    }

    @GetMapping("/decide")
    public ScenarioResponse<String> decide(
            @RequestHeader(value = ScenarioHeaders.GRAY_VERSION, required = false) String version,
            @RequestHeader(value = ScenarioHeaders.USER_ID, required = false) String userId) {
        return ScenarioResponse.ok(featureService.decide(version, userId));
    }

    @GetMapping("/config")
    public ScenarioResponse<?> config(
            @RequestHeader(value = ScenarioHeaders.GRAY_VERSION, required = false) String version,
            @RequestHeader(value = ScenarioHeaders.USER_ID, required = false) String userId) {
        return ScenarioResponse.ok(featureService.current(version, userId));
    }

    @PostMapping("/seed")
    public ScenarioResponse<?> seed() {
        return ScenarioResponse.ok(featureService.seed());
    }

    @GetMapping("/policy")
    public ScenarioResponse<GrayPolicy> policy() {
        return ScenarioResponse.ok(featureService.policy());
    }

    @PutMapping("/policy")
    public ScenarioResponse<GrayPolicy> updatePolicy(@RequestBody GrayPolicyCommand command) {
        return ScenarioResponse.ok(featureService.updatePolicy(command));
    }
}
