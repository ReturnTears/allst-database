begin
    DBMS_STATS.GATHER_TABLE_STATS(
            ownname => 'SCOTT',
            tabname => 'TB_ACCOUNT',
            estimate_percent => 100,
            method_opt => 'for all columns size 1',
            no_invalidate => FALSE,
            degree => 1,
            cascade => TRUE
        );
end;
select a.column_name,
       b.num_rows,
       a.num_distinct                              Cardinality,
       round(a.num_distinct / b.num_rows * 100, 2) selectivity,
       a.histogram,
       a.num_buckets
from dba_tab_col_statistics a,
     dba_tables b
where a.owner = b.owner
  and a.table_name = b.table_name
  and a.owner = 'SCOTT'
  and a.table_name = 'TB_ACCOUNT';
select *
from V_$SQL_PLAN;
begin
    dbms_stats.flush_database_monitoring_info;
end;
select r.name owner,
       o.name table_name,
       c.name column_name,
       equality_preds,    ---等值过滤
       equijoin_preds,    ---等值JOIN 比如where a.id=b.id
       nonequijoin_preds, ----不等JOIN
       range_preds,       ----范围过滤次数 > >= < <= between and
       like_preds,        ----LIKE过滤
       null_preds,        ----NULL 过滤
    timestamp
from sys.col_usage$ u,
    sys.obj$ o,
    sys.col$ c,
    sys.user$ r
where o.obj# = u.obj#
  and c.obj# = u.obj#
  and c.col# = u.intcol#
  and r.name = 'SCOTT'
  and o.name = 'TB_ACCOUNT';
-- 对id列收集直方图
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS(
            ownname => 'SCOTT',
            tabname => 'TB_ACCOUNT',
            estimate_percent => 100,
            method_opt => 'for columns id size skewonly',
            no_invalidate => FALSE,
            degree => 1,
            cascade => TRUE
        );
END;
