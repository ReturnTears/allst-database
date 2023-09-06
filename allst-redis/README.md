# Redis源代码编译、安装与配置
```text

编译Redis需要使用GCC（一套GNU的编译器集）和Make工具（GNU的项目生成工具），因此在编译Redis之前需要先安装GCC和Make。

由于GCC和Make是Linux平台的编译器与生成工具，Windows平台默认并不包含这两个工具。但幸好有MSYS2(Minimal SYStem 2)工具，MSYS2工具的主要目的就是为Windows软件提供构建环境。

搭建基于MSYS2的GCC和Make编译环境按如下步骤进行：
1、登录MSYS2官网的下载页面：http：//repo.msys2.org/distrib/
2、下载msys2-base-x86_64-yyyyMMdd.tar.xz文件
```