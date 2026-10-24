-- 官方镜像自带 tpch。tiny 数据量小，适合联调；sf1 与早期 CLI 笔记一致。
SELECT 1 AS ok;

SHOW CATALOGS;

SHOW SCHEMAS FROM tpch;

SHOW TABLES FROM tpch.tiny;

SELECT nationkey, name, regionkey
FROM tpch.tiny.nation
ORDER BY nationkey;

SELECT orderstatus, count(*) AS order_count
FROM tpch.sf1.orders
GROUP BY orderstatus
ORDER BY orderstatus;

SELECT node_id, http_uri, node_version, coordinator, state
FROM system.runtime.nodes;
