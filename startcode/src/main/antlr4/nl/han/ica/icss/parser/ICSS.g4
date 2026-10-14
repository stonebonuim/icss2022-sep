grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

term:CAPITAL_IDENT| PIXELSIZE| SCALAR;
mathvariable:  PLUS| MIN ;
assigment: ASSIGNMENT_OPERATOR (COLOR| PIXELSIZE| TRUE| FALSE) SEMICOLON;

color: LOWER_IDENT  COLON  (COLOR| CAPITAL_IDENT)  SEMICOLON;
width: LOWER_IDENT  COLON  (PIXELSIZE| CAPITAL_IDENT)  SEMICOLON;
sumcommand:  LOWER_IDENT  COLON  sum SEMICOLON;

selector: LOWER_IDENT;

sum: sum MUL sum |sum mathvariable sum | term ;
declaration:   (elsecommand |ifcommand |color | width | sumcommand)+  ;


variableAssignment: CAPITAL_IDENT assigment*;
cssname:  selector OPEN_BRACE  declaration CLOSE_BRACE;
cssownname: (ID_IDENT|CLASS_IDENT) OPEN_BRACE declaration CLOSE_BRACE;
ifcommand: (IF (BOX_BRACKET_OPEN variableAssignment BOX_BRACKET_CLOSE)) OPEN_BRACE declaration CLOSE_BRACE;
elsecommand: ELSE (OPEN_BRACE declaration CLOSE_BRACE)*;





//--- PARSER: ---
stylesheet:  stylerule;
stylerule: selector OPEN_BRACE  declaration CLOSE_BRACE;
// stylerule: variableAssignment* cssname* cssownname* ;


