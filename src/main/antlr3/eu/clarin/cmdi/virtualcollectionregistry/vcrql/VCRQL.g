grammar VCRQL;

options {
  language = Java;
  output = AST;
}

tokens {
    EQ = '=';
    LT = '<';
    GT = '>';
    OR = 'or';
    AND = 'and';
    OPEN = '(';
    CLOSE = ')'; 
}

@header {
package eu.clarin.cmdi.virtualcollectionregistry.vcrql;
}

@lexer::header {
package eu.clarin.cmdi.virtualcollectionregistry.vcrql;
}

query
    : expression EOF -> expression
    ;

expression
    : logicalOrExpression
    ;

logicalOrExpression
    : logicalAndExpression (OR^ logicalAndExpression)*
    ;

logicalAndExpression
    : primaryExpression (AND^ primaryExpression)*
    ;
    
primaryExpression
    : atom
    | OPEN expression CLOSE -> expression
    ;

atom
    : field EQ^ value
    | field LT^ value
    | field GT^ value
    ;

field
    : FIELD_NAME
    ;

value
    : QUOTED_STRING
    ;

QUOTED_STRING
    : '"' (  ~'"' )* '"'
    ;

FIELD_NAME 
    : ('a'..'z')+
    ;

fragment
ESC_DBL_QUOTE
    : '\\' '"'
    ;
    
WS
    : ( ' ' | '\t' | '\r' | '\n' )+ { $channel=HIDDEN; }
    ;
