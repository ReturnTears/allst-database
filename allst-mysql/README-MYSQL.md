# MySQL8.0
```
-- 查看是否是自动提交 1自动0非自动
SELECT @@autocommit;
DROP TABLE IF EXISTS person;
CREATE TABLE person(
	id INT PRIMARY KEY auto_increment,
	NAME VARCHAR(20)
);
INSERT INTO person VALUES (8, 'D'),(9, 'E'),(10, 'F'),(11, 'G');
-- 
SELECT 
    SUBSTRING_INDEX(SUBSTRING_INDEX(p.`NAME`,',',t.seq+1),',',-1) AS num 
FROM 
    person p JOIN ts t on t.seq < (LENGTH(p.`NAME`) - LENGTH(REPLACE(p.`name`,',',''))+1) order by p.id;


--
SELECT 
    SUBSTRING_INDEX(SUBSTRING_INDEX('7654,7698,7782,7788',',',help_topic_id+1),',',-1) AS num 
FROM 
    mysql.help_topic 
WHERE 
    help_topic_id < LENGTH('7654,7698,7782,7788')-LENGTH(REPLACE('7654,7698,7782,7788',',',''))+1

--
DROP TABLE IF EXISTS Td;
create table Td (
id varchar(10),
case_num varchar(10),
document_name varchar(200),
PRIMARY KEY (ID)
);
INSERT INTO td (id, case_num, document_name) VALUES ('1', 'A,G,C', 'A');
INSERT INTO td (id, case_num, document_name) VALUES ('2', 'A,F', 'B');
INSERT INTO td (id, case_num, document_name) VALUES ('3', 'F,W', 'C');
INSERT INTO td (id, case_num, document_name) VALUES ('4', 'A,W,A', 'D');
INSERT INTO td (id, case_num, document_name) VALUES ('5', 'A,A,A,AA,A', 'F');


DROP TABLE IF EXISTS Tc;
CREATE TABLE Tc (
id varchar(10),
case_num varchar(10),
CASE_NAME varchar(20),
STATUS varchar(10),
PRIMARY KEY (id)
);
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('1', 'A', 'A', '1');
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('2', 'G', 'G', '1');
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('3', 'C', 'C', '1');
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('4', 'W', 'W', '1');
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('5', 'F', 'F', '2');
INSERT INTO tc (id, case_num, CASE_NAME, STATUS) VALUES ('6', 'AA', 'G', '1');

create table Ts(
seq varchar(10)
);
insert into Ts values (1),(2),(3),(4),(5);

select splite_table.document_name,max_seq from
( select document_name, b.seq,
#a.case_num,
SUBSTRING_INDEX(SUBSTRING_INDEX(a.case_num,',',b.seq) ,',',-1) as case_num, (LENGTH(a.case_num) - LENGTH(replace(a.case_num,',','')) ) + 1 max_seq from Td a, Ts b
where seq<=LENGTH(a.case_num)-LENGTH(replace(a.case_num,',',''))+1 ORDER BY a.document_name ) splite_table, Tc b
where splite_table.case_num=b.case_num and b.status=1 GROUP BY splite_table.document_name having count(1)=splite_table.max_seq


-- 
DROP TABLE IF EXISTS ;
CREATE TABLE `test` (
    `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
    `kids` VARCHAR(50) NOT NULL DEFAULT '',
    `sign` INT(10) UNSIGNED NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=MyISAM;

-- 流程控制函数if
SELECT IF(10 < 5, 'No', 'Yes') result
-- java
switch () CASE 常量1: 语句1; break;
							 常量2: 语句2; break;
			...
								default: 语句n; break;
-- mysql
CASE CONDITION1 WHEN 常量1 THEN 语句1;
								WHEN 常量2 THEN 语句2;
			...
								ELSE 语句n;
								END

CASE WHEN CONDITION1 THEN 语句1
		 WHEN CONDITION2 THEN 语句2
...
		 ELSE 语句n
		 END
--
SELECT NOW();
SELECT MONTH('2019-06-01 12:00:01') AS `month`;
SELECT MONTHNAME('2019-06-01 12:00:01') MonthName;
-- 聚合函数 都会忽略null
-- 效率
-- myisam存储引擎下， count(*) 的效率高
-- INNODB存储引擎下， count(*)和count(1)的效率差不多， 比count(字段)要高一些
-- count(*)一般用作统计行数
-- 聚合函数一同查询的字段要求是group by后的字段
SUM()
AVG([DISTINCT] expr)
MAX(expr)
MIN(expr)
COUNT(expr)
-- 日期差
DATEDIFF(expr1,expr2)
-- 分组查询
group by ... HAVING...
-- 分组查询中的筛选条件分为两类
-- 							数据源					位置								关键字
-- 分组前筛选		原始表					group by子句的前面	WHERE
-- 分组后筛选		分组后的结果集	group by子句的后面	HAVING

-- 多个字段分组 字段用逗号隔开,没有顺序要求
-- 排序放在整个分组查询之后

# 连接查询(多表查询)
# 当查询的字段来自多个表，或者需要查询多个表时
/*
按照功能分类：
	sql92标准:
					多表等值连接的结果为多表的交际部分
					n表连接，至少需要n-1个连接条件
					多表的顺序没有要求
					一般需要为表起别名
	sql99标准:
	内链接：
					等值连接（查询出两张表的交集部分）
					非等值连接
							不使用=作为连接条件的查询，一般有between..and..
					自连接
							在同一张表中查询，使用不同的别名即可
	外连接：（外连接的查询结果为主表中的所有记录，如果从表中有和他匹配的，则显示匹配的值，如果从表中没有和他匹配的，则显示null）
		外连接查询的结果=内连接结果+主表中有而从表中没有的记录
					左外连接	LEFT JOIN 左边的表是主表
					右外连接	RIGHT JOIN 右边的表是主表
		左外和右外交换两个表的顺序，可以实现同样的效果
(MySQL不支持)全外连接 FULL OUTER JOIN : 全外连接 = 内链接的结果 + 表1中有但表2中没有的+表2中有但表1没有的
	交叉连接 cross：
					CROSS JOIN 实现的笛卡尔乘积
SELECT 查询列表
FROM 表1 别名 【连接类型:inner left right full 】
JOIN 表2 别名
ON 连接条件
【where 筛选条件】
【group by 分组】
【having 筛选条件】
【order by 排序列表】
*/
SELECT * FROM users;
SELECT * FROM user_roles;
# 加密函数
SELECT PASSWORD('xiaohu');
SELECT MD5('yangyang');


--
SELECT COUNT(*) FROM beamdb a WHERE a.beamNumber2 is NOT NULL;
SELECT COUNT(*) FROM beamdb a WHERE a.beamNumber1 is NOT NULL;
DELETE FROM beamdb_copy a WHERE a.beamNumber1 is NOT null;








https://www.bilibili.com/video/av49181542/?p=86

-- Wx
DROP TABLE IF EXISTS `wx_msg`;
CREATE TABLE `wx_msg` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` varchar(64) DEFAULT NULL,
  `to_user_name` varchar(64) DEFAULT NULL COMMENT '开发者微信号',
  `from_user_name` varchar(64) DEFAULT NULL COMMENT '发送方帐号(一个OpenID)',
  `create_time` varchar(32) DEFAULT NULL COMMENT '消息创建时间(整型)',
  `msg_id` varchar(32) DEFAULT NULL COMMENT '消息id，64位整型',
  `msg_type` varchar(32) DEFAULT NULL COMMENT '消息类型',
  `msg_source` int(11) DEFAULT NULL COMMENT '消息来源',
  `msg_key` varchar(64) DEFAULT '0' COMMENT '外键',
  `release1` varchar(32) DEFAULT NULL COMMENT '备用1',
  `release2` varchar(32) DEFAULT NULL COMMENT '备用2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 COMMENT='消息记录表';

DROP TABLE IF EXISTS `wx_msg_link`;
CREATE TABLE `wx_msg_link` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) DEFAULT NULL COMMENT '消息标题',
  `description` varchar(256) DEFAULT NULL COMMENT '消息描述',
  `url` varchar(512) DEFAULT NULL COMMENT '消息链接',
  `msg_id` varchar(64) DEFAULT NULL COMMENT '消息Id',
  `release1` varchar(32) DEFAULT NULL COMMENT '备用1',
  `release2` varchar(32) DEFAULT NULL COMMENT '备用2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 COMMENT='消息内容存放表(链接消息内容)';

DROP TABLE IF EXISTS `wx_msg_location`;
CREATE TABLE `wx_msg_location` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `location__x` double DEFAULT NULL COMMENT '地理位置维度',
  `location__y` double DEFAULT NULL COMMENT '地理位置经度',
  `scale` varchar(11) DEFAULT NULL COMMENT '地图缩放大小',
  `label` varchar(256) DEFAULT NULL COMMENT '地理位置信息',
  `msg_id` varchar(64) DEFAULT NULL COMMENT '消息Id',
  `release1` varchar(32) DEFAULT NULL COMMENT '备用1',
  `release2` varchar(32) DEFAULT NULL COMMENT '备用2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 COMMENT='消息内容存放表(地理位置消息)';

DROP TABLE IF EXISTS `wx_msg_media`;
CREATE TABLE `wx_msg_media` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pic_url` varchar(512) DEFAULT NULL COMMENT 'Image存放路径',
  `media_id` varchar(512) DEFAULT NULL COMMENT '消息媒体id',
  `format` varchar(32) DEFAULT '0' COMMENT '语音格式，如amr，speex等',
  `recognition` varchar(512) DEFAULT '0' COMMENT '语音转译后的文字内容',
  `thumb_media_id` varchar(512) DEFAULT '0' COMMENT '消息缩略图的媒体id',
  `msg_id` varchar(64) DEFAULT NULL COMMENT '消息id',
  `release1` varchar(32) DEFAULT NULL COMMENT '备用1',
  `release2` varchar(32) DEFAULT NULL COMMENT '备用2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 COMMENT='消息内容存放表(多媒体消息内容)';

DROP TABLE IF EXISTS `wx_msg_text`;
CREATE TABLE `wx_msg_text` (
  `id` int(32) NOT NULL AUTO_INCREMENT,
  `content` text COMMENT '消息内容(消息类型为文本类型)',
  `msg_id` varchar(64) DEFAULT NULL COMMENT '消息id',
  `release1` varchar(32) DEFAULT NULL COMMENT '备用1',
  `release2` varchar(32) DEFAULT NULL COMMENT '备用2',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 COMMENT='消息内容存放表(文本消息)';

DROP TABLE IF EXISTS `wx_user_info`;
CREATE TABLE `wx_user_info` (
  `id` int(32) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'UUID值',
  `subscribe` int(10) DEFAULT NULL COMMENT '用户是否订阅该公众号标识，值为0时，代表此用户没有关注该公众号，拉取不到其余信息。',
  `openid` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户的标识，对当前公众号唯一',
  `nickname` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户的昵称',
  `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户的性别，值为1时是男性，值为2时是女性，值为0时是未知',
  `language` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户的语言，简体中文为zh_CN',
  `city` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户所在城市',
  `province` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户所在省份',
  `country` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户所在国家',
  `headimgurl` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户头像，最后一个数值代表正方形头像大小（有0、46、64、96、132数值可选，0代表640*640正方形头像），用户没有头像时该项为空。若用户更换头像，原有头像URL将失效。',
  `subscribe_time` bigint(32) DEFAULT NULL COMMENT '用户关注时间，为时间戳。如果用户曾多次关注，则取最后关注时间',
  `unionid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '只有在用户将公众号绑定到微信开放平台帐号后，才会出现该字段。',
  `remark` text CHARACTER SET utf8 COLLATE utf8_general_ci COMMENT '公众号运营者对粉丝的备注，公众号运营者可在微信公众平台用户管理界面对粉丝添加备注',
  `groupid` smallint(10) DEFAULT NULL COMMENT '用户所在的分组ID（兼容旧的用户分组接口）',
  `tagid_list` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '用户被打上的标签ID列表',
  `subscribe_scene` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '返回用户关注的渠道来源，ADD_SCENE_SEARCH 公众号搜索，ADD_SCENE_ACCOUNT_MIGRATION 公众号迁移，ADD_SCENE_PROFILE_CARD 名片分享，ADD_SCENE_QR_CODE 扫描二维码，ADD_SCENEPROFILE LINK 图文页内名称点击，ADD_SCENE_PROFILE_ITEM 图文页右上角菜单，ADD_SCENE_PAID 支付后关注，ADD_SCENE_OTHERS 其他',
  `qr_scene` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '二维码扫码场景（开发者自定义）',
  `qr_scene_str` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT '二维码扫码场景描述（开发者自定义）',
  `isdelete` int(1) DEFAULT '0' COMMENT '是否删除',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
  `release1` varchar(32) DEFAULT NULL,
  `release2` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 COMMENT ' 用户信息表';


-- 微服务用

-- 数据库1:cloudDBOne
DROP DATABASE IF EXISTS cloudDBThree;
CREATE DATABASE cloudDBThree CHARACTER SET UTF8;
USE cloudDBThree;
CREATE TABLE dept(
 deptno BIGINT NOT NULL PRIMARY KEY auto_increment,
 dname VARCHAR(60),
 db_source VARCHAR(60)
);
INSERT INTO dept (dname, db_source) VALUES ('开发部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('人事部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('财务部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('市场部', DATABASE());
INSERT INTO dept (dname, db_source) VALUES ('运维部', DATABASE());

SELECT * FROM dept;

-- ----------------------------------存储过程------------------------------------
CALL youb();
SELECT hans(2);
SELECT username FROM users WHERE id = 1;

-- 触发器(通常应用于审计的目的,业务数据性完整的目的)
-- 触发器是表的属性
-- 创建触发器
CREATE TRIGGER tr_users_insert AFTER INSERT ON users
FOR EACH ROW
BEGIN
	INSERT INTO log_icecoldmonitor(`id`,`level`,`time`,`location`,`note`)
	VALUES(new.id,new.username,NOW(),'CQ','insert'); -- 插入/更新/删除之前是new, 插入/更新/删除之后是old
END
###
#	创建触发器使用create TRIGGER 触发器名称
# 什么时候触发? AFTER INSERT ON users, 除了after还有before是在对表操作之前(before)对表操作之后(after)触发动作的
# 对什么操作事件触发? after INSERT ON users, 操作事件包括insert,update,delete
# 对什么表触发? after insert on users
# 影响的范围 for each ROW (每一行)
#
# 触发器: 与函数,存储过程一样,触发器是一种对象,他能根据对表的操作事件,触发一些动作, 这些动作可以是insert,update,delete 等修改操作
###
INSERT INTO users(`id`,`username`,`password`) VALUES(14, 'yangtingting', 'tingting123');
-- 创建触发器
CREATE TRIGGER tr_users_delete BEFORE DELETE ON users
FOR EACH ROW
BEGIN
	INSERT INTO log_icecoldmonitor(`id`,`level`,`time`,`location`,`note`)
	VALUES(old.id,old.username,NOW(),'China','delete action');
END
--
DELETE FROM users WHERE username = 'xiaohu';

-- 高级特性

-- MySQL函数
-- CHAR_LENGTH(str)返回值为字符串str所包含的字符个数。一个多字节字符算作一个单字符。
SELECT CHAR_LENGTH('date'), CHAR_LENGTH('egg');
SELECT CHAR_LENGTH('日期'), CHAR_LENGTH('鸡蛋');
SELECT LENGTH('日期'), LENGTH('鸡蛋');	-- 一个汉字3个字节,一个数字或字母1个字节
SELECT LENGTH('date'), LENGTH('egg');
SELECT CONCAT('mysql','5.7.19'), CONCAT('mysql',NULL,'8.0');
SELECT CONCAT_WS('-','mysql','5.7.19'), CONCAT_WS(NULL,'mysql','**','8.0');
SELECT INSERT('quests',2,4,'who') as col1,insert('quest',-1,4,'who') as col2, insert('quest',3,10,'who') as col3;
SELECT LOWER('MySQL'), LCASE('MySQL');
SELECT UPPER('MySQL'), UCASE('MySQL');
SELECT LEFT('MySQL', 3), RIGHT('MySQL',3);
SELECT LPAD('hello',4,'H'), LPAD('hello',7,'H');
SELECT RPAD('hello',4,'H'), RPAD('hello',7,'H');
SELECT LTRIM(' MySQL '), RTRIM(' MySQL '), TRIM(' MySQL '), TRIM('8' FROM '8MySQL8.08');
SELECT REPEAT('MySQL', 3);
SELECT SPACE(3),REPLACE('xxx.mysql.com', 'x', 'w');
SELECT STRCMP('MySQL','Mysql8');
SELECT SUBSTRING('MySQL8.0',5),SUBSTRING('MySQL8.0',3,5),SUBSTRING('MySQL',-3),SUBSTRING('MySQL',-3, 3);
SELECT MID('MySQL8.0',5),MID('MySQL8.0',3,5),MID('MySQL',-3),MID('MySQL',-3, 3);
SELECT SUBSTRING('MySQL', 1,0), MID('MySQL', 1, -1);	-- 如果对len使用的是一个小于1的值，则结果始终为空字符串
SELECT LOCATE('sql','MySQL'),POSITION('sql' IN 'MySQL'),INSTR('MySQL','sql');
SELECT REVERSE('MySQL');
SELECT ELT(3,'MySQL1','MySQL2','MySQL3','MySQL4');
SELECT FIELD('MySQL','MySQL1','MySQL2','MySQL3','MySQL');
SELECT FIND_IN_SET('MySQL','Hi,This,is,MySQL');
SELECT MAKE_SET(1,'a','b','c');
SELECT MAKE_SET(5,'a','b','c','d');
SELECT MAKE_SET(1 | 4,'a','b','c','d');











```