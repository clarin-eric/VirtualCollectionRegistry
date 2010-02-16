grammar VCRQL;

options {
  output = AST;
  ASTLabelType = CommonTree;
}

tokens {
    EQ = '=';
    LT = '<';
    GT = '>';
    OR = 'or';
    AND = 'and';
    OPEN = '(';
    CLOSE = ')';
    QUERY;
    EXPRESSION;
    ENTITY;
}

@header {
package eu.clarin.cmdi.virtualcollectionregistry.query;
}

@lexer::header {
package eu.clarin.cmdi.virtualcollectionregistry.query;
}

query
    : expression EOF -> ^(QUERY<QueryNode> expression)
    ;

expression
    : logicalOrExpression
    ;

logicalOrExpression
    : logicalAndExpression (OR<BooleanNode>^ logicalAndExpression)*
    ;

logicalAndExpression
    : primaryExpression (AND<BooleanNode>^ primaryExpression)*
    ;
    
primaryExpression
    : atom
    | OPEN expression CLOSE -> expression
    ;

atom
    : component EQ<RelationNode>^ STRING<StringNode>
    | component LT<RelationNode>^ STRING<StringNode>
    | component GT<RelationNode>^ STRING<StringNode>
    ;

component
    : ENTITY_NAME '.' PROPERTY_NAME
        -> ENTITY<EntityNode>[$ENTITY_NAME,$PROPERTY_NAME]
    ;

ENTITY_NAME
    : 'vc' | 'creator'
    ;

PROPERTY_NAME 
    : ('a'..'z' | 'A' .. 'Z' | '0'..'9')+
    ;

STRING
    : '"' ( ESCAPE_SEQUENCE | ~('\u0000'..'\u001f' | '\\' | '\"' ) )* '"'
    ;

fragment
ESCAPE_SEQUENCE
    :   '\\' (UNICODE_ESCAPE | 'b' | 't' | 'n' | 'f' | 'r' | '\"' | '\'' | '\\')
    ;

fragment
UNICODE_ESCAPE
    : 'u' HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment
HEX_DIGIT
    : '0'..'9' | 'A'..'F' | 'a'..'f'
    ;

fragment
DIGIT
    : '0'..'9'
    ;

WS
    : ( ' ' | '\t' | '\r' | '\n' )+ { $channel=HIDDEN; }
    ;
