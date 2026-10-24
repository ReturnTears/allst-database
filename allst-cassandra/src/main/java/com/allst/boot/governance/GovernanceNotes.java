package com.allst.boot.governance;

import java.util.List;

public final class GovernanceNotes {

    private GovernanceNotes() {
    }

    public static List<String> advice() {
        return List.of(
                "删除不会马上腾出磁盘。它先写墓碑，gc_grace_seconds 之后下一次压缩才可能清掉。本示例是 86400 秒。",
                "能删整个分区就不要循环删单元格。分区删除一个墓碑，单元格删除是一堆墓碑。",
                "范围删除生成 range tombstone。读这个分区时，墓碑也要被读出来。",
                "绑定 null 等于写墓碑。可选列用 unset，让这条写入不碰那一列。",
                "表级 TTL 到期也是墓碑。时序表用 TWCS，过期窗口会整窗丢掉，比一条条删除便宜。",
                "gc_grace_seconds 不要在没做 repair 之前改成 0。副本没拿到删除时，旧数据会复活。单节点 RF 1 没有别的副本，演示才比较安全。",
                "双写的区域表不能按设备做时间范围删除，因为 device_id 排在 recorded_at 后面。区域表靠同样的 TTL 过期。单行删除因为主键齐全，两张表一起删。",
                "计数器不能 DELETE 某一格再当普通列用。计数器表和普通表分开。"
        );
    }
}
