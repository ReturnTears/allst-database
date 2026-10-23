package com.allst.es.scenario.siem;

import com.allst.es.scenario.common.NamedCount;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 把聚合结果翻译成告警。阈值判断放在这里，方便不启动 Elasticsearch 做回归。
 */
public class SiemRuleEngine {
    public List<SecurityAlert> evaluate(List<NamedCount> loginFailureByIp,
                                         int bruteForceThreshold,
                                         List<NamedCount> distinctPortsByIp,
                                         int portScanThreshold,
                                         List<NamedCount> privilegeChangeByUser,
                                         int privilegeThreshold) {
        int brute = bruteForceThreshold <= 0 ? 5 : bruteForceThreshold;
        int ports = portScanThreshold <= 0 ? 6 : portScanThreshold;
        int privilege = privilegeThreshold <= 0 ? 3 : privilegeThreshold;
        List<SecurityAlert> alerts = new ArrayList<SecurityAlert>();
        collect(alerts, "BRUTE_FORCE", "登录失败次数过多", "high", "ip",
                loginFailureByIp, brute, "来源IP %s 在统计窗口内登录失败 %d 次，阈值 %d");
        collect(alerts, "PORT_SCAN", "短时间探测端口过多", "high", "ip",
                distinctPortsByIp, ports, "来源IP %s 在统计窗口内访问了 %d 个目的端口，阈值 %d");
        collect(alerts, "PRIVILEGE_CHANGE", "权限变更过于频繁", "critical", "user",
                privilegeChangeByUser, privilege, "账号 %s 在统计窗口内发生 %d 次权限变更，阈值 %d");
        return alerts;
    }

    private void collect(List<SecurityAlert> alerts, String code, String name, String severity,
                          String subjectType, List<NamedCount> buckets, int threshold, String template) {
        if (buckets == null) {
            return;
        }
        for (NamedCount bucket : buckets) {
            if (bucket == null || !StringUtils.hasText(bucket.getKey())) {
                continue;
            }
            long count = bucket.signal();
            if (count < threshold) {
                continue;
            }
            alerts.add(SecurityAlert.builder()
                    .ruleCode(code)
                    .ruleName(name)
                    .severity(severity)
                    .subjectType(subjectType)
                    .subject(bucket.getKey())
                    .count(count)
                    .threshold(threshold)
                    .summary(String.format(template, bucket.getKey(), count, threshold))
                    .build());
        }
    }
}
