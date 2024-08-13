# MySQL
```text
为了解决like存在的问题，还可以使用mysql提供的find_in_set(str, strlist)，sql可以这样写：
select * from task_assignment_personnel where deleted = false and find_in_set('202', task_handler) > 0 ;
如果查询条件存在多个，sql可以这样写：
select * from task_assignment_personnel where deleted = false and find_in_set('101', task_handler) or find_in_set('102', task_handler);
regexp是mysql提供的强大的正则匹配方式。上面的sql就可以改写成：
select * from task_assignment_personnel where task_handler regexp '(^|,)(101|102)(,|$)';
regexp是mysql提供的强大的正则匹配方式。当需要匹配多个值的时候可以，sql如下：
select * from task_assignment_personnel where CONCAT (',',task_handler,',') REGEXP ',(101|102),'
```
