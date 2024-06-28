《GraphQL学习指南》

第一十一章 GraphQL是什么

◆ GraphQL（http://www.graphql.cn）是一种API查询语言，也是一种用于实现数据查询的运行时（runtime）。GraphQL服务与传输方式没有关系，但通常基于HTTP协议

◆ 每当我们向GraphQL服务器请求数据时，它会自动通过类型检测系统进行验证。每个GraphQL服务器都会在其GraphQL Schema中定义数据类型。可以将类型检测系统看作是API数据的架构，并自定义数据对象的内容


第一十二章 GraphQL规范

◆ GraphQL是客户端和服务器之间通信的规范


第一十六章 远程过程调用

◆ 远程过程调用（remote procedure call，RPC）发明于20世纪60年代。远程过程调用由客户端发起，向远程计算机发起请求以执行某些操作。接下来，远程计算机则向客户端发送响应


第一十七章 简单对象访问协议

◆ 20世纪90年代末，微软提出了简单对象访问协议（Simple Object Access Protocol，SOAP）。SOAP使用XML将消息编码并通过HTTP传输，集成了类型检测系统，并引入了面向资源的数据调用概念。SOAP所提供的结果具有可预测性，可惜的是由于其实现过于复杂而导致失败的概率也很高


第一十八章 表述性状态传递（REST）

◆ REST描述了一种面向资源的架构，用户可以通过执行GET、POST、PUT和DELETE等操作来浏览Web资源

