select * from dept;

select case when comm is not null then comm else 0 end as comma from emp ;

-- 查找部门编号为10的所有员工
select * from emp where deptno = 10 ;
-- 找出部门编号为10的所有员工、有业务提成的所有员工以及部门编号是20且工资低于2000美元的所有员工
select * from emp where deptno = 10 or comm is not null or sal <= 2000 and deptno=20 ;
-- 只查看员工的名字、部门编号和工资
select ename, deptno, sal from emp ;
-- 创建有意义的列名
select ename, deptno, sal as salary from emp ;
-- 在WHERE子句中引用别名列,查询包装为一个内嵌视图
select * from (select sal as salary, comm as commission from emp ) x where salary < 5000 ;
-- 串联多列的值
select ename || ' WORKS AS A ' || job as msg from emp where deptno = 10 ;
-- 在SELECT语句中针对查询结果值执行IF-ELSE操作
select ename,sal, case when sal <= 2000 then 'UNDERPAID' when sal >= 4000 then 'OVERPAID' else 'OK' end as status from emp ;
-- 限定返回行数, 从1开始，逐渐增大
select * from emp where rownum <= 5 ;
-- 随机返回若干行记录
select * from (select ename, job from emp order by dbms_random.value()) where rownum <= 5 ;
-- 要判断一个值是否为Null，必须使用IS Null
select * from emp where comm is null ;
-- 使用COALESCE函数将Null值替代为实际值
select e.*, coalesce(e.comm,0) as comma from EMP e;
-- 返回匹配某个特定字符串或模式的行
select  ename, job from emp where deptno in (10, 20) ;
-- 编号为10和20的两个部门中找到名字中含有字母I或职位以ER结尾的人
select ename, job from emp where deptno in (10, 20) and (ename like '%I%' or job like '%ER') ;




