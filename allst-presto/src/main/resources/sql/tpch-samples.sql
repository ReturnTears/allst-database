SELECT 1 AS ok;

SHOW CATALOGS;

SHOW SCHEMAS FROM tpch;

SHOW TABLES FROM tpch.tiny;

SELECT nationkey, name, regionkey
FROM tpch.tiny.nation
ORDER BY nationkey;

SELECT orderstatus, count(*) AS order_count
FROM tpch.tiny.orders
GROUP BY orderstatus
ORDER BY orderstatus;
