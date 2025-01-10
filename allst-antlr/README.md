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
 
 
```