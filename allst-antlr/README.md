# Antlr
```text
官方地址：https://www.antlr.org/

http://localhost:8000/antlr/?sql=select * from dual

安装antlr4
curl https://www.antlr.org/download/antlr-4.11.1-complete.jar

检查antlr是否安装正确
第一种是通过不带参数的ANTLR命令行工具
第二种是通过java-jar来直接运行ANTLR的jar包或者直接调用org.antlr.v4.Tool类。
java -jar antlr-4.11.1-complete.jar
ANTLR Parser Generator  Version 4.11.1
 -o ___              specify output directory where all output is generated       
 -lib ___            specify location of grammars, tokens files
 -atn                generate rule augmented transition network diagrams
 -encoding ___       specify grammar file encoding; e.g., euc-jp                  
 -message-format ___ specify output style for messages in antlr, gnu, vs2005      
 -long-messages      show exception details when available for errors and warnings
 -listener           generate parse tree listener (default)
 -no-listener        don't generate parse tree listener                           
 -no-visitor         don't generate parse tree visitor (default)
 -package ___        specify a package/namespace for the generated code
 -depend             generate file dependencies
 -D<option>=value    set/override a grammar-level option
 -Werror             treat warnings as errors
 -XdbgST             launch StringTemplate visualizer on generated code
 -XdbgSTWait         wait for STViz to close before continuing
 -Xforce-atn         use the ATN simulator for all predictions
 -Xlog               dump lots of logging info to antlr-timestamp.log
 -Xexact-output-dir  all output goes into -o dir regardless of paths/package

java org.antlr.v4.Tool
ANTLR Parser Generator  Version 4.11.1
 -visitor            generate parse tree visitor
 -no-visitor         don't generate parse tree visitor (default)
 -package ___        specify a package/namespace for the generated code
 -depend             generate file dependencies
 -D<option>=value    set/override a grammar-level option
 -Werror             treat warnings as errors
 -XdbgST             launch StringTemplate visualizer on generated code
 -XdbgSTWait         wait for STViz to close before continuing
 -Xforce-atn         use the ATN simulator for all predictions
 -Xlog               dump lots of logging info to antlr-timestamp.log
 -Xexact-output-dir  all output goes into -o dir regardless of paths/package
 
 
编写Hello.g4文件
java -jar antlr-4.11.1-complete.jar Hello.g4
示例：
在Hello.g4文件所在的目录下运行如下命令：
java -jar ..\..\..\..\resources\lib\antlr-4.11.1-complete.jar .\Hello.g4
使用上述命令生成语法分析器和词法分析器

运行 javac *.java 编译Antlr生成的Java代码

的语法都编写一个main程序来测试。ANTLR在运行库中提供了一个名为TestRig的方便的调试工具。
它可以详细列出一个语言类应用程序在匹配输入文本过程中的信息，这些输入文本可以来自文件或者标准输入。
TestRig使用Java的反射机制来调用编译后的识别程序。与之前一样，最好通过别名或者批处理文件来调用它。
在本书中，我将会使用grun作为别名，你可以使用任何你喜欢的别名。
测试命令：
java org.antlr.v4.runtime.misc.TestRig Hello r -tokens
java -jar ..\..\..\..\resources\lib\antlr-4.11.1-complete.jar org.antlr.v4.runtime.misc.TestRig Hello r -tokens


```