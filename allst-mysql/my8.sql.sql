# 创建表
CREATE TABLE tb_emp1(
id INT(11),
name VARCHAR(25),
deptId INT(11),
salary FLOAT
);
# 在定义列的同时指定主键
CREATE TABLE tb_emp2(
id INT(11) PRIMARY KEY,
name VARCHAR(25),
deptId INT(11),
salary FLOAT
);
CREATE TABLE tb_emp3(
id INT(11),
name VARCHAR(25),
deptId INT(11),
salary FLOAT,
PRIMARY KEY(id)
);
# 主键由多个字段联合组成
CREATE TABLE tb_emp4 (
id INT(11),
name VARCHAR(25),
deptId INT(11),
salary FLOAT,
PRIMARY KEY(name, deptId)
);
# 使用外键约束
CREATE TABLE tb_dept1 (
id INT(11) PRIMARY KEY,
name VARCHAR(25) NOT NULL,
location VARCHAR(50)
);
CREATE TABLE tb_emp5 (
id INT(11) PRIMARY KEY,
name VARCHAR(25),
deptId INT(11),
salary FLOAT,
CONSTRAINT fk_emp_dept1 FOREIGN KEY(deptId) REFERENCES tb_dept1(id)
);
# 使用唯一性约束
CREATE TABLE tb_dept2 (
id INT(11) PRIMARY KEY,
name VARCHAR(25) UNIQUE,
location VARCHAR(50)
);
CREATE TABLE tb_dept3 (
id INT(11) PRIMARY KEY,
name VARCHAR(25),
location VARCHAR(50),
CONSTRAINT STH UNIQUE(name)
);
# 设置表的属性值自动增加
CREATE TABLE tb_emp6 (
id INT(11) PRIMARY KEY auto_increment,
name VARCHAR(25),
deptId INT(11),
salary FLOAT
);
# 这里使用INSERT声明向表中插入记录的方法，并不是SQL的标准语法，这种语法不一定被其他的数据库支持，只能在MySQL中使用。
INSERT INTO tb_emp6 (name, salary) VALUES ('kang', 10000),('yang', 20000), ('xiao', 1800);
# 查看表结构
DESC tb_emp6;
# 查看表详细结构语句
SHOW CREATE TABLE tb_emp6;
SHOW CREATE TABLE tb_emp6 \G
# 修改表名
ALTER TABLE tb_dept3 RENAME tb_deptment3;
# 修改字段的数据类型
ALTER TABLE tb_dept1 MODIFY NAME VARCHAR(30);
# 修改字段名
ALTER TABLE tb_dept1 CHANGE location loc VARCHAR(50);
# 添加字段
ALTER TABLE tb_dept1 ADD managerId INT(10);
# 添加有完整性约束条件的字段
ALTER TABLE tb_dept1 ADD column1 VARCHAR(12) NOT NULL;
# 在表的第一列添加一个字段
ALTER TABLE tb_dept1 ADD column2 int(11) FIRST;
# 在表的指定列之后添加一个字段
ALTER TABLE tb_dept1 ADD column3 int(11) AFTER name;
# 删除字段
ALTER TABLE tb_dept1 DROP column2;
# 修改字段的排列位置,修改字段为表的第一个字段
ALTER TABLE tb_dept1 MODIFY column1 VARCHAR(12) FIRST;
# 修改字段到表的指定列之后
ALTER TABLE tb_dept1 MODIFY column1 VARCHAR(12) AFTER loc;
# 修改数据表引擎
ALTER TABLE tb_deptment3 ENGINE = myisam;
# 删除表的外键约束
ALTER TABLE tb_emp5 DROP FOREIGN KEY fk_emp_dept1;
# 删除数据表
DROP TABLE IF EXISTS tb_dept2;
# 删除被其他表关联的主表
CREATE TABLE tb_dept2 (
id INT(11) PRIMARY KEY,
name VARCHAR(22),
location VARCHAR(50)
);
# 创建携带外键的表
CREATE TABLE tb_emp(
id INT(11) PRIMARY KEY,
name VARCHAR(25),
deptId INT(11),
salary FLOAT,
CONSTRAINT fk_emp_dept FOREIGN KEY(deptId) REFERENCES tb_dept2(id)
);
# 删除有外键的表
DROP TABLE tb_dept2; -- 在存在外键约束时，主表不能被直接删除
# 解除关联子表tb_emp的外键约束
ALTER TABLE tb_emp DROP FOREIGN KEY fk_emp_dept;
# 查看数据库的默认编码
SHOW VARIABLES LIKE 'character_set_database';
# 使用异或运算符“XOR”进行逻辑判断, a XOR b的计算等同于(a AND (NOT b))或者((NOT a)AND b)。
SELECT 1 XOR 2 as col_1, 1 XOR 0 as col_2  FROM DUAL;




# 备注备注备注备注备注备注备注
1.今天

SELECT * FROM 表名 WHERE TO_DAYS(时间字段名) = TO_DAYS(NOW());
2.昨天

SELECT * FROM 表名 WHERE TO_DAYS(NOW()) - TO_DAYS(时间字段名) <= 1;
3.本周
AND YEARWEEK(DATE_FORMAT(safetyPlan.product_time,'%Y-%m-%d')) = YEARWEEK(NOW())
SELECT * FROM 表名 WHERE YEARWEEK(DATE_FORMAT(时间字段名,'%Y-%m-%d')) = YEARWEEK(NOW());
4.上周

SELECT * FROM 表名 WHERE YEARWEEK(DATE_FORMAT(时间字段名,'%Y-%m-%d')) = YEARWEEK(NOW())-1;
5.近7天

SELECT * FROM 表名 WHERE DATE_SUB(CURDATE(), INTERVAL 7 DAY) <= DATE(时间字段名);
6.近30天

SELECT * FROM 表名 WHERE DATE_SUB(CURDATE(), INTERVAL 30 DAY) <= DATE(时间字段名);
7.本月

SELECT * FROM 表名 WHERE DATE_FORMAT(时间字段名,'%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m');
8.上月

SELECT * FROM 表名 WHERE PERIOD_DIFF(DATE_FORMAT(NOW(),'%Y%m'),DATE_FORMAT(时间字段名,'%Y%m')) = 1;

SELECT * FROM 表名 WHERE DATE_FORMAT(时间字段名,'%Y%m') = DATE_FORMAT(CURDATE(),'%Y%m') ; 

SELECT * FROM 表名 WHERE WEEKOFYEAR(FROM_UNIXTIME(时间字段名,'%y-%m-%d')) = WEEKOFYEAR(NOW()); 

SELECT * FROM 表名 WHERE MONTH(FROM_UNIXTIME(时间字段名,'%y-%m-%d')) = MONTH(NOW()); 

SELECT * FROM 表名 WHERE YEAR(FROM_UNIXTIME(时间字段名,'%y-%m-%d')) = YEAR(NOW()) AND MONTH(FROM_UNIXTIME(时间字段名,'%y-%m-%d')) = MONTH(NOW())；

9.近6个月

SELECT * FROM 表名 WHERE 时间字段名 BETWEEN DATE_SUB(NOW(),INTERVAL 6 MONTH) AND NOW();
10.本季度

SELECT * FROM 表名 WHERE QUARTER(时间字段名) = QUARTER(NOW());
11.上季度

SELECT * FROM 表名 WHERE QUARTER(时间字段名) = QUARTER(DATE_SUB(NOW(),INTERVAL 1 QUARTER));
12.本年

SELECT * FROM 表名 WHERE YEAR(时间字段名)=YEAR(NOW());
13.去年

SELECT * FROM 表名 WHERE YEAR(时间字段名) = YEAR(DATE_SUB(NOW(),INTERVAL 1 YEAR));

# 👇 ×
SELECT * INTO tb_emp_bak FROM tb_emp WHERE 1=2;
# 👇 √
CREATE TABLE tb_emp_bak AS SELECT * FROM tb_emp WHERE 1=2;
# 👇 √
SELECT VERSION();
# 
SELECT * FROM tb_emp;
SELECT top 1 FROM tb_emp;