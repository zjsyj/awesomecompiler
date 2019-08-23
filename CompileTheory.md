## Compiler Theory

### front-end
**Front End** is the analytical and comprehensive process for compiler to source code.
`lexical analysis` -> `syntactic analysis` -> `semantic analysis`

#### lexical analysis
Token - 词法记号
Finite Automation - 有限自动机

#### syntactic analysis
Based on lexical analysis, construct a tree structure naming **AST(abstract syntax tree)**.
A common popular algorithm for syntactic analysis is **Recursive Descent Parsing**. Only Token can be the leaf node of the AST.The main difference between Context Free Grammar and Regular Grammar is that Context Free Grammar can be recursive.

**Recursive Descent Parsing**

**Context Free Grammar** consists of a group of subsitution(Genearative) grammars
commonly notation, BNF, EBNF


Left recursive, priority, associativity are some problems you must pay attention to when you do syntactic analysis.

#### semantic analysis


### back-end
**Back End** is the process of target code generation aim at specific machine.

### trend