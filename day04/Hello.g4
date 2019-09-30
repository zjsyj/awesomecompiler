lexer grammar Hello;

If : 'if';
Int : 'int';

IntLiteral : [0-9]+;
StringLiteral : '"' .*? '"';

AssignmentOP : '=';
RelationalOP : '>'|'>='|'<'|'<=';
Star : '*';
Plus : '+';
Sharp : '#';
SemiColon : ';';
Dot : '.';
Comm : ',';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';
LeftParen : '(';
RightParen : ')';


Id : [a-zA-Z_] ([a-zA-Z_] | [0-9])*;

Whitespace : [ \t]+ -> skip;
Newline : ( '\r' '\n'?|'\n')-> skip;