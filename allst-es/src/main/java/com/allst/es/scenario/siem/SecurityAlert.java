package com.allst.es.scenario.siem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecurityAlert {
    private String ruleCode;
    private String ruleName;
    private String severity;
    private String subjectType;
    private String subject;
    private long count;
    private int threshold;
    private String summary;
}
