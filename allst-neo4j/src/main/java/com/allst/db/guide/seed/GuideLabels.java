package com.allst.db.guide.seed;

/**
 * 只清理规范示例自己的标签，不触及旧练习里的 Person、Book 等节点。
 */
public final class GuideLabels {

    public static final String PURGE = "MATCH (n) "
            + "WHERE n:Member OR n:Department OR n:Project OR n:Task OR n:Document OR n:Skill OR n:Tag OR n:TxProbe "
            + "DETACH DELETE n";

    private GuideLabels() {
    }
}
