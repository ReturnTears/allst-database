package com.allst.boot.ops;

import com.allst.boot.cassandra.common.Identifiers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BackupCommands {

    private BackupCommands() {
    }

    public static Map<String, Object> plan(String keyspace, String tag) {
        String ks = Identifiers.requireKeyspace(keyspace);
        String snapshot = Identifiers.requireSnapshot(tag);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tag", snapshot);
        body.put("commands", List.of(
                "nodetool snapshot " + ks + " -t " + snapshot,
                "nodetool listsnapshots",
                "nodetool clearsnapshot -t " + snapshot + " " + ks,
                "nodetool refresh " + ks + " sensor_reading"
        ));
        body.put("notes", List.of(
                "snapshot 是 SSTable 的硬链接，还在同一块磁盘上。拷到别的机器或对象存储之后，才算完成备份。",
                "不要直接复制正在写的表目录。先 snapshot，再复制 data/" + ks + "/*/snapshots/" + snapshot + "。",
                "增量备份把 incremental_backups 设为 true，之后的 flush 会在 backups/ 里留硬链接。它代替不了第一次全量快照。",
                "恢复到原节点：nodetool drain，停进程，把 SSTable 放回表目录，启动后执行 refresh。",
                "恢复到别的节点用 sstableloader，把某个表的快照目录传给它。不要拷贝 system keyspace。",
                "commitlog 不是备份。CDC 也不是备份。逻辑导出只包含一个分区，不能代替 snapshot。",
                "生产可以看 Medusa：https://github.com/thelastpickle/cassandra-medusa 。这个接口不在应用进程里执行 nodetool。"
        ));
        return body;
    }
}
