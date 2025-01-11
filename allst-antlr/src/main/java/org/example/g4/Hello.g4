grammar Hello;      // 定义一个名为Hello的语法
r  : 'hello' ID ;    // 匹配一个关键字hello和一个紧随其后法人标识符
ID : [a-z]+ ;        // 匹配小写字母组成的标识符
WS : [\t\r\n]+ -> skip ; // 忽略空格、Tab、换行以及\r（Windows）
