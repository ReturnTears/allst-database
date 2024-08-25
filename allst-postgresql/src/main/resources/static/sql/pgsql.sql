-- su - postgres
-- 查询pg_settings系统表
SELECT name, setting FROM pg_settings where name = 'jit';
SELECT current_setting('jit');

select current_user;
select * from "user";
select * from "table_1";
-- 给测试表test_1插入500万数据
INSERT INTO table_1(id, name) SELECT n, n || '_francs' FROM generate_series(1,5000000) n;

SELECT pid, usename, datname, query, client_addr
FROM pg_stat_activity
WHERE pid <> pg_backend_pid() AND state='active' ORDER BY query;
-- 定义integer类型的表
create table test_integer (id1 integer, id2 int4);
create table test_serial (id serial, flag text);
INSERT INTO test_serial(flag) VALUES ('a');
INSERT INTO test_serial(flag) VALUES ('b');
INSERT INTO test_serial(flag) VALUES ('c');
SELECT * FROM test_serial ;
-- 支持四则运算
SELECT 1 + 2 as col1,2 * 3 col2, 4 / 2 col3, 8 % 3 col4;
-- 取余
select mod(8, 3) as res;
-- 四舍五入
select round(10.2), round(10.9) ;
-- 小于或等于给出参数的最小整数
select ceil(3.6), ceil(-3.6) ;
-- 小于或等于给出参数的最大整数
select floor(3.6), floor(-3.6) ;
-- 字符类型
drop table test_char;
create table test_char(col1 varchar(4), col2 character(4)) ;
insert into test_char(col1, col2) values ('a', 'b') ;
select char_length(col1), char_length(col2) from test_char ;
select octet_length(col1), octet_length(col2) from test_char ;
-- 字符串中的字符数
select char_length('abcd'), octet_length('abcd') ;
-- 字符在字符串的位置
select position('a' in 'abcd') ;
-- 提取字符串中的子串
select substring('francs' from 3 for 4) ;
-- 拆分字符串
select split_part('abc@def1@nb', '@', 2) ;
-- 时间、日期类型
select now() ;
SELECT now()::timestamp without time zone ;
SELECT now()::date ;
SELECT now()::timestamp with time zone ;
SELECT now()::time with time zone ;
SELECT now(), now() + interval'1 day'  ;
SELECT now(), now()::timestamp(0)  ;
-- 时间/日期类型操作符
-- 日期相加
select date '2023-07-01' + interval '1 days' ;
-- 日期相减
select date '2023-07-20' - interval '1 hour' ;
-- 日期相乘
select 100 * interval '1 second' ;
-- 日期相除
select interval '1 hour' / double precision '3'  as result;
-- 时间/日期类型常用函数
select current_date, current_time ;
select extract(year from now()) as result ;
select extract(month from now()) as mon, extract(day from now()) as day ;
select extract(hour from now()) as hour, extract(minute from now()) as min ;
select extract(second from now()) as second ;
select extract(week from now()) as week ;
select extract(doy from now()) as day ;
--
create table test_boolean(cola boolean, colb boolean) ;
insert into test_boolean (cola, colb) values ('true', 'false') ;
insert into test_boolean (cola, colb) values ('t', 'f') ;
insert into test_boolean (cola, colb) values ('TRUE', 'FALSE') ;
insert into test_boolean (cola, colb) values ('yes', 'no') ;
insert into test_boolean (cola, colb) values ('y', 'n') ;
insert into test_boolean (cola, colb) values ('1', '0') ;
insert into test_boolean (cola, colb) values (null, null) ;
select * from test_boolean ;
-- 网络地址类型列表:inet和cidr类型都会对数据合法性进行检查，如果数据不合法会报错
select '192.168.2.100'::inet as result;
select '192.168.1.100'::cidr as result;
select '192.168.2.100/32'::inet as result;
select '192.168.0.0/16'::inet as result;
-- 网络地址函数
select host(cidr '192.168.1.0/24') as result ;
select text(cidr '192.168.1.0/24') as result ;
select netmask(cidr '192.168.1.0/24') as result ;
-- 数组类型
create table test_array1 (
                             id integer,
                             array_i integer[],
                             array_t text[]
);
insert into test_array1(id, array_i, array_t) values (1, '{1,2,3}', '{"a","b","c"}');
insert into test_array1(id, array_i, array_t) values (2, array[4,5,6], array['d','e','f']);
select * from test_array1 ;
delete from test_array1 where id = 1;
-- 数据下标从1开始：1-n,n表示数组长度
select array_i[1], array_t[3] from test_array1 where id = 1;
select array_append(array [1,2,3,4], 5) as result;
select array[1,2,3,4,3,2], array_remove(array[1,2,3,4,3,2], 2);
update test_array1 set array_i[3] = 4 where id = 1 ;
update test_array1 set array_t = array['g','h','i'] where id  = 1 ;
-- 数组操作符
select array_ndims(array[1,2]) ;
select array_length(array[1,2], 1) ;
select array_position(array['a', 'b', 'c', 'd'], 'd') as result;
select array_replace(array[1,2,3,4],3,30) as result;
select array_to_string(array[1,2,null,3], ',', '10') as result ;
-- 类型范围
select int4range(1,5) as result;
select daterange('2020-01-01', '2023-12-31') as result;
select int4range(1,5, '[]') as result;
select int4range(4, 7) @> 4 as result;
select int4range(4, 7) @> int4range(4, 6) as result ;
select int4range(4, 7) = int4range(4, 6, '[]') as result ;
select lower(int4range(1,10)) as result ;
select upper(int4range(1,10)) as result ;
select isempty(int4range(1,10)) as result ;
-- 给范围创建索引
create index idx_ip_address_range on ip_address using gist(ip_range) ;

--  json类型
select '{"a":1, "b":2}'::json ;
create table test_json1 (id serial primary key , name json) ;
insert into test_json1(name) values ('{"col1":1, "col2":"francs", "col3":"male"}');
insert into test_json1(name) values ('{"col1":2, "col2":"fp", "col3":"female"}');
select * from test_json1 ;
select name -> 'col2' from test_json1 where id = 1;
select name ->> 'col2' from test_json1 where id = 1;
select '{"bar":"baz","balance":7.77,    "active":false}'::jsonb ;
select '{"bar":"baz","balance":7.77,  "active":false}'::json ;
SELECT ' {"id":1, "name":"francs", "remark":"a good guy! ", "name":"test"}'::jsonb;
select '{"a":1, "b":2}'::jsonb ? 'a' ;
select '{"a":1, "b":2}'::jsonb - 'a' ;
select * from json_each('{"a":"foo","b":"bar"}') ;
select * from json_each_text('{"a":"foo","b":"bar"}') ;
create table test_copy (id serial primary key , name text) ;
insert into test_copy (name) values ('a') ;
select * from test_copy where id = 1;
select row_to_json(test_copy) from test_copy where id = 1 ;
select * from  json_object_keys('{"a":"foo","b":"bar"}') ;
select '{"name":"francs", "age":"31"}'::jsonb || '{"sex":"male"}'::jsonb ;
select '{"name":"james", "email":"james@localhost"}'::jsonb - 'email' ;
select '["red", "green", "blue"]'::jsonb -0;
select '{"name":"james", "contract": {"phone":"01234  567890","fax":"01987543210"}}'::jsonb #- '{contract, fax}'::text[] ;
select '{"name":"james", "aliases": ["Jamie","The Jamester","J Man"]}'::jsonb #- '{aliases, 1}'::text[] ;
select '{"name":"francs", "age": 31}'::jsonb || '{"age": 32}'::jsonb ;
select jsonb_set('{"name":"francs", "age": 31}'::jsonb, '{"age"}', '32'::jsonb, false) as result ;
select jsonb_set('{"name":"francs", "age": 31}'::jsonb, '{"sex"}', '"male"'::jsonb, true) as result ;

-- 数据类型转换
select cast(varchar'123' as text) as result ;
select cast(varchar'123' as int4) as result ;
select 1::int4, 3/2::numeric ;
SELECT oid, relname FROM pg_class WHERE relname='test_json1';
SELECT attname FROM pg_attribute WHERE attrelid='24644' AND attnum >0;
SELECT attname FROM pg_attribute WHERE attrelid='test_json1'::regclass AND attnum >0;

-- 高级特性
with t as (select generate_series(1,3))
select * from t;
/*WITH regional_sales AS (SELECT region, SUM(amount) AS total_sales FROM orders GROUP BY region),
     top_regions AS (SELECT region FROM regional_sales WHERE total_sales > (SELECT SUM(total_sales)/10 FROM regional_sales))
SELECT region, product, SUM(quantity) AS product_units, SUM(amount) AS product_sales
FROM orders
WHERE region IN (SELECT region FROM top_regions)
GROUP BY region, product;*/
WITH recursive t (x) as ( SELECT 1 UNION SELECT x + 1 FROM t WHERE x < 5)
SELECT sum(x) FROM t;

CREATE TABLE test_area(id int4, name varchar(32), parentId int4);

INSERT INTO test_area VALUES (1, '中国'   ,0);
INSERT INTO test_area VALUES (2, '辽宁'   ,1);
INSERT INTO test_area VALUES (3, '山东'   ,1);
INSERT INTO test_area VALUES (4, '沈阳'   ,2);
INSERT INTO test_area VALUES (5, '大连'   ,2);
INSERT INTO test_area VALUES (6, '济南'   ,3);
INSERT INTO test_area VALUES (7, '和平区' ,4);
INSERT INTO test_area VALUES (8, '沈河区' ,4);
WITH RECURSIVE r AS (
    SELECT * FROM test_area WHERE id = 7
    UNION   ALL
    SELECT test_area.* FROM test_area, r WHERE test_area.id = r.parentId
)
SELECT * FROM r ORDER BY id;
WITH RECURSIVE t AS (
    SELECT * FROM test_area WHERE id = 7
    UNION   ALL
    SELECT test_area.* FROM test_area, t WHERE test_area.id = t.parentId
)
select string_agg(tt.name, '') from ( SELECT name FROM t ORDER BY id ) as "tt";
WITH RECURSIVE r AS (
    SELECT * FROM test_area WHERE id = 4
    UNION   ALL
    SELECT test_area.* FROM test_area, r WHERE test_area.parentId = r.id
)
SELECT * FROM r ORDER BY id;
-- 批量插入
create table tbl_batch(id int4, info text) ;
INSERT INTO tbl_batch(id, info) VALUES (1, 'a'), (2, 'b'), (3, 'c');
CREATE TABLE tbl_batch4(id int4, info text, create_time timestamp(6) with time zone default clock_timestamp());
INSERT INTO tbl_batch4(id, info) SELECT n, n||'_batch4' FROM generate_series(1,100000) n;
-- 导出数据
copy tbl_batch4 to 'E:\TestData\PostgreSQL\tbl_batch4.txt' ;
truncate table tbl_batch4 ;
select * from tbl_batch4 ;
-- 导入数据
copy tbl_batch4 from 'E:\TestData\PostgreSQL\tbl_batch4.txt' ;
--
create table test_rl(id serial, flag char(1));
insert into test_rl (flag) values ('a') returning *;
insert into test_rl (flag) values ('b') returning id;
update test_rl set flag = 'p' where id = 1 returning *;
delete from test_rl where id = 2 returning *;
CREATE TABLE user_logins(user_name text primary key,login_cnt int4,last_login_time timestamp(0) without time zone);
INSERT INTO user_logins(user_name, login_cnt) VALUES ('francs',1);
INSERT INTO user_logins(user_name, login_cnt) VALUES ('matiler',1), ('francs',1) ON CONFLICT(user_name) DO UPDATE SET login_cnt=user_logins.login_cnt+EXCLUDED.login_cnt, last_login_time=now();
select * from user_logins;
INSERT INTO user_logins(user_name, login_cnt) VALUES ('tutu',1), ('francs',1) ON CONFLICT(user_name) DO NOTHING ;

-- 创建表
CREATE TABLE user_info(id int8, name varchar(32), age int);
select * from user_info a ;


